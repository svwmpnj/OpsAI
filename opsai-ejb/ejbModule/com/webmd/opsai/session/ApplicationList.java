package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.webmd.opsai.entity.Application;

import javax.faces.component.UIData;

@Name("applicationList")
public class ApplicationList extends EntityQuery<Application>
{
	public static final long serialVersionUID = 1L;
	private UIData table;
	   
	public UIData getTable() {
		return table;
	}
	 
	 
	public void setTable(UIData table) {
		this.table = table;
	}

    @Override
    public String getEjbql() 
    { 
        return "select application from Application application";
    }
}
