package com.webmd.opsai.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SCSChart {

	private String title;
	private String dataX1Str, dataX2Str;
	private String dataY1Str, dataY2Str;
	private String labelX1Str, labelY1Str, labelY2Str;
	private final String HOURLY_LABEL_STR = "|0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|";
	
	public SCSChart() {
	}
	public SCSChart(String title) {
		this.title = title;
	}
	
	public void useHourlyLabel(){
		labelX1Str = this.HOURLY_LABEL_STR;
	}
	
	public void useDailyLabel(Calendar firstDay, int numberOfDays, String dateFormatPattern){
		if (numberOfDays<1)
			return;
		
		//To avoid tricky daylight saving problem
		firstDay.add(Calendar.HOUR_OF_DAY, 3);
		
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
		sb.append("|");
		for (int i=0; i<numberOfDays; i++){
			sb.append(sdf.format(firstDay.getTime())+"|");
			firstDay.add(Calendar.DATE, 1);
		}
		labelX1Str = sb.toString();
	}
	
	public void useWeeklyLabel(Calendar firstDay, int numberOfWeeks, String dateFormatPattern){
		if (numberOfWeeks<1)
			return;
		
		//To avoid tricky daylight saving problem
		firstDay.add(Calendar.HOUR_OF_DAY, 3);
		
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
		sb.append("|");
		for (int i=0; i<numberOfWeeks; i++){
			sb.append(sdf.format(firstDay.getTime())+"|");
			firstDay.add(Calendar.DATE, 7);
		}
		labelX1Str = sb.toString();
	}
	
	public static int calculateDateDiffNumber(Calendar firstDay, Calendar targetDay, int maxNumber){
		//To avoid tricky daylight saving problem
		firstDay.add(Calendar.HOUR_OF_DAY, 3);
		targetDay.add(Calendar.HOUR_OF_DAY, 3);
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		for (int i=0; i<maxNumber; i++){
			if (sdf.format(firstDay.getTime()).equals(sdf.format(targetDay.getTime()))){
				return i;
			}
			firstDay.add(Calendar.DATE, 1);
		}
		return maxNumber;
	}
	
	public static int calculateWeekDiffNumber(Calendar firstDay, Calendar targetDay, int maxNumber){
		//To avoid tricky daylight saving problem
		firstDay.add(Calendar.HOUR_OF_DAY, 3);
		targetDay.add(Calendar.HOUR_OF_DAY, 3);
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		for (int i=0; i<maxNumber; i++){
			if (sdf.format(firstDay.getTime()).equals(sdf.format(targetDay.getTime()))){
				return i;
			}
			firstDay.add(Calendar.DATE, 7);
		}
		return maxNumber;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDataX1Str() {
		return dataX1Str;
	}
	public void setDataX1Str(String dataX1Str) {
		this.dataX1Str = dataX1Str;
	}
	public String getDataX2Str() {
		return dataX2Str;
	}
	public void setDataX2Str(String dataX2Str) {
		this.dataX2Str = dataX2Str;
	}
	public String getDataY1Str() {
		return dataY1Str;
	}
	public void setDataY1Str(String dataY1Str) {
		this.dataY1Str = dataY1Str;
	}
	public String getDataY2Str() {
		return dataY2Str;
	}
	public void setDataY2Str(String dataY2Str) {
		this.dataY2Str = dataY2Str;
	}
	public String getLabelX1Str() {
		return labelX1Str;
	}
	public void setLabelX1Str(String labelX1Str) {
		this.labelX1Str = labelX1Str;
	}
	public String getLabelY1Str() {
		return labelY1Str;
	}
	public void setLabelY1Str(String labelY1Str) {
		this.labelY1Str = labelY1Str;
	}
	public String getLabelY2Str() {
		return labelY2Str;
	}
	public void setLabelY2Str(String labelY2Str) {
		this.labelY2Str = labelY2Str;
	}

}
