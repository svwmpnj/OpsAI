package com.webmd.opsai.session;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import com.webmd.opsai.session.DqlStudio;
import com.webmd.opsai.util.DfcClient;
import com.webmd.opsai.util.LdapUser;

@Name("dqlstudio")
@Scope (CONVERSATION)
public class DqlStudio {
	
	@Logger private Log log;
	@In LdapUser loggedinuser;
	@In FacesMessages facesMessages;
	
	private String[] columns;
	private List<String[]> resultCollection;
	private String queryStr;
	private String docBase;
	private static String lastQuery;
	private static String lastUser;

	@SuppressWarnings("unchecked")
	@Begin(join=true)
	public void query(){
		log.debug("Start Query");
		if (!queryStr.toLowerCase().startsWith("select")){
			facesMessages.add("DQL Studio only supports dql query");
			columns = null;
			resultCollection = null;
			return;
		}
		try{
			lastQuery = queryStr;
			lastUser = loggedinuser.getName();
			DfcClient dfcc = new DfcClient(docBase);
			Map<String, Object> qResult = dfcc.query(queryStr);
			columns = (String[])qResult.get("columnHeader");
			resultCollection = (List<String[]>)qResult.get("resultCollection");
			facesMessages.add(resultCollection.size()+" rows selected");
			log.debug("columns#: " + columns.length);
			log.debug("resultCollection#: " + resultCollection.size());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public List<String[]> getResultCollection() {
		return resultCollection;
	}

	public void setResultCollection(List<String[]> resultCollection) {
		this.resultCollection = resultCollection;
	}

	public String getQueryStr() {
		return queryStr;
	}

	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}

	public String getDocBase() {
		return docBase;
	}

	public void setDocBase(String docBase) {
		this.docBase = docBase;
	}

	public static String getLastQuery() {
		return lastQuery;
	}

	public static void setLastQuery(String lastQuery) {
		DqlStudio.lastQuery = lastQuery;
	}

	public static String getLastUser() {
		return lastUser;
	}

	public static void setLastUser(String lastUser) {
		DqlStudio.lastUser = lastUser;
	}
}
