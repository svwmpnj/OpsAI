package com.webmd.opsai.session;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Order;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.log.Log;

import com.webmd.opsai.entity.DamMTSInnerEvent;

@Name("damMTSInnerEventList")
@Scope (CONVERSATION)
public class DamMTSInnerEventList extends EntityQuery<DamMTSInnerEvent>
{
	private static final long serialVersionUID = 1L;
	private DamMTSInnerEvent innerEventMax = new DamMTSInnerEvent();
	private DamMTSInnerEvent innerEventMin = new DamMTSInnerEvent();
	private DamMTSInnerEvent innerEventAvg = new DamMTSInnerEvent();
	private List<DamMTSInnerEvent> myResultList;
	private String widthxheight;
	private int completeNo, totalNo;
	private List<Map<String, Object>> formatMap;
	private List<Map<String, Object>> renditionMap;
	private List<Map<String, Object>> vCodecMap;
	private List<Map<String, Object>> aCodecMap;
	private List<Map<String, Object>> sizeMap;
	private List<Map<String, Object>> durationMap;
	private List<Map<String, Object>> wxhMap;
	private List<Map<String, Object>> hostMap;
	
	@Logger private Log log;
	
    public DamMTSInnerEventList()
    {
        setEjbql("select dMSMTSInnerEvent from DMSMTSInnerEvent dMSMTSInnerEvent");
        Calendar calReport = Calendar.getInstance();
		calReport.set(Calendar.HOUR_OF_DAY, 0);
		calReport.set(Calendar.MINUTE, 0);
		calReport.set(Calendar.SECOND, 0);
		calReport.set(Calendar.MILLISECOND, 0);
		calReport.add(Calendar.DATE, -1);
		innerEventMin.setItemEventStart(calReport.getTime());
    }
    
    @SuppressWarnings("unchecked")
	@Begin(join=true)
	public void calculateMyResultList(){
		log.info("getMyResultList is called.");
		log.info("vcodec selected: " + innerEventMin.getVCodec());
		String[] wxh;
		List<Criterion> crList = new LinkedList<Criterion>();

		Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSInnerEvent.class);			
		if (null!=innerEventMin.getItemEventStart()){	crList.add(Restrictions.ge("itemEventStart", innerEventMin.getItemEventStart()));}
		if (null!=innerEventMax.getItemEventStart()){	crList.add(Restrictions.lt("itemEventStart", innerEventMax.getItemEventStart()));}
		if (innerEventMin.getTlsStatus()!=null){			crList.add(Restrictions.eq("tlsStatus", innerEventMin.getTlsStatus()));}
		if (innerEventMin.getVideoSize()>0){				crList.add(Restrictions.ge("videoSize", innerEventMin.getVideoSize()));}
		if (innerEventMax.getVideoSize()>0){				crList.add(Restrictions.lt("videoSize", innerEventMax.getVideoSize()));}
		if (widthxheight!=null && !widthxheight.equals("ALL")){
			wxh = widthxheight.split("x");
			crList.add(Restrictions.eq("FWidth",wxh[0]));
			crList.add(Restrictions.eq("FHeight",wxh[1]));
		}
		if (innerEventMin.getVideoDuration()>0){				crList.add(Restrictions.ge("videoDuration", innerEventMin.getVideoDuration()));}
		if (innerEventMax.getVideoDuration()>0){				crList.add(Restrictions.lt("videoDuration", innerEventMax.getVideoDuration()));}
		if (innerEventMin.getVCodec()!=null){			crList.add(Restrictions.eq("VCodec", innerEventMin.getVCodec()));}
		if (innerEventMin.getACodec()!=null){			crList.add(Restrictions.eq("ACodec", innerEventMin.getACodec()));}
		if (innerEventMin.getItemFormat()!=null){			crList.add(Restrictions.eq("itemFormat", innerEventMin.getItemFormat()));}
		if (innerEventMin.getItemRendition()!=null){			crList.add(Restrictions.eq("itemRendition", innerEventMin.getItemRendition()));}
		if (innerEventMin.getMtsHost()!=null){			crList.add(Restrictions.eq("mtsHost", innerEventMin.getMtsHost()));}
		
