package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MedscapeTomcatLogParser {
	public static void main(String[] args)  throws Exception
	{
		File oldlog = new File("C:\\tmp\\error224tmp2.txt");
		PrintWriter fileOut = new PrintWriter(new FileWriter("c:\\tmp\\error224tmp3.txt")); 
		PrintWriter fileOutTarget = new PrintWriter(new FileWriter("c:\\tmp\\error224nullsvcjsponly.txt"));
		BufferedReader bufRemoveReader = new BufferedReader(new FileReader(oldlog));
		
		String line = null;
		StringBuffer sb = new StringBuffer();		
		boolean foundOne = false;
		int targetCount = 0;
		int restCount = 0;
		try{
			while ((line = bufRemoveReader.readLine()) != null)
			{
				if (line.startsWith("ERROR")||line.startsWith("FATAL")){
					if (!foundOne){
						restCount++;
						fileOut.print(sb.toString());
					}else{
						targetCount++;
						fileOutTarget.print(sb.toString());
					}
					
					sb = new StringBuffer();
					foundOne = false;
				}
				sb.append(line + "\n");
				//if (line.indexOf("Servlet.service()")!= -1){
				//if (line.indexOf("servlet URLRedirect")!= -1){
				//if (line.indexOf("MostPopularWidgetTag")!= -1){servlet jsp threw exception
				if (line.indexOf("servlet jsp threw exception")!= -1){
				//if (line.indexOf("RateContentController.java")!= -1){
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
					foundOne = true;
				}
			}
			
			if (!foundOne){
				restCount++;
				fileOut.print(sb.toString());
			}else{
				targetCount++;
				fileOutTarget.print(sb.toString());
			}
			
			fileOut.println("Total Event: " + restCount);
			fileOutTarget.println("Total Event: " + targetCount);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
			fileOutTarget.close();
		}
	}
}
