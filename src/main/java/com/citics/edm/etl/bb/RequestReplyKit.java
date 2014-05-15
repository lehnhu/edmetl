package com.citics.edm.etl.bb;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bloomberg.datalic.api.ExtendedFTPConnection;
import com.bloomberg.datalic.net.DLFTPTypes;
import com.citics.edm.etl.bb.Result.STATUS;
import com.citics.edm.etl.bb.util.Utils;
import com.citics.edm.service.IEDMPropertyService;

@Component
public class RequestReplyKit {

	protected static final Log LOG = LogFactory.getLog(RequestReplyKit.class);

	public static final int MAX_TRY_TIMES = 10;

	@Value("${bb.host}")
	private String host;

	@Value("${bb.account}")
	private String account;

	@Value("${bb.password}")
	private String password;

	@Value("${bb.encryptKey}")
	private String encryptKey;

	@Autowired
	private IEDMPropertyService EDMPropertyService;

	// 文件开始标签
	protected static final String START_OF_FILE = "START-OF-FILE";
	// 属性列表开始标签
	protected static final String START_OF_FIELDS = "START-OF-FIELDS";
	// 数据开始标签
	protected static final String START_OF_DATA = "START-OF-DATA";

	// 文件结束标签
	protected static final String END_OF_FILE = "END-OF-FILE";

	// 属性列表结束标签
	protected static final String END_OF_FIELDS = "END-OF-FIELDS";

	// 数据结束标签
	protected static final String END_OF_DATA = "END-OF-DATA";

	/**
	 * 根据提供的属性配置生成request文件
	 * @author huliang
	 * @param props
	 * replyfilename 返回文件名
	 * startdate	行情开始日期
	 * enddate	行情结束日期
	 * issuelist ticker+market sector列表，每行一个
	 * @return
	 */
	public String formartRequestMessage(Properties props) {
		String tmpl = EDMPropertyService
			.getStringProperty("edmetl.bb.request.histpricetmpl");
		String str = new String(tmpl);
		Pattern pt = Pattern.compile("(\\$\\{(\\w+)\\})", Pattern.MULTILINE);
		Matcher mt = pt.matcher(tmpl);
		while (mt.find()) {
			String q = mt.group(1);
			String k = mt.group(2);
			str = str.replace(q, props.getProperty(k, ""));
		}
		return str;
	}

