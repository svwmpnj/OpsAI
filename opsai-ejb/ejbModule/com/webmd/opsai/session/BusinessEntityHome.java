package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.BusinessEntity;

@Name("businessEntityHome")
public class BusinessEntityHome extends EntityHome<BusinessEntity>
{
	public static final long serialVersionUID = 1L;
	
    @RequestParameter 
    Long businessEntityId;
    
    @Override
    public Object getId() 
    { 
        if (businessEntityId==null)
        {
            return super.getId();
        }
        else
        {
            return businessEntityId;
        }
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }
 	
}
