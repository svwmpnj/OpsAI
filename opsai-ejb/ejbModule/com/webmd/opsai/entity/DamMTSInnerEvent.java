package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import org.hibernate.validator.Length;

import com.webmd.opsai.util.DAMLogParserDB.ItemEvent;

@Entity
public class DamMTSInnerEvent implements Serializable
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
	private String itemFormat;
	private String itemRendition;
	private String tlsStatus;
	private String prfStatus;
	private String obj100Count;
	private String errorMsg;

    private Date itemEventStart;
    private Date itemEventEnd;
    private Date flipFirstStatusDate;
    private long innerWaitTime;
    private long flipWaitTime;
    private long encodingTime;
    private long innerTotalTime;
	private DamMTSMainEvent parent;

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

	public String getTlsStatus() {
		return tlsStatus;
	}

	public void setTlsStatus(String tlsStatus) {
		this.tlsStatus = tlsStatus;
	}

	public String getPrfStatus() {
		return prfStatus;
	}

	public void setPrfStatus(String prfStatus) {
		this.prfStatus = prfStatus;
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

	public Date getFlipFirstStatusDate() {
		return flipFirstStatusDate;
	}

	public void setFlipFirstStatusDate(Date flipFirstStatusDate) {
		this.flipFirstStatusDate = flipFirstStatusDate;
	}

	public long getInnerWaitTime() {
		return innerWaitTime;
	}

	public void setInnerWaitTime(long innerWaitTime) {
		this.innerWaitTime = innerWaitTime;
	}

	public long getFlipWaitTime() {
		return flipWaitTime;
	}

	public void setFlipWaitTime(long flipWaitTime) {
		this.flipWaitTime = flipWaitTime;
	}

	public long getEncodingTime() {
		return encodingTime;
	}

	public void setEncodingTime(long encodingTime) {
		this.encodingTime = encodingTime;
	}

	public long getInnerTotalTime() {
		return innerTotalTime;
	}

	public void setInnerTotalTime(long innerTotalTime) {
		this.innerTotalTime = innerTotalTime;
	}

	@ManyToOne
	public DamMTSMainEvent getParent() {
		return parent;
	}

	public void setParent(DamMTSMainEvent parent) {
		this.parent = parent;
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
	

	public String getMtsHost() {
		return mtsHost;
	}

	public void setMtsHost(String mtsHost) {
		this.mtsHost = mtsHost;
	}

	public void recordFromParser(DamMTSMainEvent pe, ItemEvent ie){
	    name = ie.getItemName();
	    if (ie.getParent()!=null){
	    	srcObjId = ie.getParent().getSrcObj().getObjId();
	    }
	    
	    int hr, min, sec;
	    String durationStr;
	    String[] hrminsec = new String[3];
	    if (ie.getParent().getSrcObj().getSize()!=null)
	    	videoSize = Double.parseDouble(ie.getParent().getSrcObj().getSize());
	    if (ie.getParent().getSrcObj().getDuration()!=null){
	    	//System.out.println(pe.getSrcObj().getDuration().substring(0, pe.getSrcObj().getDuration().lastIndexOf(";")));
	    	if (-1 != ie.getParent().getSrcObj().getDuration().lastIndexOf(";")){
	    		durationStr = ie.getParent().getSrcObj().getDuration().substring(0, ie.getParent().getSrcObj().getDuration().lastIndexOf(";"));
	    	}else{
	    		System.out.println("Strange duration: " + ie.getParent().getSrcObj().getDuration());
	    		durationStr = ie.getParent().getSrcObj().getDuration();
	    	}
	    	hrminsec = durationStr.split(":");
	    	hr = Integer.parseInt(hrminsec[0]);
	    	min = Integer.parseInt(hrminsec[1]);
	    	sec = Integer.parseInt(hrminsec[2]);
	    	System.out.println("int duration: " + hr + ":" + min + ":" + sec);
	    	videoDuration =  sec*1000 + min*60*1000 + hr*3600*1000;
	    }
	    	
	    vCodec = ie.getParent().getSrcObj().getVCodec();
	    aCodec = ie.getParent().getSrcObj().getACodec();
	    fWidth = ie.getParent().getSrcObj().getFWidth();
	    fHeight = ie.getParent().getSrcObj().getFHeight();
	    fRate = ie.getParent().getSrcObj().getFrameRate();
	    vTrackNo = ie.getParent().getSrcObj().getVTrackNo();
	    aBits = ie.getParent().getSrcObj().getABitsPerSample();
	    aChannels = ie.getParent().getSrcObj().getAchannels();
	    aSampleRate = ie.getParent().getSrcObj().getASampleRate();

		
		itemFormat = ie.getItemFormat();
		itemRendition = ie.getItemRendition();
		tlsStatus = ie.getStatus();
		prfStatus = ie.getPrfStatus();
		obj100Count = ie.getObj100Count();
		errorMsg = ie.getErrorMsg();

	    itemEventStart = ie.getItemEventStart();
	    itemEventEnd = ie.getItemEventEnd();
	    flipFirstStatusDate = ie.getFlipFirstStatusDate();	
	    if (ie.getParent().getParentEventStart()!=null && ie.getItemEventStart()!=null)
	    	innerWaitTime = ie.getItemEventStart().getTime() - ie.getParent().getParentEventStart().getTime();
	    if (ie.getItemEventStart()!=null && ie.getFlipFirstStatusDate()!=null)
	    	flipWaitTime = ie.getFlipFirstStatusDate().getTime() - ie.getItemEventStart().getTime();
	    if (ie.getFlipFirstStatusDate()!=null && ie.getItemEventEnd()!=null)
	    	encodingTime = ie.getItemEventEnd().getTime() - ie.getFlipFirstStatusDate().getTime();
	    if (ie.getItemEventEnd()!=null && ie.getItemEventStart()!=null)
	    	innerTotalTime = ie.getItemEventEnd().getTime() - ie.getItemEventStart().getTime();
	    parent = pe;
	    mtsHost = parent.getMtsHost();
	}
}
