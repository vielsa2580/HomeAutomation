package com.orvito.homevito.presentors;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.animation.Flip3dAnimation;
import com.orvito.homevito.helpers.ADPTUserDirPager;
import com.orvito.homevito.models.MODELBlock;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.outmsgfactory.OMUserDirectory;


public class ACTUserDirectory extends Activity {

	ViewPager viewPager;
	ImageView _UIIVResidentImage;
	public Button _UIBTIntercom,_UIBTSms,_UIBTEmail,_UIBTReload;
	LinearLayout _UILLResidentDetail,_UILLResidentDirectory;//_UILLBlockDetail;
	Boolean isFirstImage = true;
	public Timer timer;
	public LinearLayout loadingLayout;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_userdir);
		getHandles();
		requestData();

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageSelected(int position) {

				switch (position) {
				case 0:

					applyRotation(0, 90);
					isFirstImage = !isFirstImage;

					break;

				case 1:

					applyRotation(0, 90);
					isFirstImage = !isFirstImage;

					break;


				default:
					break;
				}
			}

			
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			public void onPageScrollStateChanged(int arg0) {}
		});
	}
	
	@Override
	protected void onStop() {
		clearTimers();
		super.onStop();
	}
	
	public void reload(View v){
		_UIBTReload.setVisibility(View.GONE);
		requestData();
	}
	
	public void requestData(){
		
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
		new OMUserDirectory().tcpEmpDirectory(ACTUserDirectory.this,new Handler());
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

	public void populateUserData(MODELResultSet resultSetObject){
		_UIBTReload.setVisibility(View.GONE);
		ViewPager viewPager = (ViewPager)_UILLResidentDirectory.findViewById(R.id.awesomepager);
		
		if(resultSetObject.getError()==null){
			List<MODELBlock> blocksList=(List<MODELBlock>)resultSetObject.getDataList();
			
			if(blocksList==null){
				viewPager.setVisibility(View.INVISIBLE);
			}else if(blocksList.size()==0){
				viewPager.setVisibility(View.INVISIBLE);
			}else{
				viewPager.setVisibility(View.VISIBLE);
				LinearLayout LLResidentDetail=(LinearLayout) _UILLResidentDirectory.findViewById(R.id.LLresidentDetail);
				viewPager.setAdapter(new ADPTUserDirPager(ACTUserDirectory.this,new Handler(), LLResidentDetail,blocksList));
			}

		}else{
			Toast.makeText(getBaseContext(), resultSetObject.getMessage(), Toast.LENGTH_LONG).show();
			return ;
		}	
	}

	private void getHandles() {

		_UILLResidentDetail = (LinearLayout)findViewById(R.id.LLresidentDetail);
		//_UILLBlockDetail = (LinearLayout)findViewById(R.id.LLBlockDetail);
		_UILLResidentDirectory= (LinearLayout)findViewById(R.id.LLresidentDirectory);
		_UIBTReload=(Button) findViewById(R.id.reload);
		viewPager = (ViewPager) findViewById(R.id.awesomepager);
		loadingLayout=(LinearLayout) findViewById(R.id.loadinglayout);
		
	}

	private void applyRotation(float start, float end) {
		float centerX = _UILLResidentDetail.getWidth() / 2.0f;
		float centerY = _UILLResidentDetail.getHeight() / 2.0f;

		Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);

		rotation.setDuration(300);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		//rotation.setAnimationListener(new DisplayNextView(isFirstImage, _UILLResidentDetail, _UILLBlockDetail));

		if (isFirstImage)
		{
			_UILLResidentDetail.startAnimation(rotation);
		} else {
			//_UILLBlockDetail.startAnimation(rotation);
		}

	}


}