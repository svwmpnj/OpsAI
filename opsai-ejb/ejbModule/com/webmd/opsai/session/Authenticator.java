package com.webmd.opsai.session;

import static org.jboss.seam.ScopeType.SESSION;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.security.auth.callback.SecurityAssociationHandler;

import com.webmd.opsai.util.LdapUser;
import com.webmd.opsai.util.LdapUtil;

@Name("authenticator")
public class Authenticator
{
    @Logger private Log log;

    @In Identity identity;
    @In Credentials credentials;
    
    @Out(required=false, scope = SESSION) LdapUser loggedinuser;

    public boolean authenticate()
    {
        log.info("authenticating {0}", credentials.getUsername());
        SimplePrincipal user = new SimplePrincipal(credentials.getUsername()); 
        SecurityAssociationHandler handler = new SecurityAssociationHandler();
        handler.setSecurityInfo(user, credentials.getPassword()); 
        try{
      	   LoginContext lc = new LoginContext("WebMDLDAP", handler);
      	   lc.login();
      	   Subject subject = lc.getSubject();
      	   log.info("subject #0", subject);
      	   Set<Principal> p = subject.getPrincipals();
      	   log.info("Principal size: #0", p.size());
      	   //for(Iterator<Principal> itp = p.iterator();itp.hasNext();){
      		   //Principal princ = itp.next();
      		   //log.info("princ: #0", princ.getName());
      	   //}
      	   
      	   LdapUtil util = new LdapUtil();
      	   //util.init(null, null, null);
      	   loggedinuser = util.getUserInfo(credentials.getUsername());
      	   int commaPos=0;
      	   String[] memberOf = loggedinuser.getMemberOf();
      	   String roleToAdd = null;
      	   for (int i=0; i<memberOf.length; i++){
				commaPos = memberOf[i].indexOf(",");
				roleToAdd = memberOf[i].substring(3, commaPos);
				log.info("roleToAdd: " + roleToAdd);
				identity.addRole(roleToAdd);
      	   }
      	   //util.printUser(loggedinuser);
          }catch(Exception e){
          	log.error("authenticating #0", e.getMessage());
          	e.printStackTrace();
          	return false;
          }

        return true;
    }

}
