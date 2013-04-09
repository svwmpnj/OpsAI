package com.webmd.opsai.session;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import com.webmd.opsai.entity.DamMTSMainEvent;
import com.webmd.opsai.util.DAMLogParserDB;

@Name("damMTSMainEventList")
@Scope (CONVERSATION)
public class DamMTSMainEventList extends EntityQuery<DamMTSMainEvent>
{
	private static final long serialVersionUID = 1L;
	private DamMTSMainEvent mainEventMax = new DamMTSMainEvent();
	private DamMTSMainEvent mainEventMin = new DamMTSMainEvent();
	private DamMTSMainEvent mainEventAvg = new DamMTSMainEvent();
	private DamMTSMainEvent mainProxyAvg = new DamMTSMainEvent();
	private DamMTSMainEvent mainRenditionAvg = new DamMTSMainEvent();
	private List<DamMTSMainEvent> myResultList;
	private String widthxheight;
	private int completeProxyNo, totalProxyNo;
	private int completeRenditionNo, totalRenditionNo;
	
	@Logger private Log log;
    public DamMTSMainEventList()
    {
        setEjbql("select damMTSMainEvent from DamMTSMainEvent damMTSMainEvent");
        Calendar calReport = Calendar.getInstance();
		calReport.set(Calendar.HOUR_OF_DAY, 0);
		calReport.set(Calendar.MINUTE, 0);
		calReport.set(Calendar.SECOND, 0);
		calReport.set(Calendar.MILLISECOND, 0);
		calReport.add(Calendar.DATE, -1);
		mainEventMin.setMtsMainStartTime(calReport.getTime());
		/*
		if (widthxheightArray == null){
			List<Object[]> result;
			SQLQuery sq = ((Session)getEntityManager().getDelegate())
				.createSQLQuery("select distinct concat(concat(fwidth, 'x'), fheight), fwidth from DAMMTSMAINEVENT order by fwidth");
			result = sq.list();
			widthxheightArray = new String[result.size()];
			for (int i=0; i<result.size();i++){
				System.out.println("result: " + (String)(result.get(i)[0]));
				widthxheightArray[i] = (String)(result.get(i)[0]);
			}
			//widthxheightArray = (String[]) result.toArray();
		}
		//setWidthxheightArray(result.get(0));*/
		
    }
    
    @Begin(join=true)
    public void processMTSLogs() throws Exception {
    	log.info("Triggered!!");
    	FacesContext context = FacesContext.getCurrentInstance();
		String dateStr = getContextParam(context, "logDate");
		String[] mtsHosts = {"01l", "02l"};
		//String[] mtsHosts = {"01l"};
		String[] mbSites = {"1001", "1006"};
		DAMLogParserDB logParser = new DAMLogParserDB(dateStr, mtsHosts, mbSites);
		logParser.processMTS();
    }
    
    @Begin(join=true)
    public void processMTSLogs1() throws Exception {
    	log.info("Triggered!!");
    	//FacesContext context = FacesContext.getCurrentInstance();
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(this.mainEventMin.getMtsMainStartTime());
		String[] mtsHosts = {"01l", "02l", "03l", "04l"};
		//String[] mtsHosts = {"04l"};
		String[] mbSites = {"1001", "1006"};
		DAMLogParserDB logParser = new DAMLogParserDB(dateStr, mtsHosts, mbSites);
		logParser.processMTS();
    }
    
    @SuppressWarnings("unchecked")
	@Begin(join=true)
	public void calculateMyResultList(){
		log.info("getMyResultList is called.");
		log.info("vcodec selected: " + mainEventMin.getVCodec());
		String[] wxh;
		List<Criterion> crList = new LinkedList<Criterion>();

		Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSMainEvent.class);		
		ProjectionList pl = Projections.projectionList().add(Projections.avg("mtsTotalTime"), "totalTimeAvg");
		Map<String, Object> m = null;
		
		if (null!=mainEventMin.getMtsMainStartTime()){	crList.add(Restrictions.ge("mtsMainStartTime", mainEventMin.getMtsMainStartTime()));}
		if (null!=mainEventMax.getMtsMainStartTime()){	crList.add(Restrictions.lt("mtsMainStartTime", mainEventMax.getMtsMainStartTime()));}
		if (mainEventMin.getStatus()!=null){			crList.add(Restrictions.eq("status", mainEventMin.getStatus()));}
		if (mainEventMin.getVideoSize()>0){				crList.add(Restrictions.ge("videoSize", mainEventMin.getVideoSize()));}
		if (mainEventMax.getVideoSize()>0){				crList.add(Restrictions.lt("videoSize", mainEventMax.getVideoSize()));}
		if (widthxheight!=null && !widthxheight.equals("ALL")){
			wxh = widthxheight.split("x");
			crList.add(Restrictions.eq("FWidth",wxh[0]));
			crList.add(Restrictions.eq("FHeight",wxh[1]));
		}
		if (mainEventMin.getVideoDuration()>0){				crList.add(Restrictions.ge("videoDuration", mainEventMin.getVideoDuration()));}
		if (mainEventMax.getVideoDuration()>0){				crList.add(Restrictions.lt("videoDuration", mainEventMax.getVideoDuration()));}
		if (mainEventMin.getVCodec()!=null){			crList.add(Restrictions.eq("VCodec", mainEventMin.getVCodec()));}
		if (mainEventMin.getACodec()!=null){			crList.add(Restrictions.eq("ACodec", mainEventMin.getACodec()));}
		/*if (mainEventMin.getChildren()!=0){
			if (mainEventMin.getChildren()==-1){		crList.add(Restrictions.eq("children", 0));}
			else if (mainEventMin.getChildren()==1){	crList.add(Restrictions.eq("children", 1));}
			else if (mainEventMin.getChildren()==2){	crList.add(Restrictions.gt("children", 1));}			
		}*/
		if (mainEventMin.getType()!=null){			crList.add(Restrictions.eq("type", mainEventMin.getType()));}
		
