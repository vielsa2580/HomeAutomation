package com.orvito.homevito.presentors;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orvito.homevito.R;
import com.orvito.homevito.outmsgfactory.OMInitiateWenDisc;

public class ACTAbout extends Activity{

	TextView _UITVName,_UITVVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_about);
		
		final Handler handler=new Handler();
		
		_UITVName = (TextView)findViewById(R.id.textView1);
		_UITVVersion = (TextView)findViewById(R.id.textView2);
		
		_UITVName.setText(R.string.app_name);
		try {
			_UITVVersion.setText("Version "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	
		Button discWen = (Button)findViewById(R.id.btInitiateWenDisc);
		discWen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				new OMInitiateWenDisc().tcpInitiateWenDisc(ACTAbout.this, handler);
				
			}
		});
		
	}
	
	

}
