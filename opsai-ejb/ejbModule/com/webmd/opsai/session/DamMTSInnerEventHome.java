package com.webmd.opsai.session;


import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.DamMTSInnerEvent;
import com.webmd.opsai.entity.DamMTSMainEvent;

@Name("dMSMTSInnerEventHome")
public class DamMTSInnerEventHome extends EntityHome<DamMTSInnerEvent>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@RequestParameter Long dMSMTSInnerEventId;

    @Override
    public Object getId()
    {
        if (dMSMTSInnerEventId == null)
        {
            return super.getId();
        }
        else
        {
            return dMSMTSInnerEventId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }
    @SuppressWarnings("unchecked")
	public DamMTSInnerEvent find(DamMTSMainEvent parent, String format, String rendition){
    	SQLQuery sq = ((Session)getEntityManager().getDelegate())
		.createSQLQuery("select i.* from DAMMTSINNEREVENT i " +
				"where i.parent_id = " + parent.getId() + " and i.itemformat = '" + format + "' and i.itemrendition = '" + rendition + "'")
				.addEntity(DamMTSInnerEvent.class);
	
    	/*
    	Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSInnerEvent.class);
    	DamMTSInnerEvent tmpEvent = new DamMTSInnerEvent();
    	tmpEvent.setParent(parent);
    	tmpEvent.setItemFormat(format);
    	tmpEvent.setItemRendition(rendition);
    	
    	c.add(Example.create(tmpEvent).excludeZeroes());*/
		//List<SimpleExpression> restrictions = new ArrayList<SimpleExpression>();
		/*
    	c.add(Restrictions.eq("parent", parent));
		if (null!=format){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("itemFormat", format));
		}
		if (null!=rendition){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("itemRendition", rendition));
		}*/
		List<DamMTSInnerEvent> result = sq.list();
		if (result!=null && result.size()!=0)
			return result.get(0);
		else
			return null;
    }
}
