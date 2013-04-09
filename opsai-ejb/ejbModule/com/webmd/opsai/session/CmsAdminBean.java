package com.webmd.opsai.session;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import com.webmd.opsai.entity.ScsPubEvent;
import com.webmd.opsai.entity.ServiceConf;
import com.webmd.opsai.util.LdapUser;
import com.webmd.opsai.util.OpsClient;
import com.webmd.opsai.util.SCSAnalyzer;
import com.webmd.opsai.util.OpsClient.OpsClientResponse;

@Name("cmsadmin")
@Scope (CONVERSATION)
public class CmsAdminBean {

	@Logger private Log log;
	
	@In Identity identity;
	@In FacesMessages facesMessages;
	@In LdapUser loggedinuser;
	
    private String environment, portal, product, hostType;
    private String actionTaken, pasId,reportUrl, startTime, endTime, response, flowResult;
    private final String SCSLOGFOLDER_CD = "scslogfolder";
    private final String CMSAIENV_CD = "cmsaienvrionment";
    
	private OpsClientResponse ocr = null;
	
	@Begin(join=true)
    public void processMTSLogs(){
    	log.info("Triggered!!"); 
    }
    
	@Begin(join=true)
    public void processHostTypeSelection() {
    	log.info("processHostTypeSelection action called ");
    	FacesContext context = FacesContext.getCurrentInstance();
    	this.portal = getContextParam(context, "portalName");
    	this.product = getContextParam(context, "productName");
    	this.hostType = getContextParam(context, "hosttypeName");
    	log.info("processHostTypeSelection: " + hostType);
    }    

    public String hostTypeAction(String action){
    	log.info("action: " + action);
    	log.info("be: " + portal);
    	log.info("product: " + product);
    	log.info("hostType: " + hostType);
    	log.info("environment: " + environment);
    	if (hostType == null){
    		facesMessages.add("Please pick Host Type");
    		return "";
    	}
    	String permissionAction = "";
    	if (null==environment){
    		facesMessages.add("Please pick environment");
    	}else if ("live".equals(environment)||"lab".equals(environment)){
    		permissionAction = portal+" - OPS";
    	}else if (environment.startsWith("dev")){
    		permissionAction = portal+" - DEV";
    	}else{
    		permissionAction = portal+" - QA";
    	}
    	log.info("permissionAction: " + permissionAction);
    	if (!identity.hasPermission("environemnt",permissionAction)){
    		facesMessages.add("You don't have permission to " + environment);
    		ocr = null;
    		return "";
    	}
    	OpsClient oc = new OpsClient();
		String result = oc.triggerPASflow("CMSAdminSvc/HostTypeCtrl/"+hostType+"Ctrl", 
				"?portal="+portal+"&product="+product+"&hostType="+hostType+"&environment="+environment+
				"&action="+action+"&emailNotice=Yes&operatorName=" + loggedinuser.getName()+
				"&operatorEmail="+loggedinuser.getEmailAddr()+"&ccEmailList=cmsops@webmd.net&parentFlow=CMS AI");
		ocr =oc.new OpsClientResponse(result);
		actionTaken = action;
		return "";
    }
    
    public String consRestart(){
    	log.info("Restart Cons");
    	facesMessages.add("Business Entity: Consumer");
		return restart("Qa01ConsWPRestart");
    }
    public String profRestart(){
    	log.info("Restart Prof");
    	facesMessages.add("Business Entity: Professional");
    	return restart("Qa01ProfCCERestart");
    }

    @Begin(join=true)
    public void moveLogs(){
    	FacesContext context = FacesContext.getCurrentInstance();
    	String docbase = getContextParam(context, "docbaseName");
    	log.info("docbase: " + docbase);
    	ServiceConfList sc = new ServiceConfList();
    	ServiceConf con = (ServiceConf)((Session)sc.getEntityManager().getDelegate()).createQuery(
				"from ServiceConf as sc where sc.serviceCD = ?").setString(0, CMSAIENV_CD).uniqueResult();
		String envName = con.getServiceURL();
		docbase+="production".equals(envName)?"":envName;
    	OpsClient oc = new OpsClient();
		String result = oc.triggerPASflow("SelfHealing/SCSLogMover", "?dmcsName="+docbase+"&operatorName=" + loggedinuser.getName()+
				"&operatorEmail="+loggedinuser.getEmailAddr()+"&ccEmailList=cmsops@webmd.net&parentFlow=CMS AI");
		facesMessages.add(result);
    }
    
