package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;

@Entity
public class ScsPubEvent implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;
	private String logName;//1
	private String docbase;
	private String status;

	private String type;
	private String lcState;
	private Date startTime;//2
	private Date completeTime;//18  18-2 = total (19)
	//private Date publishBegineTime;//3
	//private Date exportCompleteTime;//4
	private long contBaseTaken;//5.1
	private long contlessBaseTaken;//5.2
	private long exportTimeTaken;//5
	private double contlessObjCount;//6-7
	private double contObjCount;//7
	
	private long localSyncTaken;//11
	private long targetTransferTaken;//13
	private long targetSyncTaken;//17
	private long bytesTransfered;
	
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

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public String getDocbase() {
		return docbase;
	}

	public void setDocbase(String docbase) {
		this.docbase = docbase;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLcState() {
		return lcState;
	}

	public void setLcState(String lcState) {
		this.lcState = lcState;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}

	public long getContBaseTaken() {
		return contBaseTaken;
	}

	public void setContBaseTaken(long contBaseTaken) {
		this.contBaseTaken = contBaseTaken;
	}

	public long getContlessBaseTaken() {
		return contlessBaseTaken;
	}

	public void setContlessBaseTaken(long contlessBaseTaken) {
		this.contlessBaseTaken = contlessBaseTaken;
	}

	public long getExportTimeTaken() {
		return exportTimeTaken;
	}

	public void setExportTimeTaken(long exportTimeTaken) {
		this.exportTimeTaken = exportTimeTaken;
	}

	public double getContlessObjCount() {
		return contlessObjCount;
	}

	public void setContlessObjCount(double contlessObjCount) {
		this.contlessObjCount = contlessObjCount;
	}

	public double getContObjCount() {
		return contObjCount;
	}

	public void setContObjCount(double contObjCount) {
		this.contObjCount = contObjCount;
	}

	public long getLocalSyncTaken() {
		return localSyncTaken;
	}

	public void setLocalSyncTaken(long localSyncTaken) {
		this.localSyncTaken = localSyncTaken;
	}

	public long getTargetTransferTaken() {
		return targetTransferTaken;
	}

	public void setTargetTransferTaken(long targetTransferTaken) {
		this.targetTransferTaken = targetTransferTaken;
	}

	public long getTargetSyncTaken() {
		return targetSyncTaken;
	}

	public void setTargetSyncTaken(long targetSyncTaken) {
		this.targetSyncTaken = targetSyncTaken;
	}

	public long getBytesTransfered() {
		return bytesTransfered;
	}

	public void setBytesTransfered(long bytesTransfered) {
		this.bytesTransfered = bytesTransfered;
	}


}
