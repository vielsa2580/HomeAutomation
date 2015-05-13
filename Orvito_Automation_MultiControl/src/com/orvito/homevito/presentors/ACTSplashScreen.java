package com.orvito.homevito.presentors;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.orvito.homevito.R;
import com.orvito.homevito.utils.UTILConstants;

public class ACTSplashScreen extends Activity{
	
	int begin=20,end=39;
	Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		handler=new Handler();
		final ImageView img=(ImageView)findViewById(R.id.imageView1);

		UTILConstants.deviceIp = getLocalIpAddress();

		final Timer timer=new Timer();
		TimerTask task=new TimerTask() {

			@Override
			public void run() {
		
						if(begin==end){
							startActivity(new Intent(ACTSplashScreen.this,ACTUserLogin.class));
							ACTSplashScreen.this.finish();
							timer.cancel();
							return;
						}
						String imagename="splash"+ACTSplashScreen.this.begin;
						final BitmapDrawable bitmapDrawable= new BitmapDrawable(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(imagename, "drawable", getPackageName())));
						handler.post(new Runnable() {							
							@Override
							public void run() {
								img.setBackgroundDrawable(bitmapDrawable);								
							}
						});  
						ACTSplashScreen.this.begin++;							
			}
		};
		timer.schedule(task, 1,60);
	}
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                    return inetAddress.getHostAddress();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}



}
