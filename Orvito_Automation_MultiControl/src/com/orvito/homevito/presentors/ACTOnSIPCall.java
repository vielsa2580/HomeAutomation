package com.orvito.homevito.presentors;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELLiveStream;
import com.orvito.homevito.utils.UTILConstants;

public class ACTOnSIPCall extends Activity implements OnClickListener, OnCompletionListener,SurfaceHolder.Callback{	

	TextView TVVideoInfo,TVStreamUnavailable,sipCallStatus;
	LinearLayout videoUnavailableLayout;
	Button _pauseButton,videoRefresh,BTHoldCall,BTMuteCall,BTEndCall;

	Timer playerStartTimer;
	TimerTask playerStartTimerTask;
	Handler handler;
	Animation refreshButtonAnimation;

	SurfaceView _UIVVVideoStream;
	MediaPlayer _mMediaPlayer;
	SurfaceHolder _holder;
	RelativeLayout _UILLVideoStream;
	ProgressBar _PBStreamVideo;

	ASYNCStreamer _asyncmpegStreamer;

	public static Boolean _playing=false;
	String cameraUnavailableInfo="";
	int refreshTimeInSec=5;

	public static SipAudioCall sipCall = null;
	public static SipAudioCall getSipCall() {
		return sipCall;
	}
	public static void setSipCall(SipAudioCall sipCall) {
		ACTOnSIPCall.sipCall = sipCall;
	}



	//-----------------activity lifecycle methods---------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView();
		handler=new Handler();
		try{
			if(sipCall!=null){
				sipCall.answerCall(30);
				sipCall.startAudio();
				sipCall.setSpeakerMode(true);
				updateStatus("On Call");
				
			}
		}catch (Exception e) {
			e.printStackTrace();
			UTILConstants.toastMsg(getBaseContext(), e.toString());
		}
		
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		_pauseButton.setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaPlayer();

	}

	protected void onRestart() {
		if( !_playing){//case if player not yet initiated	
			_playing=true;
		}
		super.onRestart();
	}

	private void setContentView() {
		setContentView(R.layout.act_onsipcall);
		TVVideoInfo=(TextView) findViewById(R.id.videoinfo);
		_pauseButton=(Button) findViewById(R.id.pausebutton);
		TVStreamUnavailable=(TextView) findViewById(R.id.streamUnavailableText);
		sipCallStatus=(TextView) findViewById(R.id.sipCallStatus);
		videoUnavailableLayout=(LinearLayout) findViewById(R.id.videounavilablelayout);
		refreshButtonAnimation = AnimationUtils.loadAnimation(this,R.anim.spin);
		_UILLVideoStream = (RelativeLayout)findViewById(R.id.LLCamOne);
		_UILLVideoStream.setOnClickListener(this);
		_UIVVVideoStream = (SurfaceView)findViewById(R.id.VVCamera1);
		_holder=_UIVVVideoStream.getHolder();
		_holder.addCallback(this);
		_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		_PBStreamVideo = (ProgressBar)findViewById(R.id.PBStreamVideo);
		BTHoldCall=(Button) findViewById(R.id.holdcall);
		BTMuteCall=(Button) findViewById(R.id.mutecall);
		BTEndCall=(Button) findViewById(R.id.endcall);
	}


	//--------click listeners---------------------

	public void playVideo(View v){
		playPauseVideo();
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
			}
			_asyncmpegStreamer.execute(UTILConstants.getStreamAtPos(-1));
			//prepAndStartVideoStreamingAsyncTask(UTILConstants.getStreamAtPos(_currentTrack));

		}else if(!_playing){//case if player initiated but in pause mode		
			_mMediaPlayer.start();
			_playing=true;
			_pauseButton.setVisibility(View.INVISIBLE);

		}else{				//case if player is in play mode	
			_pauseButton.setVisibility(View.VISIBLE);
			_mMediaPlayer.pause();
			_playing=false; 

		}
	}

	public void holdCall(View v){
		try {
			if(BTHoldCall.getText().equals("HOLD")){
				sipCall.holdCall(10);
				updateStatus("Call Held");
				BTHoldCall.setText("UNHOLD");			
			}else{
				sipCall.continueCall(10);
				updateStatus("On Call");
				BTHoldCall.setText("HOLD");			
			}
		} catch (SipException e) {
			e.printStackTrace();
		}
	}

	public void muteCall(View v){
		try {
			if(BTMuteCall.getText().equals("MUTE")){
				updateStatus("Call Muted");
				BTMuteCall.setText("UNMUTE");			
			}else{
				updateStatus("On Call");
				BTMuteCall.setText("MUTE");			
			}
			sipCall.toggleMute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void endCall(View v){

		if(sipCall != null) {
			try {
				sipCall.endCall();
				sipCall.close();
				updateStatus("Call Ended");		
				finish();
			} catch (SipException se) {
			}
		}
	}

	private void updateStatus(String status) {
		sipCallStatus.setText(status);
	}

	public class ASYNCStreamer extends AsyncTask<MODELLiveStream, String, Boolean> {

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
				_mMediaPlayer.setDataSource(getBaseContext(),Uri.parse(liveStream.getUrl()));
				_mMediaPlayer.setDisplay(_holder);
				_mMediaPlayer.setVolume(0, 0);//AudioStreamType(AudioManager.STREAM_MUSIC);
				_mMediaPlayer.prepare();
				return true;
			} catch (Exception e) {
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
					_mMediaPlayer.setOnCompletionListener(ACTOnSIPCall.this);

					_mMediaPlayer.start();

					_playing=true;

				}else{
					TVVideoInfo.setVisibility(View.INVISIBLE);
					_PBStreamVideo.setVisibility(View.INVISIBLE);
					cameraUnavailableInfo=liveStream.getName()+" live stream unavailable";
					//publishProgress(cameraUnavailableInfo);
					_pauseButton.setVisibility(View.INVISIBLE);
					ACTOnSIPCall._playing=false;
					videoUnavailableLayout.setVisibility(View.VISIBLE);
					TVStreamUnavailable.setText(cameraUnavailableInfo);


				}
				super.onPostExecute(result);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	public void onCompletion(MediaPlayer arg0) {
		Toast.makeText(getBaseContext(), "onCompletion",Toast.LENGTH_SHORT).show();
		releaseMediaPlayer();
		startOrSwitchStream(1);

	}

	private void releaseMediaPlayer() {

		if (_mMediaPlayer != null) {
			_mMediaPlayer.reset();
			_mMediaPlayer.release();
			_mMediaPlayer = null;
		}
		if(_asyncmpegStreamer!=null){
			_asyncmpegStreamer.cancel(true);
			_asyncmpegStreamer=null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startOrSwitchStream(1);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}


	//-------------------SIP related methods-------------------------------------

	



}