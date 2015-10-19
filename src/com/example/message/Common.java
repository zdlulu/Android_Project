package com.example.message;

import android.util.Log;

public class Common {
	
	static String setstring="";
	public void set_string(String str){
		setstring = setstring+str+"\n";
	}
	public String get_string(){
		return setstring;
	}

}
