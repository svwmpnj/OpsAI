package com.webmd.opsai.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import org.hibernate.validator.Length;

@Entity
public class Application implements Serializable {
	
	public static final long serialVersionUID = 1L;

	//seam-gen attributes (you should probably edit these)
	private Long id;
	private Integer version;
	private String name;
	private Product product;
	private HostType hostType;
	
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
	
	@Length(max=100)
	public String getName() {
	     return name;
	}

	public void setName(String name) {
	     this.name = name;
	}   
	
	
	@ManyToOne
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	@ManyToOne
	public HostType getHostType() {
		return hostType;
	}

	public void setHostType(HostType hostType) {
		this.hostType = hostType;
	}
}
