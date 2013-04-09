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

import com.webmd.opsai.entity.DamMBEvent;

@Name("damMBEventHome")
public class DamMBEventHome extends EntityHome<DamMBEvent>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@RequestParameter Long damMBEventId;

    @Override
    public Object getId()
    {
        if (damMBEventId == null)
        {
            return super.getId();
        }
        else
        {
            return damMBEventId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }
    
    @SuppressWarnings("unchecked")
	public DamMBEvent find(String srcObjId, Date mbStartTime){
    	Criteria c = ((Session)getEntityManager().getDelegate()).createCriteria(DamMBEvent.class);
		//List<SimpleExpression> restrictions = new ArrayList<SimpleExpression>();
		if (null!=mbStartTime){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("elemEventStart", mbStartTime));
		}
		if (null!=srcObjId && !srcObjId.equals("")){
			//restrictions.add(Restrictions.ge("emailDate", startDate));
			c.add(Restrictions.eq("objId", srcObjId));
		}
		
		List<DamMBEvent> result = c.list();
		if (result!=null && result.size()!=0)
			return result.get(0);
		else
			return null;
    }

}
