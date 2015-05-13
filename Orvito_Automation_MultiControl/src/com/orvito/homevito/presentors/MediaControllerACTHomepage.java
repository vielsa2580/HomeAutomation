//package com.orvito.homevito.presentors;
//
//import java.net.URI;
//import java.text.DateFormatSymbols;
//import java.util.Calendar;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.GestureDetector.SimpleOnGestureListener;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.MediaController;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextSwitcher;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//import android.widget.ViewSwitcher.ViewFactory;
//
//import com.orvito.homevito.R;
//import com.orvito.homevito.mjpegstreamer.MjpegInputStream;
//import com.orvito.homevito.models.MODELLiveStream;
//import com.orvito.homevito.outmsgfactory.OMTabSync;
//import com.orvito.homevito.outmsgfactory.OMWeather;
//import com.orvito.homevito.utils.UTILConstants;
//
//public class MediaControllerACTHomepage extends Activity implements OnClickListener, OnCompletionListener{	
//
//	TextView ampm,date,temperature,maxmin,city,weatherdesc,TVDeviceControl1,TVEmpDir1,TVDeviceControl2,TVEmpDir2,TVAppVersion,TVVideoInfo,TVStreamUnavailable;
//	TextSwitcher timehour,timeminute,timesecond;
//	LinearLayout weatherLayout,videoUnavailableLayout;
//	ImageView weatherImage,deviceControlImage,naImage;
//	Button updateWeather,_pauseButton,videoRefresh;
//	private TimerTask scrollerSchedule;
//	private Timer scrollTimer =	null;
//	BroadcastReceiver timeTickReceiver;
//	Timer timeDateTimer,playerStartTimer;
//	TimerTask timeDateTimerTask,playerStartTimerTask;
//	Handler handler;
//	Animation refreshButtonAnimation;
//
//
//	VideoView _UIVVVideoStream;
//	MediaController _mMediaController;
//	SurfaceHolder _holder;
//	RelativeLayout _UILLVideoStream;
//	ProgressBar _PBStreamVideo;
//	int _currentTrack=0;
//	ASYNCStreamer _asyncmpegStreamer;
//
//	Toast swipeToast;
//
//	public static Boolean _playing=false;
//	int numberOfVideos=2;
//	int backKeyPressed=0;
//	String cameraUnavailableInfo="";
//
//	int refreshTimeInSec=5;
//
//
//
//	//-----------------activity lifecycle methods---------------------------
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);		
//		setContentView();
//		try {
//			TVAppVersion.setText("V "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		timeTickReceiver=new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				updateDateTime();
//			}
//		};
//
//		timeDateTimer=new Timer();
//		handler=new Handler();
//
//
//
//		_UILLVideoStream.setOnTouchListener(new OnFlingGestureListener() {
//
//			@Override
//			public void onTopToBottom() {
//				//Your code here
//			}
//
//			@Override
//			public void onRightToLeft() {
//				playNextStream();
//
//			}
//
//
//
//			@Override
//			public void onLeftToRight() {
//
//				playPreviousStream();
//
//			}
//
//
//
//			@Override
//			public void onBottomToTop() {
//				//Your code here
//			}
//		});
//
//		Calendar calendar=Calendar.getInstance();
//		if(calendar.get(Calendar.HOUR_OF_DAY)>=6 && calendar.get(Calendar.HOUR_OF_DAY)<19){
//			weatherLayout.setBackgroundResource(R.drawable.daynabg);
//			naImage.setBackgroundResource(R.drawable.dayna);
//		}else{
//			weatherLayout.setBackgroundResource(R.drawable.nightnabg);
//			naImage.setBackgroundResource(R.drawable.nightna);	
//		}
//
//		prepareSwipeToast();
//	}
//
//	@Override
//	protected void onStart() {
//		super.onStart();
//		new OMTabSync().syncTablet(MediaControllerACTHomepage.this);//sending a tab sync request so that we have updated state of pir sensor
//		swipeToast.show();
//		_pauseButton.setVisibility(View.GONE);
//		registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
//		updateDateTime();
//		new OMWeather().tcpGetCurrentWeather(MediaControllerACTHomepage.this);
//		updateDateTime();
//
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		updateDateTime();
//		startOrSwitchStream(500);
//		prepareSwipeToast();		
//
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		swipeToast.cancel();
//		releaseMediaPlayer();
//		backKeyPressed=0;
//	}
//
//	@Override	
//	protected void onStop() {
//		super.onStop();
//		releaseMediaPlayer();
//		unregisterReceiver(timeTickReceiver);
//	}
//
//	protected void onRestart() {
//		if( !_playing){//case if player not yet initiated				
//
//			_playing=true;
//		}
//		super.onRestart();
//	}
//
//	protected void onDestroy() {
//		super.onDestroy();
//		clearTimerTaks(scrollerSchedule);		
//		clearTimers(scrollTimer);
//
//	}
//
//	private void setContentView() {
//		setContentView(R.layout.act_homepage);
//		timehour=(TextSwitcher) findViewById(R.id.timehour);
//		timeminute=(TextSwitcher) findViewById(R.id.timeminute);
//		ampm=(TextView) findViewById(R.id.ampm);
//		date=(TextView) findViewById(R.id.date);
//		temperature=(TextView) findViewById(R.id.temperature);
//		maxmin=(TextView) findViewById(R.id.maxmintemp);
//		city=(TextView) findViewById(R.id.city);
//		weatherdesc=(TextView) findViewById(R.id.weatherdesc);
//		timesecond=(TextSwitcher) findViewById(R.id.timesecond);
//		weatherLayout=(LinearLayout) findViewById(R.id.weatherlayout);
//		weatherImage=(ImageView) findViewById(R.id.weatherimage);
//		naImage=(ImageView) findViewById(R.id.na);
//		TVDeviceControl1=(TextView) findViewById(R.id.TVDeviceControl1);
//		TVVideoInfo=(TextView) findViewById(R.id.videoinfo);
//		TVEmpDir1=(TextView) findViewById(R.id.TVEmpDir1);
//		TVDeviceControl2=(TextView) findViewById(R.id.TVDeviceControl2);
//		TVAppVersion=(TextView) findViewById(R.id.version);
//		TVEmpDir2=(TextView) findViewById(R.id.TVEmpDir2);
//		updateWeather=(Button) findViewById(R.id.updateweather);
//		_pauseButton=(Button) findViewById(R.id.pausebutton);
//		TVStreamUnavailable=(TextView) findViewById(R.id.streamUnavailableText);
//		//mv = (MjpegView) findViewById(R.id.mjpeg);	
//		videoUnavailableLayout=(LinearLayout) findViewById(R.id.videounavilablelayout);
//		refreshButtonAnimation = AnimationUtils.loadAnimation(this,R.anim.spin);
//		Typeface tf1=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
//		Typeface tf2=Typeface.createFromAsset(getAssets(),"segoeuil.ttf");
//		//time.setTypeface(tf1);
//		ampm.setTypeface(tf1);
//		date.setTypeface(tf1);
//
//		temperature.setTypeface(tf1);
//		maxmin.setTypeface(tf1);
//		city.setTypeface(tf1);
//		weatherdesc.setTypeface(tf1);
//		//timesecond.setTypeface(tf1);
//
//		TVDeviceControl1.setTypeface(tf2);
//		TVEmpDir1.setTypeface(tf2);
//		TVDeviceControl2.setTypeface(tf2);
//		TVEmpDir2.setTypeface(tf2);
//		timehour.setFactory(new ViewFactory() {
//
//			public View makeView() {
//				TextView t = new TextView(getBaseContext());
//				t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//				t.setTextSize(40);
//				t.setTextColor(Color.WHITE);
//				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
//				t.setTypeface(tf);
//				return t;
//
//			}
//		});
//
//		timeminute.setFactory(new ViewFactory() {
//
//			public View makeView() {
//				TextView t = new TextView(getBaseContext());
//				t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//				t.setTextSize(40);
//				t.setTextColor(Color.WHITE);
//				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
//				t.setTypeface(tf);
//				return t;
//
//			}
//		});
//
//		timesecond.setFactory(new ViewFactory() {
//			public View makeView() {
//
//				TextView t = new TextView(getBaseContext());
//				t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//				t.setTextSize(20);
//				t.setTextColor(Color.WHITE);
//				Typeface tf=Typeface.createFromAsset(getAssets(),"HelveticaNeueLTCom-Th.ttf");
//				t.setTypeface(tf);
//				return t;
//
//			}
//		});
//
//
//		Animation in = AnimationUtils.loadAnimation(this,R.anim.slide_in_top);
//		in.setInterpolator(this, android.R.anim.accelerate_interpolator);
//		Animation out = AnimationUtils.loadAnimation(this,R.anim.slide_out_bottom);
//
//		Animation in2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
//		in2.setInterpolator(this, android.R.anim.decelerate_interpolator);
//		Animation out2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
//
//		timehour.setInAnimation(in2);
//		timehour.setOutAnimation(out2);
//		timeminute.setInAnimation(in);
//		timeminute.setOutAnimation(out);
//		timesecond.setInAnimation(in);
//		timesecond.setOutAnimation(out);
//
//		/*horizontalScrollview  = (HorizontalScrollView) findViewById(R.id.horiztonal_scrollview_id);
//		horizontalOuterLayout =	(LinearLayout)findViewById(R.id.horiztonal_outer_layout_id);
//		horizontalScrollview.setHorizontalScrollBarEnabled(false);
//		time.setTextColor(android.R.color.white);
//		ampm.setTextColor(android.R.color.white);
//		date.setTextColor(android.R.color.white);
//		temperature.setTextColor(android.R.color.white);
//		maxmin.setTextColor(android.R.color.white);
//		city.setTextColor(android.R.color.white);*/
//
//		_UILLVideoStream = (RelativeLayout)findViewById(R.id.LLCamOne);
//		_UILLVideoStream.setOnClickListener(this);
//		_UIVVVideoStream = (VideoView)findViewById(R.id.VVCamera1);
//		
//		//getHolder();
//		_PBStreamVideo = (ProgressBar)findViewById(R.id.PBStreamVideo);
//		
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(keyCode==KeyEvent.KEYCODE_BACK){
//			if(backKeyPressed==0){
//				Toast.makeText(getBaseContext(), "Press again to logout", 1).show();
//				backKeyPressed=1;
//				return true;
//			}
//		}	
//		return super.onKeyDown(keyCode, event);
//	}
//
//	private void prepareSwipeToast() {
//		swipeToast=new Toast(getBaseContext());
//		ImageView view=new ImageView(getBaseContext());
//		view.setBackgroundResource(R.drawable.swipe);
//		swipeToast.setView(view);
//		swipeToast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 50, 50);
//		swipeToast.setDuration(Toast.LENGTH_LONG);
//	}
//
//
//
//	//------------menu ------------------
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		//MenuInflater mi = getMenuInflater();
//		//mi.inflate(R.menu.homepage, menu);        
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override	
//	public boolean onOptionsItemSelected(MenuItem item) {
//		//logout here
//		return super.onOptionsItemSelected(item);
//	}
//
//
//
//
//	//--------homepage click listeners---------------------
//	public void devicecontrol(View v){
//		startActivity(new Intent(MediaControllerACTHomepage.this,ACTDeviceControl.class));
//	}
//
//	public void userdirectory(View v){
//		startActivity(new Intent(MediaControllerACTHomepage.this,ACTUserDirectory.class));
//	}
//
//	public void openabout(View v){
//		startActivity(new Intent(MediaControllerACTHomepage.this,ACTAbout.class));
//	}
//
//	public void opendevsummary(View v){
//		startActivity(new Intent(MediaControllerACTHomepage.this,ACTDevSummary.class));
//	}
//
//	public void playVideo(View v){
//		playPauseVideo();
//	}
//
//	public void updateWeather(View v){
//		updateweather();
//		updateDateTime();
//	}
//
//	@Override
//	public void onClick(View v) {
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE && videoUnavailableLayout.getVisibility() != View.VISIBLE){
//			playPauseVideo();
//		}		
//	}
//
//	public void refreshVideoStream(View v){
//
//		releaseMediaPlayer();
//		playPauseVideo();
//	}
//
//
//
//
//	//-------------time and weather---------------------------------
//	private void updateDateTime() {
//		DateFormatSymbols dateFormat=new DateFormatSymbols();
//		Calendar calendar=Calendar.getInstance();
//
//		int hour=calendar.get(Calendar.HOUR)==0?12:calendar.get(Calendar.HOUR);
//		String hourString=(String) (new String(""+hour).length()==1? "0"+hour :""+hour);
//
//		int minute=calendar.get(Calendar.MINUTE);
//		String minuteString=(String) (new String(""+minute).length()==1? "0"+minute :""+minute);		
//
//		int second=calendar.get(Calendar.SECOND);
//		String secondString=(String) (new String(""+second).length()==1? "0"+second :""+second);
//
//		String ampm=dateFormat.getAmPmStrings()[calendar.get(Calendar.AM_PM)];
//		String date=""+calendar.get(Calendar.DAY_OF_MONTH)+" , "+dateFormat.getShortMonths()[calendar.get(Calendar.MONTH)]+"  "+dateFormat.getShortWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
//
//		MediaControllerACTHomepage.this.timehour.setText(hourString+":");
//		MediaControllerACTHomepage.this.timeminute.setText(minuteString);
//		MediaControllerACTHomepage.this.timesecond.setText(secondString);
//		MediaControllerACTHomepage.this.ampm.setText(ampm);
//		MediaControllerACTHomepage.this.date.setText(date);
//		if(timeDateTimerTask!=null){
//			timeDateTimerTask.cancel();
//		}
//		timeDateTimerTask=null;
//		timeDateTimerTask=new TimerTask() {			
//			@Override
//			public void run() {
//				Calendar calendar=Calendar.getInstance();
//				int hour=calendar.get(Calendar.HOUR)==0?12:calendar.get(Calendar.HOUR);
//				String hourString=(String) (new String(""+hour).length()==1? "0"+hour :""+hour);
//				int minute=calendar.get(Calendar.MINUTE);
//				String minuteString=(String) (new String(""+minute).length()==1? "0"+minute :""+minute);		
//				int second=calendar.get(Calendar.SECOND);
//				final String secondString=(String) (new String(""+second).length()==1? "0"+second :""+second);
//				handler.post(new Runnable() {
//
//					public void run() {
//						//ACTHomepage.this.time.setText(time);
//						MediaControllerACTHomepage.this.timesecond.setText(secondString);
//					}
//				});
//
//
//			}
//		};
//		timeDateTimer.schedule(timeDateTimerTask, 0, 1000);
//
//		if(minute%10==0) updateweather();
//	}
//
//	private void clearTimers(Timer timer){
//		if(timer != null) {
//			timer.cancel();
//			timer = null;
//		}
//	}
//
//	private void clearTimerTaks(TimerTask timerTask){
//		if(timerTask != null) {
//			timerTask.cancel();
//			timerTask = null;
//		}
//	}
//
//	private void updateweather(){
//		updateWeather.startAnimation(refreshButtonAnimation);
//		new OMWeather().tcpGetCurrentWeather(MediaControllerACTHomepage.this);
//	}
//
//
//
//
//
//	//--------------video player methods---------------------------
//
//	private void startOrSwitchStream(int delay) {
//
//		playerStartTimerTask=new TimerTask() {
//			@Override
//			public void run() {
//				handler.post(new Runnable() {					
//					@Override
//					public void run() {			
//						playPauseVideo();	
//						
//					}
//				});				
//			}
//		};
//		playerStartTimer=new Timer();
//		playerStartTimer.schedule(playerStartTimerTask, delay);
//	}
//	
//	
//	private void playPauseVideo() {		
//		
//		
//		if(_mMediaController==null){//case if player not yet initiated
//			
//		
//			if(_asyncmpegStreamer==null){	
//				_asyncmpegStreamer=new ASYNCStreamer();
//			}
//			_asyncmpegStreamer.execute(UTILConstants.getStreamAtPos(_currentTrack));
//			//prepAndStartVideoStreamingAsyncTask(UTILConstants.getStreamAtPos(_currentTrack));
//
//		}else if(!_playing){//case if player initiated but in pause mode		
//			_UIVVVideoStream.start();
//			_playing=true;
//			_pauseButton.setVisibility(View.INVISIBLE);
//
//		}else{				//case if player is in play mode	
//			_pauseButton.setVisibility(View.VISIBLE);
//			_UIVVVideoStream.pause();
//			_playing=false; 
//
//		}
//	}
//
//	public abstract class OnFlingGestureListener implements OnTouchListener {
//
//		private final GestureDetector gdt = new GestureDetector(new GestureListener());
//
//		public boolean onTouch(final View v, final MotionEvent event) {
//			return gdt.onTouchEvent(event);
//		}
//
//		private final class GestureListener extends SimpleOnGestureListener {
//
//			private static final int SWIPE_MIN_DISTANCE = 60;
//			private static final int SWIPE_THRESHOLD_VELOCITY = 100;
//
//			@Override
//			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//					onRightToLeft();
//					return true;
//				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//					onLeftToRight();
//					return true;
//				}
//				if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//					onBottomToTop();
//					return true;
//				} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//					onTopToBottom();
//					return true;
//				}
//				return false;
//			}
//		}
//
//		public abstract void onRightToLeft();
//
//		public abstract void onLeftToRight();
//
//		public abstract void onBottomToTop();
//
//		public abstract void onTopToBottom();
//
//	}
//
//
//	public class ASYNCStreamer extends AsyncTask<MODELLiveStream, String, Boolean> {
//
//		MODELLiveStream liveStream=null;
//
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			if(_mMediaController!=null) releaseMediaPlayer();				
//			_PBStreamVideo.setVisibility(View.VISIBLE);
//			TVVideoInfo.setVisibility(View.INVISIBLE);
//			_pauseButton.setVisibility(View.INVISIBLE);
//		}
//
//		protected Boolean doInBackground(MODELLiveStream... params) {
//
//			try {
//				liveStream=params[0];
//				
//				//_mMediaPlayer.setDisplay(_holder);
//				//_mMediaPlayer.prepare();
//				return true;
//			} catch (Exception e) {
//				Log.e("error: ", e.getMessage());
//				//Toast.makeText(getBaseContext(), ""+e.toString(), Toast.LENGTH_LONG).show();
//				return false;
//			}
//
//		}
//
//		protected void onPostExecute(Boolean result) {
//
//			if(result){
//				_mMediaController = new MediaController(MediaControllerACTHomepage.this);
//				_mMediaController.setAnchorView(_UIVVVideoStream);
//				_UIVVVideoStream.setMediaController(_mMediaController);
//				_UIVVVideoStream.setVideoPath(liveStream.getUrl());
//				
//				
//				TVVideoInfo.setVisibility(View.VISIBLE);
//				TVVideoInfo.setText(liveStream.getName());
//				_PBStreamVideo.setVisibility(View.GONE);
//				_pauseButton.setVisibility(View.INVISIBLE);
//				//_mMediaPlayer.setOnCompletionListener(ACTHomepage.this);
//				//_mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				_UIVVVideoStream.start();
//				_playing=true;
//
//			}else{
//				TVVideoInfo.setVisibility(View.INVISIBLE);
//				_PBStreamVideo.setVisibility(View.INVISIBLE);
//				cameraUnavailableInfo=liveStream.getName()+" live stream unavailable";
//				//publishProgress(cameraUnavailableInfo);
//				_pauseButton.setVisibility(View.INVISIBLE);
//				MJPEGACTHomepage._playing=false;
//				videoUnavailableLayout.setVisibility(View.VISIBLE);
//				TVStreamUnavailable.setText(cameraUnavailableInfo);
//
//				if(_currentTrack == numberOfVideos-1){
//					_currentTrack=0;										
//				}else {
//					++_currentTrack;					
//				}
//			}
//			super.onPostExecute(result);
//		}	
//	}
//
//
//
//	public void onCompletion(MediaPlayer arg0) {
//		playNextStream();
//
//	}
//
//	private void releaseMediaPlayer() {
//		if (_mMediaController != null) {
//			_mMediaController = null;
//		}
//		if(_asyncmpegStreamer!=null){
//			_asyncmpegStreamer.cancel(true);
//			_asyncmpegStreamer=null;
//		}
//	}
//
//	private void playNextStream() {
//
//		_pauseButton.setVisibility(View.INVISIBLE);
//		TVVideoInfo.setVisibility(View.INVISIBLE);
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE){
//
//			releaseMediaPlayer();
//			//doCleanUp();
//			if(_currentTrack == numberOfVideos-1){
//				_currentTrack=0;
//				playPauseVideo();
//			}else {
//				++_currentTrack;
//				playPauseVideo();
//			}
//		}
//	}
//
//	private void playPreviousStream() {
//
//		_pauseButton.setVisibility(View.INVISIBLE);
//		TVVideoInfo.setVisibility(View.INVISIBLE);
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE){
//
//			releaseMediaPlayer();
//			//doCleanUp();
//			if(_currentTrack == 0){
//				_currentTrack = numberOfVideos-1;
//				playPauseVideo();
//
//			}else{
//				--_currentTrack;
//				playPauseVideo();
//			}
//		}
//	}
//}