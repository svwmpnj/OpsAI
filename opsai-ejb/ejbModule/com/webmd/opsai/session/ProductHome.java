package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import com.webmd.opsai.entity.Product;

@Name("productHome")
public class ProductHome extends EntityHome<Product>
{
	public static final long serialVersionUID = 1L;
	
    @RequestParameter 
    Long productId;
    
    @Override
    public Object getId() 
    { 
        if (productId==null)
        {
            return super.getId();
        }
        else
        {
            return productId;
        }
    }
    
    @Override @Begin
    public void create() {
        super.create();
    }
 	
}
