package com.webmd.opsai.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.validator.Length;

@Entity
public class HostType implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	//seam-gen attributes (you should probably edit these)
	private Long id;
	private Integer version;
	private String name;
	private BusinessEntity businessEntity;
	private List<Application> applications;
	private Set<Product> products;
	
    //add additional entity attributes
	
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
	
	@Length(max=20)
	public String getName() {
	     return name;
	}

	public void setName(String name) {
	     this.name = name;
	}

	@OneToMany(targetEntity=Application.class, mappedBy="hostType")
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}

	@ManyToMany(mappedBy="hostTypes")
	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(Set<Product> products) {
		this.products = products;
	}

	@ManyToOne
	public BusinessEntity getBusinessEntity() {
		return businessEntity;
	}

	public void setBusinessEntity(BusinessEntity businessEntity) {
		this.businessEntity = businessEntity;
	}   	
}
