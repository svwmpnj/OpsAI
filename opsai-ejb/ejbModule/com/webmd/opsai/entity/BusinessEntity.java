package com.webmd.opsai.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;
import javax.persistence.OneToMany;
import java.util.List;
import org.hibernate.validator.Length;

@Entity
public class BusinessEntity implements Serializable {
	
	public static final long serialVersionUID = 1L;
	
	//seam-gen attributes (you should probably edit these)
	private Long id;
	private Integer version;
	private String name;
	private List<Product> products;
	private List<HostType> hostTypes;
	
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

	@OneToMany(targetEntity=Product.class, mappedBy="businessEntity")
	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	@OneToMany(targetEntity=HostType.class, mappedBy="businessEntity")
	public List<HostType> getHostTypes() {
		return hostTypes;
	}

	public void setHostTypes(List<HostType> hostTypes) {
		this.hostTypes = hostTypes;
	}
}
