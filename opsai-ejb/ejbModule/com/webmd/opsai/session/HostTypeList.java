package com.webmd.opsai.session;

import javax.faces.component.UIData;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.webmd.opsai.entity.HostType;

@Name("hostTypeList")
public class HostTypeList extends EntityQuery<HostType>
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
        return "select hostType from HostType hostType";
    }
}