		for(int i=0; i<crList.size();i++)
			c.add(crList.get(i));
		c.addOrder( Order.asc("itemEventStart"));
		myResultList = c.list();
		totalNo = myResultList.size();
		
		Criteria cAggr = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSInnerEvent.class);
		for(int i=0; i<crList.size();i++)
			cAggr.add(crList.get(i));
		cAggr.add(Restrictions.eq("tlsStatus", "Complete"));
		completeNo = cAggr.list().size();
		    
	    ProjectionList pl = Projections.projectionList()
		.add(Projections.avg("innerWaitTime"), "innerWaitTime")
		.add(Projections.avg("flipWaitTime"), "flipWaitTime")
		.add(Projections.avg("encodingTime"), "encodingTime")
		.add(Projections.avg("innerTotalTime"), "innerTotalTime");
		if (completeNo>0){
			List avgList = cAggr.setProjection(pl)
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			if(avgList.size()>0){
				Map<String, Object> m = (Map<String, Object>) avgList.get(0);
				innerEventAvg.setInnerWaitTime(((Double)m.get("innerWaitTime")).longValue());
				innerEventAvg.setFlipWaitTime(((Double)m.get("flipWaitTime")).longValue());
				innerEventAvg.setEncodingTime(((Double)m.get("encodingTime")).longValue());
				innerEventAvg.setInnerTotalTime(((Double)m.get("innerTotalTime")).longValue());
			}
		}else{
			innerEventAvg.setInnerWaitTime(0l);
			innerEventAvg.setFlipWaitTime(0l);
			innerEventAvg.setEncodingTime(0l);
			innerEventAvg.setInnerTotalTime(0l);
		}
		
		System.out.println("total: "+totalNo);
		System.out.println("complete: "+completeNo);
		System.out.println("avg: "+innerEventAvg.getInnerTotalTime());
		
		
		//c.setProjection(null);
		//c.setResultTransformer(Criteria.ROOT_ENTITY);
		if (completeNo>0){
			//Try break by format
			//cAggr.addOrder( Order.asc("groupkey"));
			formatMap = cAggr.setProjection(Projections.projectionList().add(Projections.avg("encodingTime"), "encodingTime")
								.add(Projections.rowCount(), "count")			
								.add(Projections.groupProperty("itemFormat"),"groupkey"))
								.addOrder( Order.asc("groupkey"))
								.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			
			renditionMap = cAggr.setProjection(Projections.projectionList().add(Projections.avg("encodingTime"), "encodingTime")
							.add(Projections.rowCount(), "count")		
							.add(Projections.groupProperty("itemRendition"),"groupkey"))
							.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			
			vCodecMap = cAggr.setProjection(Projections.projectionList().add(Projections.avg("encodingTime"), "encodingTime")
					.add(Projections.rowCount(), "count")
					.add(Projections.groupProperty("VCodec"),"groupkey"))
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			
			aCodecMap = cAggr.setProjection(Projections.projectionList().add(Projections.avg("encodingTime"), "encodingTime")
					.add(Projections.rowCount(), "count")
					.add(Projections.groupProperty("ACodec"),"groupkey"))
					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			
			sizeMap = cAggr.setProjection(Projections.projectionList()
						.add(Projections.avg("encodingTime"), "encodingTime")
						.add(Projections.rowCount(), "count")
						.add(Projections.alias(Projections.sqlGroupProjection("round(videosize/500)*0.5 as groupkey", "round(videosize/500)", new String[] {"groupkey"}, new Type[] {Hibernate.DOUBLE}), "groupkey"))
					).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			durationMap = cAggr.setProjection(Projections.projectionList()
					.add(Projections.avg("encodingTime"), "encodingTime")
					.add(Projections.rowCount(), "count")
					.add(Projections.alias(Projections.sqlGroupProjection("round(videoduration/120000)*120000 as groupkey", "round(videoduration/120000)", new String[] {"groupkey"}, new Type[] {Hibernate.LONG}), "groupkey"))
				).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			
			wxhMap = cAggr.setProjection(Projections.projectionList()
					.add(Projections.avg("encodingTime"), "encodingTime")
					.add(Projections.rowCount(), "count")
					.add(Projections.alias(Projections.sqlGroupProjection("concat(concat(fwidth,'x'),fheight) as groupkey", "concat(concat(fwidth,'x'),fheight)", new String[] {"groupkey"}, new Type[] {Hibernate.STRING}), "groupkey"))
				).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
			hostMap = cAggr.setProjection(Projections.projectionList()
					.add(Projections.avg("encodingTime"), "encodingTime")
					.add(Projections.rowCount(), "count")
					.add(Projections.groupProperty("mtsHost"),"groupkey")
				).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();

		}
		
		
		
	}
    
    @Begin(join=true)
    public List<DamMTSInnerEvent> getMyResultList(){
    	if (myResultList==null)
    		calculateMyResultList();
    	return myResultList;
    }

	public DamMTSInnerEvent getInnerEventMax() {
		return innerEventMax;
	}

	public void setInnerEventMax(DamMTSInnerEvent innerEventMax) {
		this.innerEventMax = innerEventMax;
	}

	public DamMTSInnerEvent getInnerEventMin() {
		return innerEventMin;
	}

	public void setInnerEventMin(DamMTSInnerEvent innerEventMin) {
		this.innerEventMin = innerEventMin;
	}

	public DamMTSInnerEvent getInnerEventAvg() {
		return innerEventAvg;
	}

	public void setInnerEventAvg(DamMTSInnerEvent innerEventAvg) {
		this.innerEventAvg = innerEventAvg;
	}

	public String getWidthxheight() {
		return widthxheight;
	}

	public void setWidthxheight(String widthxheight) {
		this.widthxheight = widthxheight;
	}

	public int getCompleteNo() {
		return completeNo;
	}

	public void setCompleteNo(int completeNo) {
		this.completeNo = completeNo;
	}

	public int getTotalNo() {
		return totalNo;
	}

	public void setTotalNo(int totalNo) {
		this.totalNo = totalNo;
	}

	public void setMyResultList(List<DamMTSInnerEvent> myResultList) {
		this.myResultList = myResultList;
	}

	public List<Map<String, Object>> getFormatMap() {
		return formatMap;
	}

	public void setFormatMap(List<Map<String, Object>> formatMap) {
		this.formatMap = formatMap;
	}

	public List<Map<String, Object>> getRenditionMap() {
		return renditionMap;
	}

	public void setRenditionMap(List<Map<String, Object>> renditionMap) {
		this.renditionMap = renditionMap;
	}

	public List<Map<String, Object>> getVCodecMap() {
		return vCodecMap;
	}

	public void setVCodecMap(List<Map<String, Object>> codecMap) {
		vCodecMap = codecMap;
	}

	public List<Map<String, Object>> getACodecMap() {
		return aCodecMap;
	}

	public void setACodecMap(List<Map<String, Object>> codecMap) {
		aCodecMap = codecMap;
	}

	public List<Map<String, Object>> getSizeMap() {
		return sizeMap;
	}

	public void setSizeMap(List<Map<String, Object>> sizeMap) {
		this.sizeMap = sizeMap;
	}
	public List<Map<String, Object>> getDurationMap() {
		return durationMap;
	}

	public void setDurationMap(List<Map<String, Object>> durationMap) {
		this.durationMap = durationMap;
	}

	public List<Map<String, Object>> getWxhMap() {
		return wxhMap;
	}

	public void setWxhMap(List<Map<String, Object>> wxhMap) {
		this.wxhMap = wxhMap;
	}

	public List<Map<String, Object>> getHostMap() {
		return hostMap;
	}

	public void setHostMap(List<Map<String, Object>> hostMap) {
		this.hostMap = hostMap;
	}
    
    
}
