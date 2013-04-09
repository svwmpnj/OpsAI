package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Iterator;

import com.webmd.opsai.entity.DamMTSInnerEvent;
import com.webmd.opsai.util.DAMLogParserDB.ItemEvent;


public class DAMLogParser {
	private String[] mtsHosts = null;
	private String[] mbSites = null;
	//private String logFileLocTLS = null;
	//private File logFileTLS = null;
	//private String logFileLocPRF = null;
	//private File logFilePRF = null;
	//private String logFileLocMBUS = null;
	//private File logFileMBUS = null;
	//private String logFileLocMBUK = null;
	//private File logFileMBUK = null;
	private String dateStr = null;
	private String prfDate = null;
	private BufferedReader bufReaderTLS = null;
	private BufferedReader bufReaderPRF = null;
	private BufferedReader bufReaderMB = null;
	private FileReader fileReaderTLS = null;
	private FileReader fileReaderPRF = null;
	private FileReader fileReaderMB = null;
	
	//private List<ParentEvent> parentListOpen, parentListError;
	//private Map<String, LinkedList<ParentEvent>> threadParentEventMap;
	//private Map<String, LinkedList<ItemEvent>> threadInnerEventMap;
	//private Map<String, LinkedList<ItemEvent>> threadEventMap;
	//private Map<String, SrcObj> srcObjMap;
	//private Map<String, LinkedList<ParentEvent>> totalParents;
	//private List<MBBusEvent> mbUSBusEventList;
	//private List<MBBusEvent> mbUKBusEventList;

	
	//MTS String
	private final String JOBSTART = "] TelestreamPlugin -       FILEPATH: C:\\webmd";
	private final String JOBEND = "In loadProfiles(),   directory C:";
	private final String JOBERROR = "ERROR";
	private final String JOBPERCENT = "Message transcoding, ";
	private final String JOBCTSPROFILE = "New CTSProfile attributes:";
	
	//MB String
	private final String QJOBSTART = "++++++++++++++ MEDIA BRIDGE";
	private final String QJOBEND = "+++++++++++++++++ pushToMainCS...DONE ";
	private final String EMPTYQUEUE = "There is nothing found in the queue. Hence hibernating...";
	private final String ELEMCOUNT = "Processing queue elements. Found no of elements";
	private final String PARSEELEM = "------------------------TARGET CS------------------------------";
	private final String FTPSTART = "---------------FTP--------------";
	private final String FTPEND = "...akaresult initially -->";
	private final String ELEMSTART = "] objectName -->";
	private final String ELEMEND = "processing the next one if exists";
	private final String OBJNAME = "  object_name                     :";
	private final String TITLE = "  title                           :";
	private final String OBJTYPE = "  r_object_type                   :";
	private final String RCREATIONDATE = "  r_creation_date                 :";
	private final String RMODIFYDATE = "  r_modify_date                   :";
	private final String VIDFSIZE = "  wbmd_c_vid_fsize                :";
	private final String DURATION = "  wbmd_c_duration                 :";
	private final String OBJSTATE = ": target doc promoted.";
	//private final String ROBJID = "+++++-----++++++++++vidLastApprovedObjectId:";
	//private final String IMAGEROBJID = "+++++-----++++++++++vidLastApprovedObjectId:";

	
	private final String SDFSTRING = "yyyy-MM-dd HH:mm:ss";
	private final String SDFSTRING1 = "yyyy-MM-dd HH:mm:ss,SSS";
	private final String SDFSTRING2 = "yyyy-MM-dd HH:mm:ss:SSS";
	private final int THREADSTRINGTLS = 0;
	private final int TIMESTAMP = 1;
	private final int CACHENAME = 2;
	private final int MESSAGEONLY = 3;
	private final int PERCENTAGE = 4;
	private final int AFTERLASTEQ = 5;
	private final int AFTERLASTDASH = 6;
	private final int AFTERLASTSLASH = 7;
	private final int THREADSTRINGPRF = 8;

