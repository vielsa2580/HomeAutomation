package com.orvito.homevito.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELLiveStream;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELRoom;

public class UTILConstants {

	public final static Boolean debugModeForLogs=true;
	public final static Boolean debugModeForToasts=false;
	public final static char DEVTYPE='2';
	//public final static String SERVERURL="http://192.168.1.113/xampp/homevito/";
	public static int TCPRECEIVERPORT=12369;
	public static int UDPRECEIVERPORT=50005;
	public final static char PROTOCOLVERSION='1';
	public final static String fieldServerIP="192.168.1.114";
	public static int fieldServerPort=50002;
//	public final static String fieldServerIP="192.168.1.158";
//	public static int fieldServerPort=9999;
	public static String deviceIp = "";
	public static String RECEIVEDSHIP;


	/* ------------------ OUTGOING Message Types------------------------------*/
	public final static char TABREG=(char)49;//stands for 1;
	public final static char WEATHER=(char)63;//stands for 15
	public final static char USERDIR=(char)65;//stands for 17
	public final static char TABSYNC=(char)61;//stands for 13;
	public final static char CTRLDEV=(char)50;//stands for 2;

	public final static char STATUSUPDATETOCLOUD=(char)55;//stands for 7;
	public final static char USERIMAGEREQ=(char)67;//stands for 19;

	public final static char TABTOSHDISC=(char)84;//stands for 36;
	public final static char SHTOTABDISC=(char)85;//stands for 37;
	public final static char DEVIDSH=(char)1; 
	public final static char OUTWENDISC=(char)78;//stands for 30;
	public final static char INWENDISC=(char)83;//stands for 35;

	/* ------------------ INCOMING Message Types------------------------------*/
	public final static char TABREGACK=(char)'\t';//stands for 9
	public final static char WEATHERACK=(char)64;//stands for 16
	public final static char USERDIRACK=(char)66;//stands for 18
	public final static char TABSYNCACK=(char)62;//stands for 14;
	public final static char CTRLDEVACK=(char)58;//stands for 10;
	public final static char STATUSUPDATEFROMCLOUD=(char)56;//stands for 8;
	public final static char STATUSUPDATEACK=(char)60;//stands for 12;
	public final static char AUTOCTRLDEVACK=(char)53;//stands for 12;
	public final static char USERIMAGEACK=(char)68;//stands for 20;
	public final static char CTRLDEVFROMCLOUD=(char)51;//stands for 3;


	/* ------------------TCP Message PacketMaxSize------------------------------*/
	public final static int REGPACKETSIZE=15;

	public final static List<MODELReqPacket> reqPacketList=new ArrayList<MODELReqPacket>();

	/* ------------------Preference Keys---------------------------------------------*/
	public final static String AUTHTOKEN="authtoken";
	public final static String FIRSTNAME="firstname";
	public final static String LASTNAME="lastname";
	public final static String DOB="dob";
	public final static String EMAIL="email";
	public final static String PHONENUM="phonenum";


	public static MODELNode pirSensorData=null;
	public static List<MODELRoom> sensorsData=null;



	public static MODELLiveStream   getStreamAtPos(int position){
		MODELLiveStream modelLiveStream=null;
		switch (position) {

		case 0:
			//modelLiveStream=new MODELLiveStream("Front Door Camera","rtsp://192.168.1.114:5544/");
			//modelLiveStream=new MODELLiveStream("MJPEG Stream","http://192.168.1.117:8090/?action=stream");
			modelLiveStream=new MODELLiveStream("CGI Stream","http://192.168.1.107:80/mjpeg.cgi");
			//modelLiveStream=new MODELLiveStream("Beautician","http://podcast.20min-tv.ch/podcast/20min/199693.mp4");
			//modelLiveStream=new MODELLiveStream("WarZone","http://podcast.20min-tv.ch/podcast/20min/199733.mp4");
			//modelLiveStream=new MODELLiveStream("Big buck Bunny","rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov");
			//modelLiveStream=new MODELLiveStream("NEWS Time","http://176.34.175.163:8000/camera/s7RIqmHU/?t=180&ts=1346851352&digest=5IaMR3hKO67azIE1zt1AIogYxpM.");
			//modelLiveStream=new MODELLiveStream("NEWS Time","rtsp://v5.cache1.c.youtube.com/CjYLENy73wIaLQnhycnrJQ8qmRMYESARFEIJbXYtZ29vZ2xlSARSBXdhdGNoYPj_hYjnq6uUTQw=/0/0/0/video.3gp");
			break;
		case -1:
			modelLiveStream=new MODELLiveStream("Front Door Camera","rtsp://192.168.1.111:5544/");
			break;
		}
		return modelLiveStream;
	}



