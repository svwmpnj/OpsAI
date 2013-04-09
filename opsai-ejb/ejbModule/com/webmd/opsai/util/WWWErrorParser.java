package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class WWWErrorParser {
	public static void main(String[] args)  throws Exception
	{
		File oldlog = new File("C:\\tmp\\error224.txt");
		//File sbmFile = new File("C:\\sbm.txt");
		PrintWriter[] fileOuts = new PrintWriter[6];
		fileOuts[0] = new PrintWriter(new FileWriter("c:\\tmp\\error224rest.txt")); 
		fileOuts[1] = new PrintWriter(new FileWriter("c:\\tmp\\error224tld.txt"));
		fileOuts[2] = new PrintWriter(new FileWriter("c:\\tmp\\error224mps.txt"));
		fileOuts[3] = new PrintWriter(new FileWriter("c:\\tmp\\error224headfoot.txt")); 
		fileOuts[4] = new PrintWriter(new FileWriter("c:\\tmp\\error224jasperunabletoload.txt"));
		fileOuts[5] = new PrintWriter(new FileWriter("c:\\tmp\\error224questionaire.txt"));
		//fileOuts[6] = new PrintWriter(new FileWriter("c:\\tmp\\error224prehandleurl.txt"));
		BufferedReader bufRemoveReader = new BufferedReader(new FileReader(oldlog));
		
		String line = null;
		StringBuffer sb = new StringBuffer();
		int foundInd = 0;
		int[] counter = new int[6];
		
		try{
		
			while ((line = bufRemoveReader.readLine()) != null)
			{
				if (line.startsWith("ERROR")||line.startsWith("FATAL")){
					counter[foundInd]++;
					fileOuts[foundInd].print(sb.toString());
					
					sb = new StringBuffer();
					foundInd = 0;
				}
				sb.append(line + "\n");
				
				if (foundInd == 0){
					if(line.indexOf("prof-medlinks.tld")!= -1)
						foundInd = 1;
					else if(line.indexOf("most-popular-sidebar.jsp")!= -1)
						foundInd = 2;
					else if(line.indexOf("header.jsp")!= -1 || line.indexOf("footer.jsp")!= -1)
						foundInd = 3;
					else if(line.indexOf("JasperException: Unable to")!= -1)
						foundInd = 4;
					else if(line.toLowerCase().indexOf("buildNameValueMap() - questionnaireID".toLowerCase())!= -1)
						foundInd = 5;
					
				}
					
				
				//if (line.indexOf("NullPointerException")!= -1){
				//if (line.indexOf("Cursor")!= -1){
				//if (line.indexOf("QNA")!= -1){
				//if (line.indexOf("FATAL")!= -1){
				//if (line.indexOf("legacyid")!= -1){
				//if (line.indexOf("prof-medlinks.tld")!= -1){
				//if (line.indexOf("ClassNotFoundException")!= -1){
				//if (line.indexOf("URLRedirect")!= -1){
				//if (line.indexOf("JasperException: java.lang.NullPointerException")!= -1){
				//if (line.indexOf("doStartTag() - Error")!= -1){
				//if (line.indexOf("errorCode=404")!= -1){
				//if (line.indexOf("servlet jsp threw exception")!= -1){
				//if (line.indexOf("preHandle() - request URL")!= -1){
				//if (line.indexOf("buildNameValueMap() - questionnaireID")!= -1){
				//if (line.indexOf("buildNameValueMap() - questionnaireid")!= -1){
				//if (line.indexOf("buildNameValueMap()")!= -1){
				//if (line.indexOf("JasperException: Unable to")!= -1){
				//if (line.indexOf("most-popular-sidebar.jsp")!= -1){
				//if (line.toLowerCase().indexOf("brick")!= -1 || line.toLowerCase().indexOf("auth")!= -1){
				//if (line.indexOf("header.jsp")!= -1 || line.indexOf("footer.jsp")!= -1){
					//foundOne = true;
				//}
			}
			counter[foundInd]++;
			fileOuts[foundInd].print(sb.toString());
			
			for(int i=0; i<6; i++){
				fileOuts[i].println("Total Event: " + counter[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			for(int i=0; i<6; i++){
				fileOuts[i].close();
			}
		}
	}
}
