package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.ServiceConf;

@Name("serviceConfHome")
public class ServiceConfHome extends EntityHome<ServiceConf>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@RequestParameter Long serviceConfId;

    @Override
    public Object getId()
    {
        if (serviceConfId == null)
        {
            return super.getId();
        }
        else
        {
            return serviceConfId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