	public static int byteArrayToInt(byte[] byteArray) {
		//Log.e("received byte[0]", "" + byteArray[0]);
		//Log.e("received byte[1]", "" + byteArray[1]);
		int tempInt = 0;
		for (int i = 0; i < byteArray.length; i++) {
			tempInt = (tempInt << 8) + (byteArray[i] & 0xff);
		}
		return tempInt;
	}


	public static byte[] intToByteArray(int value) {
		//byte[] bytes = ByteBuffer.allocate(4).putInt(1695609641).array();
		byte[] b = new byte[4];

		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}

		return b;

	}


	public static Boolean isActivityOnTop(Context context,String activityName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

		String topActivity = taskInfo.get(0).topActivity.getClassName();
		if (topActivity.equals(activityName))
			return true;
		else
			return false;

	}

	public static Boolean isActivityOnTop(Activity activity,String activityName) {
		ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

		String topActivity = taskInfo.get(0).topActivity.getClassName();
		if (topActivity.equals(activityName))
			return true;
		else
			return false;

	}

	public static void toastMsg(Handler handler,final Context context,final String msg) {

		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
		//Log.e("Error", "MsgType Doesnt Match. Received: " + msg);
	}

	public static void toastMsg(Context context,final String msg) {

		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

	}

	public static byte[] getIPAddressAsArray() {
		String ipAddress=getLocalIpAddress();
		byte[] ipAddressArray=new byte[4];
		int occurenceOfColon=0;
		int arrayCount=0;
		for (int i = 0; i < ipAddress.length(); i++) {
			if(new String(""+ipAddress.charAt(i)).equals(".") || (i==ipAddress.length()-1)){
				int temp=0;
				if((i==ipAddress.length()-1)){
					temp=Integer.valueOf(ipAddress.substring(occurenceOfColon, i+1));	
				}else{
					temp=Integer.valueOf(ipAddress.substring(occurenceOfColon, i));	
				}

				ipAddressArray[arrayCount++]= (byte)temp;
				occurenceOfColon=i+1;
			}
		}
		return ipAddressArray;
	}


	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf .getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					// for getting IPV4 format
					String ipv4;
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {
						//Log.e("ipaddress",ipv4);
						return ipv4;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return null;
	}


	public static int getRoomsColor(int pos,Activity activity){

		switch (pos) {
		case 0:
			return activity.getResources().getColor(R.color.purple);

		case 1:

			return activity.getResources().getColor(R.color.orange);
		case 2:

			return activity.getResources().getColor(R.color.lightblue);
		case 3:

			return activity.getResources().getColor(R.color.skyblue);
		case 4:

			return activity.getResources().getColor(R.color.green);

		default:

			return getRoomsColor(pos-5, activity);
		}	

	}


	public static int getUsersColor(int pos,Activity activity){

		switch (pos) {
		case 0:
			return activity.getResources().getColor(R.color.green);

		case 1:

			return activity.getResources().getColor(R.color.orange);
		case 2:

			return activity.getResources().getColor(R.color.lightblue);
		case 3:

			return activity.getResources().getColor(R.color.purple);
		case 4:

			return activity.getResources().getColor(R.color.skyblue);

		default:
			return getUsersColor(pos-5, activity);
		}	

	}

	public static int getRandomSeqNum(){
		int seqNum=new Random().nextInt(32767);
		if(seqNum<256) seqNum+=256;
		return seqNum;

	}


	public static void removeTimeLapsedRequestPackets(){

		try{
			new Thread(new Runnable() {			
				@Override
				public void run() {
					try{
						if(reqPacketList!=null){
							for (int i = 0; i < reqPacketList.size(); i++) {
								if(System.currentTimeMillis()-UTILConstants.reqPacketList.get(i).getTimeOfCreation()>=(1000*60)){//60 seconds
									UTILConstants.reqPacketList.remove(i);
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}	

				}
			}).start();
		}catch(Exception e){}

	}

	public static void removeReqPacketFromQueue(final MODELReqPacket reqPacket){

		try{
			new Thread(new Runnable() {			
				@Override
				public void run() {
					if(reqPacketList!=null){
						for (int i = 0; i < reqPacketList.size(); i++) {
							if(reqPacket.getSequenceNumber()==UTILConstants.reqPacketList.get(i).getSequenceNumber()){
								UTILConstants.reqPacketList.remove(i);
								break;
							}
						}
					}

				}
			}).start();
		}catch(Exception e){}

	}


	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.orvito.homevito.tcpsocket.SRVReceiver".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static Boolean isWIFION(Context context){
		ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) return true;		
		return false;

	}

}
