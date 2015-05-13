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
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.GestureDetector;
//import android.view.GestureDetector.SimpleOnGestureListener;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextSwitcher;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ViewSwitcher.ViewFactory;
//
//import com.orvito.homevito.R;
//import com.orvito.homevito.mjpegstreamer.MjpegInputStream;
//import com.orvito.homevito.mjpegstreamer.MjpegView;
//import com.orvito.homevito.models.MODELLiveStream;
//import com.orvito.homevito.outmsgfactory.OMTabSync;
//import com.orvito.homevito.outmsgfactory.OMWeather;
//import com.orvito.homevito.utils.UTILConstants;
//
//public class MJPEGACTHomepage extends Activity implements OnClickListener{	
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
//	RelativeLayout _UILLVideoStream;
//	ProgressBar _PBStreamVideo;
//	int _currentTrack=0;
//
//	Toast swipeToast;
//
//	public static Boolean _playing=false;
//	int numberOfVideos=2;
//	int backKeyPressed=0;
//	String cameraUnavailableInfo="";
//	MjpegView mv;
//	ASYNCMJPEGStreamer _asyncmjpegStreamer;
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
//				playNextMJPEGStream();
//
//			}
//
//
//
//			@Override
//			public void onLeftToRight() {
//
//				playPreviousMJPEGStream();
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
//		new OMTabSync().syncTablet(MJPEGACTHomepage.this);//sending a tab sync request so that we have updated state of pir sensor
//		swipeToast.show();
//		_pauseButton.setVisibility(View.GONE);
//		registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
//		updateDateTime();
//		new OMWeather().tcpGetCurrentWeather(MJPEGACTHomepage.this);
//		updateDateTime();
//
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		updateDateTime();
//		startOrSwitchMJPEGStream(10000);
//		prepareSwipeToast();		
//
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		swipeToast.cancel();
//		cancelAnyExistingMJPEGPlayerTimer(true);
//		releaseMJPEGMediaPlayer();
//		backKeyPressed=0;
//	}
//
//	@Override	
//	protected void onStop() {
//		super.onStop();
//		releaseMJPEGMediaPlayer();
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
//		mv = (MjpegView) findViewById(R.id.mjpeg);	
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
//		//_UIVVVideoStream = (VideoView)findViewById(R.id.VVCamera1);
//		_PBStreamVideo = (ProgressBar)findViewById(R.id.PBStreamVideo);
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
//		startActivity(new Intent(MJPEGACTHomepage.this,ACTDeviceControl.class));
//	}
//
//	public void userdirectory(View v){
//		startActivity(new Intent(MJPEGACTHomepage.this,ACTUserDirectory.class));
//	}
//
//	public void openabout(View v){
//		startActivity(new Intent(MJPEGACTHomepage.this,ACTAbout.class));
//	}
//
//	public void opendevsummary(View v){
//		startActivity(new Intent(MJPEGACTHomepage.this,ACTDevSummary.class));
//	}
//
//	public void playVideo(View v){
//		playPauseMJPEGVideo();
//	}
//
//	public void updateWeather(View v){
//		updateweather();
//		updateDateTime();
//	}
//
//	@Override
//	public void onClick(View v) {
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE){
//			playPauseMJPEGVideo();
//		}		
//	}
//
//	public void refreshVideoStream(View v){
//		cancelAnyExistingMJPEGPlayerTimer(true);
//		releaseMJPEGMediaPlayer();
//		playPauseMJPEGVideo();
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
//		MJPEGACTHomepage.this.timehour.setText(hourString+":");
//		MJPEGACTHomepage.this.timeminute.setText(minuteString);
//		MJPEGACTHomepage.this.timesecond.setText(secondString);
//		MJPEGACTHomepage.this.ampm.setText(ampm);
//		MJPEGACTHomepage.this.date.setText(date);
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
//						MJPEGACTHomepage.this.timesecond.setText(secondString);
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
//		new OMWeather().tcpGetCurrentWeather(MJPEGACTHomepage.this);
//	}
//
//
//
//
//
//	//--------------video player methods---------------------------
//
//	public class ASYNCMJPEGStreamer extends AsyncTask<MODELLiveStream, String, MjpegInputStream> {
//
//
//		MODELLiveStream url=null;
//
//		@Override
//		protected void onPreExecute() {
//			_PBStreamVideo.setVisibility(View.VISIBLE);
//			TVVideoInfo.setVisibility(View.INVISIBLE);
//			videoUnavailableLayout.setVisibility(View.GONE);
//			super.onPreExecute();
//		}
//
//		protected MjpegInputStream doInBackground(MODELLiveStream... url) {
//			//TODO: if camera has authentication deal with it and don't just not work
//			this.url=url[0];
//			HttpResponse res = null;
//			try {
//				res = new DefaultHttpClient().execute(new HttpGet(URI.create(url[0].getUrl())));
//				if(res.getStatusLine().getStatusCode()==401) return null;
//
//				return new MjpegInputStream(res.getEntity().getContent());  
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			} 
//		}
//
//		@Override
//		protected void onProgressUpdate(String... values) {
//			Toast.makeText(getBaseContext(), values[0], 1).show();
//			super.onProgressUpdate(values);
//		}
//
//		protected void onPostExecute(MjpegInputStream result) {
//			if(result!=null){
//				TVVideoInfo.setVisibility(View.VISIBLE);
//				TVVideoInfo.setText(url.getName());
//				_PBStreamVideo.setVisibility(View.GONE);
//				_pauseButton.setVisibility(View.INVISIBLE);
//				MJPEGACTHomepage._playing=true;
//				videoUnavailableLayout.setVisibility(View.GONE);
//				
//				mv.setSource(result);
//				mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
//				mv.showFps(false);
//			}else{
//				cancelAnyExistingMJPEGPlayerTimer(false);
//				TVVideoInfo.setVisibility(View.INVISIBLE);
//				_PBStreamVideo.setVisibility(View.INVISIBLE);
//				cameraUnavailableInfo=url.getName()+" live stream unavailable";
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
//				startOrSwitchMJPEGStream(1000*refreshTimeInSec);
//			}
//			
//		}
//
//	}
//
//	private void startOrSwitchMJPEGStream(int delay) {
//
//		playerStartTimerTask=new TimerTask() {
//			@Override
//			public void run() {
//				handler.post(new Runnable() {					
//					@Override
//					public void run() {
//						releaseMJPEGMediaPlayer();					
//						playPauseMJPEGVideo();	
//						
//					}
//				});				
//			}
//		};
//		playerStartTimer=new Timer();
//		playerStartTimer.schedule(playerStartTimerTask, delay);
//	}
//
//	private void playPauseMJPEGVideo() {
//		if(_asyncmjpegStreamer==null){//case if player not yet initiated		
//			_asyncmjpegStreamer=new ASYNCMJPEGStreamer();
//			_asyncmjpegStreamer.execute(UTILConstants.getStreamAtPos(_currentTrack));
//
//		}else if(!_playing){//case if player initiated but in pause mode	
//			releaseMJPEGMediaPlayer();
//			playPauseMJPEGVideo();
//			_pauseButton.setVisibility(View.INVISIBLE);
//
//		}else{		//case if player is in play mode	
//			_pauseButton.setVisibility(View.VISIBLE);
//			mv.stopPlayback();
//			_playing=false;		
//
//		}
//	}
//
//	private void playNextMJPEGStream() {
//		cancelAnyExistingMJPEGPlayerTimer(false);
//		_pauseButton.setVisibility(View.INVISIBLE);
//		TVVideoInfo.setVisibility(View.INVISIBLE);
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE){
//
//			releaseMJPEGMediaPlayer();
//			//doCleanUp();
//			if(_currentTrack == numberOfVideos-1){
//				_currentTrack=0;
//				playPauseMJPEGVideo();
//			}else {
//				++_currentTrack;
//				playPauseMJPEGVideo();
//			}
//		}
//	}
//
//	public void cancelAnyExistingMJPEGPlayerTimer(Boolean resetCurrentTrack){
//		if(playerStartTimerTask!=null){
//			playerStartTimer.cancel();
//			playerStartTimerTask=null;
//			playerStartTimer=null;
//			//doCleanUp();
//
//			if(resetCurrentTrack){
//				if(_currentTrack == 0){
//					_currentTrack = numberOfVideos-1;
//				}else{
//					--_currentTrack;
//				}
//			}
//		}
//	}
//
//	private void playPreviousMJPEGStream() {
//
//		cancelAnyExistingMJPEGPlayerTimer(false);
//		_pauseButton.setVisibility(View.INVISIBLE);
//		TVVideoInfo.setVisibility(View.INVISIBLE);
//		if(_PBStreamVideo.getVisibility() != View.VISIBLE){
//
//			releaseMJPEGMediaPlayer();
//			//doCleanUp();
//			if(_currentTrack == 0){
//				_currentTrack = numberOfVideos-1;
//				playPauseMJPEGVideo();
//
//			}else{
//				--_currentTrack;
//				playPauseMJPEGVideo();
//			}
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
//	private void releaseMJPEGMediaPlayer() {
//		
//		if(_asyncmjpegStreamer!=null){
//			mv.forceStopPlaying();
//			_asyncmjpegStreamer.cancel(true);
//			_asyncmjpegStreamer=null;
//		}
//
//	}
//}