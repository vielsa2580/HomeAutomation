package com.orvito.homevito.presentors;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.helpers.ADPTSensorControlPager;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.outmsgfactory.OMTabSync;
import com.orvito.homevito.utils.UTILConstants;


public class ACTSensorControl extends Activity {

	ViewPager viewPager;
	public Button _UIBTReload;
	RelativeLayout _UILLResidentDetail;//,_UILLBlockDetail;
	Boolean isFirstImage = true;	
	List<MODELRoom> groupList;
	public static ACTSensorControl currentInstance;	
	public LinearLayout loadingLayout;
	public Timer timer;
	Handler handler;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView();

	}

	@Override
	protected void onStart() {
		super.onStart();
		ACTSensorControl.currentInstance=ACTSensorControl.this;
		populateSensorControl(UTILConstants.sensorsData);
		if(UTILConstants.sensorsData==null){
			refreshData();
		}
	}

	@Override
	protected void onStop() {
		clearTimers();
		super.onStop();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		ACTSensorControl.currentInstance=null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.devicecontrol, menu);        
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		refreshData();
		return super.onOptionsItemSelected(item);
	}

	public void reload(View v){
		refreshData();
	}


	public void refreshData(){
		_UIBTReload.setVisibility(View.GONE);
		loadingLayout.setVisibility(View.VISIBLE);
		clearTimers();


		final Handler handler=new Handler();
		timer=new Timer();
		TimerTask timerTask=new TimerTask() {

			@Override
			public void run() {

				handler.post(new Runnable() {					
					@Override
					public void run() {
						//alertDialog.cancel();
						loadingLayout.setVisibility(View.GONE);
						Toast.makeText(getBaseContext(), "No response from server.Retry...!!", 1).show();	
						_UIBTReload.setVisibility(View.VISIBLE);
					}
				});
			}
		};
		timer.schedule(timerTask, 1000*20);
		new OMTabSync().syncTablet(ACTSensorControl.this,new Handler());
	}

	public void clearTimers() {
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
	}


	public void populateSensorControl(List<MODELRoom> roomsListWithSensors){
		_UIBTReload.setVisibility(View.GONE);

		
		if(roomsListWithSensors==null){
			viewPager.setVisibility(View.INVISIBLE);
			UTILConstants.toastMsg(getBaseContext(), "No Sensors available");
			loadingLayout.setVisibility(View.GONE);	
			_UIBTReload.setVisibility(View.VISIBLE);
			clearTimers();

		}else if(roomsListWithSensors.size()<1){
			UTILConstants.toastMsg(getBaseContext(), "No Sensors available");
			viewPager.setVisibility(View.INVISIBLE);
			loadingLayout.setVisibility(View.GONE);	
			_UIBTReload.setVisibility(View.VISIBLE);
			clearTimers();
		}else{
			viewPager.setVisibility(View.VISIBLE);
			viewPager.setAdapter(new ADPTSensorControlPager(ACTSensorControl.this,handler,_UILLResidentDetail,roomsListWithSensors));
		}


	}

	private void setContentView() {
		setContentView(R.layout.act_sensorcontrol);
		_UILLResidentDetail = (RelativeLayout)findViewById(R.id.LLresidentDetail);		
		viewPager = (ViewPager)findViewById(R.id.viewpager);
		_UIBTReload=(Button) findViewById(R.id.reload);
		loadingLayout=(LinearLayout) findViewById(R.id.loadinglayout);		
		handler=new Handler();
	}


}