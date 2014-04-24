package com.citics.edm.etl.bb;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bloomberg.datalic.api.ExtendedFTPConnection;
import com.bloomberg.datalic.net.DLFTPTypes;

@Component
public class RequestReplyKit {

	protected static final Log LOG = LogFactory
			.getLog(RequestReplyKit.class);

	@Value("${bb.host}")
	private String host;

	@Value("${bb.account}")
	private String account;

	@Value("${bb.password}")
	private String password;

	@Value("${bb.encryptKey}")
	private String encryptKey;
	
	
	static final String START_OF_FILE = "START-OF-FILE";
	static final String START_OF_FIELDS = "START-OF-FIELDS";
	static final String START_OF_DATA = "START-OF-DATA";

	static final String END_OF_FILE = "END-OF-FILE";
	static final String END_OF_FIELDS = "END-OF-FIELDS";
	static final String END_OF_DATA = "END-OF-DATA";
	
	private String tmpl;
	{
		try {
			tmpl = IOUtils.toString(this.getClass().getResourceAsStream(
					"/tmpl/hist.req"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @author leon
	 * @param props
	 * @return
	 */
	public String formartRequestMessage(Properties props){
		String str=new String(tmpl);
		Pattern pt=Pattern.compile("(\\$\\{(\\w+)\\})",Pattern.MULTILINE);
		Matcher mt=pt.matcher(tmpl);
		while(mt.find()){
			String q=mt.group(1);
			String k=mt.group(2);
			str=str.replace(q, props.getProperty(k,""));
		}
		return str;
	}

	/**
	 * 
	 * @param lines
	 * @return
	 */
	public ResponseMessage parseResponseMessage(List<String> lines) {

		ResponseMessage respMessage = new ResponseMessage();
		boolean b_StartOfFile = false, b_StartOfField = false, b_StartOfData = false;
		boolean hori = false;
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
					hori = true;
					beginIndex = 4;
					l_Fields.add("##MEM");
					l_Fields.add("##STATUS");
					l_Fields.add("##CNT");
					l_Fields.add("##DATE");
				} else {
					hori = false;
					beginIndex = 2;
					l_Fields.add("##MEM");
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
				l_Fields.add(line);
			} else if (b_StartOfData) {
				if (!hori
						&& (line.startsWith("START SECURITY") || line
								.startsWith("END SECURITY"))) {
					continue;
				}

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
	
	/**
	 * 
	 * @param requestFile
	 * @param replyFile
	 * @param outputPath
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void requestReply(String requestFile, String replyFile,
			String outputPath) throws UnknownHostException, IOException {
		LOG.debug(String
				.format("start request reply: using host=%s, account=%s , password=%s, encryptKey=%s",
						host, account, password, encryptKey));
		ExtendedFTPConnection ftc;
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

		while (true) {
			try {
				ftc.getDESgz(replyFile + ".gz", outputPath + File.separator
						+ replyFile, encryptKey);
				System.out.println("file downloaded:"+replyFile);
			} catch (IOException e) {
				if(e.getMessage().contains("UUEncoded")){
					throw e;
				}
				try {
					System.out.println("response file not prepared,wait 5 seconds");
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				continue;
			}
			break;
		}

		ftc.close();
	}

	public void fetchResponseFile(String replyFile,
			String outputPath) throws IOException{
		LOG.debug(String
				.format("start request reply: using host=%s, account=%s , password=%s, encryptKey=%s",
						host, account, password, encryptKey));
		ExtendedFTPConnection ftc;
		ftc = new ExtendedFTPConnection(host, account, password, null);
		ftc.open();
		System.out.println("open ftp connection");
		ftc.login();
		System.out.println("login");
		ftc.setMode(DLFTPTypes.ASCII);
		ftc.setAutoUUDecode(true);
		while (true) {
			try {
				ftc.getDESgz(replyFile + ".gz", outputPath + File.separator
						+ replyFile, encryptKey);
				System.out.println("file downloaded:"+replyFile);
			} catch (IOException e) {
				if(e.getMessage().contains("UUEncoded")){
					throw e;
				}
				try {
					System.out.println("response file not prepared,wait 5 seconds");
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				continue;
			}
			break;
		}

		ftc.close();
	}
}
