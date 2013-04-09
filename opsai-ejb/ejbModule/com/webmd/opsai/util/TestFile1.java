package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class TestFile1 {
	public static void main(String[] args)  throws Exception
	{
		File oldlog = new File("C:\\tmp\\tomcat.log.2011-02-24");
		//File sbmFile = new File("C:\\sbm.txt");
		PrintWriter fileOut = new PrintWriter(new FileWriter("c:\\tmp\\log224rest.txt")); 
		PrintWriter fileOutTarget1 = new PrintWriter(new FileWriter("c:\\tmp\\fatal224.txt"));
		PrintWriter fileOutTarget2 = new PrintWriter(new FileWriter("c:\\tmp\\error224.txt"));
		BufferedReader bufRemoveReader = new BufferedReader(new FileReader(oldlog));
		//BufferedReader bufSBMReader = new BufferedReader(new FileReader(sbmFile));
		
		String line = null;
		StringBuffer sb = new StringBuffer();
		//String tmp = null;
		//Set<String> toRemoveSet = new HashSet<String>();
		//Set<String> sbmSet = new HashSet<String>();
		//Set<String> resultSet = new HashSet<String>();
		//int i = 0;
		boolean found1 = false;
		boolean found2 = false;
		int target1Count = 0;
		int target2Count = 0;
		int restCount = 0;
		
		try{
		
			while ((line = bufRemoveReader.readLine()) != null)
			{
				if (line.startsWith("ERROR")||line.startsWith("FATAL")){
					if (found1){
						target1Count++;
						fileOutTarget1.print(sb.toString());
					}else if (found2){
						target2Count++;
						fileOutTarget2.print(sb.toString());
					}else {
						restCount++;
						fileOut.print(sb.toString());
					}
					sb = new StringBuffer();
					found1 = false;
					found2 = false;
				}
				sb.append(line + "\n");
				if (!(found1 || found2)){
					if (line.startsWith("FATAL")){
						found1 = true;
					}else if (line.startsWith("ERROR")){
						found2 = true;
					}
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
			if (found1){
				target1Count++;
				fileOutTarget1.print(sb.toString());
			}else if (found2){
				target2Count++;
				fileOutTarget2.print(sb.toString());
			}else {
				restCount++;
				fileOut.print(sb.toString());
			}
			
			fileOut.println("Total Event: " + restCount);
			fileOutTarget1.println("Total Event: " + target1Count);
			fileOutTarget2.println("Total Event: " + target2Count);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
			fileOutTarget1.close();
			fileOutTarget2.close();
		}
	}
}
