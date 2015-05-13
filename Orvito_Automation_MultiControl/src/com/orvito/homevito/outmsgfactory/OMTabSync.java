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

public class OMTabSync {

	final String dataInvalidMsg="Invalid input data";	
	
	
	public MODELResultSet syncTablet(Activity activity,Handler handler){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing tab sync MSG ");
		MODELResultSet resultSet=new MODELResultSet();
		try{
		char protocolVersion=UTILConstants.PROTOCOLVERSION;
		char msgType=UTILConstants.TABSYNC;
		byte[] seqNumArray=new byte[2];
		int seqNum=UTILConstants.getRandomSeqNum();
		seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
		//if(UTILConstants.debugModeForLogs) Log.e("sent byte[0]", ""+seqNumArray[0]);
		//if(UTILConstants.debugModeForLogs) Log.e("sent byte[1]", ""+seqNumArray[1]);
		Short portNum=Integer.valueOf(UTILConstants.TCPRECEIVERPORT).shortValue();
		
		byte[] authTokenByteArray=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN).getBytes();
		//14582881711344233207999999999999
		byte[] devCtrlDataPacket=new byte[38];
		devCtrlDataPacket[0]=(byte)protocolVersion;
		devCtrlDataPacket[1]=(byte)msgType;		
		devCtrlDataPacket[2]=seqNumArray[0];
		devCtrlDataPacket[3]=seqNumArray[1];
		//convertion of port number into two byte array
		devCtrlDataPacket[4]=(byte)portNum.byteValue();
		devCtrlDataPacket[5]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
		for (int i = 6; i < authTokenByteArray.length+6 && i<38; i++) {
			devCtrlDataPacket[i]=authTokenByteArray[i-6];
		}
		
		//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(devCtrlDataPacket));
		new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");

		if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Tab sync msg sent");
	}catch (Exception e) {			
		resultSet.setError("Error");
		resultSet.setMessage(e.toString());
		e.printStackTrace();
	}
	return resultSet;
		
	}

}