		for(int i=0; i<crList.size();i++)
			c.add(crList.get(i));
		c.addOrder( Order.asc("mtsMainStartTime"));
		myResultList = c.list();
		
		List avgList = null;
		//proxy job
		Criteria cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSMainEvent.class);
		for(int i=0; i<crList.size();i++)
			cAggr.add(crList.get(i));
		cAggr.add(Restrictions.eq("type", "Proxy"));
		totalProxyNo = cAggr.list().size();
		cAggr.add(Restrictions.eq("status", "Complete"));
		completeProxyNo = cAggr.list().size();
		if (completeProxyNo>0){
			avgList = cAggr.setProjection(pl)
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			if(avgList.size()>0){
				m = (Map<String, Object>) avgList.get(0);
				mainProxyAvg.setMtsTotalTime(((Double)m.get("totalTimeAvg")).longValue());
			}
	    }
		
		//Rendition Job
		cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSMainEvent.class);
		for(int i=0; i<crList.size();i++)
			cAggr.add(crList.get(i));
		cAggr.add(Restrictions.eq("type", "Rendition"));
		totalRenditionNo = cAggr.list().size();
		cAggr.add(Restrictions.eq("status", "Complete"));
		completeRenditionNo = cAggr.list().size();
		if (completeRenditionNo>0){
			avgList = cAggr.setProjection(pl)
				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			if(avgList.size()>0){
				m = (Map<String, Object>) avgList.get(0);
				mainRenditionAvg.setMtsTotalTime(((Double)m.get("totalTimeAvg")).longValue());
			}
	    }
		/*
		totalNo = myResultList.size();
		c.add(Restrictions.eq("status", "Complete"));
		completeNo = c.list().size();
		if (completeNo>0){
			List avgList = c.setProjection(Projections.projectionList()
					.add(Projections.avg("mtsTotalTime"), "totalTimeAvg"))
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			if(avgList.size()>0){
				Map<String, Object> m = (Map<String, Object>) avgList.get(0);
				mainEventAvg.setMtsTotalTime(((Double)m.get("totalTimeAvg")).longValue());
			}
		}else{
			mainEventAvg.setMtsTotalTime(0l);
		}*/
		
		
		
		
	}
    
    public List<DamMTSMainEvent> getMyResultList(){
    	if (myResultList==null)
    		calculateMyResultList();
    	return myResultList;
    }
	
	public void resetMyResultList(){
		log.info("resetMyResultList is called.");
		myResultList = null;
	}

    
    private String getContextParam(FacesContext context, String key){
    	Map<String, String> params = context.getExternalContext().getRequestParameterMap();
    	return params.get(key);
    }

	public DamMTSMainEvent getMainEventMax() {
		return mainEventMax;
	}

	public void setMainEventMax(DamMTSMainEvent mainEventMax) {
		this.mainEventMax = mainEventMax;
	}

	public DamMTSMainEvent getMainEventMin() {
		return mainEventMin;
	}

	public void setMainEventMin(DamMTSMainEvent mainEventMin) {
		this.mainEventMin = mainEventMin;
	}
	public String getWidthxheight() {
		return widthxheight;
	}

	public void setWidthxheight(String widthxheight) {
		this.widthxheight = widthxheight;
	}

	public DamMTSMainEvent getMainEventAvg() {
		return mainEventAvg;
	}

	public void setMainEventAvg(DamMTSMainEvent mainEventAvg) {
		this.mainEventAvg = mainEventAvg;
	}

	public int getCompleteProxyNo() {
		return completeProxyNo;
	}

	public void setCompleteProxyNo(int completeProxyNo) {
		this.completeProxyNo = completeProxyNo;
	}

	public int getTotalProxyNo() {
		return totalProxyNo;
	}

	public void setTotalProxyNo(int totalProxyNo) {
		this.totalProxyNo = totalProxyNo;
	}

	public int getCompleteRenditionNo() {
		return completeRenditionNo;
	}

	public void setCompleteRenditionNo(int completeRenditionNo) {
		this.completeRenditionNo = completeRenditionNo;
	}

	public int getTotalRenditionNo() {
		return totalRenditionNo;
	}

	public void setTotalRenditionNo(int totalRenditionNo) {
		this.totalRenditionNo = totalRenditionNo;
	}

	public void setMyResultList(List<DamMTSMainEvent> myResultList) {
		this.myResultList = myResultList;
	}

	public DamMTSMainEvent getMainProxyAvg() {
		return mainProxyAvg;
	}

	public void setMainProxyAvg(DamMTSMainEvent mainProxyAvg) {
		this.mainProxyAvg = mainProxyAvg;
	}

	public DamMTSMainEvent getMainRenditionAvg() {
		return mainRenditionAvg;
	}

	public void setMainRenditionAvg(DamMTSMainEvent mainRenditionAvg) {
		this.mainRenditionAvg = mainRenditionAvg;
	}
	
    
}
