package com.webmd.opsai.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfLoginInfo;

public class DfcClient {
	
	private String strRepositoryName;
	
	public DfcClient(String strRepositoryName) {
		super();
		this.strRepositoryName = strRepositoryName;
	}

	public static void main(String[] args){
		DfcClient dfcc = new DfcClient("webmddoc01");
		dfcc.query();
	}
	
	public Map<String, Object> query(){
		
		return query("select * from dm_user");
	}

	public Map<String, Object> query(String dqlStatement){
		Map<String, Object> result = new HashMap<String, Object>();
		IDfCollection coll = null;
		try{
			//String strRepositoryName = "webmddoc01";
			System.out.println("Classpath:"+System.getProperty("java.class.path"));
			System.out.println("java.library.path:"+System.getProperty("java.library.path"));
			IDfClientX cx = new DfClientX(); //Step 1
			IDfClient client = cx.getLocalClient(); //Step 2
			IDfSessionManager sMgr = client.newSessionManager(); //Step 3
			IDfLoginInfo loginInfo = cx.getLoginInfo();
			if(strRepositoryName.equals("webmddam01")){
				loginInfo.setUser( "dmmig" );
				loginInfo.setPassword( "dmmig0920!" );
			}else{
				loginInfo.setUser( "opsai" );
				loginInfo.setPassword( "cms0nly!!" );
				//loginInfo.setPassword( "cs27qa01p" );
			}
			loginInfo.setDomain( "" );
			sMgr.setIdentity( strRepositoryName, loginInfo );
			IDfSession session = sMgr.getSession( strRepositoryName ); //Step 4
			IDfQuery qry = new DfQuery();
			qry.setDQL(dqlStatement);
			coll = qry.execute(session, DfQuery.DF_QUERY);
			IDfTypedObject typedObject = coll.getTypedObject();
            int attrCount = typedObject.getAttrCount();
            String[] columns = new String[attrCount];
            for (int i = 0; i < attrCount; i++) {
                  IDfAttr attr = typedObject.getAttr(i);
                  columns[i] = attr.getName();
                  System.out.print(attr.getDataType()+"-");
                  if (i == 0)
                	  System.out.print(columns[i]);
                  else
                	  System.out.print("," + columns[i]);
            }
            result.put("columnHeader", columns);
            System.out.print("\n");
            List<String[]> resultList = new LinkedList<String[]>();
            String[] tmpRowList;
            while (coll.next()) {
            	tmpRowList =  new String[attrCount];
                for (int i = 0; i < columns.length; i++) {
                	//tmpRowList[i]=coll.getValueAt(i).asString();
                	tmpRowList[i]=coll.getAllRepeatingStrings(columns[i], ",");
                	/*
                	if (i == 0)
                		System.out.print(coll.getValueAt(i).asString());
                	else
                		System.out.print("," + coll.getString(columns[i]));
                		*/
                }
                resultList.add(tmpRowList);
                //System.out.print("\n");
            }
            result.put("resultCollection", resultList);

            coll.close();
            sMgr.release( session );
            
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public Map<String, Object>  query(String dqlStatement, String delimiter){
		Map<String, Object> result = new HashMap<String, Object>();
		IDfCollection coll = null;
		try{
			//String strRepositoryName = "webmddoc01";
			//System.out.println("Classpath:"+System.getProperty("java.class.path"));
			//System.out.println("java.library.path:"+System.getProperty("java.library.path"));
			IDfClientX cx = new DfClientX(); //Step 1
			IDfClient client = cx.getLocalClient(); //Step 2
			IDfSessionManager sMgr = client.newSessionManager(); //Step 3
			IDfLoginInfo loginInfo = cx.getLoginInfo();
			if(strRepositoryName.equals("webmddam01")){
				loginInfo.setUser( "dmmig" );
				loginInfo.setPassword( "dmmig0920!" );
			}else{
				loginInfo.setUser( "opsai" );
				loginInfo.setPassword( "cms0nly!!" );
				//loginInfo.setPassword( "cs27qa01p" );
			}
			loginInfo.setDomain( "" );
			sMgr.setIdentity( strRepositoryName, loginInfo );
			IDfSession session = sMgr.getSession( strRepositoryName ); //Step 4
			IDfQuery qry = new DfQuery();
			qry.setDQL(dqlStatement);
			coll = qry.execute(session, DfQuery.DF_QUERY);
			IDfTypedObject typedObject = coll.getTypedObject();
            int attrCount = typedObject.getAttrCount();
            String[] columns = new String[attrCount];
            for (int i = 0; i < attrCount; i++) {
                  IDfAttr attr = typedObject.getAttr(i);
                  columns[i] = attr.getName();
                  //System.out.print(attr.getDataType()+"-");
                  //if (i == 0)
                	  //System.out.print(columns[i]);
                 // else
                	  //System.out.print("," + columns[i]);
            }
            result.put("columnHeader", columns);
            //System.out.print("\n");
            List<String[]> resultList = new LinkedList<String[]>();
            String[] tmpRowList;
            while (coll.next()) {
            	tmpRowList =  new String[attrCount];
                for (int i = 0; i < columns.length; i++) {
                	//tmpRowList[i]=coll.getValueAt(i).asString();
                	//if(typedObject.getAttr(i).getDataType()==4){
                		//System.out.println(coll.getTime(columns[i]).getDate());
                	//}
                	tmpRowList[i]=coll.getAllRepeatingStrings(columns[i], delimiter);
                	
                	//if (i == 0)
                		//System.out.print(coll.getValueAt(i).asString());
                	//else
                		//System.out.print("," + coll.getString(columns[i]));
                		
                }
                resultList.add(tmpRowList);
                //System.out.print("\n");
            }
            result.put("resultCollection", resultList);

            coll.close();
            sMgr.release( session );
            
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public String getStrRepositoryName() {
		return strRepositoryName;
	}

	public void setStrRepositoryName(String strRepositoryName) {
		this.strRepositoryName = strRepositoryName;
	}
}
