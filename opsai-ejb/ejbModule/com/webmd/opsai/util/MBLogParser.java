package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MBLogParser {
	
	private String logFileLoc = null;
	private File logFile = null;
	private BufferedReader bufReader = null;
	private List<MBBusEvent> mbBusEventList = null;
	
	private static final String QJOBSTART = "++++++++++++++ MEDIA BRIDGE";
	private static final String QJOBEND = "+++++++++++++++++ pushToMainCS...DONE ";
	private static final String EMPTYQUEUE = "There is nothing found in the queue. Hence hibernating...";
	private static final String ELEMCOUNT = "Processing queue elements. Found no of elements";
	private static final String PARSEELEM = "------------------------TARGET CS------------------------------";
	private static final String SDFSTRING = "yyyy-MM-dd HH:mm:ss";
	private static final String FTPSTART = "---------------FTP--------------";
	private static final String FTPEND = "...akaresult initially -->";
	private static final String ELEMSTART = "] objectName -->";
	private static final String ELEMEND = "processing the next one if exists";
	private static final String OBJNAME = "  object_name                     :";
	private static final String TITLE = "  title                           :";
	private static final String OBJTYPE = "  r_object_type                   :";
	private static final String RCREATIONDATE = "  r_creation_date                 :";
	private static final String RMODIFYDATE = "  r_modify_date                   :";
	private static final String VIDFSIZE = "  wbmd_c_vid_fsize                :";
	private static final String DURATION = "  wbmd_c_duration                 :";
	private final String ROBJID = "+++++-----++++++++++vidLastApprovedObjectId:";
	
	public MBLogParser(String logFileLoc) throws FileNotFoundException
	{
		this.logFileLoc = logFileLoc;
		this.logFile = new File(this.logFileLoc);
		this.mbBusEventList = new LinkedList<MBBusEvent>();
		
		if (!logFile.exists())
			throw new FileNotFoundException(logFileLoc);
		
		bufReader = new BufferedReader(new FileReader(logFile));
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void parseMBQueue() throws IOException
	{
		String line = null;
		int elemNumInQueue = 0;
		Date busStartTime = null;
		Date busEndTime = null;
		MBBusEvent busEvent = null;
		
		while ((line = bufReader.readLine()) != null)
		{
			if (-1 != line.indexOf(QJOBSTART)){
				busStartTime = this.getDate(line.subSequence(0, 20).toString(), SDFSTRING);
				busEvent = new MBBusEvent();
				busEvent.setBusStartTime(busStartTime);
				System.out.println("Queue Job Start: " + busStartTime);
				elemNumInQueue = 0;
				//break;
			}else if (-1 != line.indexOf(EMPTYQUEUE)){
				//System.out.println("Nothing: " + line.subSequence(0, 20));
				//break;
			}else if (-1 != line.indexOf(ELEMCOUNT)){
				elemNumInQueue = Integer.parseInt(line.substring(89, line.lastIndexOf('.')));
				System.out.println("Element in queue: " + elemNumInQueue);
				//break;
			}else if (-1 != line.indexOf("MediaBridge [INFO] objectName:")){
				//System.out.println("Element info: " + line);
				//break;
			}else if (-1 != line.indexOf(PARSEELEM)){
				//System.out.println("start element: " + line);
				for (int i=0; i<elemNumInQueue; i++)
					parseMBElem(busEvent);
				//break;
			}else if (-1 != line.indexOf(QJOBEND)){
				busEndTime = this.getDate(line.subSequence(0, 20).toString(), SDFSTRING);
				System.out.println("Queue Job end: " + line.subSequence(0, 20));
				busEvent.setBusEndTime(busEndTime);
				this.mbBusEventList.add(busEvent);
				//break;
			}
			
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void parseMBElem(MBBusEvent busEvent) throws IOException
	{
		String line = null;		
		MBElemEvent ee = null; 
		
		
		while ((line = bufReader.readLine()) != null)
		{
			if (-1 != line.indexOf(ELEMSTART)){
				if (ee != null){//incomplete event
					busEvent.getElemEventList().add(ee);
				}
				ee = new MBElemEvent();
				ee.setElemEventStart(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
				//System.out.println("Element Start: " + ee.getElemEventStart());
				//break;
			}else if (-1 != line.indexOf(FTPSTART)){
				ee.setElemFtpStart(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
				//System.out.println("Start FTP: " + ee.getElemFtpStart());
			}else if (-1 != line.indexOf(FTPEND)){
				ee.setElemFtpEnd(this.getDate(line.subSequence(0, 20).toString(), SDFSTRING));
				//System.out.println("End FTP: " + ee.getElemFtpEnd());
			}else if (line.startsWith(OBJNAME)){
				ee.setObjName(line.substring(36));
				//System.out.println("Object name: " + ee.getObjName());
			}else if (line.startsWith(TITLE)){
				ee.setObjTitle(line.substring(36));
				//System.out.println("title: " + ee.getObjTitle());
			}else if (line.startsWith(OBJTYPE)){
				ee.setObjType(line.substring(36));
				//System.out.println("Object type: " + ee.getObjType());
			}else if (line.startsWith(RCREATIONDATE)){
				ee.setrCreateDate(this.getDate(line.substring(36),"M/dd/yyyy HH:mm:ss a"));
				//System.out.println("r_creation_date: " + ee.getrCreateDate());
			}else if (line.startsWith(RMODIFYDATE)){
				ee.setrModifyDate(this.getDate(line.substring(36), "MM/dd/yyyy HH:mm:ss a"));
				//System.out.println("r_modify_date: " + ee.getrModifyDate());
			}else if (line.startsWith(VIDFSIZE)){
				ee.setObjSize(line.substring(36));
				//System.out.println("wbmd_c_vid_fsize: " + ee.getObjSize());
			}else if (line.startsWith(DURATION)){
				ee.setObjDuration(line.substring(36));
				//System.out.println("wbmd_c_duration: " + ee.getObjDuration());
			}else if (-1 != line.indexOf(ROBJID)){
				ee.setObjID(line.substring(line.lastIndexOf(":")+1).trim());
				System.out.println("MB objid: " + ee.getObjID());
			}else if (-1 != line.indexOf(ELEMEND)){
				if (ee!=null){
					ee.setElemEventEnd(this.getDate(line.subSequence(0, 20).toString(),SDFSTRING));
					busEvent.getElemEventList().add(ee);
					ee = null;
				}else{
					System.out.println("Error close elem: " + line);
					//ee.getElemEventEnd();
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
	/**
	 * 
	 * @throws IOException
	 */
	public void busDebug() throws IOException
	{
		String line = null;
		String tmpLine = null;
		int pollBalance = 0;
		int totalFatal = 0;
		int overlapFatal = 0;
		int totalPoll = 0;
		int overlapPoll = 0;
		int currentPollSize = 0;
		int overlapItem = 0;
		int totalItem = 0;
		
		while ((line = bufReader.readLine()) != null)
		{
			if (-1 != line.indexOf(QJOBSTART)){
				System.out.println(pollBalance+"|"+line.subSequence(0, 20)+"|Poll Start");
				totalPoll++;
				pollBalance++;
				if (pollBalance > 1){
					overlapPoll++;
					//System.out.println("OVERLAP|"+line.subSequence(0, 20)+"|Poll Start");
				}else{
					//System.out.println("|"+line.subSequence(0, 20)+"|Poll Start");
				}
			}else if (-1 != line.indexOf(QJOBEND) || -1 != line.indexOf(EMPTYQUEUE)){
				pollBalance--;
				//if (-1 != line.indexOf(EMPTYQUEUE))
					//System.out.println("|0");
				System.out.println(pollBalance+"|"+line.subSequence(0, 20)+"|Poll End");
			}else if (-1 != line.indexOf("FATAL")){
				tmpLine = line;
				totalFatal++;
				if (pollBalance > 1){
					overlapFatal++;
				}
				System.out.println(pollBalance+"|"+line.subSequence(0, 20)+"|"+tmpLine.substring(20));
			}else if (-1 != line.indexOf(ELEMCOUNT)){
				currentPollSize = Integer.parseInt(line.substring(89, line.lastIndexOf('.')));
				System.out.println("|"+currentPollSize);
				if (pollBalance > 1){
					overlapItem += currentPollSize;					
				}
				totalItem +=currentPollSize;
			}
		}
		System.out.println(totalFatal+"|"+overlapFatal+"|"+totalPoll+"|"+overlapPoll+"|"+totalItem+"|"+overlapItem);
		//System.out.println("FailRelated/Total: " + failRelatedElemTotal+"/"+totalElem);
		//System.out.println("Overlap/Total polls: " + pollOverlap+"/"+totalPoll);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception
	{
		String fileLoc = "C:\\tmp\\mb\\US";
		File folder = new File(fileLoc);
		File[] files = folder.listFiles();
		//fileOut.println("File number|" + files.length);
		//fileOut.println("Date," + new Date());
		System.out.println("File number: " + files.length);


		MBLogParser logparser = null;
		for (int i=0; i<files.length; i++){
		//for (int i=0; i<1; i++){
			//System.out.println("File name: " + files[i].getName());
			//System.out.print(files[i].getName()+"|");
			logparser = new MBLogParser(files[i].getAbsolutePath());
			logparser.parseMBQueue();
			//logparser.busDebug();
		}
		
		System.out.print("event count: " + logparser.mbBusEventList.size());
		for (int j=0; j<logparser.mbBusEventList.size(); j++)
			System.out.print(logparser.mbBusEventList.get(j).print());
	}
	
	
	
	public List<MBBusEvent> getBusEventList() {
		return mbBusEventList;
	}

	public void setBusEventList(List<MBBusEvent> busEventList) {
		this.mbBusEventList = busEventList;
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
	
	public class MBBusEvent{
		private Date busStartTime, busEndTime;
		private List<MBElemEvent> elemEventList;
		
		public StringBuffer print(){
			StringBuffer sb = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			//System.out.print("Bus Start Time: " + sdf.format(busStartTime) + "	");
			//sb.append(sdf.format(busStartTime) + "|" + sdf.format(busEndTime) + "|||" + elemEventList.size() + "\n");
			//System.out.println("Bus End Time: " + sdf.format(busEndTime));
			//System.out.println("Element count: " + elemEventList.size());
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
	
	public class MBElemEvent{
		private Date elemEventStart, elemEventEnd, elemFtpStart, elemFtpEnd, rCreateDate, rModifyDate;
		private String objName, objTitle, objType, objSize, objDuration, objID;
		
		public StringBuffer print(){
			StringBuffer sb = new StringBuffer();
			SimpleDateFormat sdf = new SimpleDateFormat(SDFSTRING);
			try{
				//System.out.print("		" + objType);
				//System.out.print("   " + elemEventStart==null?"NA":sdf.format(elemEventStart));
				//System.out.print("   " + elemFtpStart==null?"NA":sdf.format(elemFtpStart));
				//System.out.print("   " + elemFtpEnd==null?"NA":sdf.format(elemFtpEnd));
				//System.out.println("   " + elemEventEnd==null?"NA":sdf.format(elemEventEnd));
				
				sb.append(objID+"||"+objType + "|");
				sb.append(elemEventStart==null?"NA|":sdf.format(elemEventStart) + "|");
				if (!"wbmd_cons_video".equals(objType)){
					sb.append("NA|");
					sb.append("NA|");
				}else{
					sb.append(elemFtpStart==null?"NA|":sdf.format(elemFtpStart) + "|");
					sb.append(elemFtpEnd==null?"NA|":sdf.format(elemFtpEnd) + "|");
				}
				sb.append(elemEventEnd==null?"NA|\n":sdf.format(elemEventEnd) + "\n");
			}catch(Exception e){
				System.out.println(e);
				System.out.println(elemEventStart);
				System.out.println(elemFtpStart);
				System.out.println(elemFtpEnd);
				System.out.println(elemEventEnd);
			}
			
			return sb;
		}
		
		
		public String getObjID() {
			return objID;
		}
		public void setObjID(String objId) {
			this.objID = objId;
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
		
		
	}
}
