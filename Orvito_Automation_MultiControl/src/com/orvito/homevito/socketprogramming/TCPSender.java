package com.orvito.homevito.socketprogramming;

import java.io.DataOutputStream;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.outmsgfactory.OMServerNotReachable;
import com.orvito.homevito.utils.UTILConstants;

public class TCPSender extends AsyncTask<String, MODELResultSet, Void>{

	String fieldServerIP;
	int fieldServerPort;
	Activity activity;
	Handler handler;
	byte[] msg;
	MODELReqPacket reqPacket;
	Context context;
	MODELResultSet resultSetToReturn;


	public TCPSender(String fieldServerIP, int fieldServerPort,Activity activity,Handler handler,byte[] msg,MODELReqPacket reqPacket){
		this.fieldServerIP=fieldServerIP;
		this.fieldServerPort=fieldServerPort;
		this.activity=activity;
		this.context=activity.getBaseContext();
		this.handler=handler;
		this.msg=msg;
		this.reqPacket=reqPacket;
		this.reqPacket.setDataPacketSent(msg);
		
		resultSetToReturn=new MODELResultSet();
	}

	public TCPSender(String fieldServerIP, int fieldServerPort,Context context,Handler handler,byte[] msg,MODELReqPacket reqPacket){
		this.fieldServerIP=fieldServerIP;
		this.fieldServerPort=fieldServerPort;
		this.context=context;
		this.handler=handler;
		this.msg=msg;
		this.reqPacket=reqPacket;
		this.reqPacket.setDataPacketSent(msg);
		
		resultSetToReturn=new MODELResultSet();
	}



	@Override
	protected Void doInBackground(String... params) {
		if(!UTILConstants.isWIFION(context)){
			resultSetToReturn.setError("Error");
			resultSetToReturn.setMessage("Please switch on Wifi..!!");
			new OMServerNotReachable(reqPacket,resultSetToReturn);
			//TCPSender.this.cancel(true);
			return null;
		}
		
		try {	

			Socket socket=new Socket(fieldServerIP,fieldServerPort);
			DataOutputStream  dataout = new  DataOutputStream(socket.getOutputStream());
			dataout.write(msg,0,msg.length);
			dataout.flush(); 
			resultSetToReturn.setMessage("Message sent successfully");
			reqPacket.setDataPacketSent(msg);
			UTILConstants.reqPacketList.add(reqPacket);
		} catch (Exception e) {
			resultSetToReturn.setError("Error");
			resultSetToReturn.setMessage("Server not reachable");
//			e.printStackTrace();			
			new OMServerNotReachable(reqPacket,resultSetToReturn);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(MODELResultSet... values) {
		UTILConstants.toastMsg(handler, context, values[0].getMessage());	
		super.onProgressUpdate(values);
	}




}
