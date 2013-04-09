package com.webmd.opsai.session;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import com.webmd.opsai.entity.DamMBEvent;
import com.webmd.opsai.util.DAMLogParserDB;

@Name("damMBEventList")
public class DamMBEventList extends EntityQuery<DamMBEvent>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<DamMBEvent> myResultList;
	private DamMBEvent mbMin = new DamMBEvent();
	private DamMBEvent mbMax = new DamMBEvent();
	private DamMBEvent mbAvg = new DamMBEvent();
	private DamMBEvent videoAvg = new DamMBEvent();
	private DamMBEvent imageAvg = new DamMBEvent();
	private int totalNo, videoNo, imageNo;
	
	@Logger private Log log;
    public DamMBEventList()
    {
        setEjbql("select damMBEvent from DamMBEvent damMBEvent");
        Calendar calReport = Calendar.getInstance();
		calReport.set(Calendar.HOUR_OF_DAY, 0);
		calReport.set(Calendar.MINUTE, 0);
		calReport.set(Calendar.SECOND, 0);
		calReport.set(Calendar.MILLISECOND, 0);
		calReport.add(Calendar.DATE, -1);
		mbMin.setElemEventStart(calReport.getTime());
    }
    
    
    @Begin(join=true)
    public void processMBLogs() throws Exception{
    	log.info("Triggered!!");
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(this.mbMin.getElemEventStart());
		String[] mtsHosts = {"01l", "02l"};
		//String[] mtsHosts = {"01l"};
		String[] mbSites = {"1001", "1006"};
		DAMLogParserDB logParser = new DAMLogParserDB(dateStr, mtsHosts, mbSites);
		logParser.processMB();

    }

    @SuppressWarnings("unchecked")
	@Begin(join=true)
	public void calculateMyResultList(){
		log.info("getMyResultList is called.");
		List<Criterion> crList = new LinkedList<Criterion>();
		Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMBEvent.class);	
		Criteria cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMBEvent.class);
		ProjectionList pl = Projections.projectionList()
			.add(Projections.avg("queueWaiting"), "queueWaitTime")
			.add(Projections.avg("cmsTime1"), "cmsTime1")
			.add(Projections.avg("ftpTime"), "ftpTime")
			.add(Projections.avg("cmsTime2"), "cmsTime2")
			.add(Projections.avg("totalTime"), "totalTime");
		
		Map<String, Object> m = null;
		if (null!=mbMin.getElemEventStart()){	crList.add(Restrictions.ge("elemEventStart", mbMin.getElemEventStart()));}
		if (null!=mbMax.getElemEventStart()){	crList.add(Restrictions.lt("elemEventStart", mbMax.getElemEventStart()));}
		if (null!=mbMin.getObjState()){	crList.add(Restrictions.eq("objState", mbMin.getObjState()));}
		if (null!=mbMin.getObjType()){	crList.add(Restrictions.eq("objType", mbMin.getObjType()));}
		
		//Calculate Total
		for(int i=0; i<crList.size();i++)
			c.add(crList.get(i));
		c.addOrder( Order.asc("elemEventStart"));
		myResultList = c.list();
		totalNo = myResultList.size();	
		List avgList = null;
		if(totalNo>0){
			for(int i=0; i<crList.size();i++)
				cAggr.add(crList.get(i));		
			avgList = cAggr.setProjection(pl)
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			if(avgList.size()>0){
				m = (Map<String, Object>) avgList.get(0);
				mbAvg.setQueueWaiting(((Double)m.get("queueWaitTime")).longValue());
				mbAvg.setTotalTime(((Double)m.get("totalTime")).longValue());
			}
		
			//Calculate Video
			cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMBEvent.class);
			for(int i=0; i<crList.size();i++)
				cAggr.add(crList.get(i));
			cAggr.add(Restrictions.eq("objType", "wbmd_cons_video"));
			videoNo = cAggr.list().size();
			if (videoNo>0){
				avgList = cAggr.setProjection(pl)
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
				if(avgList.size()>0){
					m = (Map<String, Object>) avgList.get(0);
					videoAvg.setQueueWaiting(((Double)m.get("queueWaitTime")).longValue());
					videoAvg.setCmsTime1(((Double)m.get("cmsTime1")).longValue());
					videoAvg.setFtpTime(((Double)m.get("ftpTime")).longValue());
					videoAvg.setCmsTime2(((Double)m.get("cmsTime2")).longValue());
					videoAvg.setTotalTime(((Double)m.get("totalTime")).longValue());
				}
		    }
			
			//Calculate image
			cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMBEvent.class);
			for(int i=0; i<crList.size();i++)
				cAggr.add(crList.get(i));
			cAggr.add(Restrictions.eq("objType", "wbmd_cons_img"));
			imageNo = cAggr.list().size();
			if (imageNo>0){
				avgList = cAggr.setProjection(pl)
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
				if(avgList.size()>0){
					m = (Map<String, Object>) avgList.get(0);
					imageAvg.setQueueWaiting(((Double)m.get("queueWaitTime")).longValue());
					imageAvg.setTotalTime(((Double)m.get("totalTime")).longValue());
				}
			}
		}
    }
	public DamMBEvent getMbMin() {
		return mbMin;
	}


	public void setMbMin(DamMBEvent mbMin) {
		this.mbMin = mbMin;
	}


	public DamMBEvent getMbMax() {
		return mbMax;
	}


	public void setMbMax(DamMBEvent mbMax) {
		this.mbMax = mbMax;
	}


	@Begin(join=true)
    public List<DamMBEvent> getMyResultList() {
    	if (myResultList==null)
    		calculateMyResultList();
    	return myResultList;
    }

	public void setMyResultList(List<DamMBEvent> myResultList) {
		this.myResultList = myResultList;
	}


	public DamMBEvent getMbAvg() {
		return mbAvg;
	}


	public void setMbAvg(DamMBEvent mbAvg) {
		this.mbAvg = mbAvg;
	}


	public int getTotalNo() {
		return totalNo;
	}


	public void setTotalNo(int totalNo) {
		this.totalNo = totalNo;
	}


	public int getVideoNo() {
		return videoNo;
	}


	public void setVideoNo(int videoNo) {
		this.videoNo = videoNo;
	}


	public int getImageNo() {
		return imageNo;
	}


	public void setImageNo(int imageNo) {
		this.imageNo = imageNo;
	}


	public DamMBEvent getVideoAvg() {
		return videoAvg;
	}


	public void setVideoAvg(DamMBEvent videoAvg) {
		this.videoAvg = videoAvg;
	}


	public DamMBEvent getImageAvg() {
		return imageAvg;
	}


	public void setImageAvg(DamMBEvent imageAvg) {
		this.imageAvg = imageAvg;
	}
	
    
    
}
