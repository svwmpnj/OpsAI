package com.webmd.opsai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class TestFile2 {
	public static void main(String[] args)  throws Exception
	{
		File sbmFile = new File("C:\\sbmemail.txt");
		PrintWriter fileOut = new PrintWriter(new FileWriter("c:\\sbmnew1.txt")); 
		BufferedReader bufRemoveReader = new BufferedReader(new FileReader(sbmFile));
		
		String line = null;
		StringBuffer sb = new StringBuffer();
		
		try{
		
			while ((line = bufRemoveReader.readLine()) != null)
			{
				line = line.substring(54) + "; ";
				System.out.println(line);
				sb.append(line);

			}

			fileOut.print(sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			fileOut.close();
		}
	}
}
