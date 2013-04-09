package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import org.hibernate.validator.Length;

import com.webmd.opsai.util.DAMLogParserDB.ParentEvent;

@Entity
public class DamMTSMainEvent implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;
    private String name;
    private String mtsHost;
    private String srcObjId;
    private double videoSize;
    private long videoDuration;
    private String vCodec;
    private String aCodec;
    private String fWidth;
    private String fHeight;
    private String fRate;
    private String vTrackNo;
    private String aBits;
    private String aChannels;
    private String aSampleRate;
    private String type;
    private String status;
    private Date mtsMainStartTime;
    private Date mtsMainEndTime;
    private long mtsTotalTime;
    private int children;
    
    private List<DamMTSInnerEvent> innerEvents;
    
 
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
	public String getSrcObjId() {
		return srcObjId;
	}
	public void setSrcObjId(String srcObjId) {
		this.srcObjId = srcObjId;
	}
	public double getVideoSize() {
		return videoSize;
	}
	public void setVideoSize(double videoSize) {
		this.videoSize = videoSize;
	}
	public long getVideoDuration() {
		return videoDuration;
	}
	public void setVideoDuration(long videoDuration) {
		this.videoDuration = videoDuration;
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
	public String getFRate() {
		return fRate;
	}
	public void setFRate(String rate) {
		fRate = rate;
	}
	public String getVTrackNo() {
		return vTrackNo;
	}
	public void setVTrackNo(String trackNo) {
		vTrackNo = trackNo;
	}
	public String getABits() {
		return aBits;
	}
	public void setABits(String bits) {
		aBits = bits;
	}
	public String getAChannels() {
		return aChannels;
	}
	public void setAChannels(String channels) {
		aChannels = channels;
	}
	public String getASampleRate() {
		return aSampleRate;
	}
	public void setASampleRate(String sampleRate) {
		aSampleRate = sampleRate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getMtsMainStartTime() {
		return mtsMainStartTime;
	}
	public void setMtsMainStartTime(Date mtsMainStartTime) {
		this.mtsMainStartTime = mtsMainStartTime;
	}
	public Date getMtsMainEndTime() {
		return mtsMainEndTime;
	}
	public void setMtsMainEndTime(Date mtsMainEndTime) {
		this.mtsMainEndTime = mtsMainEndTime;
	}
	public long getMtsTotalTime() {
		return mtsTotalTime;
	}
	public void setMtsTotalTime(long mtsTotalTime) {
		this.mtsTotalTime = mtsTotalTime;
	}
	public String getMtsHost() {
		return mtsHost;
	}
	public void setMtsHost(String mtsHost) {
		this.mtsHost = mtsHost;
	}
	@OneToMany(targetEntity=DamMTSInnerEvent.class, mappedBy="parent")
	public List<DamMTSInnerEvent> getInnerEvents() {
		return innerEvents;
	}
	public void setInnerEvents(List<DamMTSInnerEvent> innerEvents) {
		this.innerEvents = innerEvents;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}	
	public int getChildren() {
		return children;
	}
	public void setChildren(int children) {
		this.children = children;
	}
	public void recordFromParser(String parserMtsHost, ParentEvent pe){
		name = pe.getParentFileName();
	    mtsHost = parserMtsHost;
	    srcObjId = pe.getSrcObj().getObjId();
	    int hr, min, sec;
	    String durationStr;
	    String[] hrminsec = new String[3];
	    if (pe.getSrcObj().getSize()!=null)
	    	videoSize = Double.parseDouble(pe.getSrcObj().getSize());
	    if (pe.getSrcObj().getDuration()!=null){
	    	//System.out.println(pe.getSrcObj().getDuration().substring(0, pe.getSrcObj().getDuration().lastIndexOf(";")));
	    	if (-1 != pe.getSrcObj().getDuration().lastIndexOf(";")){
	    		durationStr = pe.getSrcObj().getDuration().substring(0, pe.getSrcObj().getDuration().lastIndexOf(";"));
	    	}else{
	    		System.out.println("Strange duration: " + pe.getSrcObj().getDuration());
	    		durationStr = pe.getSrcObj().getDuration();
	    	}
	    	hrminsec = durationStr.split(":");
	    	hr = Integer.parseInt(hrminsec[0]);
	    	min = Integer.parseInt(hrminsec[1]);
	    	sec = Integer.parseInt(hrminsec[2]);
	    	System.out.println("int duration: " + hr + ":" + min + ":" + sec);
	    	videoDuration =  sec*1000 + min*60*1000 + hr*3600*1000;
	    }
	    	
	    vCodec = pe.getSrcObj().getVCodec();
	    aCodec = pe.getSrcObj().getACodec();
	    fWidth = pe.getSrcObj().getFWidth();
	    fHeight = pe.getSrcObj().getFHeight();
	    fRate = pe.getSrcObj().getFrameRate();
	    vTrackNo = pe.getSrcObj().getVTrackNo();
	    aBits = pe.getSrcObj().getABitsPerSample();
	    aChannels = pe.getSrcObj().getAchannels();
	    aSampleRate = pe.getSrcObj().getASampleRate();
	    type = pe.getType();
	    mtsMainStartTime = pe.getParentEventStart();
	    mtsMainEndTime  = pe.getParentEventEnd();
	    status = mtsMainEndTime==null?"Incomplete":"Complete";
	    if (pe.getParentEventEnd()!=null && pe.getParentEventStart()!=null)
	    	mtsTotalTime = pe.getParentEventEnd().getTime() - pe.getParentEventStart().getTime();
	    children = pe.getItemList().size();
	}
    
}
