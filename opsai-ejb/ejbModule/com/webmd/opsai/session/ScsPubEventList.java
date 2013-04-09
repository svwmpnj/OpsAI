package com.webmd.opsai.session;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.type.Type;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import com.webmd.opsai.entity.ScsPubEvent;
import com.webmd.opsai.entity.ServiceConf;
import com.webmd.opsai.util.SCSAnalyzer;
import com.webmd.opsai.util.SCSChart;

@Name("scsPubEventList")
@Scope (CONVERSATION)
public class ScsPubEventList extends EntityQuery<ScsPubEvent>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Logger private Log log;
	
	private static final String EJBQL = "select scsPubEvent from ScsPubEvent scsPubEven";
	private final String SCSLOGFOLDER_CD = "scslogfolder";
	
	private ScsPubEvent scspubevent = new ScsPubEvent();
	private int rowNumberPerPage;
	private Date startDate, endDate, scsStartDate;
	private Calendar scsChartBaseDate, scsChart1Date, scsChart7Date, scsChart105Date, scsChart105BaseDate;
	private final TimeZone defaultTimeZone = TimeZone.getTimeZone("EDT");
	private List<ScsPubEvent> myResultList;
	
	private SCSChart cons1Inc = null;
	private SCSChart cons7Inc = null;
	private SCSChart cons105Inc = null;
	private SCSChart boots1Inc = null;
	private SCSChart boots7Inc = null;
	private SCSChart boots105Inc = null;
	private SCSChart prof1SIP = null;
	private SCSChart prof7SIP = null;
	private SCSChart prof105SIP = null;
	
	private boolean showX1data = false;
	private String dataX1Label = "";

	public TimeZone getDefaultTimeZone() {
		return defaultTimeZone;
	}

	@Begin(join=true)
    public void processSCSLogsFromPAS(){
    	log.info("Triggered!!"); 
		ServiceConfList sc = new ServiceConfList();
    	ServiceConf con = (ServiceConf)((Session)sc.getEntityManager().getDelegate()).createQuery(
				"from ServiceConf as sc where sc.serviceCD = ?").setString(0, SCSLOGFOLDER_CD).uniqueResult();
		String folderUrl = con.getServiceURL();
    	try{
			//consumerdmcs
			File folder = new File(folderUrl+"consumerdmcs");
			//File folder = new File("\\\\nasfs01x-ops-07\\shr_dtm_log\\scslog\\consumerdmcs");
			//next line only for IAD1 consumer logs
			//File folder = new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcs");
    		//File folder = new File("C:\\scslog\\test");
			File[] files = folder.listFiles();
			int logNum = files.length;
			log.info("consumerdmcs number: "+logNum);
			for (int i=0; i<logNum; i++){
			//for (int i=0; i<10; i++){
				log.info("file: "+i);
				if (processSCSAnalyzer(new SCSAnalyzer(files[i].getAbsolutePath()))){
					files[i].delete();
				}else{
					files[i].renameTo(new File(folderUrl+"consumerdmcsfail\\f"+files[i].getName()));
				}
			}
			log.info("consumerdmcs finish");
		}catch(Exception e){
			e.printStackTrace();
		}/*
		try{
			//consumerdmcs2			
			File folder = new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcs2");
			File[] files = folder.listFiles();
			int logNum = files.length;
			log.info("professionaldmcs number: "+logNum);
			for (int i=0; i<logNum; i++){
				log.info("file: "+i);
				if (processSCSAnalyzer(new SCSAnalyzer(files[i].getAbsolutePath()))){
					files[i].delete();
				}else{
					files[i].renameTo(new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcsfail\\"+files[i].getName()));
				}
			}
			log.info("professionaldmcs finish");
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		try{
			//professionaldmcs			
			File folder = new File(folderUrl+"professionaldmcs");
			File[] files = folder.listFiles();
			int logNum = files.length;
			log.info("professionaldmcs number: "+logNum);
			for (int i=0; i<logNum; i++){
				log.info("file: "+i);
				if (processSCSAnalyzer(new SCSAnalyzer(files[i].getAbsolutePath()))){
					files[i].delete();
				}else{
					files[i].renameTo(new File(folderUrl+"professionaldmcsfail\\f"+files[i].getName()));
				}
			}
			log.info("professionaldmcs finish");
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	private boolean processSCSAnalyzer(SCSAnalyzer ana){
		boolean result = false;
		try{
			ScsPubEventHome sph = new ScsPubEventHome();
			ana.analyze();
			Map<String, Date> eventTimeStamps = ana.getEventTimeStamps();
			log.info(ana.getLogFile().getName());
			log.info(ana.getBytesTransfered());
			ScsPubEvent spe = sph.find(ana.getLogFile().getName(), eventTimeStamps.get(SCSAnalyzer.START));
			if (spe==null){
				log.info("Cannot find the record in DB");
				spe = sph.getInstance();
			}else{
				log.info("record found");
			}
			spe.setLogName(ana.getLogFile().getName());
			spe.setStatus(ana.getStatus());
			spe.setType(ana.getType());
			spe.setDocbase(ana.getDocbase());
			spe.setLcState(ana.getLcState());
			spe.setStartTime(eventTimeStamps.get(SCSAnalyzer.START));
			spe.setCompleteTime(eventTimeStamps.get(SCSAnalyzer.COMPLETED));
			try{
				String bytesTransStr = ana.getBytesTransfered().trim();
				bytesTransStr = bytesTransStr.substring(0, bytesTransStr.indexOf("bytes")-1);
				log.info(bytesTransStr);
				long bytesTrans = Long.parseLong(bytesTransStr);
				log.info(bytesTrans);
				spe.setBytesTransfered(bytesTrans);
			}catch(Exception e){
				//e.printStackTrace();
				spe.setBytesTransfered(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND)!=null&& eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART)!=null){
				spe.setContBaseTaken(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART).getTime());
			}else{
				spe.setContBaseTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND)!=null&& eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART)!=null){
				spe.setContlessBaseTaken(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART).getTime());
			}else{
				spe.setContlessBaseTaken(0);
			}
			if(spe.getCompleteTime()!=null&& spe.getStartTime()!=null){
				spe.setExportTimeTaken(spe.getCompleteTime().getTime()-spe.getStartTime().getTime());
			}else{
				spe.setExportTimeTaken(0);
			}
			spe.setContObjCount(ana.getContentObjCount());
			spe.setContlessObjCount(ana.getTotalObjCount()-ana.getContentObjCount());
			
			if(eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG_COMPLETE)!=null&& eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG)!=null){
				spe.setLocalSyncTaken(eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG_COMPLETE).getTime() - eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG).getTime());
			}else{
				spe.setLocalSyncTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION)!=null&& eventTimeStamps.get(SCSAnalyzer.TRANSFER_TO_AGENT)!=null){
				spe.setTargetTransferTaken(eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION).getTime() - eventTimeStamps.get(SCSAnalyzer.TRANSFER_TO_AGENT).getTime());
			}else{
				spe.setTargetTransferTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.TARGET_SYNC_COMPLETE)!=null&& eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION)!=null){
				spe.setTargetSyncTaken(eventTimeStamps.get(SCSAnalyzer.TARGET_SYNC_COMPLETE).getTime() - eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION).getTime());
			}else{
				spe.setTargetSyncTaken(0);
			}
			sph.getEntityManager().persist(spe);
			result = "successful".equals(ana.getStatus());
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}		
		return result;
	}
	@SuppressWarnings("unchecked")
	@Begin(join=true)
	public List<ScsPubEvent> getMyResultList(){

		if (myResultList==null){
			Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
			if  (null!=scspubevent.getLogName()&& !"".equals(scspubevent.getLogName())){
				c.add(Restrictions.ilike("logName", "%"+scspubevent.getLogName()+"%"));
			}
			log.info("StartDate "+startDate);
			if (null!=startDate){
				c.add(Restrictions.ge("startTime", startDate));
			}
			if(null!=endDate){
				c.add(Restrictions.lt("startTime", endDate));
			}
			if (!"ALL".equals(scspubevent.getDocbase())){
				c.add(Restrictions.eq("docbase", scspubevent.getDocbase()));
			}
			if (!"ALL".equals(scspubevent.getType())){
				c.add(Restrictions.eq("type", scspubevent.getType()));
			}
			if (!"ALL".equals(scspubevent.getStatus())){
				c.add(Restrictions.eq("status", scspubevent.getStatus()));
			}
			myResultList = c.list();
		
			if (myResultList.size()>0){
				List avgList = c.setProjection(Projections.projectionList()
						.add(Projections.avg("exportTimeTaken"), "expAvg")
						.add(Projections.avg("contBaseTaken"), "cbTaken")
						.add(Projections.avg("contObjCount"), "coCount")
						.add(Projections.avg("contlessBaseTaken"), "clbTaken")
						.add(Projections.avg("contlessObjCount"), "cloCount")
						.add(Projections.avg("localSyncTaken"), "lsTaken")
						.add(Projections.avg("targetTransferTaken"), "ttTaken")
						.add(Projections.avg("bytesTransfered"), "bytesTransfered")
						.add(Projections.avg("targetSyncTaken"), "tsTaken")).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
				if(avgList.size()>0){
					Map<String, Object> m = (Map<String, Object>) avgList.get(0);
					scspubevent.setExportTimeTaken(((Double)m.get("expAvg")).longValue());
					scspubevent.setContBaseTaken(((Double)m.get("cbTaken")).longValue());
					scspubevent.setContlessBaseTaken(((Double)m.get("clbTaken")).longValue());
					scspubevent.setLocalSyncTaken(((Double)m.get("lsTaken")).longValue());
					scspubevent.setTargetTransferTaken(((Double)m.get("ttTaken")).longValue());
					scspubevent.setBytesTransfered(((Double)m.get("bytesTransfered")).longValue());
					scspubevent.setTargetSyncTaken(((Double)m.get("tsTaken")).longValue());
					scspubevent.setContObjCount(((Double)m.get("coCount")).doubleValue());
					scspubevent.setContlessObjCount(((Double)m.get("cloCount")).doubleValue());
					
				}
			}
		}else{
			log.info("My Result List is not null");
		}
		return myResultList;
	}
	
	public void resetMyResultList(){
		log.info("resetMyResultList is called.");
		myResultList = null;
	}
	
	public void resetMyChart(){
		log.info("resetMyChart is called.");
		cons1Inc = null;
		cons7Inc = null;
		cons105Inc = null;
		prof1SIP = null;
		prof7SIP = null;
		prof105SIP = null;
	}

	@SuppressWarnings("unchecked")
	private void calculateCharts(){
		log.info("Start calculateCharts");
		
		//Prepare
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		SimpleDateFormat sdf = new SimpleDateFormat();

		scsChart1Date = Calendar.getInstance();
		scsChart1Date.setTime(scsStartDate);
		scsChartBaseDate = (Calendar)scsChart1Date.clone();
		scsChartBaseDate.add(Calendar.DATE, 1);
		scsChart7Date = (Calendar)scsChartBaseDate.clone();
		scsChart105Date = (Calendar)scsChartBaseDate.clone();
		scsChart105BaseDate = (Calendar)scsChartBaseDate.clone();
		scsChart7Date.add(Calendar.DATE, -7);
		scsChart105Date.add(Calendar.DATE, -105);

		//Consumer one day
		sdf.applyPattern("MM/dd/yyyy");
		cons1Inc = new SCSChart("Consumer Incremental 24Hr Chart|" + sdf.format(scsChart1Date.getTime()));
		//log.info("last day start: " + scsChart1Date);
		//log.info("last day end: " + scsChartBaseDate);
		Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart1Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "webmddoc01"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		List cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'HH') as STARTTIME_DATE", "trunc(STARTTIME, 'HH')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		Map<String, Object> m;
		StringBuffer sbx = new StringBuffer(); 
		StringBuffer sbavgt = new StringBuffer(); 
		StringBuffer sbavgc = new StringBuffer(); 
		log.info("how many: " + cList.size());
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(((Calendar)m.get("STARTTIME_DATE")).get(Calendar.HOUR_OF_DAY)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("hr: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			cons1Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			cons1Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			cons1Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			cons1Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			cons1Inc.useHourlyLabel();
		}
		
		//Consumer 7 day
		sdf.applyPattern("MM/dd/yyyy");
		cons7Inc = new SCSChart("Consumer Incremental 7-day Chart|" + sdf.format(scsChart7Date.getTime()) + " - " + sdf.format(scsChart1Date.getTime()));
		//log.info("7 day start: " + sdf.format(scsChart7Date.getTime()));
		//log.info("7 day end: " + sdf.format(scsChartBaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart7Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "webmddoc01"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'DD') as STARTTIME_DATE", "trunc(STARTTIME, 'DD')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateDateDiffNumber((Calendar)scsChart7Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 7)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			cons7Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			cons7Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			cons7Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			cons7Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			cons7Inc.useDailyLabel((Calendar)scsChart7Date.clone(), 7, "M/dd");
			//log.info("daily label: " + cons7Inc.getLabelX1Str() + " END");
		}
		
		//Consumer 105 day
		scsChart105BaseDate.add(Calendar.DATE, -scsChart105BaseDate.get(Calendar.DAY_OF_WEEK)+1);
		scsChart105Date.add(Calendar.DATE, -scsChart105Date.get(Calendar.DAY_OF_WEEK)+1);
		//log.info("105 day start: " + sdf.format(scsChart105Date.getTime()));
		//log.info("105 day end: " + sdf.format(scsChartBaseDate.getTime()));
		//log.info("105 base weekday " + scsChart105BaseDate.get(Calendar.DAY_OF_WEEK));
		//log.info("105 weekday " + scsChart105Date.get(Calendar.DAY_OF_WEEK));
		sdf.applyPattern("MM/dd/yyyy");
		cons105Inc = new SCSChart("Consumer Incremental 15-week Chart|" + sdf.format(scsChart105Date.getTime()) + " - " + sdf.format(scsChart105BaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart105Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChart105BaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "webmddoc01"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(next_day(STARTTIME-7, 'SUNDAY')) as STARTTIME_DATE", "trunc(next_day(STARTTIME-7, 'SUNDAY'))", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateWeekDiffNumber((Calendar)scsChart105Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 15)+",");
			//sbx.append(sdf.format(((Calendar)m.get("STARTTIME_DATE")).getTime())+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			cons105Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			cons105Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			cons105Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			cons105Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			cons105Inc.useWeeklyLabel((Calendar)scsChart105Date.clone(), 15, "M/dd");
			//log.info("weekly label: " + cons105Inc.getLabelX1Str() + " END");
		}

		//Boots one day
		sdf.applyPattern("MM/dd/yyyy");
		boots1Inc = new SCSChart("Boots Incremental 24Hr Chart|" + sdf.format(scsChart1Date.getTime()));
		//log.info("last day start: " + scsChart1Date);
		//log.info("last day end: " + scsChartBaseDate);
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart1Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "boots"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'HH') as STARTTIME_DATE", "trunc(STARTTIME, 'HH')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(((Calendar)m.get("STARTTIME_DATE")).get(Calendar.HOUR_OF_DAY)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("hr: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			//log.info("cons1Inc: " + cons1Inc.getTitle() + " END");
			boots1Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			boots1Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			boots1Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			boots1Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			boots1Inc.useHourlyLabel();	
		}

		//Boots 7 day
		sdf.applyPattern("MM/dd/yyyy");
		boots7Inc = new SCSChart("Boots Incremental 7-day Chart|" + sdf.format(scsChart7Date.getTime()) + " - " + sdf.format(scsChart1Date.getTime()));
		//log.info("7 day start: " + sdf.format(scsChart7Date.getTime()));
		//log.info("7 day end: " + sdf.format(scsChartBaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart7Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "boots"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'DD') as STARTTIME_DATE", "trunc(STARTTIME, 'DD')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateDateDiffNumber((Calendar)scsChart7Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 7)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			boots7Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			boots7Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			boots7Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			boots7Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			boots7Inc.useDailyLabel((Calendar)scsChart7Date.clone(), 7, "M/dd");
			//log.info("daily label: " + cons7Inc.getLabelX1Str() + " END");
		}
		
		//Boots 105 day
		scsChart105BaseDate.add(Calendar.DATE, -scsChart105BaseDate.get(Calendar.DAY_OF_WEEK)+1);
		scsChart105Date.add(Calendar.DATE, -scsChart105Date.get(Calendar.DAY_OF_WEEK)+1);
		//log.info("105 day start: " + sdf.format(scsChart105Date.getTime()));
		//log.info("105 day end: " + sdf.format(scsChartBaseDate.getTime()));
		//log.info("105 base weekday " + scsChart105BaseDate.get(Calendar.DAY_OF_WEEK));
		//log.info("105 weekday " + scsChart105Date.get(Calendar.DAY_OF_WEEK));
		sdf.applyPattern("MM/dd/yyyy");
		boots105Inc = new SCSChart("Boots Incremental 15-week Chart|" + sdf.format(scsChart105Date.getTime()) + " - " + sdf.format(scsChart105BaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart105Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChart105BaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "boots"));
		c.add(Restrictions.eq("type", "I"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(next_day(STARTTIME-7, 'SUNDAY')) as STARTTIME_DATE", "trunc(next_day(STARTTIME-7, 'SUNDAY'))", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateWeekDiffNumber((Calendar)scsChart105Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 15)+",");
			//sbx.append(sdf.format(((Calendar)m.get("STARTTIME_DATE")).getTime())+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/60000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			boots105Inc.setDataX1Str(sbx.substring(0, sbx.length()-1));
			boots105Inc.setDataX2Str(sbx.substring(0, sbx.length()-1));
			boots105Inc.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			boots105Inc.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			boots105Inc.useWeeklyLabel((Calendar)scsChart105Date.clone(), 15, "M/dd");
			log.info("weekly label: " + boots105Inc.getLabelX1Str() + " END");
		}
		
		//Professional one day
		sdf.applyPattern("MM/dd/yyyy");
		prof1SIP = new SCSChart("Professional SIP 24Hr Chart|" + sdf.format(scsChart1Date.getTime()));
		//log.info("last day start: " + scsChart1Date);
		//log.info("last day end: " + scsChartBaseDate);
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart1Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "meddoc01"));
		c.add(Restrictions.eq("type", "S"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'HH') as STARTTIME_DATE", "trunc(STARTTIME, 'HH')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(((Calendar)m.get("STARTTIME_DATE")).get(Calendar.HOUR_OF_DAY)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/1000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("hr: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			//log.info("cons1Inc: " + cons1Inc.getTitle() + " END");
			prof1SIP.setDataX1Str(sbx.substring(0, sbx.length()-1));
			prof1SIP.setDataX2Str(sbx.substring(0, sbx.length()-1));
			prof1SIP.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			prof1SIP.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			prof1SIP.useHourlyLabel();	
		}

		//Professional 7 day
		sdf.applyPattern("MM/dd/yyyy");
		prof7SIP = new SCSChart("Professional SIP 7-day Chart|" + sdf.format(scsChart7Date.getTime()) + " - " + sdf.format(scsChart1Date.getTime()));
		//log.info("7 day start: " + sdf.format(scsChart7Date.getTime()));
		//log.info("7 day end: " + sdf.format(scsChartBaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart7Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChartBaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "meddoc01"));
		c.add(Restrictions.eq("type", "S"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(STARTTIME, 'DD') as STARTTIME_DATE", "trunc(STARTTIME, 'DD')", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateDateDiffNumber((Calendar)scsChart7Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 7)+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/1000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			prof7SIP.setDataX1Str(sbx.substring(0, sbx.length()-1));
			prof7SIP.setDataX2Str(sbx.substring(0, sbx.length()-1));
			prof7SIP.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			prof7SIP.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			prof7SIP.useDailyLabel((Calendar)scsChart7Date.clone(), 7, "M/dd");
			//log.info("daily label: " + cons7Inc.getLabelX1Str() + " END");
		}
		
		//Professional 105 day
		scsChart105BaseDate.add(Calendar.DATE, -scsChart105BaseDate.get(Calendar.DAY_OF_WEEK)+1);
		scsChart105Date.add(Calendar.DATE, -scsChart105Date.get(Calendar.DAY_OF_WEEK)+1);
		//log.info("105 day start: " + sdf.format(scsChart105Date.getTime()));
		//log.info("105 day end: " + sdf.format(scsChartBaseDate.getTime()));
		//log.info("105 base weekday " + scsChart105BaseDate.get(Calendar.DAY_OF_WEEK));
		//log.info("105 weekday " + scsChart105Date.get(Calendar.DAY_OF_WEEK));
		sdf.applyPattern("MM/dd/yyyy");
		prof105SIP = new SCSChart("Professional SIP 15-week Chart|" + sdf.format(scsChart105Date.getTime()) + " - " + sdf.format(scsChart105BaseDate.getTime()));
		c = null;
		m = null;
		cList = null;
		sbx = new StringBuffer(); 
		sbavgt = new StringBuffer(); 
		sbavgc = new StringBuffer(); 
		
		c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		c.add(Restrictions.ge("startTime", scsChart105Date.getTime()));
		c.add(Restrictions.lt("startTime", scsChart105BaseDate.getTime()));
		c.add(Restrictions.isNotNull("startTime"));
		c.add(Restrictions.eq("docbase", "meddoc01"));
		c.add(Restrictions.eq("type", "S"));
		c.add(Restrictions.eq("status", "successful"));
		c.add(Restrictions.eq("lcState", "Active"));
		cList = c.setProjection(Projections.projectionList()
				.add(Projections.sqlGroupProjection("trunc(next_day(STARTTIME-7, 'SUNDAY')) as STARTTIME_DATE", "trunc(next_day(STARTTIME-7, 'SUNDAY'))", new String[] {"STARTTIME_DATE"}, new Type[] {Hibernate.CALENDAR}), "STARTTIME_DATE")
				.add(Projections.avg("exportTimeTaken"), "expAvg")
				.add(Projections.avg("contObjCount"), "contObjCountAvg")
				.add(Projections.sqlProjection("STDDEV(exportTimeTaken)", new String[] {"STDDEV(exportTimeTaken)"}, new Type[] {Hibernate.DOUBLE})))
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
				.addOrder(Order.asc("STARTTIME_DATE")).list();
		sdf.applyPattern("MM/dd");
		for (int i=0; i<cList.size(); i++){
			m = (Map<String, Object>) cList.get(i);
			sbx.append(SCSChart.calculateWeekDiffNumber((Calendar)scsChart105Date.clone(), (Calendar)m.get("STARTTIME_DATE"), 15)+",");
			//sbx.append(sdf.format(((Calendar)m.get("STARTTIME_DATE")).getTime())+",");
			sbavgt.append(nf.format(((Double)m.get("expAvg")).longValue()/1000.0)+",");
			sbavgc.append(nf.format(((Double)m.get("contObjCountAvg")).doubleValue())+",");
		}
		if (cList.size()>0){
			log.info("date: " + sbx.substring(0, sbx.length()-1) + " END");
			log.info("time: " + sbavgt.substring(0, sbavgt.length()-1) + " END");
			log.info("count: " + sbavgc.substring(0, sbavgc.length()-1) + " END");
			prof105SIP.setDataX1Str(sbx.substring(0, sbx.length()-1));
			prof105SIP.setDataX2Str(sbx.substring(0, sbx.length()-1));
			prof105SIP.setDataY1Str(sbavgt.substring(0, sbavgt.length()-1));
			prof105SIP.setDataY2Str(sbavgc.substring(0, sbavgc.length()-1));
			prof105SIP.useWeeklyLabel((Calendar)scsChart105Date.clone(), 15, "M/dd");
			log.info("weekly label: " + prof105SIP.getLabelX1Str() + " END");
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getRowNumberPerPage() {
		return rowNumberPerPage;
	}

	public void setRowNumberPerPage(int rowNum) {
		this.rowNumberPerPage = rowNum;
	}
	
	public ScsPubEventList()
    {
        setEjbql(EJBQL);
        rowNumberPerPage = 15;
        scspubevent.setDocbase("ALL");
		scspubevent.setType("ALL");
		scspubevent.setStatus("ALL");
		Calendar calReport = Calendar.getInstance();
		calReport.set(Calendar.HOUR_OF_DAY, 0);
		calReport.set(Calendar.MINUTE, 0);
		calReport.set(Calendar.SECOND, 0);
		calReport.set(Calendar.MILLISECOND, 0);
		calReport.add(Calendar.DATE, -1);
		startDate = calReport.getTime();

		//Date for SCS Chart
		//set basedate = 9/10/2009 - test 9/9/2009	
		Calendar calChart = Calendar.getInstance();
		calChart.set(Calendar.HOUR_OF_DAY, 0);
		calChart.set(Calendar.MINUTE, 0);
		calChart.set(Calendar.SECOND, 0);
		calChart.set(Calendar.MILLISECOND, 0);
		//calChart.set(Calendar.MONTH, Calendar.SEPTEMBER);
		//calChart.set(Calendar.DAY_OF_MONTH, 10);
		calChart.add(Calendar.DATE, -1);
		scsStartDate = ((Calendar)calChart.clone()).getTime();
    }
	
	public ScsPubEvent getScspubevent() {
		return scspubevent;
	}

	public SCSChart getCons1Inc() {
		if (cons1Inc==null)
			calculateCharts();
		return cons1Inc;
	}

	public void setCons1Inc(SCSChart cons1Inc) {
		this.cons1Inc = cons1Inc;
	}

	public SCSChart getCons7Inc() {
		return cons7Inc;
	}

	public void setCons7Inc(SCSChart cons7Inc) {
		this.cons7Inc = cons7Inc;
	}

	public SCSChart getCons105Inc() {
		return cons105Inc;
	}

	public void setCons105Inc(SCSChart cons105Inc) {
		this.cons105Inc = cons105Inc;
	}

	public SCSChart getProf1SIP() {
		return prof1SIP;
	}

	public void setProf1SIP(SCSChart prof1SIP) {
		this.prof1SIP = prof1SIP;
	}

	public SCSChart getProf7SIP() {
		return prof7SIP;
	}

	public void setProf7SIP(SCSChart prof7SIP) {
		this.prof7SIP = prof7SIP;
	}

	public SCSChart getProf105SIP() {
		return prof105SIP;
	}

	public void setProf105SIP(SCSChart prof105SIP) {
		this.prof105SIP = prof105SIP;
	}

	public Date getScsStartDate() {
		return scsStartDate;
	}

	public void setScsStartDate(Date scsStartDate) {
		this.scsStartDate = scsStartDate;
	}

	public SCSChart getBoots1Inc() {
		return boots1Inc;
	}

	public void setBoots1Inc(SCSChart boots1Inc) {
		this.boots1Inc = boots1Inc;
	}

	public SCSChart getBoots7Inc() {
		return boots7Inc;
	}

	public void setBoots7Inc(SCSChart boots7Inc) {
		this.boots7Inc = boots7Inc;
	}

	public SCSChart getBoots105Inc() {
		return boots105Inc;
	}

	public void setBoots105Inc(SCSChart boots105Inc) {
		this.boots105Inc = boots105Inc;
	}

	public boolean isShowX1data() {
		return showX1data;
	}

	public void setShowX1data(boolean showX1data) {
		this.showX1data = showX1data;
	}

	public String getDataX1Label() {
		if (showX1data)
			dataX1Label = "|N*f2*,555555,0,-1,9,7";
		else
			dataX1Label = "";
		return dataX1Label;
	}

	public void setDataX1Label(String dataX1Label) {
		this.dataX1Label = dataX1Label;
	}

}