	public DAMLogParser(String dateStr, String[] mtsHosts, String[] mbSites) throws FileNotFoundException{
		this.dateStr = dateStr;
		this.prfDate = dateStr + " ";
		this.mtsHosts = mtsHosts;
		this.mbSites = mbSites;
	}
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void parsePRF(HashMap<String, SrcObj> srcObjMap, LinkedList<ParentEvent> parentListOpen, LinkedList<ParentEvent> parentListError,
			HashMap<String, LinkedList<ParentEvent>> totalParents, HashMap<String, LinkedList<ParentEvent>> threadParentEventMap,
			HashMap<String, LinkedList<ItemEvent>> threadInnerEventMap) throws IOException
	{
		String line = null;
		String tmpThreadKey = null;
		ParentEvent tmpParent = null;
		ItemEvent tmpItem = null;
		SrcObj tmpSrcObj = null;
		int totalInnerItem = 0;
		SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING2);
		line = bufReaderPRF.readLine(); //skip 1st line for R11 prf logs
		while ((line = bufReaderPRF.readLine()) != null){
			tmpThreadKey = this.getSubString(line, this.THREADSTRINGPRF);
			if (-1 != line.indexOf("register          status:")||
					-1 != line.indexOf("center-well       status:")||
					-1 != line.indexOf("two-column        status:")){
				//Main Task
				if (-1 != line.indexOf("status:Main_task_started")){
					tmpParent = new ParentEvent();
					tmpParent.setParentEventStart(this.getDate(prfDate+line.substring(13, 25), SDFSTRING2));
					parentListOpen.add(tmpParent);
					if(!threadParentEventMap.containsKey(tmpThreadKey)){
						threadParentEventMap.put(tmpThreadKey, new LinkedList<ParentEvent>());
					}
					threadParentEventMap.get(tmpThreadKey).add(tmpParent);
					//System.out.println("Main " + tmpThreadKey);
				}else if (-1 != line.indexOf("status:Completed")){
					tmpParent = threadParentEventMap.get(tmpThreadKey).getLast();
					tmpParent.setParentEventEnd(this.getDate(prfDate+line.substring(13, 25), SDFSTRING2));
				}else if (-1 != line.indexOf("status:source_file_size")){
					tmpParent = threadParentEventMap.get(tmpThreadKey).getLast();
					tmpParent.setParentSize(Integer.parseInt(line.substring(line.lastIndexOf(":")+1).trim()));
				}else if (-1 != line.indexOf("status:source_object_name")){
					tmpParent = threadParentEventMap.get(tmpThreadKey).getLast();
					tmpParent.setParentFileName(line.substring(line.lastIndexOf(":")+1).trim());
					//System.out.println("Main " + tmpThreadKey + " " + tmpParent.getParentFileName());
				}else if (-1 != line.indexOf("status:source_object_id")){
					tmpParent = threadParentEventMap.get(tmpThreadKey).getLast();
					for(int i=0; i<parentListOpen.size();i++){
						if (line.substring(line.lastIndexOf(":")+1).trim().equals(parentListOpen.get(i).getParentId())){
							parentListError.add(parentListOpen.get(i));
							parentListOpen.remove(i);
							break;
						}
					}
					tmpParent.setParentId(line.substring(line.lastIndexOf(":")+1).trim());
					if (!totalParents.containsKey(tmpParent.getParentId())){
						totalParents.put(tmpParent.getParentId(), new LinkedList<ParentEvent>());
					}
					totalParents.get(tmpParent.getParentId()).add(tmpParent);
					if(!srcObjMap.containsKey(tmpParent.getParentId())){
						tmpSrcObj = new SrcObj(tmpParent.getParentId()); 
						srcObjMap.put(tmpParent.getParentId(), tmpSrcObj);
					}else{
						tmpSrcObj = srcObjMap.get(tmpParent.getParentId());
					}
					tmpParent.setSrcObj(tmpSrcObj);					
				}
			}else if(-1 != line.indexOf("telestream_registration status:")||
					-1 != line.indexOf("to_newTelestreamProfile status:")){
				//Inner Task
				if (-1 != line.indexOf("status:source_object_name:")){
					tmpItem = new ItemEvent();
					totalInnerItem++;
					tmpItem.setItemEventStart(this.getDate(prfDate+line.substring(13, 25), SDFSTRING2));
					if(!threadInnerEventMap.containsKey(tmpThreadKey)){
						threadInnerEventMap.put(tmpThreadKey, new LinkedList<ItemEvent>());
						//System.out.println("*New Inner " + tmpThreadKey + " " + tmpItem.getItemEventStart());
					}
					threadInnerEventMap.get(tmpThreadKey).add(tmpItem);
					tmpItem = threadInnerEventMap.get(tmpThreadKey).getLast();
					tmpItem.setItemName(line.substring(line.lastIndexOf(":")+1).trim());
				}else if (-1 != line.indexOf("status:source_object_id:")){
					tmpItem = threadInnerEventMap.get(tmpThreadKey).getLast();
					tmpItem.setSrcObjId(line.substring(line.lastIndexOf(":")+1).trim());
					System.out.println("Inner " + tmpThreadKey + " " + tmpItem.getItemEventStart()+" " + tmpItem.getSrcObjId());
					for(int i=0; i<parentListOpen.size();i++){
						if (tmpItem.getSrcObjId().equals(parentListOpen.get(i).getParentId())){
							parentListOpen.get(i).getItemList().add(tmpItem);
							tmpItem.setParent(parentListOpen.get(i));
							//System.out.println("Parent match" + tmpThreadKey + " " + tmpItem.getItemName() + " " + tmpItem.getItemEventStart()+" " + tmpItem.getParent().getParentEventStart());
							break;
						}
					}
					if (tmpItem.getParent()==null)
						System.out.println("Parent not found "+line + "--------------------------------------------");
				}else if (-1 != line.indexOf("status:Completed")||-1 != line.indexOf("status:Failed")){
					tmpItem = threadInnerEventMap.get(tmpThreadKey).getLast();
					tmpItem.setItemEventEnd(this.getDate(prfDate+line.substring(13, 25), SDFSTRING2));
					tmpItem.setPrfStatus(line.substring(line.lastIndexOf(":")+1).trim());
					//System.out.println("Inner complete" + tmpThreadKey + " " + tmpItem.getItemName());
				}
			}
			
		}
		
