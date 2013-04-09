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

import com.webmd.opsai.entity.DamMTSMainEvent;

@Name("damMTSMainEventHome")
public class DamMTSMainEventHome extends EntityHome<DamMTSMainEvent>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@RequestParameter Long damMTSMainEventId;

    @Override
    public Object getId()
    {
        if (damMTSMainEventId == null)
        {
            return super.getId();
        }
        else
        {
            return damMTSMainEventId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }
	@SuppressWarnings("unchecked")
	public DamMTSMainEvent find(String mtsHost, String srcObjId, Date mtsMainStartTime){
    	Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMTSMainEvent.class);
		//List<SimpleExpression> restrictions = new ArrayList<SimpleExpression>();
		if (null!=mtsMainStartTime){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("mtsMainStartTime", mtsMainStartTime));
		}
		if (null!=srcObjId && !srcObjId.equals("")){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("srcObjId", srcObjId));
		}
		if (null!=mtsHost && !mtsHost.equals("")){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("mtsHost", mtsHost));
		}
		List<DamMTSMainEvent> result = c.list();
		if (result!=null && result.size()!=0)
			return result.get(0);
		else
			return null;
    }
}
