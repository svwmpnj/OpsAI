package com.webmd.opsai.session;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.ScsPubEvent;

@Name("scsPubEventHome")
public class ScsPubEventHome extends EntityHome<ScsPubEvent>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@RequestParameter Long scsPubEventId;

    @Override
    public Object getId()
    {
        if (scsPubEventId == null)
        {
            return super.getId();
        }
        else
        {
            return scsPubEventId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }
    
	@SuppressWarnings("unchecked")
	public ScsPubEvent find(String logName, Date startDate){
    	Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(ScsPubEvent.class);
		//List<SimpleExpression> restrictions = new ArrayList<SimpleExpression>();
		if (null!=startDate){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("startTime", startDate));
		}
		if (null!=logName && !logName.equals("")){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("logName", logName));
		}
		List<ScsPubEvent> result = c.list();
		if (result!=null && result.size()!=0)
			return result.get(0);
		else
			return null;
    }
}
