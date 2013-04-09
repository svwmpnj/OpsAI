package com.webmd.opsai.util;

import static org.jboss.seam.ScopeType.SESSION; 
import static org.jboss.seam.annotations.Install.APPLICATION;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.drools.FactHandle;
import org.drools.StatefulSession;
import org.drools.base.ClassObjectFilter;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope; 
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity; 
import org.jboss.seam.security.Role; 
import org.jboss.seam.security.permission.PermissionCheck;
import org.jboss.seam.security.permission.RuleBasedPermissionResolver;

@Name("org.jboss.seam.security.ruleBasedPermissionResolver")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class CustomRuleBasedPermissionResolver extends RuleBasedPermissionResolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private static final LogProvider log = Logging.getLogProvider(CustomRuleBasedPermissionResolver.class);
	
	@SuppressWarnings("unchecked")
	public boolean hasPermission(Object target, String action, Object... args) {
		//log.info("CustomRuleBasedPermissionResolver called");
		StatefulSession securityContext = getSecurityContext();
		if (securityContext == null)
			return false;

		List<FactHandle> handles = new ArrayList<FactHandle>();

		PermissionCheck check;

		synchronized (securityContext) {
			if (!(target instanceof String) && !(target instanceof Class)) {
				handles.add(securityContext.insert(target));
			} else if (target instanceof Class) {
				String componentName = Seam.getComponentName((Class) target);
				target = componentName != null ? componentName
						: ((Class) target).getName();
			}

			check = new PermissionCheck(target, action);
			

			try {
				synchronizeContext();
				
				for(Object nextObject : args) {
					handles.add(securityContext.insert(nextObject));
				}

				handles.add(securityContext.insert(check));

				securityContext.fireAllRules();
			} finally {
				for (FactHandle handle : handles) {
					securityContext.retract(handle);
				}
			}
		}

		return check.isGranted();
	}

	/**
	    *  Synchronises the state of the security context with that of the subject
	    */
	   @SuppressWarnings("unchecked")
	public void synchronizeContext()
	   {
	      Identity identity = Identity.instance();
	      
	      if (getSecurityContext() != null)
	      {
	         getSecurityContext().insert(identity.getPrincipal());
	         
	         for ( Group sg : identity.getSubject().getPrincipals(Group.class) )      
	         {
	            if ( Identity.ROLES_GROUP.equals( sg.getName() ) )
	            {
	               Enumeration e = sg.members();
	               while (e.hasMoreElements())
	               {
	                  Principal role = (Principal) e.nextElement();
	   
	                  boolean found = false;
	                  Iterator<Role> iter = (Iterator<Role>) getSecurityContext().iterateObjects(new ClassObjectFilter(Role.class)); 
	                  while (iter.hasNext()) 
	                  {
	                     Role r = iter.next();
	                     if (r.getName().equals(role.getName()))
	                     {
	                        found = true;
	                        break;
	                     }
	                  }
	                  
	                  if (!found)
	                  {
	                     getSecurityContext().insert(new Role(role.getName()));
	                  }
	                  
	               }
	            }
	         }    
	         
	         Iterator<Role> iter = (Iterator<Role>) getSecurityContext().iterateObjects(new ClassObjectFilter(Role.class)); 
	         while (iter.hasNext()) 
	         {
	            Role r = iter.next();
	            if (!identity.hasRole(r.getName()))
	            {
	               FactHandle fh = getSecurityContext().getFactHandle(r);
	               getSecurityContext().retract(fh);
	            }
	         }
	      }
	   }

}
