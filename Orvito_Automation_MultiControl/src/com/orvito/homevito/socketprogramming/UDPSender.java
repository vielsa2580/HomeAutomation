package com.orvito.homevito.socketprogramming;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.outmsgfactory.OMServerNotReachable;
import com.orvito.homevito.utils.UTILConstants;


public class UDPSender extends AsyncTask<String, String, Void>{

	public static int i=0;
	String PCBIP;
	int PCBPort;
	Activity activity;
	Handler handler;
	byte[] msg;
	MODELReqPacket reqPacket;
	Context context;
	MODELResultSet resultSetToReturn;


	public UDPSender(String PCBIP, int PCBPort,Activity activity,Handler handler,byte[] msg,MODELReqPacket reqPacket){
		this.PCBIP=PCBIP;
		this.PCBPort=PCBPort;
		this.activity=activity;
		this.context=activity.getBaseContext();
		this.handler=handler;
		this.msg=msg;
		this.reqPacket=reqPacket;
		resultSetToReturn=new MODELResultSet();
	}

	public UDPSender(String PCBIP, int PCBPort,Context context,Handler handler,byte[] msg,MODELReqPacket reqPacket){
		this.PCBIP=PCBIP;
		this.PCBPort=PCBPort;
		this.context=context;
		this.handler=handler;
		this.msg=msg;
		this.reqPacket=reqPacket;
		resultSetToReturn=new MODELResultSet();
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(!UTILConstants.isWIFION(context)){
			resultSetToReturn.setError("Error");
			resultSetToReturn.setMessage("Please switch on Wifi..!!");
			new OMServerNotReachable(reqPacket,resultSetToReturn);
			this.cancel(true);
		}

	}

	@Override
	protected Void doInBackground(String... params) {

		DatagramSocket ds=null;
		try {			

			ds = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName(PCBIP);
			DatagramPacket dp= new DatagramPacket(msg, msg.length, serverAddr, PCBPort);
			ds.send(dp); 
			resultSetToReturn.setMessage("Message sent successfully");
			Log.e("", ++i+" UDP Message sent successfully to "+PCBIP+"@"+PCBPort);
			UTILConstants.reqPacketList.add(reqPacket);
			//publishProgress(++i+" UDP Message sent successfully to "+PCBIP+"@"+PCBPort);
		} catch (Exception e) {
			resultSetToReturn.setError("Error");
			resultSetToReturn.setMessage(e.toString());
			e.printStackTrace();	
			//Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();

		}finally {
			if (ds != null) {
				ds.close();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		UTILConstants.toastMsg(handler, context, values[0]);
		super.onProgressUpdate(values);
	}




}
