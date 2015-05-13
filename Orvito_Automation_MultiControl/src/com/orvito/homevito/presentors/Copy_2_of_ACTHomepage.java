package com.orvito.homevito.presentors;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.AsyncTask;
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
import android.view.SurfaceView;
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
import com.orvito.homevito.models.MODELLiveStream;
import com.orvito.homevito.outmsgfactory.OMTabSync;
import com.orvito.homevito.outmsgfactory.OMWeather;
import com.orvito.homevito.utils.UTILConstants;

public class Copy_2_of_ACTHomepage extends Activity implements OnClickListener,SurfaceHolder.Callback,OnCompletionListener{	

	TextView ampm,date,temperature,maxmin,city,weatherdesc,TVDeviceControl1,TVEmpDir1,TVDeviceControl2,TVEmpDir2,TVAppVersion,TVVideoInfo,TVStreamUnavailable;
	public TextView TVTimehour,TVTimeminute,TVTimesecond;
	TextSwitcher timehour,timeminute,timesecond;
	LinearLayout weatherLayout,videoUnavailableLayout;
	ImageView weatherImage,deviceControlImage,naImage;
	Button updateWeather,_pauseButton,videoRefresh;
	private TimerTask scrollerSchedule;
	private Timer scrollTimer =	null;
	BroadcastReceiver timeTickReceiver;
	Timer timeDateTimer,playerStartTimer,tempPlayerStartTimer,postExecuteTimer;
	TimerTask timeDateTimerTask,playerStartTimerTask;
	Handler handler;
	Animation refreshButtonAnimation;


