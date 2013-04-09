package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;
import org.hibernate.validator.Length;

import com.webmd.opsai.util.DAMLogParserDB.MBElemEvent;

@Entity
public class DamMBEvent implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;
    private String name;
    
    private Date putInQTime, elemEventStart, elemEventEnd, elemFtpStart, elemFtpEnd;
	private String objId, objState, objType, mbSite;
	private long queueWaiting, cmsTime1, ftpTime, cmsTime2, totalTime;

    // add additional entity attributes

    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
	private void setVersion(Integer version) {
        this.version = version;
    }

    @Length(max = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
	public Date getPutInQTime() {
		return putInQTime;
	}
	public void setPutInQTime(Date putInQTime) {
		this.putInQTime = putInQTime;
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
	public String getObjId() {
		return objId;
	}
	public void setObjId(String objId) {
		this.objId = objId;
	}
	public String getObjState() {
		return objState;
	}
	public void setObjState(String objState) {
		this.objState = objState;
	}
	public String getObjType() {
		return objType;
	}
	public void setObjType(String objType) {
		this.objType = objType;
	}
	public String getMbSite() {
		return mbSite;
	}
	public void setMbSite(String mbSite) {
		this.mbSite = mbSite;
	}
	public long getQueueWaiting() {
		return queueWaiting;
	}
	public void setQueueWaiting(long queueWaiting) {
		this.queueWaiting = queueWaiting;
	}
	public long getCmsTime1() {
		return cmsTime1;
	}
	public void setCmsTime1(long cmsTime1) {
		this.cmsTime1 = cmsTime1;
	}
	public long getFtpTime() {
		return ftpTime;
	}
	public void setFtpTime(long ftpTime) {
		this.ftpTime = ftpTime;
	}
	public long getCmsTime2() {
		return cmsTime2;
	}
	public void setCmsTime2(long cmsTime2) {
		this.cmsTime2 = cmsTime2;
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	public void recordFromParser(String site, MBElemEvent mbe){
		name = mbe.getObjName();
		objType = mbe.getObjType();
		if (mbe.getSrcObj()!=null){
			objId = mbe.getSrcObj().getObjId();
			objState = mbe.getSrcObj().getState();
			putInQTime = mbe.getSrcObj().getPutInQTime();
		}
		this.elemEventStart = mbe.getElemEventStart();
		this.elemEventEnd = mbe.getElemEventEnd();
		if ("wbmd_cons_video".equals(objType)){
			this.elemFtpStart = mbe.getElemFtpStart();
			this.elemFtpEnd = mbe.getElemFtpEnd();
		}
		
		if(putInQTime!=null && elemEventStart!=null){
			queueWaiting = elemEventStart.getTime() - putInQTime.getTime();
			if (queueWaiting > 43200000L)
				queueWaiting-=43200000L;
		}
		if(elemFtpStart!=null && elemEventStart!=null){
			cmsTime1 = elemFtpStart.getTime() - elemEventStart.getTime();
		}
		if(elemFtpStart!=null && elemFtpEnd!=null){
			ftpTime = elemFtpEnd.getTime() - elemFtpStart.getTime();
		}
		if(elemFtpEnd!=null && elemEventEnd!=null){
			cmsTime2 = elemEventEnd.getTime() - elemFtpEnd.getTime();
		}
		if(elemEventStart!=null && elemEventEnd!=null){
			totalTime = elemEventEnd.getTime() - elemEventStart.getTime();
		}
		mbSite = site;
		
	}
}