	public String restart(String flowName){
    	log.info("start action called ");
		try{
			//log.info("Actually we call deploy on host: dmas01t-con-08.portal.webmd.com software policy: prodops cms essential kit_es4u6_x86");
			/*
			OpsClient oc = new OpsClient();
			String result = oc.triggerPASflow("CMSAdminSvc/Emergency/"+flowName, "");
			log.info("Result String from PAS: " + result);
			this.parsePASResult(result);
			facesMessages.add("Response: " + response);
			facesMessages.add("Start Time: " + startTime);
			facesMessages.add("End Time: " + endTime);			*/
		}catch(Exception e){
			facesMessages.add("Exception caught: " + e.getMessage());
			return "";
		}
		return "deployresult";
	}
    private String getContextParam(FacesContext context, String key){
    	Map<String, String> params = context.getExternalContext().getRequestParameterMap();
    	return params.get(key);
    }
    
    public String concatePortal(String env){
    	return this.portal+env;
    }
    
    @Begin(join=true)
	public void processSCSLogs(){
		ServiceConfList sc = new ServiceConfList();
    	ServiceConf con = (ServiceConf)((Session)sc.getEntityManager().getDelegate()).createQuery(
				"from ServiceConf as sc where sc.serviceCD = ?").setString(0, SCSLOGFOLDER_CD).uniqueResult();
		String folderUrl = con.getServiceURL();
/*
    	try{
			//consumerdmcs
			//File folder = new File(folderUrl+"consumerdmcs");
			//File folder = new File("\\\\nasfs01x-ops-07\\shr_dtm_log\\scslog\\consumerdmcs");
			//next line only for IAD1 consumer logs
			//File folder = new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcs");
    		//File folder = new File("C:\\scslog\\test");
			File[] files = folder.listFiles();
			int logNum = files.length;
			log.info("consumerdmcs number: "+logNum);
			for (int i=0; i<logNum; i++){
			//for (int i=0; i<10; i++){
				log.info("file: "+i);
				if (processSCSAnalyzer(new SCSAnalyzer(files[i].getAbsolutePath()))){
					files[i].delete();
					//files[i].renameTo(new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcsfail\\"+files[i].getName()));
				}else{
					files[i].renameTo(new File("\\\\nasfs01x-ops-08\\shr_dtm_log\\scslog\\consumerdmcsfail\\f"+files[i].getName()));
				}
			}
			log.info("consumerdmcs finish");
		}catch(Exception e){
			e.printStackTrace();
		}
    	
	*/	
		try{
			FacesContext context = FacesContext.getCurrentInstance();
	    	String folderName = getContextParam(context, "folderName");
	    	File folder = new File(folderUrl+folderName);
			//File folder = new File("\\\\nasfs01x-ops-07\\shr_dtm_log\\scslog\\" + folderName);
	    	//File folder = new File("C:\\scslog\\test");
			File[] files = folder.listFiles();
			//SCSAnalyzer analyzer = null;
			int logNum = files.length;
			facesMessages.add("Parse #0 logs", logNum);
			for (int i=0; i<logNum; i++){
			//for (int i=0; i<1; i++){
				if (processSCSAnalyzer(new SCSAnalyzer(files[i].getAbsolutePath()))){
					files[i].delete();
				}else{
					files[i].renameTo(new File(folderUrl + folderName + "fail\\"+files[i].getName()));
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
    
	
	private boolean processSCSAnalyzer(SCSAnalyzer ana){
		boolean result = false;
		try{
			ScsPubEventHome sph = new ScsPubEventHome();
			ana.analyze();
			Map<String, Date> eventTimeStamps = ana.getEventTimeStamps();
			log.info(ana.getLogFile().getName());
			log.info(ana.getBytesTransfered());
			ScsPubEvent spe = sph.find(ana.getLogFile().getName(), eventTimeStamps.get(SCSAnalyzer.START));
			if (spe==null){
				log.info("Cannot find the record in DB");
				spe = new ScsPubEvent();
			}
			spe.setLogName(ana.getLogFile().getName());
			spe.setStatus(ana.getStatus());
			spe.setType(ana.getType());
			spe.setDocbase(ana.getDocbase());
			spe.setLcState(ana.getLcState());
			spe.setStartTime(eventTimeStamps.get(SCSAnalyzer.START));
			spe.setCompleteTime(eventTimeStamps.get(SCSAnalyzer.COMPLETED));
			try{
				String bytesTransStr = ana.getBytesTransfered().trim();
				bytesTransStr = bytesTransStr.substring(0, bytesTransStr.indexOf("bytes")-1);
				log.info(bytesTransStr);
				long bytesTrans = Long.parseLong(bytesTransStr);
				log.info(bytesTrans);
				spe.setBytesTransfered(bytesTrans);
			}catch(Exception e){
				//e.printStackTrace();
				spe.setBytesTransfered(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND)!=null&& eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART)!=null){
				spe.setContBaseTaken(eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTQUERYSTART).getTime());
			}else{
				spe.setContBaseTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND)!=null&& eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART)!=null){
				spe.setContlessBaseTaken(eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYEND).getTime()-eventTimeStamps.get(SCSAnalyzer.CONTENTLESSQUERYSTART).getTime());
			}else{
				spe.setContlessBaseTaken(0);
			}
			if(spe.getCompleteTime()!=null&& spe.getStartTime()!=null){
				spe.setExportTimeTaken(spe.getCompleteTime().getTime()-spe.getStartTime().getTime());
			}else{
				spe.setExportTimeTaken(0);
			}
			spe.setContObjCount(ana.getContentObjCount());
			spe.setContlessObjCount(ana.getTotalObjCount()-ana.getContentObjCount());
			
			if(eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG_COMPLETE)!=null&& eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG)!=null){
				spe.setLocalSyncTaken(eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG_COMPLETE).getTime() - eventTimeStamps.get(SCSAnalyzer.SYNC_LOCAL_CATALOG).getTime());
			}else{
				spe.setLocalSyncTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION)!=null&& eventTimeStamps.get(SCSAnalyzer.TRANSFER_TO_AGENT)!=null){
				spe.setTargetTransferTaken(eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION).getTime() - eventTimeStamps.get(SCSAnalyzer.TRANSFER_TO_AGENT).getTime());
			}else{
				spe.setTargetTransferTaken(0);
			}
			if(eventTimeStamps.get(SCSAnalyzer.TARGET_SYNC_COMPLETE)!=null&& eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION)!=null){
				spe.setTargetSyncTaken(eventTimeStamps.get(SCSAnalyzer.TARGET_SYNC_COMPLETE).getTime() - eventTimeStamps.get(SCSAnalyzer.APPLY_TARGET_SYNCHRONIZATION).getTime());
			}else{
				spe.setTargetSyncTaken(0);
			}
			sph.getEntityManager().persist(spe);
			sph.getEntityManager().flush();
			result = "successful".equals(ana.getStatus());
		}catch(Exception e){
			e.printStackTrace();
		}		
		return result;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getPortal() {
		return portal;
	}
	public void setPortal(String portal) {
		this.portal = portal;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getHostType() {
		return hostType;
	}
	public void setHostType(String hostType) {
		this.hostType = hostType;
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

    public OpsClientResponse getOcr() {
		return ocr;
	}

	public void setOcr(OpsClientResponse ocr) {
		this.ocr = ocr;
	}

    public String getActionTaken() {
		return actionTaken;
	}

	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}

}
