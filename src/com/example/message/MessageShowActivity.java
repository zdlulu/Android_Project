package com.example.message;

import com.example.android_map_location.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class MessageShowActivity extends Activity{
	
	TextView tv_message;
	private Common common= new Common();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);
		init_widget();
		tv_message.setText(common.get_string());
		
	}
	/***************************************************/
	private void init_widget(){
		tv_message = (TextView) findViewById(R.id.tv_message);
		tv_message.setMovementMethod(new ScrollingMovementMethod());
	}
	/***************************************************/
}
