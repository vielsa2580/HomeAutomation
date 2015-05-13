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
import com.orvito.homevito.helpers.ADPTDeviceMasterPager;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.outmsgfactory.OMTabSync;


public class ACTDeviceMaster extends Activity {

	ViewPager viewPager;
	public Button _UIBTReload;
	RelativeLayout _UILLResidentDetail;//,_UILLBlockDetail;
	Boolean isFirstImage = true;	
	public static ACTDeviceMaster currentInstance;	
	public LinearLayout loadingLayout;
	public Timer timer;
	Handler handler;
	Button _BTkingOff,_BTkingOn;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		ACTDeviceMaster.currentInstance=ACTDeviceMaster.this;
		refreshData();
	}
	
	@Override
	protected void onStop() {
	clearTimers();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ACTDeviceMaster.currentInstance=null;
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
		clearTimers();
		
		showDialog();
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
		new OMTabSync().syncTablet(ACTDeviceMaster.this,new Handler());
	}

	public void clearTimers() {
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
	}
	
	private void showDialog() {
		loadingLayout.setVisibility(View.VISIBLE);
		
	}
	
	public void populateDeviceControl(MODELResultSet response){
		_UIBTReload.setVisibility(View.GONE);
		if(response.getError()==null){
			List<MODELRoom> roomsList=(List<MODELRoom>)response.getDataList();
			if(roomsList==null){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(getBaseContext(), "No files assigned to you", Toast.LENGTH_LONG).show();

			}else if(roomsList.size()<1){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(getBaseContext(), "No files assigned to you", Toast.LENGTH_LONG).show();

			}else{
				viewPager.setVisibility(View.VISIBLE);
				viewPager.setAdapter(new ADPTDeviceMasterPager(ACTDeviceMaster.this,handler,_UILLResidentDetail,roomsList));

			}

		}else{
			Toast.makeText(getBaseContext(), response.getMessage(), Toast.LENGTH_LONG).show();
		}	
	}

	private void setContentView() {
		setContentView(R.layout.act_devicecontrol);
		_UILLResidentDetail = (RelativeLayout)findViewById(R.id.LLresidentDetail);		
		viewPager = (ViewPager)findViewById(R.id.viewpager);
		_UIBTReload=(Button) findViewById(R.id.reload);
		loadingLayout=(LinearLayout) findViewById(R.id.loadinglayout);	
		
		_BTkingOff = (Button)findViewById(R.id.btKingControlOFF);
		_BTkingOn = (Button)findViewById(R.id.btKingControlOn);
		handler=new Handler();
	}

}