package com.webmd.opsai.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.webmd.opsai.entity.Product;

@Name("productList")
public class ProductList extends EntityQuery<Product>
{
	public static final long serialVersionUID = 1L;
	
    @Override
    public String getEjbql() 
    { 
        return "select product from Product product";
    }
}
