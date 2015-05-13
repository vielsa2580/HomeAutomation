package com.orvito.homevito.presentors;
//01-03 18:56:21.757: W/SurfaceView(27673): app visibility event must be pending in main thread.
//01-03 18:56:21.757: A/libc(27673): Fatal signal 11 (SIGSEGV) at 0x00000000 (code=1)
//01-03 18:56:21.843: A/libc(27673): Fatal signal 7 (SIGBUS) at 0x00000000 (code=128)

import java.net.URI;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.orvito.homevito.R;
import com.orvito.homevito.mjpegstreamer.MjpegInputStream;
import com.orvito.homevito.mjpegstreamer.MjpegView;
import com.orvito.homevito.models.MODELLiveStream;
import com.orvito.homevito.outmsgfactory.OMTabSync;
import com.orvito.homevito.outmsgfactory.OMWeather;
import com.orvito.homevito.utils.UTILConstants;

public class ACTHomepage extends Activity implements OnClickListener,SurfaceHolder.Callback{	

	TextView ampm,date,temperature,maxmin,city,weatherdesc,TVDeviceControl1,TVEmpDir1,TVDeviceControl2,TVEmpDir2,TVAppVersion,TVVideoInfo,TVStreamUnavailable;
	public TextView TVTimehour,TVTimeminute,TVTimesecond;
	TextSwitcher timehour,timeminute,timesecond;
	LinearLayout weatherLayout,videoUnavailableLayout;
	ImageView weatherImage,deviceControlImage,naImage;
	Button updateWeather,_pauseButton,videoRefresh;
	private TimerTask scrollerSchedule;
	private Timer scrollTimer =	null;
	BroadcastReceiver timeTickReceiver;
	Timer timeDateTimer,playerStartTimer,tempPlayerStartTimer;
	TimerTask timeDateTimerTask,playerStartTimerTask;
	Handler handler;
	Animation refreshButtonAnimation;
	SurfaceHolder _holder;
	RelativeLayout _UILLVideoStream;
	ProgressBar _PBStreamVideo;
	int _currentTrack=0;
	ASYNCMJPEGStreamer _asyncmjpegStreamer;
	MjpegView mjpegView;

	Toast swipeToast;

	public static Boolean _playing=false;
	public static int lastColorCodeUsed=Color.WHITE;
	int numberOfVideos=1;
	int backKeyPressed=0;
	String cameraUnavailableInfo="";

	int refreshTimeInSec=5;

	SipManager manager;
	public SipProfile sipProfile = null;
	public SipAudioCall sipCall = null;
	public IncomingCallReceiver callReceiver;

	//-----------------activity lifecycle methods---------------------------	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView();
		try {
			TVAppVersion.setText("V "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		timeTickReceiver=new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateDateTime();
			}
		};

		timeDateTimer=new Timer();
		handler=new Handler();



		_UILLVideoStream.setOnTouchListener(new OnFlingGestureListener() {
			@Override
			public void onTopToBottom() {}
			@Override
			public void onBottomToTop() {}

			@Override
			public void onRightToLeft() {
				playNextStream();
			}

			@Override
			public void onLeftToRight() {
				playPreviousStream();
			}


		});

		Calendar calendar=Calendar.getInstance();
		if(calendar.get(Calendar.HOUR_OF_DAY)>=6 && calendar.get(Calendar.HOUR_OF_DAY)<19){
			weatherLayout.setBackgroundResource(R.drawable.daynabg);
			naImage.setBackgroundResource(R.drawable.dayna);
		}else{
			weatherLayout.setBackgroundResource(R.drawable.nightnabg);
			naImage.setBackgroundResource(R.drawable.nightna);
		}

		prepareSwipeToast();
		swipeToast.show();


