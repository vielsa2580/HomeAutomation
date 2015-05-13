package com.orvito.homevito.socketprogramming;


import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SRVReceiver extends Service{


	private Thread tcpReceiverThread,udpReceiverThread;
	private Timer timer;

	@Override
	public void onCreate() {
		super.onCreate();

		tcpReceiverThread=new TCPReceiver(getBaseContext(),new Handler());
		udpReceiverThread=new UDPReceiver(getBaseContext(),new Handler());
		timer=new Timer();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);	

		TimerTask receiverTimerTask=new TimerTask() {
			@Override
			public void run() {
				if(!tcpReceiverThread.isAlive())
					tcpReceiverThread.start();
				if(!udpReceiverThread.isAlive())
					udpReceiverThread.start();
			}
		};
		timer.schedule(receiverTimerTask, 1);

	}
	

	@Override
	public void onDestroy() {
		Log.v("socket_programming", "destroying service");
		
		((TCPReceiver)tcpReceiverThread).closeExistingSocket();
		((UDPReceiver)udpReceiverThread).closeExistingSocket();
		timer.cancel();
		timer=null;
		tcpReceiverThread=null;
		udpReceiverThread=null;
	
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
