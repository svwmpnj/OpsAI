package com.webmd.opsai.util;

import org.hibernate.Session;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opsware.pas.content.utils.rsflowinvoke.RSFlowInvoke;
import com.webmd.opsai.entity.ServiceConf;
import com.webmd.opsai.session.ServiceConfList;
public class OpsClient {
	
	private final String PAS_SERVICE_CD = "OpswarePasService";
	
	private String url, user, pw;
	
	public OpsClient(){
    	ServiceConfList sc = new ServiceConfList();
    	ServiceConf con = (ServiceConf)((Session)sc.getEntityManager().getDelegate()).createQuery(
				"from ServiceConf as sc where sc.serviceCD = ?").setString(0, PAS_SERVICE_CD).uniqueResult();
		url = con.getServiceURL();
		user = con.getAdminID();
		pw = con.getAdminPW();
	}
	
	public OpsClient(String javaEnv){
		url = "https://pasc01x-ops-08.portal.webmd.com:8443/PAS/services/http/execute/Library/OpsAI/";
		user = "opswareoo";
		pw = "opsaioo";
	}
	

	public OpsClientResponse  attachSoftwarePolicy(String targetProduct, String targetEnv, String targetApp, String targetVersion){
		System.out.println("Call attachSoftwarePolicy()");
		String flowName = "SoftwarePolicyDeployment/SoftwarePolicyDeploy";
		String paraString = "?targetProduct="+targetProduct+"&targetEnv="+targetEnv+"&targetApp="+targetApp+"&targetVersion="+targetVersion;
		String result =triggerPASflow(flowName, paraString);
		OpsClientResponse ocr = new OpsClientResponse(result);
		return ocr;
	}

	public String triggerPASflow(String flowName, String paraString){
		String result = null;
		try{
			//String url = con.getServiceURL();
			//String user = con.getAdminID();
			//String pw = con.getAdminPW();

			//System.out.println("Start");
			//String url = "https://pasc01x-ops-08.portal.webmd.com:8443/PAS/services/http/execute/Library/OpsAI/IISCheck?host=ats01t-con-03.itweb.webmd.net";
			
			/*
			String[] args = new String[5];
			args[0] = url;
			args[1] = "-u";
			args[2] = user;
			args[3] = "-p";
			args[4] = pw;
			RSFlowInvoke.main(args);*/
			RSFlowInvoke rsf = new RSFlowInvoke();
			rsf.setUrl(url+flowName+paraString);
			//rsf.setUrl(url);
			System.out.println("Call triggerPASflow() "+url+flowName+paraString);
			rsf.setUsername(user);
			rsf.setPassword(pw);
			result = rsf.invoke();
			System.out.println("Call PAS: " + result);
			System.out.println("done");
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
		
	public static void main(String[] args) {
		OpsClient oc = new OpsClient("java");
		//oc.attachSoftwarePolicy("dmas01t-con-08.portal.webmd.com", "prodops cms essential kit_es4u6_x86");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <executeResponse><executeReturn><run-id>4512</run-id><run-report-url>https://pasc01x-ops-08.portal.webmd.com:8443/PAS/app?service=RCLinkService/ReportLinkDispatch&amp;sp=SINDIVIDUAL_REPAIR_LEVEL&amp;sp=Se1172e42-810a-457c-832a-00900b2db167&amp;sp=l0&amp;sp=l4513</run-report-url><run-start-time>1/15/09 5:32 PM</run-start-time><run-end-time>1/15/09 5:33 PM</run-end-time><run-history-id>4513</run-history-id><flow-response>success</flow-response><flow-result>{}</flow-result><flow-return-code>Resolved</flow-return-code></executeReturn></executeResponse>";
		//oc.parsePASResult(xml);
		OpsClientResponse ocr = oc.new OpsClientResponse(xml);
		System.out.println(ocr.reportUrl);
	}
	
	public class OpsClientResponse{
		private String pasId,reportUrl, startTime, endTime, response, flowResult, affectedServers, builtRPM;
		
		public OpsClientResponse(String xmlResult){
			parsePASResult(xmlResult);	
		}
		@SuppressWarnings("deprecation")
		public void parsePASResult(String xmlResult){
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream is = new StringBufferInputStream(xmlResult);
				Document doc = db.parse(is);
				doc.getDocumentElement().normalize();
				System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
				NodeList nodeLst = doc.getElementsByTagName("executeReturn");
				System.out.println("executeResponse Details: ");
		
				Node fstNode = nodeLst.item(0);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					pasId = getNodeValue("run-id", fstElmnt);
					reportUrl = getNodeValue("run-report-url", fstElmnt);
					startTime = getNodeValue("run-start-time", fstElmnt);
					endTime = getNodeValue("run-end-time", fstElmnt);
					response = getNodeValue("flow-response", fstElmnt);
					flowResult = getNodeValue("flow-result", fstElmnt);
					
					int bLocation = flowResult.indexOf("builtRPM=");
					if (bLocation > -1){
						int bStart = flowResult.indexOf("=", bLocation)+1;
						int bEnd = flowResult.indexOf(";", bStart);
						System.out.println("builtRPM : " + flowResult.substring(bStart, bEnd));
						builtRPM = flowResult.substring(bStart, bEnd);
					}
					
					int aLocation = flowResult.indexOf("sasServerNameList=");
					int aStart = flowResult.indexOf("=", aLocation)+1;
					int aEnd = flowResult.indexOf(";", aStart)-1;
					System.out.println("affectedServers : " + flowResult.substring(aStart, aEnd));
					affectedServers = flowResult.substring(aStart, aEnd);
					
					System.out.println("ID : "  + pasId);
					System.out.println("URL : " + reportUrl);
					System.out.println("start : " + startTime);
					System.out.println("End : " + endTime);	
					System.out.println("Response : " + response);
					System.out.println("flowResult : " + flowResult);
				}
			}catch(Exception e){e.printStackTrace();}
		}
		
		public String getNodeValue(String key, Element pElmnt){
			String result=null;
			NodeList elmntLst = pElmnt.getElementsByTagName(key);
			Element elmnt = (Element) elmntLst.item(0);
			NodeList resultNode = elmnt.getChildNodes();
			result = ((Node) resultNode.item(0)).getNodeValue();
			return result;
		}
		public String getPasId() {
			return pasId;
		}
		public void setPasId(String pasId) {
			this.pasId = pasId;
		}
		public String getReportUrl() {
			return reportUrl;
		}
		public void setReportUrl(String reportUrl) {
			this.reportUrl = reportUrl;
		}
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String response) {
			this.response = response;
		}
		public String getFlowResult() {
			return flowResult;
		}
		public void setFlowResult(String flowResult) {
			this.flowResult = flowResult;
		}
		public String getAffectedServers() {
			return affectedServers;
		}
		public void setAffectedServers(String affectedServers) {
			this.affectedServers = affectedServers;
		}
		public String getBuiltRPM() {
			return builtRPM;
		}
		public void setBuiltRPM(String builtRPM) {
			this.builtRPM = builtRPM;
		}
	}
}
