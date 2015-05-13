package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class OMInitiateWenDisc {

	final String dataInvalidMsg="Invalid input data";	


	public MODELResultSet tcpInitiateWenDisc(final Activity activity,final Handler handler){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing Tab Reg MSG ");

		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.OUTWENDISC;
			byte[] seqNumArray=new byte[2];
			final int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			Short portNum=new Integer(UTILConstants.TCPRECEIVERPORT).shortValue();

			
			byte[] authTokenArray=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN).getBytes();
			
			
			
			final byte[] wenDiscPacket=new byte[38];

			/*----------------------HEADER------------------------------------------------*/
			wenDiscPacket[0]=(byte)protocolVersion;
			wenDiscPacket[1]=(byte)msgType;		
			wenDiscPacket[2]=seqNumArray[0];
			wenDiscPacket[3]=seqNumArray[1];

			/*-------------------------BODY--------------------------------------------*/
			
			for (int i = 0; i < authTokenArray.length; i++) {
				wenDiscPacket[i+5]=authTokenArray[i];
			}

			wenDiscPacket[6]=(byte)portNum.byteValue();
			wenDiscPacket[7]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();

			
			handler.post(new Runnable() {				
				@Override
				public void run() { //---------- the fielsServerIp will be replace by IP stored in the shared preference in the name of UTILConstants.RECEIVEDSHIP
					new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,wenDiscPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");					
				}
			});
				
			
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Tab reg MSG sent");
		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;


	}
	
}
