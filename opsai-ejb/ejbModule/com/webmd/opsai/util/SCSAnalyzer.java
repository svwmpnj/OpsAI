package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SCSAnalyzer 
{
	//private static final String FILE_LOC = "C:\\scslog";
	private static final String XD = "|";
	private static final String LCSTATE = "version_labels=";
	//private static final String LCSTATE = "effective_label=";
	private static final String DOCBASE = "Docbase Name:";
	private static final String UKDOCBASE = "WEBCONFIG_NAME=";
	public static final String START = "Starting webcache publish";
	private static final String BEGIN_PUBLISH = "Begin Publish Operation";
	private static final String EXPORT_COMPLETE = "Webcache Export complete";
	public static final String SYNC_LOCAL_CATALOG = "Synchronize local catalogs";
	public static final String SYNC_LOCAL_CATALOG_COMPLETE = "Source side - nothing more to do";
	public static final String TRANSFER_TO_AGENT = "Transfer to WebCache Agent";
	private static final String TRANSFER_TO_AGENT_COMPLETE = "bytes transferred to Webcache Agent";
	public static final String APPLY_TARGET_SYNCHRONIZATION = "Apply Target Synchronization";
	public static final String TARGET_SYNC_COMPLETE = "Closing manifest and keep list";
	public static final String COMPLETED = "Webcache publish completed successfully";
	public static final String CONTENTQUERYSTART = "Export base_query";
	public static final String CONTENTQUERYEND = "Adding list entry for object";
	public static final String CONTENTLESSQUERYSTART = "Export contentless base_query";
	public static final String CONTENTLESSQUERYEND = "Adding list entry for contentless";

	private String logFileLoc = null;
	private File logFile = null;
	private String status = "failure";
	private String type="I";
	private String lcState = null;
	private String docbase = null;
	private boolean contentQueryHit=false;
	private boolean contentlessQueryHit=false;
	private int contentObjCount;
	private int totalObjCount;
	
	private BufferedReader bufReader = null;
	
	private long sourceDeleteTime = 0L;
	private long sourceInsertTime = 0L;
	private long sourceDeleteTimeS = 0L;
	//private long sourceInsertTimeS = 0L;
	private long sourceDeleteTimeL = 0L;
	//private long sourceInsertTimeL = 0L;
	private long sourceDeleteTimeR = 0L;
	//private long sourceInsertTimeR = 0L;
	private long targetDeleteTime = 0L;
	private long targetInsertTime = 0L;
	private long targetDeleteTimeS = 0L;
	//private long targetInsertTimeS = 0L;
	private long targetDeleteTimeL = 0L;
	//private long targetInsertTimeL = 0L;
	private long targetDeleteTimeR = 0L;
	//private long targetInsertTimeR = 0L;
	private long targetContentMoveTime = 0L;
	private long relationQueryTime = 0L;
	
	private Map<String, Date> eventTimeStamps = null;
	private StringBuffer exportExtraInfo = new StringBuffer();
	private String bytesTransfered = null;
	
	/**
	 * 
	 * @param logFileLoc
	 * @throws FileNotFoundException
	 */
	public SCSAnalyzer(String logFileLoc) throws FileNotFoundException
	{
		this.logFileLoc = logFileLoc;
		logFile = new File(this.logFileLoc);
		
		

		
		if (!logFile.exists())
			throw new FileNotFoundException(logFileLoc);
		
		bufReader = new BufferedReader(new FileReader(logFile));
		
		eventTimeStamps = new HashMap<String, Date>();
	}

	/**
	 * 
	 * @param logFileLoc
	 * @throws FileNotFoundException
	 */
	public SCSAnalyzer(File logFile) throws FileNotFoundException
	{
		if (!logFile.exists())
			throw new FileNotFoundException(logFileLoc);
		
		bufReader = new BufferedReader(new FileReader(logFile));
		
		eventTimeStamps = new HashMap<String, Date>();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception
	{
		try{
			String vlabel = "WEBCONFIG_NAME=";
			String ukTmp = vlabel.substring(vlabel.lastIndexOf("=")+1);
			if (ukTmp!=null && ukTmp.startsWith("UK")){
				System.out.println("boots");
			}else{
				System.out.println("no change");
			}
			/*
			//PrintWriter fileOut = new PrintWriter(new FileWriter("c:\\logReport.xls")); 
			String fileLoc = (args.length>0) ? args[0] : FILE_LOC ;
			File folder = new File(fileLoc);
			File[] files = folder.listFiles();
			//fileOut.println("File number|" + files.length);
			//fileOut.println("Date," + new Date());
			System.out.println("File number: " + files.length);

			SCSAnalyzer analyzer = null;
			for (int i=0; i<files.length; i++){
			//for (int i=0; i<1; i++){
				if(files[i].getName().equals("webc_log_35053.txt")){
					analyzer = new SCSAnalyzer(files[i].getAbsolutePath());
					//analyzer = new SCSAnalyzer(files[i]);
					analyzer.analyze();
					//fileOut.print(analyzer.toString());
					analyzer.insertDB();
					System.out.println(analyzer.toString());
				}
			}*/
			//SCSAnalyzer analyzer = new SCSAnalyzer(fileLoc);
			
			//analyzer.analyze();
			//fileOut.print(analyzer.toString());
			//fileOut.close();
			

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void insertDB(){
		 
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void analyze() throws IOException
	{
		//log("analyze", "-----------------------");
		
		String line = null;
		
		while ((line = bufReader.readLine()) != null)
		{
			if (-1 != line.indexOf(LCSTATE))
			{
				lcState=line.substring(line.indexOf("=")+1);
				if(lcState!=null && lcState.length()>1){
					lcState = lcState.substring(1, lcState.length()-1);
				}
			}
			else if (-1 != line.indexOf("-source_object_id"))
			{
				type="S";
			}
			else if (-1 != line.indexOf(DOCBASE))
			{
				docbase = line.substring(line.lastIndexOf(" ")+1);
			}
			else if (-1 != line.indexOf(UKDOCBASE))
			{
				String ukTmp = line.substring(line.lastIndexOf("=")+1);
				if (ukTmp!=null && ukTmp.startsWith("UK")){
					docbase = "boots";
				}
			}
			else if (-1 != line.indexOf(CONTENTLESSQUERYSTART))
			{
				logEvent(line, CONTENTLESSQUERYSTART);
			}
			else if (-1 != line.indexOf(CONTENTLESSQUERYEND) && !contentlessQueryHit)
			{
				contentlessQueryHit = true;
				logEvent(line, CONTENTLESSQUERYEND);
			}
			else if (-1 != line.indexOf(START))
			{
				logEvent(line, START);
			}
			else if (-1 != line.indexOf(BEGIN_PUBLISH))
			{
				logEvent(line, BEGIN_PUBLISH);
				collectSourceExportStatistics();
			}
			else if (-1 != line.indexOf(EXPORT_COMPLETE))
			{
				logEvent(line, EXPORT_COMPLETE);
			}
			else if (-1 != line.indexOf(SYNC_LOCAL_CATALOG))
			{
				logEvent(line, SYNC_LOCAL_CATALOG);
				collectSourceSyncStatistics();
			}
			else if (-1 != line.indexOf(SYNC_LOCAL_CATALOG_COMPLETE))
			{
				logEvent(line, SYNC_LOCAL_CATALOG_COMPLETE);
			}
			else if (-1 != line.indexOf(TRANSFER_TO_AGENT))
			{
				logEvent(line, TRANSFER_TO_AGENT);
			}
			else if (-1 != line.indexOf(TRANSFER_TO_AGENT_COMPLETE))
			{
				logEvent(line, TRANSFER_TO_AGENT_COMPLETE);
				handleTransferToAgentComplete(line);
			}
			else if (-1 != line.indexOf(APPLY_TARGET_SYNCHRONIZATION))
			{
				logEvent(line, APPLY_TARGET_SYNCHRONIZATION);
				collectTargetSyncStatistics();
			}
			else if (-1 != line.indexOf(TARGET_SYNC_COMPLETE))
			{
				logEvent(line, TARGET_SYNC_COMPLETE);
			}
			else if (-1 != line.indexOf(COMPLETED))
			{
				status="successful";
				logEvent(line, COMPLETED);
			}
			else if (line.indexOf(CONTENTQUERYSTART) != -1)
			{
				logEvent(line, CONTENTQUERYSTART);
				logExportBaseQuery(line);
			}else if (-1 != line.indexOf(CONTENTQUERYEND) && !contentQueryHit)
			{
				contentQueryHit = true;
				logEvent(line, CONTENTQUERYEND);
			}
			else if (line.indexOf("were written out to the export data set") != -1)
			{
				//log("EXPORT", line);
				String newline = line.substring(line.indexOf(":S:") + 10, line.length());
				System.out.println("hit " + newline);
				exportExtraInfo.append(newline);
				exportExtraInfo.append(XD);
				newline = newline.trim();
				if (newline.indexOf("contentless") != -1){
					
				}else if (newline.indexOf("folder") != -1){
					
				}else if (newline.indexOf("content") != -1){
					contentObjCount = Integer.parseInt(newline.trim().substring(0, newline.indexOf("content")-1));
				}else if(newline.indexOf("objects") != -1){
					totalObjCount = Integer.parseInt(newline.trim().substring(0, newline.indexOf("objects")-1));
				}
				//exportExtraInfo.append("\n");
			}
		}
		bufReader.close();
	}
	
	private void handleTransferToAgentComplete(String line) 
	{
		bytesTransfered = line.substring(line.indexOf(":S:") + 10, line.length());
	}

	/**
	 * 
	 * @throws IOException
	 */
	private void collectSourceExportStatistics() throws IOException
	{
		String line = null;
		//System.out.println("hit collectSourceExportStatistics");
		
		while ((line = bufReader.readLine()) != null)
		{
			if (line.indexOf("Adding relations for object.") != -1)
			{
				long startTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
				line = bufReader.readLine();
				long endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
				relationQueryTime += endTimeInMillis - startTimeInMillis;
			}
			
			if (line.indexOf("Total export operation took") != -1)
			{
				//System.out.println("hit " + line);
				//log("EXPORT", line);
				exportExtraInfo.append(line.substring(line.indexOf(":S:") + 10, line.length()));
				exportExtraInfo.append(XD);
				//exportExtraInfo.append("\n");
				return;
			}
			
			if (line.indexOf("Delete sync query") != -1)
			{
				logDeleteSyncQuery(line);
			}
			
			if (line.indexOf("Delete sync query for expired contentfull documents") != -1)
			{
				logDeleteSyncQueryForExpiredContentfullObjects(line);
			}
			
			if (line.indexOf(CONTENTQUERYSTART) != -1)
			{
				logEvent(line, CONTENTQUERYSTART);
				logExportBaseQuery(line);
			}else if (-1 != line.indexOf(CONTENTQUERYEND) && !contentQueryHit)
			{
				contentQueryHit = true;
				logEvent(line, CONTENTQUERYEND);
			}
			
			if (line.indexOf("were written out to the export data set") != -1)
			{
				//log("EXPORT", line);
				String newline = line.substring(line.indexOf(":S:") + 10, line.length());
				System.out.println("hit " + newline);
				exportExtraInfo.append(newline);
				exportExtraInfo.append(XD);
				newline = newline.trim();
				if (newline.indexOf("contentless") != -1){
					
				}else if (newline.indexOf("content") != -1){
					contentObjCount = Integer.parseInt(newline.trim().substring(0, newline.indexOf("content")-1));
				}else if(newline.indexOf("objects") != -1){
					totalObjCount = Integer.parseInt(newline.trim().substring(0, newline.indexOf("objects")-1));
				}
				//exportExtraInfo.append("\n");
			}
		}
	}
	
	/**
	 * 
	 * @param line
	 */
	private void logDeleteSyncQuery(String line)
	{
		//log("EXPORT", line);
	}
	
	/**
	 * 
	 * @param line
	 */
	private void logDeleteSyncQueryForExpiredContentfullObjects(String line)
	{
		//log("EXPORT", line);
	}
	
	/**
	 * 
	 * @param line
	 */
	private void logExportBaseQuery(String line)
	{
		//log("EXPORT", line);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void collectSourceSyncStatistics() throws IOException
	{
		String line = null;
		
		while ((line = bufReader.readLine()) != null)
		{
			if (line.indexOf("Closing statement and connection") != -1)
				return;
			
			if (line.indexOf("Delete object: ") != -1)
			{
				handleObjectSyncOnSource(line);
			}
		}
	}
	
	/**
	 * 
	 * @param currentLine
	 * @throws IOException
	 */
	private void handleObjectSyncOnSource(String currentLine) throws IOException 
	{
		String line = bufReader.readLine();
		long startTimeInMillis = 0L;
		long endTimeInMillis = 0L;
		
		//System.out.println(line);
		startTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnSource('s', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnSource('l', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnSource('r', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		line = bufReader.readLine();
		
		while (line != null)
		{
			if (line.indexOf("Done with this content") != -1)
			{
				endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
				sourceInsertTime += endTimeInMillis - startTimeInMillis;
				return;
			}
			
			line = bufReader.readLine();
		}
	}

	/**
	 * 
	 * @param c
	 * @param startTimeInMillis
	 * @param timeInMillis
	 */
	private void logDeleteTimeOnSource(char c, long startTimeInMillis, long timeInMillis) 
	{
		long timeTaken = (timeInMillis - startTimeInMillis);
		sourceDeleteTime+= timeTaken;
		
		if (c == 's')
		{
			sourceDeleteTimeS+= timeTaken;
		}
		if (c == 'l')
		{
			sourceDeleteTimeL+= timeTaken;
		}
		if (c == 'r')
		{
			sourceDeleteTimeR+= timeTaken;
		}
	}
	
	/**
	 * 
	 */
	private void collectTargetSyncStatistics() throws IOException
	{
		String line = null;
		
		while ((line = bufReader.readLine()) != null)
		{
			if (line.indexOf("Closing statement and connection") != -1)
				return;
			
			if (line.indexOf("Delete object: ") != -1)
			{
				handleObjectSyncOnTarget(line);
			}
		}
	}

	/**
	 * 
	 * @param currentLine
	 * @throws IOException
	 */
	private void handleObjectSyncOnTarget(String currentLine) throws IOException 
	{
		String line = bufReader.readLine();
		long startTimeInMillis = 0L;
		long endTimeInMillis = 0L;
		
		//System.out.println(line);
		startTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnTarget('s', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnTarget('l', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		line = bufReader.readLine();
		endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
		logDeleteTimeOnTarget('r', startTimeInMillis, endTimeInMillis);
		
		startTimeInMillis = endTimeInMillis;
		boolean foundSourceUrl = false;
		
		while (line != null)
		{
			line = bufReader.readLine();
			
			if (line.indexOf("Source url") != -1)
			{
				foundSourceUrl = true;
				endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
				targetInsertTime += endTimeInMillis - startTimeInMillis;
				startTimeInMillis = endTimeInMillis;
			}
			
			if (line.indexOf("Done with this content") != -1)
			{
				endTimeInMillis = getDate(getTimeStampFromLine(line)).getTime();
				
				if (foundSourceUrl)
				{
					targetContentMoveTime += endTimeInMillis - startTimeInMillis;
					foundSourceUrl = false;
				}
				else
				{
					targetInsertTime += endTimeInMillis - startTimeInMillis;
				}
				
				return;
			}
		}
	}

	/**
	 * 
	 * @param c
	 * @param startTimeInMillis
	 * @param timeInMillis
	 */
	private void logDeleteTimeOnTarget(char c, long startTimeInMillis, long timeInMillis) 
	{
		long timeTaken = (timeInMillis - startTimeInMillis);
		targetDeleteTime+= timeTaken;
		
		if (c == 's')
		{
			targetDeleteTimeS+= timeTaken;
		}
		if (c == 'l')
		{
			targetDeleteTimeL+= timeTaken;
		}
		if (c == 'r')
		{
			targetDeleteTimeR+= timeTaken;
		}
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer(1024);
		StringBuffer fileBuf = new StringBuffer(1024);
		
		strBuf.append("\n\n-------------------------------------------------------------------------\n");
		strBuf.append("----------------- SCS Publishing Job Analysis Report --------------------\n");
		strBuf.append("-------------------------------------------------------------------------\n\n");
		strBuf.append("Log file\t");
		strBuf.append(logFile.getName());
		fileBuf.append(logFile.getName()+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Content Object#\t");
		strBuf.append(contentObjCount);
		fileBuf.append(contentObjCount+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Status\t");
		strBuf.append(status);
		fileBuf.append(status+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("LC State\t");
		strBuf.append(lcState);
		fileBuf.append(lcState+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Doc Base:");
		strBuf.append(docbase);
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Content Query Start\t");
		strBuf.append(eventTimeStamps.get(CONTENTQUERYSTART));
		fileBuf.append(eventTimeStamps.get(CONTENTQUERYSTART)+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Content Query End\t");
		strBuf.append(eventTimeStamps.get(CONTENTQUERYEND));
		fileBuf.append(eventTimeStamps.get(CONTENTQUERYEND)+XD);//1
		strBuf.append("\n");
		strBuf.append("Content Query Taken\t");
		if (eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND)!=null && eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART)!=null){
			strBuf.append(getFriendlyTimeTaken1(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART).getTime()));
			fileBuf.append(getFriendlyTimeTaken1(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART).getTime())+XD);//1
		}
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Contentless Query Start\t");
		strBuf.append(eventTimeStamps.get(CONTENTLESSQUERYSTART));
		fileBuf.append(eventTimeStamps.get(CONTENTLESSQUERYSTART)+XD);//1
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("Contentless Query End\t");
		strBuf.append(eventTimeStamps.get(CONTENTLESSQUERYEND));
		fileBuf.append(eventTimeStamps.get(CONTENTLESSQUERYEND)+XD);//1
		strBuf.append("\n");
		strBuf.append("Contentless Query Taken\t");
		if (eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND)!=null && eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART)!=null){
			strBuf.append(getFriendlyTimeTaken1(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART).getTime()));
			fileBuf.append(getFriendlyTimeTaken1(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART).getTime())+XD);//1
		}
		strBuf.append("\n");
		strBuf.append("\n");
		strBuf.append("-------- Main Events -----------\n");
		strBuf.append(START);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(START));
		fileBuf.append(eventTimeStamps.get(START)+XD);//2
		strBuf.append("\n");
		strBuf.append(BEGIN_PUBLISH);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(BEGIN_PUBLISH));
		fileBuf.append(eventTimeStamps.get(BEGIN_PUBLISH)+XD);//3
		strBuf.append("\n");
		strBuf.append(EXPORT_COMPLETE);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(EXPORT_COMPLETE));
		fileBuf.append(eventTimeStamps.get(EXPORT_COMPLETE)+XD);//4
		strBuf.append("\n");
		
		strBuf.append("\tTime taken for Export of attributes and content\t");
		if(eventTimeStamps.get(EXPORT_COMPLETE)!=null&& eventTimeStamps.get(BEGIN_PUBLISH)!=null){
			strBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(EXPORT_COMPLETE).getTime() - eventTimeStamps.get(BEGIN_PUBLISH).getTime()));
			fileBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(EXPORT_COMPLETE).getTime() - eventTimeStamps.get(BEGIN_PUBLISH).getTime())+XD);//5
		}else{
			strBuf.append("Something is wrong");
			fileBuf.append("NA"+XD);//5
		}
		strBuf.append("\n");
		
		strBuf.append(exportExtraInfo);
		fileBuf.append(exportExtraInfo);//6,7,8?
		
		strBuf.append(SYNC_LOCAL_CATALOG);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(SYNC_LOCAL_CATALOG));
		fileBuf.append(eventTimeStamps.get(SYNC_LOCAL_CATALOG)+XD);//9
		strBuf.append("\n");
		strBuf.append(SYNC_LOCAL_CATALOG_COMPLETE);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(SYNC_LOCAL_CATALOG_COMPLETE));
		fileBuf.append(eventTimeStamps.get(SYNC_LOCAL_CATALOG_COMPLETE)+XD);//10
		strBuf.append("\n");
		
		strBuf.append("\tTime taken for synchronization of local catalogs\t");
		if(eventTimeStamps.get(SYNC_LOCAL_CATALOG_COMPLETE)!=null&& eventTimeStamps.get(SYNC_LOCAL_CATALOG)!=null){
			strBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(SYNC_LOCAL_CATALOG_COMPLETE).getTime() - eventTimeStamps.get(SYNC_LOCAL_CATALOG).getTime()));
			fileBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(SYNC_LOCAL_CATALOG_COMPLETE).getTime() - eventTimeStamps.get(SYNC_LOCAL_CATALOG).getTime())+XD);//11
		}else{
			strBuf.append("Something is wrong");
			fileBuf.append("NA"+XD);//11
		}
		
		strBuf.append("\n");
		
		strBuf.append(TRANSFER_TO_AGENT);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(TRANSFER_TO_AGENT));
		fileBuf.append(eventTimeStamps.get(TRANSFER_TO_AGENT)+XD);//12
		strBuf.append("\n");
		
		strBuf.append("\tTime taken for transferring content to target\t");
		if(eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION)!=null&& eventTimeStamps.get(TRANSFER_TO_AGENT)!=null){
			strBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION).getTime() - eventTimeStamps.get(TRANSFER_TO_AGENT).getTime()));
			fileBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION).getTime() - eventTimeStamps.get(TRANSFER_TO_AGENT).getTime())+XD);//13
		}else{
			strBuf.append("Something is wrong");
			fileBuf.append("NA"+XD);//13
		}
		strBuf.append("\n");
		
		strBuf.append(bytesTransfered);
		fileBuf.append(bytesTransfered+XD);//14
		strBuf.append("\n");
		
		strBuf.append(APPLY_TARGET_SYNCHRONIZATION);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION));
		fileBuf.append(eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION)+XD);//15
		strBuf.append("\n");
		strBuf.append(TARGET_SYNC_COMPLETE);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(TARGET_SYNC_COMPLETE));
		fileBuf.append(eventTimeStamps.get(TARGET_SYNC_COMPLETE)+XD);//16
		strBuf.append("\n");
		
		strBuf.append("\tTime taken for synchronization of target\t");
		if(eventTimeStamps.get(TARGET_SYNC_COMPLETE)!=null&& eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION)!=null){
			strBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(TARGET_SYNC_COMPLETE).getTime() - eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION).getTime()));
			fileBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(TARGET_SYNC_COMPLETE).getTime() - eventTimeStamps.get(APPLY_TARGET_SYNCHRONIZATION).getTime())+XD);//17
			
		}else{
			strBuf.append("Something is wrong");
			fileBuf.append("NA"+XD);//17
		}
		strBuf.append("\n");
		
		strBuf.append(COMPLETED);
		strBuf.append("\t");
		strBuf.append(eventTimeStamps.get(COMPLETED));
		fileBuf.append(eventTimeStamps.get(COMPLETED)+XD);//18
		strBuf.append("\n");
		
		strBuf.append("\tTotal time taken\t");
		
		if(eventTimeStamps.get(COMPLETED)!=null&& eventTimeStamps.get(START)!=null){
			strBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(COMPLETED).getTime() - eventTimeStamps.get(START).getTime()));
			fileBuf.append(getFriendlyTimeTaken(eventTimeStamps.get(COMPLETED).getTime() - eventTimeStamps.get(START).getTime())+XD);//19
		}else{
			strBuf.append("Something is wrong");
			fileBuf.append("NA"+XD);//19
		}
		strBuf.append("\n");
		
		strBuf.append("\n");
		strBuf.append("-------- Sub Events --------\n");
		strBuf.append("-------- Source Export Events --------\n");
		
		strBuf.append("Total time spent on relation queries\t");
		strBuf.append(getFriendlyTimeTaken(relationQueryTime));
		strBuf.append("\n");
		
		strBuf.append("-------- Source Catalog Events --------\n");
		strBuf.append("Source Total Delete Time\t");
		strBuf.append(getFriendlyTimeTaken(sourceDeleteTime));
		strBuf.append("\n");
		strBuf.append("Source Total Insert Time\t");
		strBuf.append(getFriendlyTimeTaken(sourceInsertTime));
		strBuf.append("\n");
		strBuf.append("Source Total Delete Time for S table\t");
		strBuf.append(getFriendlyTimeTaken(sourceDeleteTimeS));
		strBuf.append("\n");
		//strBuf.append("Source Total Insert Time for S table\t");
		//strBuf.append(getFriendlyTimeTaken(sourceInsertTimeS));
		//strBuf.append("\n");
		strBuf.append("Source Total Delete Time for L table\t");
		strBuf.append(getFriendlyTimeTaken(sourceDeleteTimeL));
		strBuf.append("\n");
		//strBuf.append("Source Total Insert Time for L table\t");
		//strBuf.append(getFriendlyTimeTaken(sourceInsertTimeL));
		//strBuf.append("\n");
		strBuf.append("Source Total Delete Time for R table\t");
		strBuf.append(getFriendlyTimeTaken(sourceDeleteTimeR));
		strBuf.append("\n");
		//strBuf.append("Source Total Insert Time for R table\t");
		//strBuf.append(getFriendlyTimeTaken(sourceInsertTimeR));
		//strBuf.append("\n");
		strBuf.append("-------- Target Sync Events --------\n");
		strBuf.append("Target Total Delete Time\t");
		strBuf.append(getFriendlyTimeTaken(targetDeleteTime));
		strBuf.append("\n");
		strBuf.append("Target Total Insert Time\t");
		strBuf.append(getFriendlyTimeTaken(targetInsertTime));
		strBuf.append("\n");
		strBuf.append("Target Total Delete Time for S table\t");
		strBuf.append(getFriendlyTimeTaken(targetDeleteTimeS));
		strBuf.append("\n");
		//strBuf.append("Target Total Insert Time for S table\t");
		//strBuf.append(getFriendlyTimeTaken(targetInsertTimeS));
		//strBuf.append("\n");
		strBuf.append("Target Total Delete Time for L table\t");
		strBuf.append(getFriendlyTimeTaken(targetDeleteTimeL));
		strBuf.append("\n");
		//strBuf.append("Target Total Insert Time for L table\t");
		//strBuf.append(getFriendlyTimeTaken(targetInsertTimeL));
		//strBuf.append("\n");
		strBuf.append("Target Total Delete Time for R table\t");
		strBuf.append(getFriendlyTimeTaken(targetDeleteTimeR));
		strBuf.append("\n");
		//strBuf.append("Target Total Insert Time for R table\t");
		//strBuf.append(getFriendlyTimeTaken(targetInsertTimeR));
		//strBuf.append("\n");
		strBuf.append("Target Total Content Move time\t");
		strBuf.append(getFriendlyTimeTaken(targetContentMoveTime));
		strBuf.append("\n");
		return strBuf.toString();
		//fileBuf.append("\n");//end line
		//return fileBuf.toString();
	}
	
	public static String getFriendlyTimeTaken1(long timeInMillis) {
		Date conD = new Date(timeInMillis);
		Calendar conC = Calendar.getInstance();
		Calendar conC1 = Calendar.getInstance();
		conC.setTime(conD);
		//Duration du = Duration.;
		conC1.set(0, 0, 0, conC.get(Calendar.HOUR_OF_DAY)-19, conC.get(Calendar.MINUTE), conC.get(Calendar.SECOND));
		SimpleDateFormat sdf = new SimpleDateFormat("HH'hr'mm'm'ss's'");

		if (timeInMillis>1000){
			sdf = new SimpleDateFormat("HH'hr'mm'm'ss's'");
		}else{
			sdf = new SimpleDateFormat("HH'hr'mm'm'ss's'S'ms'");
		}
		
		return sdf.format(conC1.getTime());
	}
	
	public static String getFriendlyTimeTaken(long timeInMillis) 
	{
		StringBuffer strBufTime = new StringBuffer();
		
		long timeInSeconds = timeInMillis / 1000;
		
		long hh = (long)timeInSeconds / 3600;
		
		if (hh > 0)
		{
			strBufTime.append(hh);
			strBufTime.append("h ");
			timeInSeconds = timeInSeconds - (hh * 3600);
		}
		
		long mm = (long)timeInSeconds / 60;
		long ss = (long)timeInSeconds % 60;
		
		if (mm > 0)
		{
			strBufTime.append(mm);
			strBufTime.append("m ");
		}
		
		if (ss > 0)
		{
			strBufTime.append(ss);
			strBufTime.append("s");
		}
		else
		{
			strBufTime.append(timeInMillis % 1000);
			strBufTime.append("ms");
		}
		
		return strBufTime.toString();
	}

	/**
	 * 
	 * @param location
	 * @param string
	 */
	@SuppressWarnings("unused")
	private void log(String location, String string)
	{
		if (location != null)
			System.out.print(location + "\t");

		System.out.println(string);
	}
	
	/**
	 * 
	 * @param line
	 * @param event
	 */
	private void logEvent(String line, String event)
	{
		logEvent(line, event, null);
	}
	
	/**
	 * 
	 * @param line
	 * @param event
	 * @param location
	 */
	private void logEvent(String line, String event, String location)
	{
		if (location != null)
			location = "\t"+location;
		
		String timeStamp = getTimeStampFromLine(line);
		//log(location, event + "\t" + timeStamp);
		eventTimeStamps.put(event, getDate(timeStamp));
	}
	
	/**
	 * 
	 * @param timeStamp
	 * @return
	 */
	private Date getDate(String timeStamp) 
	{
		if (timeStamp == null || timeStamp.length() == 0)
		{
			System.out.println("Found null date");
			return null;
		}
		
		Calendar yearCal = Calendar.getInstance();
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss.SSS");
			return sdf.parse(yearCal.get(Calendar.YEAR)+ " " + timeStamp);
			//return sdf.parse("2010 " + timeStamp);
		}
		catch (Throwable t)
		{
			System.out.println("Exception in parsing time.");
			return null;
		}
	}
	
	
	/**
	 * 
	 * @param line
	 * @return
	 */
	private String getTimeStampFromLine(String line)
	{
		if (line == null || line.length() == 0)
			return "";
		
		int index = line.indexOf(":S:");
		
		if (-1 == index)
			index = line.indexOf(":T:");
		
		if (-1 == index)
			return "";
		
		//Mar 29 14:34:25.388:S:
		String timeStamp = line.substring(0, index);
		//System.out.println(timeStamp);
		return timeStamp;
	}

	public Map<String, Date> getEventTimeStamps() {
		return eventTimeStamps;
	}

	public File getLogFile() {
		return logFile;
	}

	public String getStatus() {
		return status;
	}

	public String getLcState() {
		return lcState;
	}

	public int getContentObjCount() {
		return contentObjCount;
	}

	public void setContentObjCount(int contentObjCount) {
		this.contentObjCount = contentObjCount;
	}

	public int getTotalObjCount() {
		return totalObjCount;
	}

	public void setTotalObjCount(int totalObjCount) {
		this.totalObjCount = totalObjCount;
	}

	public String getBytesTransfered() {
		return bytesTransfered;
	}

	public void setBytesTransfered(String bytesTransfered) {
		this.bytesTransfered = bytesTransfered;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getDocbase() {
		return docbase;
	}

	public void setDocbase(String docbase) {
		this.docbase = docbase;
	}

}
