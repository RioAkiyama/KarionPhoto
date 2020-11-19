package com.rio.aki;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {
	
	public static String objectToJson(Object obj) {
		String json = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static String sanitize(String str) {
		if(str == null) return "";
		return str.replace("&", "&amp;")
					.replace("<", "")
					.replace(">", "")
					.replace("\"", "")
					.replace("\'", "")
					.replace("/", "");
	}
	
	public static boolean isEmptyOrNull(String str) {
		if(str == null) return true;
		if("".equals(str)) return true;
		return false;
	}
	
	public static boolean isNumber(String str) {
		if(str == null) return false;
		try {
			Integer.parseInt(str);
			return true;
		}catch(Exception e){  }
		return false;
	}

	public static com.google.type.Date convertToGoogleDate(java.util.Date date) {
		TimeZone timeZone = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTime(date);
		com.google.type.Date.Builder dateBuilder = com.google.type.Date.newBuilder();
		dateBuilder.setDay(cal.get(Calendar.DAY_OF_MONTH));
		// Months start at 0, not 1
		dateBuilder.setMonth(cal.get(Calendar.MONTH) + 1);
		dateBuilder.setYear(cal.get(Calendar.YEAR));
		return dateBuilder.build();
	}
	
	public static java.util.Date strToDate(String dateStr) throws ParseException {
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
        return sdFormat.parse(dateStr);
	}
	
	public static int diffDate(java.util.Date dateFrom, java.util.Date dateTo) {
		long dateTimeTo = dateTo.getTime();
        long dateTimeFrom = dateFrom.getTime();
        return Math.round(( dateTimeTo - dateTimeFrom  ) / (1000 * 60 * 60 * 24 ));
	}
	
	public static java.util.Date addDate(java.util.Date date, int cnt){
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, cnt);
        return calendar.getTime();
	}
	
}
