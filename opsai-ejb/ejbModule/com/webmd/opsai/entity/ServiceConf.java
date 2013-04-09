package com.webmd.opsai.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;
import org.hibernate.validator.Length;

@Entity
public class ServiceConf implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;
	private String serviceName;
	private String serviceCD;
	private String serviceURL;
	private String adminID;
	private String adminPW;
	private String instruction;

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

    public String getServiceCD() {
		return serviceCD;
	}

	public void setServiceCD(String serviceCD) {
		this.serviceCD = serviceCD;
	}

	public String getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}

	public String getAdminID() {
		return adminID;
	}

	public void setAdminID(String adminID) {
		this.adminID = adminID;
	}

	public String getAdminPW() {
		return adminPW;
	}

	public void setAdminPW(String adminPW) {
		this.adminPW = adminPW;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	@Length(max = 20)
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
