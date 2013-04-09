package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.HostType;

@Name("hostTypeHome")
public class HostTypeHome extends EntityHome<HostType>
{
	public static final long serialVersionUID = 1L;
	
    @RequestParameter 
    Long hostTypeId;
    
    @Override
    public Object getId() 
    { 
        if (hostTypeId==null)
        {
            return super.getId();
        }
        else
        {
            return hostTypeId;
        }
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }
 	
}