		System.out.println("total inner item: " + totalInnerItem);
		/*
		Iterator<Entry<String, LinkedList<ParentEvent>>> threadLinks = threadParentEventMap.entrySet().iterator();
		Iterator<ParentEvent> threadEvents = null;
		Entry<String, LinkedList<ParentEvent>> tmpThreadEvent;
		while(threadLinks.hasNext()){
			tmpThreadEvent = threadLinks.next();
			threadEvents = tmpThreadEvent.getValue().iterator();
			while(threadEvents.hasNext()){
				tmpParent = threadEvents.next();
				if (!tmpParent.getParentFileName().endsWith("jpg"))
					System.out.println(tmpParent.printParentEvent());
			}
		}
		
		//getVideoInfo(srcObjMap);
		Iterator<Entry<String, LinkedList<ItemEvent>>> threadInnerLinks = threadInnerEventMap.entrySet().iterator();
		Iterator<ItemEvent> threadEvents = null;
		Entry<String, LinkedList<ItemEvent>> tmpThreadEvent;
		ItemEvent tmpItemEvent;
		while(threadInnerLinks.hasNext()){
			tmpThreadEvent = threadInnerLinks.next();
			threadEvents = tmpThreadEvent.getValue().iterator();
			while(threadEvents.hasNext()){
				tmpItemEvent = threadEvents.next();
				mtsm = mtsmh.find(mtsHost, tmpItemEvent.getParent().getSrcObj().getObjId(), tmpItemEvent.getParent().getParentEventStart());
				mtsi = mtsih.find(mtsm, tmpItemEvent.getItemFormat(), tmpItemEvent.getItemRendition());
				if (mtsi == null){
					System.out.println("Cannot find the itemevent in DB");
					mtsi = new DamMTSInnerEvent();
				}
				mtsi.recordFromParser(mtsm,tmpItemEvent);
				mtsih.getEntityManager().persist(mtsi);
			}
		}*/
	}
	

	
	/**
	 * 
	 * @throws IOException
	 */
	public void parseTLS(HashMap<String, LinkedList<ItemEvent>> threadEventMap,HashMap<String, LinkedList<ItemEvent>> threadInnerEventMap) throws IOException
	{
		String line = null;
		
		String tmpThreadKey = null;
		ItemEvent tmpEvent = null;
		String[] strArray = null;
		Date tmpDate = null;
		//SimpleDateFormat sdf1 = new SimpleDateFormat(SDFSTRING1);
		
		while ((line = bufReaderTLS.readLine()) != null)
		{
			if (-1 != line.indexOf(JOBSTART) || -1 != line.indexOf(JOBEND) ||  -1 != line.indexOf(JOBERROR) || 
					-1 != line.indexOf(JOBPERCENT) || -1 != line.indexOf(JOBCTSPROFILE)){
				tmpThreadKey = this.getSubString(line, this.THREADSTRINGTLS);

				if(!threadEventMap.containsKey(tmpThreadKey))
					threadEventMap.put(tmpThreadKey, new LinkedList<ItemEvent>());
				
				if(-1 != line.indexOf(JOBSTART)){
					tmpDate = this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1);
					tmpEvent = null;
					for (int i=threadInnerEventMap.get(tmpThreadKey).size()-1; i>=0; i--){
						if (tmpDate.after(threadInnerEventMap.get(tmpThreadKey).get(i).getItemEventStart())){// &&
								//tmpDate.before(threadInnerEventMap.get(tmpThreadKey).get(i).getItemEventEnd())){
							tmpEvent = threadInnerEventMap.get(tmpThreadKey).get(i);
							break;
						}
					}
					if (tmpEvent == null){
						break;
					}
					tmpEvent.setItemEventStart(this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1));
					tmpEvent.setCacheFolder(this.getSubString(line, this.CACHENAME));
					threadEventMap.get(tmpThreadKey).addLast(tmpEvent);
				}
				else if (-1 != line.indexOf(JOBEND)){					
					try{
						tmpEvent = threadEventMap.get(tmpThreadKey).getLast();
						if (tmpEvent.getItemEventEnd()!=null && tmpEvent.getItemEventEnd().before(this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1))){
							System.out.println("Event matching Error"+line+"-------------------------------------");
						}
						tmpEvent.setItemEventEnd(this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1));
						tmpEvent.setStatus("Complete");
					}catch(Exception e){
						System.out.println(line);
						e.printStackTrace();
					}
				}else if (-1 != line.indexOf(JOBERROR)){					
					try{
						tmpEvent = threadEventMap.get(tmpThreadKey).getLast();
						tmpEvent.setItemEventEnd(this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1));
						tmpEvent.setStatus("Error");
						//tmpLine = tmpEvent.getErrorMsg();
						//tmpLine += this.getSubString(line, MESSAGEONLY)+"\n";
						tmpEvent.appendErrorMsg(this.getSubString(line, MESSAGEONLY));
					}catch(Exception e){
						System.out.println(line);
						e.printStackTrace();
					}
				}else if (-1 != line.indexOf(JOBPERCENT)){
					try{
						tmpEvent = threadEventMap.get(tmpThreadKey).getLast();
						tmpEvent.setObj100Count(this.getSubString(line, this.PERCENTAGE));
						if (tmpEvent.getFlipFirstStatusDate()==null){
							tmpEvent.setFlipFirstStatusDate(this.getDate(this.getSubString(line, this.TIMESTAMP), SDFSTRING1));
						}
					}catch(Exception e){
						System.out.println(line);
						e.printStackTrace();
					}
				}else if (-1 != line.indexOf(JOBCTSPROFILE)){
					try{
						tmpEvent = threadEventMap.get(tmpThreadKey).getLast();
						strArray = line.split(",");
						tmpEvent.setItemRendition(this.getSubString(strArray[5], this.AFTERLASTDASH));
						tmpEvent.setItemFormat(this.getSubString(strArray[6], this.AFTERLASTEQ));
						tmpEvent.setItemName(this.getSubString(strArray[9], this.AFTERLASTSLASH));
						tmpEvent.getParent().setType("6 Rendition");
					}catch(Exception e){
						System.out.println(line);
						e.printStackTrace();
					}
				}				
			}
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void parseMBQueue(HashMap<String, SrcObj> srcObjMap, List<MBBusEvent> mbBusEventList) throws IOException
	{
		String line = null;
		int elemNumInQueue = 0;
		Date busStartTime = null;
		Date busEndTime = null;
		MBBusEvent busEvent = null;
		LinkedList<SrcObj> srcObjEvents = null;
		SrcObj tmpSrcObj = null;
		//mbBusEventList = new LinkedList<MBBusEvent>();
		
		while ((line = bufReaderMB.readLine()) != null)
		{
			if (-1 != line.indexOf(QJOBSTART)){
				srcObjEvents = new LinkedList<SrcObj>();
				busStartTime = this.getDate(line.subSequence(0, 20).toString(), SDFSTRING);
				busEvent = new MBBusEvent();
				busEvent.setBusStartTime(busStartTime);
				elemNumInQueue = 0;
				//break;
			}else if (-1 != line.indexOf(EMPTYQUEUE)){
				//break;
			}else if (-1 != line.indexOf(ELEMCOUNT)){
				elemNumInQueue = Integer.parseInt(line.substring(89, line.lastIndexOf('.')));
				//break;
			}else if (-1 != line.indexOf("MediaBridge [INFO] objectName:")){
				//break;
			}else if (-1 != line.indexOf(PARSEELEM)){
				for (int i=0; i<elemNumInQueue; i++)
					parseMBElem(srcObjEvents.get(i),srcObjMap, busEvent, bufReaderMB);
				//break;
			}else if (-1 != line.indexOf(QJOBEND)){
				busEndTime = this.getDate(line.subSequence(0, 20).toString(), SDFSTRING);
				busEvent.setBusEndTime(busEndTime);
				mbBusEventList.add(busEvent);
				//break;
			}else if (-1 != line.indexOf("[INFO] objectId -->")){
				tmpSrcObj = new SrcObj(line.substring(line.indexOf(">")+2, line.indexOf(" objectName -->")));
				tmpSrcObj.setState(line.substring(line.lastIndexOf(" ")).trim());
				srcObjEvents.add(tmpSrcObj);
			}
		}
		System.out.println("Video found: " + srcObjMap.size());
		System.out.println("Q Job found: " + mbBusEventList.size());
		getMBQInfo(srcObjMap);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void parseMBElem(SrcObj tmpSrcObj,HashMap<String, SrcObj> srcObjMap, MBBusEvent busEvent, BufferedReader bufReaderMB) throws IOException
	{
		String line = null;		
		MBElemEvent ee = null; 
		
		while ((line = bufReaderMB.readLine()) != null)
		{
			if (-1 != line.indexOf(ELEMSTART)){
				if (ee != null){//incomplete event
					busEvent.getElemEventList().add(ee);
				}
				ee = new MBElemEvent();
				ee.setElemEventStart(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
				ee.setObjID(line.substring(line.lastIndexOf("[")+1, line.lastIndexOf("]")).trim());
				if (!tmpSrcObj.getObjId().equals(ee.getObjID()))
					System.out.println("NOT match");
				else{
					ee.setSrcObj(tmpSrcObj);
					srcObjMap.put(tmpSrcObj.getObjId()+"|"+tmpSrcObj.getState(), tmpSrcObj);
				}
			}else if (-1 != line.indexOf(FTPSTART)){
				ee.setElemFtpStart(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
			}else if (-1 != line.indexOf(FTPEND)){
				ee.setElemFtpEnd(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
			}else if (line.startsWith(OBJNAME)){
				ee.setObjName(line.substring(36));
			}else if (line.startsWith(TITLE)){
				ee.setObjTitle(line.substring(36));
			}else if (line.startsWith(OBJTYPE)){
				ee.setObjType(line.substring(36));
			}else if (line.startsWith(RCREATIONDATE)){
				ee.setrCreateDate(this.getDate(line.substring(36),"M/dd/yyyy HH:mm:ss a"));
			}else if (line.startsWith(RMODIFYDATE)){
				ee.setrModifyDate(this.getDate(line.substring(36), "MM/dd/yyyy HH:mm:ss a"));
			}else if (line.startsWith(VIDFSIZE)){
				ee.setObjSize(line.substring(36));
			}else if (line.startsWith(DURATION)){
				ee.setObjDuration(line.substring(36));
			}else if (-1 != line.indexOf(ELEMEND)){
				if (ee!=null){
					ee.setElemEventEnd(this.getDate(line.subSequence(0, 20).toString(),SDFSTRING));
					busEvent.getElemEventList().add(ee);
					ee = null;
				}else{
					System.out.println("Error close elem: " + line);
				}
				break;
			}else if (-1 != line.indexOf(QJOBSTART)){
				System.out.println("SOMETHING WRONG Queue Job start: " + line);
			}else if (-1 != line.indexOf(QJOBEND)){
				System.out.println("SOMETHING WRONG Queue Job end: " + line);
			}else if (-1 != line.indexOf("FATAL")){
				System.out.println("FATAL: " + line);
			}
		}
	}
	
	public void closeMTS(FileReader fileReaderTLS, FileReader fileReaderPRF, BufferedReader bufReaderTLS, BufferedReader bufReaderPRF) throws IOException{
		fileReaderPRF.close();
		fileReaderTLS.close();
		bufReaderPRF.close();
		bufReaderTLS.close();
	}
	public void closeMB(FileReader fileReaderMB, BufferedReader bufReaderMB) throws IOException{
		fileReaderMB.close();
		bufReaderMB.close();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception
	{
		String dateStr = "2012-04-06";
		//String[] mtsHosts = {"01l", "02l"};
		String[] mtsHosts = {"04l"};
		String[] mbSites = {"1001", "1006"};
		DAMLogParser logParser = new DAMLogParser(dateStr, mtsHosts, mbSites);
		logParser.processMTS();
		//logParser.processMB();
	}
	
	public void processMTS()  throws Exception{
		String folderLoc = "C:\\tmp\\DAMLogs\\";
		LinkedList<ParentEvent> parentListOpen, parentListError;
		HashMap<String, LinkedList<ParentEvent>> totalParents, threadParentEventMap;
		HashMap<String, LinkedList<ItemEvent>> threadInnerEventMap, threadEventMap;
		HashMap<String, SrcObj> srcObjMap;
		File logFileTLS, logFilePRF;
		for(int i=0; i<this.mtsHosts.length; i++){
			//this.logFileLocTLS = ;
			logFileTLS = new File(folderLoc+"TLSS_log.txt."+mtsHosts[i]+"."+dateStr);
			//this.logFileLocPRF = logFileLocPRF;
			logFilePRF = new File(folderLoc+"Performance_log.txt."+mtsHosts[i]+"."+dateStr);
			
			if (!logFileTLS.exists())
				throw new FileNotFoundException(logFileTLS.getAbsolutePath());
			if (!logFilePRF.exists())
				throw new FileNotFoundException(logFilePRF.getAbsolutePath());
			
			fileReaderTLS = new FileReader(logFileTLS);
			fileReaderPRF = new FileReader(logFilePRF);
			bufReaderTLS = new BufferedReader(fileReaderTLS);
			bufReaderPRF = new BufferedReader(fileReaderPRF);
			
			parentListOpen = new LinkedList<ParentEvent>();
			parentListError = new LinkedList<ParentEvent>();
			totalParents = new HashMap<String, LinkedList<ParentEvent>>();
			threadParentEventMap = new HashMap<String, LinkedList<ParentEvent>>();
			threadInnerEventMap = new HashMap<String, LinkedList<ItemEvent>>();
			threadEventMap = new HashMap<String, LinkedList<ItemEvent>>();
			srcObjMap = new HashMap<String, SrcObj>();
			
			parsePRF(srcObjMap, parentListOpen, parentListError, totalParents, threadParentEventMap, threadInnerEventMap);
			parseTLS(threadEventMap, threadInnerEventMap);
			
			generateMTSReport(mtsHosts[i], threadParentEventMap,threadEventMap, parentListError);
			closeMTS(fileReaderTLS, fileReaderPRF, bufReaderTLS, bufReaderPRF);

		}
		System.out.println("DONE MTS!!");
		
	}
	
	public void processMB()  throws Exception{
		String folderLoc = "C:\\tmp\\DAMLogs\\";
		File logFileMB = null;
		HashMap<String, SrcObj> srcObjMap = null;
		LinkedList<MBBusEvent> mbBusEventList = null;

		
		for(int i=0; i<this.mbSites.length; i++){
			System.out.println("Start MB: " + mbSites[i]);
			logFileMB = new File(folderLoc+"mb.log."+mbSites[i]+"."+dateStr);
			if (!logFileMB.exists())
				throw new FileNotFoundException(logFileMB.getAbsolutePath());
			fileReaderMB = new FileReader(logFileMB);
			bufReaderMB = new BufferedReader(fileReaderMB);
			srcObjMap = new HashMap<String, SrcObj>();
			mbBusEventList = new LinkedList<MBBusEvent>();
			parseMBQueue(srcObjMap, mbBusEventList);
			System.out.println("bus after parse: " + mbBusEventList.size());
			generateMBReport(mbSites[i], mbBusEventList);
			closeMB(fileReaderMB, bufReaderMB);
		}
		

		//DAMLogParser logMBParser = new DAMLogParser(dateStr);
		//logMBParser.parsePRF();
		//logMBParser.parseTLS();
		//parseMBQueue(srcObjMap, mbUSBusEventList);
		//parseMBQueue(srcObjMap, mbUKBusEventList);
		//logparser.mapMTSMB(logparser.mbUSBusEventList);
		//logparser.mapMTSMB(logparser.mbUKBusEventList);
		System.out.println("MB DONE!!");
		
	}
	/*
	private void mapMTSMB(List<MBBusEvent> mbBusEventList){		
		ParentEvent tmpParent=null;
		LinkedList<ParentEvent> tmpParentList = null;
		MBElemEvent tmpMBElem = null;

		for (int i=0; i<mbBusEventList.size(); i++){
			//System.out.print(logparser.mbBusEventList.get(i).print());
			for (int j=0; j<mbBusEventList.get(i).getElemEventList().size(); j++){
				tmpMBElem = mbBusEventList.get(i).getElemEventList().get(j);
				if (this.totalParents.containsKey(tmpMBElem.getObjID())){
					tmpParentList = this.totalParents.get(tmpMBElem.getObjID());					
				}else{
					continue;
				}
				for (int k=0; k<tmpParentList.size(); k++){
					tmpParent = tmpParentList.get(k);
					if (tmpParent.getParentEventEnd()==null){
						continue;
					}else if (tmpMBElem.getElemEventStart().after(tmpParent.getParentEventEnd())){
						tmpParent.setMbEvent(tmpMBElem);
						tmpMBElem.setParent(tmpParent);
					}
				}
				if (tmpMBElem.getParent()==null){
					System.out.print("Parent not found for MBEvent: " + tmpMBElem.getObjID());
				}else{
					System.out.println("MB->MTS match " + tmpMBElem.getParent().printParentEvent());
				}
			}
		}
	}
	*/
	private String generateMTSDqlStr(HashMap<String, SrcObj> srcObjMap){
		StringBuffer result = new StringBuffer();
		result.append("select r_object_id, parent_id, full_content_size/1000 as size, content_attr_name, content_attr_value ");
		result.append("from dmr_content where full_format = 'quicktime' and any parent_id in(");
		//result.append("'0903112d8014ac58')");
		
		Iterator<String> objIds = srcObjMap.keySet().iterator();
		while(objIds.hasNext()){
			result.append("'"+objIds.next()+"',");
		}
		result.deleteCharAt(result.length()-1);
		result.append(")");
		return result.toString();
	}
	private String generateMBDqlStr(HashMap<String, SrcObj> srcObjMap){
		StringBuffer result = new StringBuffer();
		result.append("select r_object_id, q_creation_date, document_state  from dm_dbo.wbmd_media_queue where r_object_id in(");
		//result.append("'0903112d8014ac58')");
		String tmpKey;
		
		Iterator<String> objIds = srcObjMap.keySet().iterator();
		while(objIds.hasNext()){
			tmpKey = objIds.next();
			result.append("'"+tmpKey.split("\\|")[0]+"',");
		}
		result.deleteCharAt(result.length()-1);
		result.append(")");
		return result.toString();
	}
	
	private void getMBQInfo(HashMap<String, SrcObj> srcObjMap){
		if (srcObjMap.size()==0)
			return;
		String[] columns;
		List<String[]> resultCollection;
		SrcObj tmpSrcObj;
		//String queryStr = "select  r_object_id, content_attr_name, content_attr_value" +
		//String queryStr = "select r_object_id, parent_id, full_content_size/1000 as size, content_attr_name, content_attr_value" +
		//		" from dmr_content where full_format = 'quicktime' and any parent_id in('0903112d80147dd5','0903112d80147db6')";
		String queryStr = generateMBDqlStr(srcObjMap);
		String docBase = "webmddam01";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		try{
			
			DfcClient dfcc = new DfcClient(docBase);
			Map<String, Object> qResult = dfcc.query(queryStr, "|");
			
			columns = (String[])qResult.get("columnHeader");
			resultCollection = (List<String[]>)qResult.get("resultCollection");
			for (int i=0; i<resultCollection.size(); i++){
				tmpSrcObj = srcObjMap.get(resultCollection.get(i)[0]+"|"+resultCollection.get(i)[2]);
				if (tmpSrcObj==null){
					System.out.println("\nsrcobj not found" + resultCollection.get(i)[0]);
				}
				tmpSrcObj.setPutInQTime(sdf.parse(resultCollection.get(i)[1]));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void getVideoInfo(HashMap<String, SrcObj> srcObjMap){
		if (srcObjMap.size()==0)
			return;
		String[] columns, tmpRepeatName, tmpRepeatValue;
		List<String[]> resultCollection;
		SrcObj tmpSrcObj;
		//String queryStr = "select  r_object_id, content_attr_name, content_attr_value" +
		//String queryStr = "select r_object_id, parent_id, full_content_size/1000 as size, content_attr_name, content_attr_value" +
		//		" from dmr_content where full_format = 'quicktime' and any parent_id in('0903112d80147dd5','0903112d80147db6')";
		String queryStr = generateMTSDqlStr(srcObjMap);
		String docBase = "webmddam01";
		
		try{
			
			DfcClient dfcc = new DfcClient(docBase);
			Map<String, Object> qResult = dfcc.query(queryStr, "|");
			
			columns = (String[])qResult.get("columnHeader");
			resultCollection = (List<String[]>)qResult.get("resultCollection");
			for (int i=0; i<resultCollection.size(); i++){
				tmpSrcObj = srcObjMap.get(resultCollection.get(i)[1]);
				if (tmpSrcObj==null){
					System.out.println("\nsrcobj not found" + resultCollection.get(i)[1]);
					
				}else{
					tmpSrcObj.setSize(resultCollection.get(i)[2]);
					
					tmpRepeatName = resultCollection.get(i)[3].split("\\|");
					tmpRepeatValue = resultCollection.get(i)[4].split("\\|");
					for (int k=0; k<tmpRepeatValue.length;k++){
						if (tmpRepeatName[k].equals("Frame Rate")){
							tmpSrcObj.setFrameRate(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("No. of Video Tracks")){
							tmpSrcObj.setVTrackNo(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Audio Bits Per Sample")){
							tmpSrcObj.setABitsPerSample(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Video Codec")){
							tmpSrcObj.setVCodec(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Audio Channels Per Track")){
							tmpSrcObj.setAchannels(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Frame Width")){
							tmpSrcObj.setFWidth(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Frame Height")){
							tmpSrcObj.setFHeight(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Audio Codec")){
							tmpSrcObj.setACodec(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Duration")){
							tmpSrcObj.setDuration(tmpRepeatValue[k]);
						}else if (tmpRepeatName[k].equals("Audio Sample Rate (Hz)")){
							tmpSrcObj.setASampleRate(tmpRepeatValue[k]);
						}						
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void generateMTSReport(String mtsHost, HashMap<String, LinkedList<ParentEvent>> threadParentEventMap, 
			HashMap<String, LinkedList<ItemEvent>> threadEventMap,
			LinkedList<ParentEvent> parentListError) throws IOException
	{
		PrintWriter fileOut; 
		fileOut = new PrintWriter(new FileWriter("c:\\tmp\\DAMlogs\\cvs\\MTSInnerTaskReport"+mtsHost+"."+this.dateStr+".csv")); 
		try{
			Iterator<Entry<String, LinkedList<ItemEvent>>> threadLinks = threadEventMap.entrySet().iterator();
			Iterator<ItemEvent> threadEvents = null;
			Entry<String, LinkedList<ItemEvent>> tmpThreadEvent;
			fileOut.println("Source,Source Object ID,Size,Duration,V Codec,A Codec,Frame Width,Frame Height,Frame Rate,V Track No,Audio Bits,Audio Channels,Audio Sample Rate,Format,Rendition,Main Task Start,Start Time,Flip Start,End Time,Inner Wait,Flip Wait,Encoding,Total,TLSStatus,PRFStatus,Percentage,Error Msg");
			while(threadLinks.hasNext()){
				tmpThreadEvent = threadLinks.next();
				threadEvents = tmpThreadEvent.getValue().iterator();
				while(threadEvents.hasNext()){
					fileOut.println(threadEvents.next().printItemEvent());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
		}
		
		fileOut = new PrintWriter(new FileWriter("c:\\tmp\\DAMlogs\\cvs\\MTSMainTaskReport"+mtsHost+"."+this.dateStr+".csv")); 
		try{
			Iterator<Entry<String, LinkedList<ParentEvent>>> threadLinks = threadParentEventMap.entrySet().iterator();
			Iterator<ParentEvent> parentEvents = null;
			Entry<String, LinkedList<ParentEvent>> tmpParentEvents;
			ParentEvent tmpParentEvent;
			fileOut.println("Source,Source Object ID,Size,Duration,V Codec,A Codec,Frame Width,Frame Height,Frame Rate,V Track No,Audio Bits,Audio Channels,Audio Sample Rate,Child #,Type,MTS start,MTS end,Total");

			while(threadLinks.hasNext()){
				tmpParentEvents = threadLinks.next();
				parentEvents = tmpParentEvents.getValue().iterator();
				while(parentEvents.hasNext()){
					tmpParentEvent = parentEvents.next();
					if (-1 != tmpParentEvent.getParentFileName().toLowerCase().indexOf("mov"))
						fileOut.println(tmpParentEvent.printParentEvent());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
		}
		fileOut = new PrintWriter(new FileWriter("c:\\tmp\\DAMlogs\\cvs\\MTSInnerTaskErrorReport"+mtsHost+"."+this.dateStr+".csv"));
		PrintWriter fileOut1 = new PrintWriter(new FileWriter("c:\\tmp\\DAMlogs\\cvs\\MOVErrorReport"+mtsHost+"."+this.dateStr+".csv")); 
		fileOut.println("Source,Source Object ID,Size,Duration,V Codec,A Codec,Frame Width,Frame Height,Frame Rate,V Track No,Audio Bits,Audio Channels,Audio Sample Rate,Format,Rendition,Main Task Start,Start Time,Flip Start,End Time,Inner Wait,Flip Wait,Encoding,Total,TLSStatus,PRFStatus,Percentage,Error Msg");
		fileOut1.println("Source,Source Object ID,Size,Duration,V Codec,A Codec,Frame Width,Frame Height,Frame Rate,V Track No,Audio Bits,Audio Channels,Audio Sample Rate,Child #,MTS start,MTS end,Total");
		try{
			ParentEvent tmpParentEvent;
			for (int e=0; e<parentListError.size(); e++){
				tmpParentEvent = parentListError.get(e);
				fileOut1.println(tmpParentEvent.printParentEvent());
				for (int k=0; k<tmpParentEvent.getItemList().size(); k++){
					fileOut.println(tmpParentEvent.getItemList().get(k).printItemEvent());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
			fileOut1.close();
		}
	}
	
	private void generateMBReport(String mbSite, LinkedList<MBBusEvent> mbBusEventList) throws IOException
	{
		PrintWriter fileOut; 
		fileOut = new PrintWriter(new FileWriter("c:\\tmp\\DAMlogs\\cvs\\MBReport"+mbSite+"."+this.dateStr+".csv")); 
		try{
			fileOut.println("Type,Source,Source Object ID,State,Queue Enter,MB start,MB ftp start,MB ftp end,MB end,Queue Wait,CMS1,FTP,CMS2,Total");
			for (int i=0; i<mbBusEventList.size(); i++){
				for (int j=0; j<mbBusEventList.get(i).getElemEventList().size(); j++){
					fileOut.println(mbBusEventList.get(i).getElemEventList().get(j).print());
				}
			}
			System.out.println("generating mb report done" + mbSite + " events: " + mbBusEventList.size());
			/*
			Iterator<Entry<String, LinkedList<ParentEvent>>> threadLinks = this.threadParentEventMap.entrySet().iterator();
			Iterator<ParentEvent> parentEvents = null;
			Entry<String, LinkedList<ParentEvent>> tmpParentEvents;
			ParentEvent tmpParentEvent;
			fileOut.println("Source,Source Object ID,Size,Duration,V Codec,MTS start,MTS end,Child #,MTS-MB Gap,MB start,MB ftp start,MB ftp end,MB end");

			while(threadLinks.hasNext()){
				tmpParentEvents = threadLinks.next();
				//System.out.println(tmpThreadEvent.getKey());
				parentEvents = tmpParentEvents.getValue().iterator();
				while(parentEvents.hasNext()){
					tmpParentEvent = parentEvents.next();
					if (-1 != tmpParentEvent.getParentFileName().toLowerCase().indexOf("mov"))
						fileOut.println(tmpParentEvent.printParentEvent());
					//threadEvents.next().printItemEvent(fileOut);
					//System.out.println(threadEvents.next().printItemEvent());
				}
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
		}
	}
	
	private Date getDate(String timeStamp, String sdfStr) 
	{
		if (timeStamp == null || timeStamp.length() == 0){
			System.out.println("Found null date");
			return null;
		}		
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(sdfStr);
			return sdf.parse(timeStamp);
		}catch (Throwable t){
			System.out.println("Exception in parsing time.");
			return null;
		}
	}
	
	private String getSubString(String str, int type) 
	{
		if (str == null){
			System.out.println("Found null String");
			return null;
		}
		
		String result = str;
		if (type == this.TIMESTAMP){
			result = result.substring(0, 24);
		}else if (type == this.THREADSTRINGTLS){
			result = result.substring(32, 41);
		}else if(type == this.CACHENAME){
			result = result.substring(0, result.lastIndexOf("\\"));
			result = result.substring(result.lastIndexOf("\\")+1);
		}else if (type == this.MESSAGEONLY){
			result = result.substring(result.indexOf(" -       ")+9);
		}else if (type == this.PERCENTAGE){
			result = result.substring(result.lastIndexOf("%")-2, result.lastIndexOf("%")+1);
		}else if (type == this.AFTERLASTEQ){
			result = result.substring(result.lastIndexOf("=")+1);
		}else if (type == this.AFTERLASTDASH){
			result = result.substring(result.lastIndexOf("-")+1);
		}else if (type == this.AFTERLASTSLASH){
			result = result.substring(result.lastIndexOf("\\")+1);
		}else if (type == this.THREADSTRINGPRF){
			result = result.substring(result.indexOf("[")+1, result.indexOf("]")).trim();
		}
		return result;
	}
	
	public class ParentEvent{
		private Date parentEventStart, parentEventEnd, parentDuration;
		private String parentId, parentFileName, type;
		private int parentSize;
		private List<ItemEvent> itemList;
		private SrcObj srcObj;
		private MBElemEvent mbEvent;
		
		public ParentEvent() {
			super();
			itemList = new LinkedList<ItemEvent>();
			type = "proxy";
		}
		
		public String printParentEvent(){
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			return parentFileName+","+srcObj.getObjId()+","+srcObj.getSize()+","+srcObj.getDuration()+","
			+srcObj.getVCodec()+","+srcObj.getACodec()+","+srcObj.getFWidth()+","+
			srcObj.getFHeight()+","+
			srcObj.getFrameRate()+","+
			srcObj.getVTrackNo()+","+
			srcObj.getABitsPerSample()+","+
			srcObj.getAchannels()+","+
			srcObj.getASampleRate()+","+itemList.size()+","+type+","+(parentEventStart==null?"na":sdf.format(parentEventStart))+","
			+(parentEventEnd==null?"na":sdf.format(parentEventEnd))+","+dateDiffStr(parentEventStart,parentEventEnd,false);
		}
		
		public SrcObj getSrcObj() {
			return srcObj;
		}
		public void setSrcObj(SrcObj srcObj) {
			this.srcObj = srcObj;
		}
		public Date getParentEventStart() {
			return parentEventStart;
		}
		public void setParentEventStart(Date parentEventStart) {
			this.parentEventStart = parentEventStart;
		}
		public Date getParentEventEnd() {
			return parentEventEnd;
		}
		public void setParentEventEnd(Date parentEventEnd) {
			this.parentEventEnd = parentEventEnd;
		}
		public Date getParentDuration() {
			return parentDuration;
		}
		public void setParentDuration(Date parentDuration) {
			this.parentDuration = parentDuration;
		}
		public String getParentId() {
			return parentId;
		}
		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
		public String getParentFileName() {
			return parentFileName;
		}
		public void setParentFileName(String parentFileName) {
			this.parentFileName = parentFileName;
		}
		public int getParentSize() {
			return parentSize;
		}
		public void setParentSize(int parentSize) {
			this.parentSize = parentSize;
		}
		public List<ItemEvent> getItemList() {
			return itemList;
		}
		public void setItemList(List<ItemEvent> itemList) {
			this.itemList = itemList;
		}
		public MBElemEvent getMbEvent() {
			return mbEvent;
		}
		public void setMbEvent(MBElemEvent mbEvent) {
			this.mbEvent = mbEvent;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	
	public class ItemEvent{
		private Date itemEventStart, itemEventEnd, flipFirstStatusDate;
		private String itemName, obj100Count, errorMsg, status, cacheFolder, itemFormat, itemRendition, srcObjId, prfStatus;
		private ParentEvent parent;
		
		public ItemEvent() {
			super();
			status = "Incomplete";
			errorMsg = "";
		}
		
		public String printItemEvent(){
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			StringBuffer sb = new StringBuffer();
			sb.append(itemName+","+parent.getSrcObj().getObjId()+","+parent.getSrcObj().getSize()+",");
			sb.append(parent.getSrcObj().getDuration()+","+parent.getSrcObj().getVCodec()+",");
			sb.append(parent.getSrcObj().getACodec()+","+parent.getSrcObj().getFWidth()+",");
			sb.append(parent.getSrcObj().getFHeight()+",");
			sb.append(parent.getSrcObj().getFrameRate()+",");
			sb.append(parent.getSrcObj().getVTrackNo()+",");
			sb.append(parent.getSrcObj().getABitsPerSample()+",");
			sb.append(parent.getSrcObj().getAchannels()+",");
			sb.append(parent.getSrcObj().getASampleRate()+",");
			sb.append(itemFormat+","+itemRendition+",");
			sb.append((parent.getParentEventStart()==null?"na":sdf.format(parent.getParentEventStart()))+",");
			sb.append((itemEventStart==null?"na":sdf.format(itemEventStart))+",");
			sb.append((flipFirstStatusDate==null?"na":sdf.format(flipFirstStatusDate))+",");
			sb.append((itemEventEnd==null?"na":sdf.format(itemEventEnd))+",");
			sb.append(dateDiffStr(parent.getParentEventStart(),itemEventStart, false)+",");
			sb.append(dateDiffStr(itemEventStart,flipFirstStatusDate, false)+",");
			sb.append(dateDiffStr(flipFirstStatusDate,itemEventEnd, false)+",");
			sb.append(dateDiffStr(parent.getParentEventStart(),itemEventEnd, false)+",");
			sb.append(status+","+prfStatus+","+obj100Count+","+errorMsg);
			
			return sb.toString();
			/*
			return itemName+","+parent.getSrcObj().getObjId()+","+parent.getSrcObj().getSize()+","+
			parent.getSrcObj().getDuration()+","+parent.getSrcObj().getVCodec()+","+
			parent.getSrcObj().getACodec()+","+parent.getSrcObj().getFWidth()+","+
			parent.getSrcObj().getFHeight()+","+
			parent.getSrcObj().getFrameRate()+","+
			parent.getSrcObj().getVTrackNo()+","+
			parent.getSrcObj().getABitsPerSample()+","+
			parent.getSrcObj().getAchannels()+","+
			parent.getSrcObj().getASampleRate()+","+
			itemFormat+","+itemRendition+","+
			(parent.getParentEventStart()==null?"na":sdf.format(parent.getParentEventStart()))+","+
			(itemEventStart==null?"na":sdf.format(itemEventStart))+","+
			(flipFirstStatusDate==null?"na":sdf.format(flipFirstStatusDate))+","+
			(itemEventEnd==null?"na":sdf.format(itemEventEnd))+",,,,,"+
			status+","+prfStatus+","+obj100Count+","+errorMsg;*/
			
		}
		
		public void printItemEvent(PrintWriter fileOut){
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING1);
			fileOut.println(itemName+","+itemFormat+","+itemRendition+","+cacheFolder+","+sdf.format(itemEventStart)+","+sdf.format(itemEventEnd)+","+status+","+obj100Count+",\""+errorMsg+"\"");
		}
		public Date getItemEventStart() {
			return itemEventStart;
		}
		public void setItemEventStart(Date itemEventStart) {
			this.itemEventStart = itemEventStart;
		}
		public Date getItemEventEnd() {
			return itemEventEnd;
		}
		public void setItemEventEnd(Date itemEventEnd) {
			this.itemEventEnd = itemEventEnd;
		}
		public String getItemName() {
			return itemName;
		}
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}
		public String getObj100Count() {
			return obj100Count;
		}
		public void setObj100Count(String obj100Count) {
			this.obj100Count = obj100Count;
		}
		public String getErrorMsg() {
			return errorMsg;
		}
		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		public void appendErrorMsg(String errorMsg) {
			this.errorMsg += errorMsg;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getCacheFolder() {
			return cacheFolder;
		}
		public void setCacheFolder(String cacheFolder) {
			this.cacheFolder = cacheFolder;
		}
		public String getItemFormat() {
			return itemFormat;
		}
		public void setItemFormat(String itemFormat) {
			this.itemFormat = itemFormat;
		}
		public String getItemRendition() {
			return itemRendition;
		}
		public void setItemRendition(String itemRendition) {
			this.itemRendition = itemRendition;
		}
		public String getSrcObjId() {
			return srcObjId;
		}
		public void setSrcObjId(String srcObjId) {
			this.srcObjId = srcObjId;
		}
		public String getPrfStatus() {
			return prfStatus;
		}
		public void setPrfStatus(String prfStatus) {
			this.prfStatus = prfStatus;
		}
		public ParentEvent getParent() {
			return parent;
		}
		public void setParent(ParentEvent parent) {
			this.parent = parent;
		}
		public Date getFlipFirstStatusDate() {
			return flipFirstStatusDate;
		}
		public void setFlipFirstStatusDate(Date flipFirstStatusDate) {
			this.flipFirstStatusDate = flipFirstStatusDate;
		}
	}
	public class SrcObj{
		private String objId, objName, size, duration, vCodec, aCodec, frameRate, vTrackNo, aBitsPerSample, 
		achannels, fWidth, fHeight, aSampleRate, state;
		private Date putInQTime;
		
		public SrcObj(String objId){
			super();
			setObjId(objId);
		}
		public String getObjId() {
			return objId;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public void setObjId(String objId) {
			this.objId = objId;
		}
		public String getObjName() {
			return objName;
		}
		public void setObjName(String objName) {
			this.objName = objName;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
		public String getDuration() {
			return duration;
		}
		public void setDuration(String duration) {
			this.duration = duration;
		}
		public String getVCodec() {
			return vCodec;
		}
		public void setVCodec(String codec) {
			vCodec = codec;
		}
		public String getACodec() {
			return aCodec;
		}
		public void setACodec(String codec) {
			aCodec = codec;
		}
		public String getFrameRate() {
			return frameRate;
		}
		public void setFrameRate(String frameRate) {
			this.frameRate = frameRate;
		}
		public String getVTrackNo() {
			return vTrackNo;
		}
		public void setVTrackNo(String trackNo) {
			vTrackNo = trackNo;
		}
		public String getABitsPerSample() {
			return aBitsPerSample;
		}
		public void setABitsPerSample(String bitsPerSample) {
			aBitsPerSample = bitsPerSample;
		}
		public String getAchannels() {
			return achannels;
		}
		public void setAchannels(String achannels) {
			this.achannels = achannels;
		}
		public String getFWidth() {
			return fWidth;
		}
		public void setFWidth(String width) {
			fWidth = width;
		}
		public String getFHeight() {
			return fHeight;
		}
		public void setFHeight(String height) {
			fHeight = height;
		}
		public String getASampleRate() {
			return aSampleRate;
		}
		public void setASampleRate(String sampleRate) {
			aSampleRate = sampleRate;
		}
		public Date getPutInQTime() {
			return putInQTime;
		}
		public void setPutInQTime(Date putInQTime) {
			this.putInQTime = putInQTime;
		}
	}
	
	public class MBBusEvent{
		private Date busStartTime, busEndTime;
		private List<MBElemEvent> elemEventList;
		
		public StringBuffer print(){
			StringBuffer sb = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			sb.append(sdf.format(busStartTime) + "|" + sdf.format(busEndTime) + "|||" + elemEventList.size() + "\n");
			for (int i=0; i<elemEventList.size(); i++){
				sb.append(elemEventList.get(i).print());
			}
			
			return sb;
		}
		
		public MBBusEvent(){
			elemEventList = new LinkedList<MBElemEvent>();
		}
		public Date getBusStartTime() {
			return busStartTime;
		}
		public void setBusStartTime(Date startTime) {
			busStartTime = startTime;
		}
		public Date getBusEndTime() {
			return busEndTime;
		}
		public void setBusEndTime(Date endTime) {
			busEndTime = endTime;
		}
		public List<MBElemEvent> getElemEventList() {
			return elemEventList;
		}
		public void setElemEventList(List<MBElemEvent> elemEventList) {
			this.elemEventList = elemEventList;
		}
		
	}
	
	private String dateDiffStr (Date firstDate, Date secondDate, boolean mbQtime){
		if (firstDate==null || secondDate==null)
			return "NA";
		
		Date date0 = new Date(firstDate.getTime());
		date0.setHours(0);
		date0.setMinutes(0);
		date0.setSeconds(0);
		
		long diff = secondDate.getTime()-firstDate.getTime();
		if (mbQtime){
			Date date12 = new Date(firstDate.getTime());
			date12.setHours(12);
			date12.setMinutes(0);
			date12.setSeconds(0);
			long twelvehrs = date12.getTime()-date0.getTime();
			if (diff > twelvehrs)
				diff-=twelvehrs;
		}
		
		diff+=date0.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
		String result = sdf.format(diff);
		return result.substring(11);
	}
	
	public class MBElemEvent{
		private Date elemEventStart, elemEventEnd, elemFtpStart, elemFtpEnd, rCreateDate, rModifyDate;
		private String objName, objTitle, objType, objSize, objDuration, objID, objState;
		private ParentEvent parent;
		private SrcObj srcObj;
		
		public String print(){
			StringBuffer sb = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			try{
				sb.append(objType+","+objName+",");
				if (srcObj!=null){
					sb.append(srcObj.getObjId()+","+srcObj.getState()+",");
					sb.append(srcObj.getPutInQTime()==null?"NA,":sdf.format(srcObj.getPutInQTime())+",");
				}else
					sb.append(objID+"*,"+"NA,NA,");
				sb.append(elemEventStart==null?"NA,":sdf.format(elemEventStart) + ",");
				if (!"wbmd_cons_video".equals(objType)){
					sb.append("NA,");
					sb.append("NA,");
				}else{
					sb.append(elemFtpStart==null?"NA,":sdf.format(elemFtpStart) + ",");
					sb.append(elemFtpEnd==null?"NA,":sdf.format(elemFtpEnd) + ",");
				}
				sb.append(elemEventEnd==null?"NA,":sdf.format(elemEventEnd) + ",");
				sb.append(dateDiffStr(srcObj.getPutInQTime(), elemEventStart, true)+",");
				sb.append(dateDiffStr(elemEventStart, elemFtpStart, false)+",");
				sb.append(dateDiffStr(elemFtpStart, elemFtpEnd, false)+",");
				sb.append(dateDiffStr(elemFtpEnd, elemEventEnd, false)+",");
				sb.append(dateDiffStr(elemEventStart, elemEventEnd, false));
			}catch(Exception e){
				e.printStackTrace();
				System.out.print(elemEventStart);
				System.out.print(elemFtpStart);
				System.out.print(elemFtpEnd);
				System.out.println(elemEventEnd);
			}
			
			return sb.toString();
		}
		
		public String getObjID() {
			return objID;
		}
		public void setObjID(String objID) {
			this.objID = objID;
		}		
		public String getObjState() {
			return objState;
		}
		public void setObjState(String objState) {
			this.objState = objState;
		}

		public Date getElemEventStart() {
			return elemEventStart;
		}
		public void setElemEventStart(Date elemEventStart) {
			this.elemEventStart = elemEventStart;
		}
		public Date getElemEventEnd() {
			return elemEventEnd;
		}
		public void setElemEventEnd(Date elemEventEnd) {
			this.elemEventEnd = elemEventEnd;
		}
		public Date getElemFtpStart() {
			return elemFtpStart;
		}
		public void setElemFtpStart(Date elemFtpStart) {
			this.elemFtpStart = elemFtpStart;
		}
		public Date getElemFtpEnd() {
			return elemFtpEnd;
		}
		public void setElemFtpEnd(Date elemFtpEnd) {
			this.elemFtpEnd = elemFtpEnd;
		}
		public Date getrCreateDate() {
			return rCreateDate;
		}
		public void setrCreateDate(Date rCreateDate) {
			this.rCreateDate = rCreateDate;
		}
		public Date getrModifyDate() {
			return rModifyDate;
		}
		public void setrModifyDate(Date rModifyDate) {
			this.rModifyDate = rModifyDate;
		}
		public String getObjName() {
			return objName;
		}
		public void setObjName(String objName) {
			this.objName = objName;
		}
		public String getObjTitle() {
			return objTitle;
		}
		public void setObjTitle(String objTitle) {
			this.objTitle = objTitle;
		}
		public String getObjType() {
			return objType;
		}
		public void setObjType(String objType) {
			this.objType = objType;
		}
		public String getObjSize() {
			return objSize;
		}
		public void setObjSize(String objSize) {
			this.objSize = objSize;
		}
		public String getObjDuration() {
			return objDuration;
		}
		public void setObjDuration(String objDuration) {
			this.objDuration = objDuration;
		}
		public ParentEvent getParent() {
			return parent;
		}
		public void setParent(ParentEvent parent) {
			this.parent = parent;
		}
		public SrcObj getSrcObj() {
			return srcObj;
		}
		public void setSrcObj(SrcObj srcObj) {
			this.srcObj = srcObj;
		}
	}
}
