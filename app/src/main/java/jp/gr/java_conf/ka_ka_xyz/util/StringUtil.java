package jp.gr.java_conf.ka_ka_xyz.util;

public class StringUtil {
	
	private StringUtil(){}
	
	public static String replace(String message, String[] replaceStr){
		if(replaceStr == null || replaceStr.length < 1){
			return message;
		} else {
			
			for(int i = 0; i < replaceStr.length; i++){
				StringBuffer placeHolder = new StringBuffer().append("\\{").append(i).append("\\}"); 
				message = message.replaceAll(placeHolder.toString(), replaceStr[i]);
			}
			return message;
		}
	}

}
