package com.webmd.opsai.util;

import javax.ejb.*;
import org.jboss.seam.annotations.*;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
import org.hibernate.Session;

import com.webmd.opsai.entity.ServiceConf;
import com.webmd.opsai.session.ServiceConfList;;


@Name("ldaputil")
@Stateless
public class LdapUtil {
	//@Logger private Log log;
	
	//@PersistenceContext
	//private EntityManager em;
	
	private final String LDAP_SERVICE_CD = "LdapService";
	private Hashtable<String, String> env;

	public LdapUtil(){
		try{
			ServiceConfList sc = new ServiceConfList();
			ServiceConf con = (ServiceConf)((Session)sc.getEntityManager().getDelegate()).createQuery(
					"from ServiceConf as sc where sc.serviceCD = ?").setString(0, LDAP_SERVICE_CD).uniqueResult();
			//log.debug("Ldap URL: #0");
			env = new Hashtable<String, String>();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	    	env.put(Context.PROVIDER_URL, con.getServiceURL());
	    	//env.put(Context.PROVIDER_URL, "ldap://iad1dc01.webmdhealth.net:389");
	    	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	    	env.put(Context.SECURITY_PRINCIPAL, con.getAdminID());
	    	env.put(Context.SECURITY_CREDENTIALS, con.getAdminPW());
	    	//env.put(Context.SECURITY_PRINCIPAL, "CN=prodopscms,OU=Service Accounts,DC=webmdhealth,DC=net");
	    	//env.put(Context.SECURITY_CREDENTIALS, "9Welcome");
	    	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public LdapUtil(String providerUrl, String adminDN, String adminPW) {		
		env = new Hashtable<String, String>();
    	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	env.put(Context.PROVIDER_URL, providerUrl);
    	env.put(Context.SECURITY_AUTHENTICATION, "simple");
    	env.put(Context.SECURITY_PRINCIPAL, adminDN);
    	env.put(Context.SECURITY_CREDENTIALS, adminPW); 
	}
	
	public LdapUser getUserInfo(String userId){
		LdapUser user = new LdapUser();
		//String rootDn = "OU=Users,OU=NYCPS,DC=webmdhealth,DC=net";
		String rootDn = "DC=webmdhealth,DC=net";
		String[] attrIDs = {"mail", "name", "telephoneNumber", "givenName", "sn", "memberof"};
		SearchControls ctls = new SearchControls();
		ctls.setReturningAttributes(attrIDs);
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		try{
			LdapContext ctx = new InitialLdapContext(env, null);
			try{
				NamingEnumeration<SearchResult> results = ctx.search(rootDn, "sAMAccountName="+userId, ctls);
				ctx.close();
				SearchResult result = null;
				//log.debug("Start to find...");
				if (results.hasMore()) {	
					//log.debug("find one...");
					result = results.next();
					Attributes atts = result.getAttributes();
					user.setEmailAddr(atts.get("mail")==null?null:(String)atts.get("mail").get(0));
					user.setFirstName(atts.get("givenName")==null?null:(String)atts.get("givenName").get(0));
					user.setLastName(atts.get("sn")==null?null:(String)atts.get("sn").get(0));
					user.setName(atts.get("name")==null?null:(String)atts.get("name").get(0));
					user.setPhoneNumber(atts.get("telephoneNumber")==null?null:(String)atts.get("telephoneNumber").get(0));
					//printUser(user);
					String[] memberOf = new String[atts.get("memberof").size()];
					//System.out.println(atts.get("memberof").size());
					for (int i=0; i<atts.get("memberof").size(); i++){
						//System.out.println((String)atts.get("memberof").get(i));
						memberOf[i]=(String)atts.get("memberof").get(i);
					}
					user.setMemberOf(memberOf);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if (ctx!=null) ctx.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return user;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LdapUtil util = new LdapUtil();
		LdapUser testUser = util.getUserInfo("rhsu");
		util.printUser(testUser);
		
	
	}
	
	public void printUser(LdapUser user){
		System.out.println("Name: "+user.getName());
		System.out.println("First Name: "+user.getFirstName());
		System.out.println("Last Name: "+user.getLastName());
		System.out.println("Mail: "+user.getEmailAddr());
		System.out.println("Phone: "+user.getPhoneNumber());		
	}
}
