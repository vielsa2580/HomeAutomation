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
/*
 * customers":[{"id":"1","name":"Satish","dob":"1969-04-17","mobile":"9032437540","email":"satish@gmail.com","specialityIdFk":"4","desigIdFk":"3"}*/
public class OMUserDirectory {
	
	public MODELResultSet tcpEmpDirectory(Activity activity,Handler handler){
		
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing employee dir MSG");
		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.USERDIR;
			byte[] seqNumArray=new byte[2];
			int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[0]", ""+seqNumArray[0]);
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[1]", ""+seqNumArray[1]);
			Short portNum=Integer.valueOf(UTILConstants.TCPRECEIVERPORT).shortValue();			
			byte[] authTokenByteArray=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN).getBytes();
			//if(UTILConstants.debugModeForLogs) Log.e("authToken sent", new String(authTokenByteArray));			
			
			byte[] userDirDataPacket=new byte[38];
			userDirDataPacket[0]=(byte)protocolVersion;
			userDirDataPacket[1]=(byte)msgType;		
			userDirDataPacket[2]=seqNumArray[0];
			userDirDataPacket[3]=seqNumArray[1];
			userDirDataPacket[4]=(byte)portNum.byteValue();
			userDirDataPacket[5]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
			
			
			for (int i = 6; i < authTokenByteArray.length+6 && i<38; i++) {
				userDirDataPacket[i]=authTokenByteArray[i-6];
			}
			
			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(userDirDataPacket));
			new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,userDirDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Employee dir msg sent");
		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;

	}




}
