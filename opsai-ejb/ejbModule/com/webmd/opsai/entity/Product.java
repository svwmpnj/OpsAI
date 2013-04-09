package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.*;
import javax.persistence.Version;
import org.hibernate.validator.Length;

@Entity
public class Product implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	//seam-gen attributes (you should probably edit these)
	private Long id;
	private Integer version;
	private String name;
	
    //add additional entity attributes
	private BusinessEntity businessEntity;
	private List<Application> applications;
	
	private Set<HostType> hostTypes;
	
	//seam-gen attribute getters/setters with annotations (you probably should edit)
		
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
	
	@Length(max=50)
	public String getName() {
	     return name;
	}

	public void setName(String name) {
	     this.name = name;
	}

	@OneToMany(targetEntity=Application.class, mappedBy="product")
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	@ManyToOne
	public BusinessEntity getBusinessEntity() {
		return businessEntity;
	}

	public void setBusinessEntity(BusinessEntity businessEntity) {
		this.businessEntity = businessEntity;
	}

	@ManyToMany
	@JoinTable(name="APPLICATION",
	        joinColumns=
	            @JoinColumn(name="PRODUCT_ID", referencedColumnName="ID"),
	        inverseJoinColumns=
	            @JoinColumn(name="HOSTTYPE_ID", referencedColumnName="ID")
	        )
	public Set<HostType> getHostTypes() {
		return hostTypes;
	}

	public void setHostTypes(Set<HostType> hostTypes) {
		this.hostTypes = hostTypes;
	}
	
	

}