	SurfaceView _UIVVVideoStream;
	MediaPlayer _mMediaPlayer;
	SurfaceHolder _holder;
	RelativeLayout _UILLVideoStream;
	ProgressBar _PBStreamVideo;
	int _currentTrack=0;
	ASYNCStreamer _asyncmpegStreamer;

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
		new OMTabSync().syncTablet(Copy_2_of_ACTHomepage.this,new Handler());//sending a tab sync request so that we have updated state of pir sensor
		_pauseButton.setVisibility(View.GONE);
		registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		updateDateTime();
		new OMWeather().tcpGetCurrentWeather(Copy_2_of_ACTHomepage.this,new Handler());
		updateDateTime();
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
		try{
			swipeToast.cancel();
			if(postExecuteTimer!=null){
				postExecuteTimer.cancel();
				postExecuteTimer=null;
				
			}
			_UIVVVideoStream.postInvalidate();
			_mMediaPlayer=null;

			if(_asyncmpegStreamer!=null){
				_asyncmpegStreamer.cancel(true);
				_asyncmpegStreamer=null;
			}
			clearPlayerTimers();	
			backKeyPressed=0;
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override	
	protected void onStop() {
		super.onStop();
		//releaseMediaPlayer();
		unregisterReceiver(timeTickReceiver);
	}

	protected void onRestart() {
//		if( !_playing){//case if player not yet initiated	
//			_playing=true;
//		}
		_mMediaPlayer=null;
		super.onRestart();
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
		//mv = (MjpegView) findViewById(R.id.mjpeg);	
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

		/*horizontalScrollview  = (HorizontalScrollView) findViewById(R.id.horiztonal_scrollview_id);
		horizontalOuterLayout =	(LinearLayout)findViewById(R.id.horiztonal_outer_layout_id);
		horizontalScrollview.setHorizontalScrollBarEnabled(false);
		time.setTextColor(android.R.color.white);
		ampm.setTextColor(android.R.color.white);
		date.setTextColor(android.R.color.white);
		temperature.setTextColor(android.R.color.white);
		maxmin.setTextColor(android.R.color.white);
		city.setTextColor(android.R.color.white);*/

		_UILLVideoStream = (RelativeLayout)findViewById(R.id.LLCamOne);
		_UILLVideoStream.setOnClickListener(this);
		_UIVVVideoStream = (SurfaceView)findViewById(R.id.VVCamera1);
		_holder=_UIVVVideoStream.getHolder();
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
		startActivity(new Intent(Copy_2_of_ACTHomepage.this,ACTDeviceMaster.class));
	}

	public void userdirectory(View v){
		startActivity(new Intent(Copy_2_of_ACTHomepage.this,ACTUserDirectory.class));
	}

	public void openabout(View v){
		//		startActivity(new Intent(ACTHomepage.this,ACTAbout.class));
	}

	public void opendevsummary(View v){
		//		startActivity(new Intent(ACTHomepage.this,ACTSensorControl.class));
	}

	public void playVideo(View v){
		playPauseVideo();
	}

	public void updateWeather(View v){
		updateweather();
		updateDateTime();
	}

	@Override
	public void onClick(View v) {
		if(_PBStreamVideo.getVisibility() != View.VISIBLE && videoUnavailableLayout.getVisibility() != View.VISIBLE){
			playPauseVideo();
		}		
	}

	public void refreshVideoStream(View v){
		releaseMediaPlayer();
		playPauseVideo();		
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

		Copy_2_of_ACTHomepage.this.timehour.setText(hourString+":");
		Copy_2_of_ACTHomepage.this.timeminute.setText(minuteString);
		Copy_2_of_ACTHomepage.this.timesecond.setText(secondString);
		Copy_2_of_ACTHomepage.this.ampm.setText(ampm);
		Copy_2_of_ACTHomepage.this.date.setText(date);
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
						Copy_2_of_ACTHomepage.this.timesecond.setText(secondString);
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
		new OMWeather().tcpGetCurrentWeather(Copy_2_of_ACTHomepage.this,new Handler());
	}



	//--------------video player methods---------------------------

	private void startOrSwitchStream(int delay) {
		playerStartTimerTask=new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {					
					@Override
					public void run() {			
						playPauseVideo();
					}
				});				
			}
		};
		playerStartTimer=new Timer();
		playerStartTimer.schedule(playerStartTimerTask, delay);
	}

	private void playPauseVideo() {
		if(_mMediaPlayer==null){//case if player not yet initiated
			if(_asyncmpegStreamer==null){
				_asyncmpegStreamer=new ASYNCStreamer();
			}else{
				_asyncmpegStreamer.cancel(true);
				_asyncmpegStreamer=null;
				_asyncmpegStreamer=new ASYNCStreamer();
			}

			_asyncmpegStreamer.execute(UTILConstants.getStreamAtPos(_currentTrack));
			//prepAndStartVideoStreamingAsyncTask(UTILConstants.getStreamAtPos(_currentTrack));

		}else if(!_playing){//case if player initiated but in pause mode		
			_mMediaPlayer.start();
			_playing=true;
			_pauseButton.setVisibility(View.INVISIBLE);

		}else if(_playing){				//case if player is in play mode	
			_pauseButton.setVisibility(View.VISIBLE);
			_mMediaPlayer.pause();
			_playing=false; 

		}
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

	public class ASYNCStreamer extends AsyncTask<MODELLiveStream, String, MODELLiveStream> {

		MODELLiveStream liveStream=null;
		long temp;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(_mMediaPlayer!=null) releaseMediaPlayer();				
			_PBStreamVideo.setVisibility(View.VISIBLE);
			TVVideoInfo.setVisibility(View.INVISIBLE);
			_pauseButton.setVisibility(View.INVISIBLE);
			videoUnavailableLayout.setVisibility(View.INVISIBLE);
		}

		protected MODELLiveStream doInBackground(MODELLiveStream... params) {
			return params[0];
		}

		protected void onPostExecute(final MODELLiveStream result) {
			TimerTask timerTask=new TimerTask() {
				@Override
				public void run() {
					try {
						liveStream=result;
						_mMediaPlayer = new MediaPlayer();
						_mMediaPlayer.setDataSource(getBaseContext(),Uri.parse(liveStream.getUrl()));
						_mMediaPlayer.setDisplay(_holder);
						temp=System.currentTimeMillis();
						//Log.e("MPTEST", "before prepare"+temp);

						_mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {					
							@Override
							public void onPrepared(MediaPlayer mp) {
								Copy_2_of_ACTHomepage.this.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										try{
											TVVideoInfo.setVisibility(View.VISIBLE);
											TVVideoInfo.setText(liveStream.getName());
											_PBStreamVideo.setVisibility(View.GONE);
											_pauseButton.setVisibility(View.INVISIBLE);
											videoUnavailableLayout.setVisibility(View.INVISIBLE);
											if(_mMediaPlayer!=null){
												_mMediaPlayer.setOnCompletionListener(Copy_2_of_ACTHomepage.this);
											}
											//_mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
											_mMediaPlayer.start();
											_playing=true;
										}catch (Exception e) {
											// TODO: handle exception
										}

									}
								});


							}
						});

						_mMediaPlayer.prepare();
						Copy_2_of_ACTHomepage.this.runOnUiThread(new Runnable() {							
							@Override
							public void run() {

								TVVideoInfo.setVisibility(View.VISIBLE);
								TVVideoInfo.setText(liveStream.getName());
								_PBStreamVideo.setVisibility(View.GONE);
								_pauseButton.setVisibility(View.INVISIBLE);
								videoUnavailableLayout.setVisibility(View.INVISIBLE);
								if(_mMediaPlayer!=null){
									_mMediaPlayer.setOnCompletionListener(Copy_2_of_ACTHomepage.this);
									_mMediaPlayer.start();
								}
								//_mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
								
								_playing=true;
							}
						});



					} catch (final IOException e) {
						Copy_2_of_ACTHomepage.this.runOnUiThread(new Runnable() {
							public void run() {
								Log.e("MP_Error", e.toString());			
								TVVideoInfo.setVisibility(View.INVISIBLE);
								_PBStreamVideo.setVisibility(View.INVISIBLE);
								_pauseButton.setVisibility(View.GONE);
								cameraUnavailableInfo=liveStream.getName()+" stream unavailable";
								publishProgress(cameraUnavailableInfo);
								Copy_2_of_ACTHomepage._playing=false;
								videoUnavailableLayout.setVisibility(View.VISIBLE);
								TVStreamUnavailable.setText(cameraUnavailableInfo);

								TimerTask tempStartTimerTask=new TimerTask() {
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
								if(postExecuteTimer!=null)	postExecuteTimer.cancel();
								tempPlayerStartTimer=new Timer();
								tempPlayerStartTimer.schedule(tempStartTimerTask, 3000);
							}
						});
					}catch (final IllegalStateException e) {

						Copy_2_of_ACTHomepage.this.runOnUiThread(new Runnable() {
							public void run() {
								Log.e("MP_Error", e.toString());			
								TVVideoInfo.setVisibility(View.INVISIBLE);
								_PBStreamVideo.setVisibility(View.INVISIBLE);
								_pauseButton.setVisibility(View.GONE);
								//cameraUnavailableInfo=liveStream.getName()+" stream unavailable";
								cameraUnavailableInfo="MediaPlayer error";
								publishProgress(cameraUnavailableInfo);
								Copy_2_of_ACTHomepage._playing=false;
								videoUnavailableLayout.setVisibility(View.VISIBLE);
								TVStreamUnavailable.setText(cameraUnavailableInfo);

								TimerTask tempStartTimerTask=new TimerTask() {
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
								if(postExecuteTimer!=null)	postExecuteTimer.cancel();
								tempPlayerStartTimer=new Timer();
								tempPlayerStartTimer.schedule(tempStartTimerTask, 3000);
							}
						});


					}

				}
			};

			postExecuteTimer=new Timer();
			postExecuteTimer.schedule(timerTask, 1);
		}
	}



	@Override
	public void onCompletion(MediaPlayer mp) {
//		cameraUnavailableInfo=UTILConstants.getStreamAtPos(_currentTrack).getName()+" stream has ended";
//		videoUnavailableLayout.setVisibility(View.VISIBLE);
//		TVStreamUnavailable.setText(cameraUnavailableInfo);
		_pauseButton.setVisibility(View.GONE);
		if(_currentTrack == numberOfVideos-1){
			_currentTrack=0;										
		}else {
			++_currentTrack;					
		}
		releaseMediaPlayer();
		UTILConstants.toastMsg(handler, getBaseContext(), "lost stream..retrying...");
		startOrSwitchStream(1000);
	}

	void clearPlayerTimers(){
		if(playerStartTimer!=null){
			playerStartTimer.cancel();
			playerStartTimer=null;
		}
		if(tempPlayerStartTimer!=null) {
			tempPlayerStartTimer.cancel();
			tempPlayerStartTimer=null;
		}
	}



	private void releaseMediaPlayer() {


		try{
			if (_mMediaPlayer != null) {
				_mMediaPlayer.reset();				
				_mMediaPlayer.release();
				_mMediaPlayer = null;
			}

			//			if(postExecuteTimer!=null){
			//				postExecuteTimer.cancel();
			//				postExecuteTimer=null;
			//				_mMediaPlayer=null;
			//			}
			if(_asyncmpegStreamer!=null){
				_asyncmpegStreamer.cancel(true);
				_asyncmpegStreamer=null;
			}

		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	private void playNextStream() {
		clearPlayerTimers();
		_pauseButton.setVisibility(View.INVISIBLE);
		TVVideoInfo.setVisibility(View.INVISIBLE);
		if(_PBStreamVideo.getVisibility() != View.VISIBLE){

			releaseMediaPlayer();
			//doCleanUp();
			if(_currentTrack == numberOfVideos-1){
				_currentTrack=0;
				playPauseVideo();
			}else {
				++_currentTrack;
				playPauseVideo();
			}
		}
	}

	private void playPreviousStream() {
		clearPlayerTimers();
		_pauseButton.setVisibility(View.INVISIBLE);
		TVVideoInfo.setVisibility(View.INVISIBLE);
		if(_PBStreamVideo.getVisibility() != View.VISIBLE){

			releaseMediaPlayer();
			//doCleanUp();
			if(_currentTrack == 0){
				_currentTrack = numberOfVideos-1;
				playPauseVideo();

			}else{
				--_currentTrack;
				playPauseVideo();
			}
		}
	}



	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startOrSwitchStream(1);
	}



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
				SipAudioCall incomingCall =Copy_2_of_ACTHomepage.this.manager.takeAudioCall(intent, sipAudioCallListener);

				final SipAudioCall finalIncomingCall = incomingCall;			
				builder.setTitle("Incoming call from "+incomingCall.getPeerProfile().getUserName());
				builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {				
						try{								 
							ACTOnSIPCall.setSipCall(finalIncomingCall);
							startActivity(new Intent(Copy_2_of_ACTHomepage.this,ACTOnSIPCall.class));
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



}