	//读取请求文件中定义的响应文件名
	private String getReplyFilenameFrom(String requestFile) {
		try {
			List<String> list = Utils.readLines(requestFile);
			for (String line : list) {
				if (line.contains("=")) {
					String[] meta = line.split("=");
					if (meta[0].trim().equalsIgnoreCase("REPLYFILENAME"))
						return meta[1];
				}
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	private ResponseMessage parseResponseMessageFromFile(List<String> lines) {

		ResponseMessage respMessage = new ResponseMessage();
		boolean b_StartOfFile = false, b_StartOfField = false, b_StartOfData = false;
		int beginIndex = 0;
		List<String> l_Fields = new ArrayList<String>();
		List<String[]> l_Columns = new LinkedList<String[]>();
		Map<String, String> metaDatas = new HashMap<String, String>();
		for (String line : lines) {
			if (line.equalsIgnoreCase(START_OF_FILE))
				b_StartOfFile = true;
			else if (line.equalsIgnoreCase(START_OF_FIELDS)) {
				b_StartOfField = true;
				if ("horizontal".equalsIgnoreCase(metaDatas.get("HIST_FORMAT"))) {
					beginIndex = 4;
					l_Fields.add("##MEM");
					l_Fields.add("##STATUS");
					l_Fields.add("##CNT");
					l_Fields.add("##DATE");
				}
			} else if (line.equalsIgnoreCase(START_OF_DATA))
				b_StartOfData = true;
			else if (line.equalsIgnoreCase(END_OF_FILE))
				b_StartOfFile = false;
			else if (line.equalsIgnoreCase(END_OF_FIELDS)) {
				b_StartOfField = false;
			} else if (line.equalsIgnoreCase(END_OF_DATA))
				b_StartOfData = false;

			//
			else if (b_StartOfField) {
				if (line.trim().length() > 0)
					l_Fields.add(line);
			} else if (b_StartOfData) {
				int fnum = l_Fields.size();
				String[] vs = line.split("\\|");
				String[] col = new String[fnum];
				System.arraycopy(vs, 0, col, 0, fnum);
				l_Columns.add(col);

			} else if (b_StartOfFile) {
				if (line.contains("=")) {
					String[] meta = line.split("=");
					metaDatas.put(meta[0], meta[1]);
				}
			}
		}
		String[] arr_Fields = new String[l_Fields.size()];
		l_Fields.toArray(arr_Fields);
		respMessage.setMetaData(metaDatas);
		respMessage.setHeaders(arr_Fields);
		respMessage.setColumns(l_Columns);
		respMessage.setFieldBeginIndex(beginIndex);
		return respMessage;
	}

	private List<BBHistoryPriceItem> convertResponseMessageToBBItemList(ResponseMessage rm)
		throws Exception {
		List<BBHistoryPriceItem> bblist = new LinkedList<BBHistoryPriceItem>();
		Map<String, BBHistoryPriceItem> cache = new HashMap<String, BBHistoryPriceItem>();
		String[] header = rm.getHeaders();
		int memIndex = -1, dateIndex = -1;
		int prefixCnt = 2;
		for (int i = 0; i < header.length; i++) {
			if ("##MEM".equalsIgnoreCase(header[i])) {
				memIndex = i;
				prefixCnt--;
			} else if ("##DATE".equalsIgnoreCase(header[i])) {
				dateIndex = i;
				prefixCnt--;
			}
			if (prefixCnt <= 0) {
				break;
			}
		}
		if (prefixCnt > 0) {
			throw new Exception("Invalid Response");
		}
		for (int i = rm.getFieldBeginIndex(); i < header.length; i++) {
			String name = header[i];
			Method setMethod = null;
			try {
				// 通过反射 找到该属性的设置方法
				setMethod = BBHistoryPriceItem.class.getMethod("set" + name,
					String.class);
			} catch (Exception e) {
				// 没有该属性则警告
				LOG.warn("cannot find properties " + name
					+ " in BBHistoryPriceItem class");
				continue;
			}
			for (String[] __clm : rm.getColumns()) {
				String date = __clm[dateIndex];
				String mString = __clm[memIndex];
				String key = "#" + date + "#" + mString;
				BBHistoryPriceItem bbp = cache.get(key);
				// 第一次如果没有找到则创建
				if (bbp == null) {
					bbp = new BBHistoryPriceItem();
					bbp.setDate(date);
					bbp.setIdContext(mString);
					cache.put(key, bbp);
				}
				// 设置属性
				setMethod.invoke(bbp, __clm[i]);
			}
		}
		for (Entry<String, BBHistoryPriceItem> entry : cache.entrySet()) {
			bblist.add(entry.getValue());
		}
		return bblist;
	}
	
	
	/**
	 * 解析响应文件
	 * @param lines
	 * @return
	 * @throws Exception
	 */
	public List<BBHistoryPriceItem> parseMessage(List<String> lines) throws Exception{
		return convertResponseMessageToBBItemList(parseResponseMessageFromFile(lines));
	}
	
	/**
	 * 标准的彭博请求响应方法
	 * @param requestFile
	 *            请求文件路径
	 * @param replyFile
	 *            响应文件路径
	 * @param outputPath
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Result requestReply(String requestFile, String outputPath) {
		int n = MAX_TRY_TIMES;
		ExtendedFTPConnection ftc = null;
		try {
			LOG.debug(String
				.format(
					"start request reply: using host=%s, account=%s , password=%s, encryptKey=%s",
					host, account, password, encryptKey));
			String replyFile = getReplyFilenameFrom(requestFile);
			if (replyFile == null) {
				return new Result(STATUS.ERROR,
					"failed to read replyFile property");
			}
			ftc = new ExtendedFTPConnection(host, account, password, null);
			ftc.open();
			System.out.println("open ftp connection");
			ftc.login();
			System.out.println("login");
			ftc.setMode(DLFTPTypes.ASCII);
			ftc.setAutoUUDecode(true);

			System.out.println(String.format("put file: %s ", requestFile));
			ftc.put(requestFile);
			System.out.println("put file success");
			try {
				System.out.println("wait 60 seconds");
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			while (n-- > 0) {
				try {
					ftc.getDESgz(replyFile + ".gz", outputPath + File.separator
						+ replyFile, encryptKey);
					System.out.println("file downloaded:" + replyFile);
					return new Result(Result.STATUS.SUCCESS, outputPath
						+ File.separator + replyFile);
				} catch (IOException e) {
					if (e.getMessage().contains("UUEncoded")) {
						throw e;
					}
					try {
						System.out
							.println("response file not prepared,wait 5 seconds");
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					continue;
				}
			}
			return new Result(Result.STATUS.TIMEOUT, "");
		} catch (Exception e) {
			return new Result(Result.STATUS.ERROR, e.getMessage());
		} finally {
			if (ftc != null)
				ftc.close();
		}
	}

	/**
	 * 下载响应文件
	 * @param replyFile
	 * @param outputPath
	 * @return
	 * @throws IOException
	 */
	public Result fetchResponseFile(String replyFile, String outputPath)
		throws IOException {
		int n = MAX_TRY_TIMES;
		ExtendedFTPConnection ftc = null;
		try {

			ftc = new ExtendedFTPConnection(host, account, password, null);
			ftc.open();
			System.out.println("open ftp connection");
			ftc.login();
			System.out.println("login");
			ftc.setMode(DLFTPTypes.ASCII);
			ftc.setAutoUUDecode(true);
			System.out.println("put file success");
			try {
				System.out.println("wait 60 seconds");
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			while (n-- > 0) {
				try {
					ftc.getDESgz(replyFile + ".gz", outputPath + File.separator
						+ replyFile, encryptKey);
					System.out.println("file downloaded:" + replyFile);
					return new Result(Result.STATUS.SUCCESS, outputPath
						+ File.separator + replyFile);
				} catch (IOException e) {
					if (e.getMessage().contains("UUEncoded")) {
						throw e;
					}
					try {
						System.out
							.println("response file not prepared,wait 5 seconds");
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					continue;
				}
			}
			return new Result(Result.STATUS.TIMEOUT, "");
		} catch (Exception e) {
			return new Result(Result.STATUS.ERROR, e.getMessage());
		} finally {
			if (ftc != null)
				ftc.close();
		}
	}
}
