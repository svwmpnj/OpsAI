package com.webmd.opsai.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Calendar;

@Entity
public class AppConfig implements Serializable
{
	public static final long serialVersionUID = 1L;
	
    // seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;

    // add additional entity attributes
    private String key;
    private String value;
    private Application application;
    private String environment;
    private String location;
    private String path;
    private String lastUpdateName;
    private Calendar lastUpdateTS;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@ManyToOne
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLastUpdateName() {
		return lastUpdateName;
	}

	public void setLastUpdateName(String lastUpdateName) {
		this.lastUpdateName = lastUpdateName;
	}

	public Calendar getLastUpdateTS() {
		return lastUpdateTS;
	}

	public void setLastUpdateTS(Calendar lastUpdateTS) {
		this.lastUpdateTS = lastUpdateTS;
	}

}
