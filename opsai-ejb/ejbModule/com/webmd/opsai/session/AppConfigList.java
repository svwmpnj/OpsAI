package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import com.webmd.opsai.entity.AppConfig;

@Name("appConfigList")
public class AppConfigList extends EntityQuery<AppConfig>
{
	public static final long serialVersionUID = 1L;
	
    public AppConfigList()
    {
        setEjbql("select appConfig from AppConfig appConfig");
    }
}
