package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import com.webmd.opsai.entity.ServiceConf;

@Name("serviceConfList")
public class ServiceConfList extends EntityQuery<ServiceConf>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceConfList()
    {
        setEjbql("select serviceConf from ServiceConf serviceConf");
    }
}
