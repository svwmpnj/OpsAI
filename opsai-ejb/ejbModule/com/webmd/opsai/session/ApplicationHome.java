package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.Application;

@Name("applicationHome")
public class ApplicationHome extends EntityHome<Application>
{

	public static final long serialVersionUID = 1L;
	
    @RequestParameter 
    Long applicationId;
    
    @Override
    public Object getId() 
    { 
        if (applicationId==null)
        {
            return super.getId();
        }
        else
        {
            return applicationId;
        }
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }
 	
}