		//initializeManager(getBaseContext(),new Handler());
	}

	@Override
	protected void onStart() {
		super.onStart();
		new OMTabSync().syncTablet(ACTHomepage.this,new Handler());//sending a tab sync request so that we have updated state of pir sensor

		_pauseButton.setVisibility(View.GONE);
		registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		updateDateTime();
		new OMWeather().tcpGetCurrentWeather(ACTHomepage.this,new Handler());
		updateDateTime();
		//playVideo();

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateDateTime();
		//startOrSwitchStream(500);
		prepareSwipeToast();	
	}

	@Override
	protected void onPause() {
		super.onPause();
		swipeToast.cancel();


		if(_asyncmjpegStreamer!=null){
			_asyncmjpegStreamer.cancel(true);
			_asyncmjpegStreamer=null;
		}
		clearPlayerTimers();
		backKeyPressed=0;
		stopStreaming();

	}

	@Override	
	protected void onStop() {
		super.onStop();
		releaseMediaPlayer();
		unregisterReceiver(timeTickReceiver);
	}

	protected void onDestroy() {
		super.onDestroy();
		clearTimerTaks(scrollerSchedule);		
		clearTimers(scrollTimer);
	}

	private void setContentView() {
		setContentView(R.layout.act_homepage);
		timehour=(TextSwitcher) findViewById(R.id.timehour);
		timeminute=(TextSwitcher) findViewById(R.id.timeminute);
		ampm=(TextView) findViewById(R.id.ampm);
		date=(TextView) findViewById(R.id.date);
		timesecond=(TextSwitcher) findViewById(R.id.timesecond);
		temperature=(TextView) findViewById(R.id.temperature);
		maxmin=(TextView) findViewById(R.id.maxmintemp);
		city=(TextView) findViewById(R.id.city);
		weatherdesc=(TextView) findViewById(R.id.weatherdesc);
		weatherLayout=(LinearLayout) findViewById(R.id.weatherlayout);
		weatherImage=(ImageView) findViewById(R.id.weatherimage);
		naImage=(ImageView) findViewById(R.id.na);
		TVDeviceControl1=(TextView) findViewById(R.id.TVDeviceControl1);
		TVVideoInfo=(TextView) findViewById(R.id.videoinfo);
		TVEmpDir1=(TextView) findViewById(R.id.TVEmpDir1);
		TVDeviceControl2=(TextView) findViewById(R.id.TVDeviceControl2);
		TVAppVersion=(TextView) findViewById(R.id.version);
		TVEmpDir2=(TextView) findViewById(R.id.TVEmpDir2);
		updateWeather=(Button) findViewById(R.id.updateweather);
		_pauseButton=(Button) findViewById(R.id.pausebutton);
		TVStreamUnavailable=(TextView) findViewById(R.id.streamUnavailableText);
		mjpegView = (MjpegView) findViewById(R.id.mjpeg);	
		videoUnavailableLayout=(LinearLayout) findViewById(R.id.videounavilablelayout);
		refreshButtonAnimation = AnimationUtils.loadAnimation(this,R.anim.spin);
		Typeface tf1=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
		Typeface tf2=Typeface.createFromAsset(getAssets(),"segoeuil.ttf");
		//time.setTypeface(tf1);
		ampm.setTypeface(tf1);
		date.setTypeface(tf1);

		temperature.setTypeface(tf1);
		maxmin.setTypeface(tf1);
		city.setTypeface(tf1);
		weatherdesc.setTypeface(tf1);
		//timesecond.setTypeface(tf1);

		TVDeviceControl1.setTypeface(tf2);
		TVEmpDir1.setTypeface(tf2);
		TVDeviceControl2.setTypeface(tf2);
		TVEmpDir2.setTypeface(tf2);

		timehour.setFactory(new ViewFactory() {
			public View makeView() {

				TVTimehour = new TextView(getBaseContext());
				TVTimehour.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				TVTimehour.setTextSize(40);
				TVTimehour.setTextColor(lastColorCodeUsed);
				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
				TVTimehour.setTypeface(tf);
				return TVTimehour;

			}
		});

		timeminute.setFactory(new ViewFactory() {
			public View makeView() {
				TVTimeminute = new TextView(getBaseContext());
				TVTimeminute.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				TVTimeminute.setTextSize(40);
				TVTimeminute.setTextColor(lastColorCodeUsed);
				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
				TVTimeminute.setTypeface(tf);
				return TVTimeminute;

			}
		});

		timesecond.setFactory(new ViewFactory() {
			public View makeView() {

				TVTimesecond = new TextView(getBaseContext());
				TVTimesecond.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				TVTimesecond.setTextSize(20);
				TVTimesecond.setTextColor(lastColorCodeUsed);
				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
				TVTimesecond.setTypeface(tf);
				return TVTimesecond;

			}
		});

		Animation in = AnimationUtils.loadAnimation(this,R.anim.slide_in_top);
		in.setInterpolator(this, android.R.anim.accelerate_interpolator);
		Animation out = AnimationUtils.loadAnimation(this,R.anim.slide_out_bottom);

		Animation in2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
		in2.setInterpolator(this, android.R.anim.decelerate_interpolator);
		Animation out2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

		timehour.setInAnimation(in2);
		timehour.setOutAnimation(out2);
		timeminute.setInAnimation(in);
		timeminute.setOutAnimation(out);
		timesecond.setInAnimation(in);
		timesecond.setOutAnimation(out);
		TVTimehour.setTextColor(lastColorCodeUsed);
		TVTimeminute.setTextColor(lastColorCodeUsed);
		TVTimesecond.setTextColor(lastColorCodeUsed);


		_UILLVideoStream = (RelativeLayout)findViewById(R.id.LLCamOne);
		_UILLVideoStream.setOnClickListener(this);

		_holder=mjpegView.getHolder();
		int apiLevel=Build.VERSION.SDK_INT;
		//if(apiLevel>13) mjpegView.setZOrderOnTop(true);
		mjpegView.setBackgroundResource(R.drawable.empdirbg);
		_holder.addCallback(this);
		_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		_PBStreamVideo = (ProgressBar)findViewById(R.id.PBStreamVideo);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(backKeyPressed==0){
				Toast.makeText(getBaseContext(), "Press back again to logout", 1).show();
				backKeyPressed=1;
				return true;
			}
		}	
		return super.onKeyDown(keyCode, event);
	}

	private void prepareSwipeToast() {
		swipeToast=new Toast(getBaseContext());
		ImageView view=new ImageView(getBaseContext());
		view.setBackgroundResource(R.drawable.swipe);
		swipeToast.setView(view);
		swipeToast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 50, 50);
		swipeToast.setDuration(Toast.LENGTH_SHORT);
	}




	//--------------video player methods---------------------------

	public class ASYNCMJPEGStreamer extends AsyncTask<MODELLiveStream, Void, MjpegInputStream> {

		MODELLiveStream liveStream;		
		@Override
		protected void onPreExecute() {
			_PBStreamVideo.setVisibility(View.VISIBLE);
			TVVideoInfo.setVisibility(View.INVISIBLE);
			_pauseButton.setVisibility(View.INVISIBLE);
			videoUnavailableLayout.setVisibility(View.INVISIBLE);
			super.onPreExecute();
		}

		protected MjpegInputStream doInBackground(MODELLiveStream... url) {
			//TODO: if camera has authentication deal with it and don't just not work
			this.liveStream=url[0];
			HttpResponse res = null;
			try {
				DefaultHttpClient httpclient=new DefaultHttpClient();
				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(null, -1),
						new UsernamePasswordCredentials("admin", "admin12"));

				res = httpclient.execute(new HttpGet(URI.create(url[0].getUrl())));
				if(res.getStatusLine().getStatusCode()==401) return null;

				return new MjpegInputStream(res.getEntity().getContent());  
			} catch (Exception e) {				
				e.printStackTrace();
				return null;
			} 

		}

		protected void onPostExecute(MjpegInputStream result) {
			if(result!=null){
				TVVideoInfo.setVisibility(View.VISIBLE);
				TVVideoInfo.setText(liveStream.getName());
				_PBStreamVideo.setVisibility(View.GONE);
				_pauseButton.setVisibility(View.INVISIBLE);
				ACTHomepage._playing=true;
				mjpegView.setSource(result);
				mjpegView.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
				mjpegView.showFps(false);
				mjpegView.setBackgroundDrawable(null);
			}else{
				TVVideoInfo.setVisibility(View.INVISIBLE);
				_PBStreamVideo.setVisibility(View.INVISIBLE);
				cameraUnavailableInfo=liveStream.getName()+" stream unavailable";
				_pauseButton.setVisibility(View.INVISIBLE);
				ACTHomepage._playing=false;
				videoUnavailableLayout.setVisibility(View.VISIBLE);
				TVStreamUnavailable.setText(cameraUnavailableInfo);

				TimerTask playerRestartTimerTask=new TimerTask() {
					@Override
					public void run() {
						handler.post(new Runnable() {					
							@Override
							public void run() {			
								if(_currentTrack == numberOfVideos-1){
									_currentTrack=0;										
								}else {
									++_currentTrack;					
								}
								releaseMediaPlayer();
								startStreaming(1);

							}
						});				
					}
				};
				tempPlayerStartTimer=new Timer();
				tempPlayerStartTimer.schedule(playerRestartTimerTask, 5000);
			}
		}

	}


	private void startStreaming(int delay) {

		playerStartTimerTask=new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {					
					@Override
					public void run() {			
						playPauseStreaming();	
					}
				});				
			}
		};
		playerStartTimer=new Timer();
		playerStartTimer.schedule(playerStartTimerTask, delay);
	}

	private void playPauseStreaming(){
		if(_asyncmjpegStreamer==null){//case if player not yet initiated							
			_asyncmjpegStreamer=new ASYNCMJPEGStreamer();
			_asyncmjpegStreamer.execute(UTILConstants.getStreamAtPos(_currentTrack));
		}else if (!_playing){//stream is paused so restart playing
			stopStreaming();
			startStreaming(1);
		}else if (_playing){//stream is playing so pause it
			mjpegView.stopPlayback();
			_playing=false;
			_pauseButton.setVisibility(View.VISIBLE);
		}
	}

	private void stopStreaming() {
		if(mjpegView!=null)mjpegView.forceStopPlaying();
		if(_asyncmjpegStreamer!=null){
			_asyncmjpegStreamer.cancel(true);
			_asyncmjpegStreamer=null;
		}
	}

	private void clearPlayerTimers(){

		if(playerStartTimer!=null) playerStartTimer.cancel();
		playerStartTimer=null;
		if(tempPlayerStartTimer!=null)  tempPlayerStartTimer.cancel();
		tempPlayerStartTimer=null;
	}

	private void releaseMediaPlayer() {
		/*if (_mMediaPlayer != null) {
			_mMediaPlayer.reset();
			_mMediaPlayer.release();
			_mMediaPlayer = null;
		}*/
		if(_asyncmjpegStreamer!=null){
			_asyncmjpegStreamer.cancel(true);
			_asyncmjpegStreamer=null;
		}
	}

	private void playNextStream() {
		clearPlayerTimers();
		_pauseButton.setVisibility(View.INVISIBLE);
		TVVideoInfo.setVisibility(View.INVISIBLE);
		if(_PBStreamVideo.getVisibility() != View.VISIBLE){

			releaseMediaPlayer();
			if(_currentTrack == numberOfVideos-1){
				_currentTrack=0;
				playPauseStreaming();
			}else {
				++_currentTrack;
				playPauseStreaming();
			}
		}
	}

	private void playPreviousStream() {
		clearPlayerTimers();
		_pauseButton.setVisibility(View.INVISIBLE);
		TVVideoInfo.setVisibility(View.INVISIBLE);
		if(_PBStreamVideo.getVisibility() != View.VISIBLE){

			releaseMediaPlayer();

			if(_currentTrack == 0){
				_currentTrack = numberOfVideos-1;
				playPauseStreaming();

			}else{
				--_currentTrack;
				playPauseStreaming();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override	
	public void surfaceCreated(SurfaceHolder holder) {
//		startStreaming(2000);
	}

	public abstract class OnFlingGestureListener implements OnTouchListener {

		private final GestureDetector gdt = new GestureDetector(new GestureListener());

		public boolean onTouch(final View v, final MotionEvent event) {
			return gdt.onTouchEvent(event);
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_MIN_DISTANCE = 60;
			private static final int SWIPE_THRESHOLD_VELOCITY = 100;

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					onRightToLeft();
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					onLeftToRight();
					return true;
				}
				if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					onBottomToTop();
					return true;
				} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					onTopToBottom();
					return true;
				}
				return false;
			}
		}

		public abstract void onRightToLeft();

		public abstract void onLeftToRight();

		public abstract void onBottomToTop();

		public abstract void onTopToBottom();

	}



	//-------------------------------SIP related methods-----------------------------------------

	public class IncomingCallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, final Intent intent) {

			try{


				final SipAudioCall.Listener sipAudioCallListener = new SipAudioCall.Listener() {
					@Override
					public void onRinging(SipAudioCall call, SipProfile caller) {
						try {
							UTILConstants.toastMsg(getBaseContext(), "onRinging");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onCallEnded(SipAudioCall call) {
						super.onCallEnded(call);
						UTILConstants.toastMsg(getBaseContext(), "callended");
					}
				};

				//final SipAudioCall.Listener finalSIPAudioCallListener=sipAudioCallListener;
				AlertDialog.Builder builder=new AlertDialog.Builder(context);
				SipAudioCall incomingCall =ACTHomepage.this.manager.takeAudioCall(intent, sipAudioCallListener);

				final SipAudioCall finalIncomingCall = incomingCall;			
				builder.setTitle("Incoming call from "+incomingCall.getPeerProfile().getUserName());
				builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {				
						try{								 
							ACTOnSIPCall.setSipCall(finalIncomingCall);
							startActivity(new Intent(ACTHomepage.this,ACTOnSIPCall.class));
						} catch (Exception e) {
							if (finalIncomingCall != null) {
								finalIncomingCall.close();
							}
						}
					}
				});

				builder.setNegativeButton("REJECT", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(finalIncomingCall!=null){
							try {
								finalIncomingCall.endCall();
							} catch (SipException e) {
								e.printStackTrace();
							}
						}
					}
				});

				builder.show();

			}catch (Exception e) {
				// TODO: handle exception
			}
		}

	}
	public void initializeManager(Context context,Handler handler) {
		if(manager == null) {
			manager = SipManager.newInstance(context);
		}

		initializeLocalProfile(context,handler);
	}

	/**
	 * Logs you into your SIP provider, registering this device as the location to
	 * send SIP calls to for your SIP address.
	 */
	public void initializeLocalProfile(final Context context,final Handler handler) {
		if (manager == null) {
			return;
		}


		closeExistingLocalProfile();


		String username = "103";
		String sipServerDomain = "192.168.1.112";
		String password = "103";

		if (username.length() == 0 || sipServerDomain.length() == 0 || password.length() == 0) {
			//showDialog(UPDATE_SETTINGS_DIALOG);
			return;
		}

		try {
			SipProfile.Builder builder = new SipProfile.Builder(username, sipServerDomain);
			builder.setPassword(password);
			sipProfile = builder.build();

			Intent i = new Intent();
			i.setAction("android.SipDemo.INCOMING_CALL");
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, Intent.FILL_IN_DATA);
			manager.open(sipProfile, pi, null);


			// This listener must be added AFTER manager.open is called,
			// Otherwise the methods aren't guaranteed to fire.

			manager.setRegistrationListener(sipProfile.getUriString(), new SipRegistrationListener() {
				public void onRegistering(String localProfileUri) {
					UTILConstants.toastMsg(handler, context, "Registering with SIP Server...");				
				}

				public void onRegistrationDone(String localProfileUri, long expiryTime) {
					UTILConstants.toastMsg(handler, context, "SIP Registration done");	
					IntentFilter filter = new IntentFilter();
					filter.addAction("android.SipDemo.INCOMING_CALL");
					callReceiver = new IncomingCallReceiver();
					context.registerReceiver(callReceiver, filter);
				}

				public void onRegistrationFailed(String localProfileUri, int errorCode,String errorMessage) {
					UTILConstants.toastMsg(handler, context, "SIP Registration failed");					
				}
			});
		} catch (ParseException pe) {
			//updateStatus("Connection Error.");
			pe.printStackTrace();
		} catch (SipException se) {
			//updateStatus("Connection error.");
			se.printStackTrace();
		}
	}

	/**
	 * Closes out your local profile, freeing associated objects into memory
	 * and unregistering your device from the server.
	 */
	public void closeExistingLocalProfile() {
		if (sipProfile != null) {
			if (manager == null) {
				return;
			}
			try {
				if (sipProfile != null) {
					manager.close(sipProfile.getUriString());
				}
			} catch (Exception ee) {
				Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
			}
		}
	}



	//-------------time and weather---------------------------------
	private void updateDateTime() {
		DateFormatSymbols dateFormat=new DateFormatSymbols();
		Calendar calendar=Calendar.getInstance();

		int hour=calendar.get(Calendar.HOUR)==0?12:calendar.get(Calendar.HOUR);
		String hourString=(String) (new String(""+hour).length()==1? "0"+hour :""+hour);

		int minute=calendar.get(Calendar.MINUTE);
		String minuteString=(String) (new String(""+minute).length()==1? "0"+minute :""+minute);		

		int second=calendar.get(Calendar.SECOND);
		String secondString=(String) (new String(""+second).length()==1? "0"+second :""+second);

		String ampm=dateFormat.getAmPmStrings()[calendar.get(Calendar.AM_PM)];
		String date=""+calendar.get(Calendar.DAY_OF_MONTH)+" , "+dateFormat.getShortMonths()[calendar.get(Calendar.MONTH)]+"  "+dateFormat.getShortWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];

		ACTHomepage.this.timehour.setText(hourString+":");
		ACTHomepage.this.timeminute.setText(minuteString);
		ACTHomepage.this.timesecond.setText(secondString);
		ACTHomepage.this.ampm.setText(ampm);
		ACTHomepage.this.date.setText(date);
		if(timeDateTimerTask!=null){
			timeDateTimerTask.cancel();
		}
		timeDateTimerTask=null;
		timeDateTimerTask=new TimerTask() {			
			@Override
			public void run() {
				Calendar calendar=Calendar.getInstance();
				int hour=calendar.get(Calendar.HOUR)==0?12:calendar.get(Calendar.HOUR);
				String hourString=(String) (new String(""+hour).length()==1? "0"+hour :""+hour);
				int minute=calendar.get(Calendar.MINUTE);
				String minuteString=(String) (new String(""+minute).length()==1? "0"+minute :""+minute);		
				int second=calendar.get(Calendar.SECOND);
				final String secondString=(String) (new String(""+second).length()==1? "0"+second :""+second);
				handler.post(new Runnable() {

					public void run() {
						//ACTHomepage.this.time.setText(time);
						ACTHomepage.this.timesecond.setText(secondString);
					}
				});


			}
		};
		timeDateTimer.schedule(timeDateTimerTask, 0, 1000);

		if(minute%10==0) updateweather();
	}

	private void clearTimers(Timer timer){
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void clearTimerTaks(TimerTask timerTask){
		if(timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	private void updateweather(){
		updateWeather.startAnimation(refreshButtonAnimation);
		new OMWeather().tcpGetCurrentWeather(ACTHomepage.this,new Handler());
	}



	//------------menu ------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//MenuInflater mi = getMenuInflater();
		//mi.inflate(R.menu.homepage, menu);        
		return super.onCreateOptionsMenu(menu);
	}

	@Override	
	public boolean onOptionsItemSelected(MenuItem item) {
		//logout here
		return super.onOptionsItemSelected(item);
	}




	//--------homepage click listeners---------------------
	public void devicecontrol(View v){
		startActivity(new Intent(ACTHomepage.this,ACTDeviceMaster.class));
	}

	public void userdirectory(View v){
		startActivity(new Intent(ACTHomepage.this,ACTUserDirectory.class));
	}

	public void openabout(View v){
		startActivity(new Intent(ACTHomepage.this,ACTAbout.class));
	}

	public void opendevsummary(View v){
		startActivity(new Intent(ACTHomepage.this,ACTSensorControl.class));
	}

	public void playVideo(View v){
		//playVideo();
	}

	public void updateWeather(View v){
		updateweather();
		updateDateTime();
	}

	@Override
	public void onClick(View v) {
		if(_PBStreamVideo.getVisibility() != View.VISIBLE && videoUnavailableLayout.getVisibility() != View.VISIBLE){
			playPauseStreaming();
		}		
	}

	public void refreshVideoStream(View v){
		releaseMediaPlayer();
		stopStreaming();
		startStreaming(1);	
	}





}






/*public class ASYNCStreamer extends AsyncTask<MODELLiveStream, String, Boolean> {

	MODELLiveStream liveStream=null;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(_mMediaPlayer!=null) releaseMediaPlayer();				
		_PBStreamVideo.setVisibility(View.VISIBLE);
		TVVideoInfo.setVisibility(View.INVISIBLE);
		_pauseButton.setVisibility(View.INVISIBLE);
		videoUnavailableLayout.setVisibility(View.INVISIBLE);
	}

	protected Boolean doInBackground(MODELLiveStream... params) {
		try {
			liveStream=params[0];
			_mMediaPlayer = new MediaPlayer();
			_mMediaPlayer.setDisplay(_holder);
			_mMediaPlayer.setDataSource(getBaseContext(),Uri.parse(liveStream.getUrl()));
			_mMediaPlayer.prepare();
			return true;
		} catch (Exception e) {
			Log.e("MP_Error", e.toString());
			e.printStackTrace();
			return false;
		}

	}

	protected void onPostExecute(Boolean result) {
		try{
			if(result){
				TVVideoInfo.setVisibility(View.VISIBLE);
				TVVideoInfo.setText(liveStream.getName());
				_PBStreamVideo.setVisibility(View.GONE);
				_pauseButton.setVisibility(View.INVISIBLE);
				videoUnavailableLayout.setVisibility(View.INVISIBLE);
				_mMediaPlayer.setOnCompletionListener(ACTHomepage.this);
				_mMediaPlayer.start();
				_playing=true;

			}else{
				TVVideoInfo.setVisibility(View.INVISIBLE);
				_PBStreamVideo.setVisibility(View.INVISIBLE);
				cameraUnavailableInfo=liveStream.getName()+" stream unavailable";
				publishProgress(cameraUnavailableInfo);
				_pauseButton.setVisibility(View.INVISIBLE);
				ACTHomepage._playing=false;
				videoUnavailableLayout.setVisibility(View.VISIBLE);
				TVStreamUnavailable.setText(cameraUnavailableInfo);

				TimerTask playerRestartTimerTask=new TimerTask() {
					@Override
					public void run() {
						handler.post(new Runnable() {					
							@Override
							public void run() {			
								if(_currentTrack == numberOfVideos-1){
									_currentTrack=0;										
								}else {
									++_currentTrack;					
								}
								releaseMediaPlayer();
								startOrSwitchStream(1);

							}
						});				
					}
				};
				tempPlayerStartTimer=new Timer();
				tempPlayerStartTimer.schedule(playerRestartTimerTask, 5000);					
			}
			super.onPostExecute(result);
		}catch (Exception e) {				
			Log.e("MP_Error onPostExecute", e.toString());	
			e.printStackTrace();
		}
	}


}*/