package com.orvito.homevito.presentors;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.outmsgfactory.OMTabReg;
import com.orvito.homevito.socketprogramming.SRVReceiver;

public class ACTUserLogin extends Activity {

	EditText _UIETUsername,_UIETPassword;
	//public AlertDialog alertDialog;
	public Timer timer;
	public ProgressBar  loginProgress;
	private LogCollector logCollector;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		if(isMyServiceRunning()){
			stopService(new Intent(ACTUserLogin.this,SRVReceiver.class));
		}
		startService(new Intent(ACTUserLogin.this,SRVReceiver.class));	


	}


	class CheckForceCloseTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return logCollector.hasForceCloseHappened();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {

				logCollector.sendLog("lintonye@gmail.com", "Error Log", "Preface\nPreface line 2");

			} else
				Toast.makeText(getApplicationContext(), "No force close detected.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//finish();		
	}

	public void login(View v){
		loginProgress.setVisibility(View.VISIBLE);
		clearTimers();


		final Handler handler=new Handler();
		timer=new Timer();
		TimerTask timerTask=new TimerTask() {

			@Override
			public void run() {

				handler.post(new Runnable() {					
					@Override
					public void run() {
						loginProgress.setVisibility(View.GONE);
						Toast.makeText(getBaseContext(), "No response from server.Retry...!!", 1).show();						
					}
				});
			}
		};
		timer.schedule(timerTask, 1000*20);

		new Thread(new Runnable() {			
			@Override
			public void run() {
				new OMTabReg().tcpRegisterTablet(ACTUserLogin.this,handler, _UIETUsername, _UIETPassword);		
				//				new OMSmartHubDisc().tcpDiscoverSH(ACTUserLogin.this, handler);
			}
		}).start();



	}

	public void clearTimers() {
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.orvito.homevito.socketprogramming.SRVReceiver".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	private void setContentView() {
		setContentView(R.layout.act_userlogin);
		_UIETUsername=(EditText) findViewById(R.id.ETUsername);
		_UIETPassword=(EditText) findViewById(R.id.ETPassword);
		loginProgress=(ProgressBar) findViewById(R.id.loginprogress);

		logCollector = new LogCollector(this);

		CheckForceCloseTask task = new CheckForceCloseTask();
		task.execute();

	}

}