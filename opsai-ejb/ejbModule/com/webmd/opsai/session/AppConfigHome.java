package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.AppConfig;

@Name("appConfigHome")
public class AppConfigHome extends EntityHome<AppConfig>
{
	public static final long serialVersionUID = 1L;
	
    @RequestParameter Long appConfigId;

    @Override
    public Object getId()
    {
        if (appConfigId == null)
        {
            return super.getId();
        }
        else
        {
            return appConfigId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
