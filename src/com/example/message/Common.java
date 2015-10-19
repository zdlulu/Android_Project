package com.example.message;

import android.util.Log;

public class Common {
	String TAG = "Common";
	
	public static String setstring="";
	public void set_string(String str){
		setstring = setstring+str+"\n";
		Log.i(TAG,"str="+str);
	}
	public String get_string(){
		return setstring;
	}
	
	public void clear_string(){
		setstring = "";
	}

}
