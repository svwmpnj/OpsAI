package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.webmd.opsai.entity.BusinessEntity;

@Name("businessEntityList")
public class BusinessEntityList extends EntityQuery<BusinessEntity>
{
	public static final long serialVersionUID = 1L;
	
    @Override
    public String getEjbql() 
    { 
        return "select businessEntity from BusinessEntity businessEntity";
    }
}
