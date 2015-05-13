package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class OMEmpImagePull {

	final String dataInvalidMsg="Invalid input data";	


	public MODELResultSet requestImage(Activity activity,Handler handler,String userId,View imageViewHandle){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing employee image MSG");
		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.USERIMAGEREQ;
			byte[] seqNumArray=new byte[2];
			int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[0]", ""+seqNumArray[0]);
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[1]", ""+seqNumArray[1]);

			//Log.e("ipaddress", ipAddressArray[0]+"  "+ipAddressArray[1]+"  "+ipAddressArray[2]+"  "+ipAddressArray[3]);		
			Short portNum=Integer.valueOf(UTILConstants.TCPRECEIVERPORT).shortValue();	
			byte[] userIdInBytes=UTILConstants.intToByteArray(Integer.valueOf(userId));//userId.getBytes();
			byte[] authTokenByteArray=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN).getBytes();



			byte[] empImagePullDataPacket=new byte[42];
			empImagePullDataPacket[0]=(byte)protocolVersion;
			empImagePullDataPacket[1]=(byte)msgType;		
			empImagePullDataPacket[2]=seqNumArray[0];
			empImagePullDataPacket[3]=seqNumArray[1];
			//convertion of port number into two byte array
			empImagePullDataPacket[4]=(byte)portNum.byteValue();
			empImagePullDataPacket[5]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
		
			for (int i = 0; i < 4 && i<userIdInBytes.length; i++) {	
			
				empImagePullDataPacket[i+6]=userIdInBytes[i];
				//if(UTILConstants.debugMode) .e("beforeuseridsent[i+]"+i,""+userIdInBytes[i]);
			}
			
			byte[] userIdByteArray=new byte[4];
			userIdByteArray[0]=empImagePullDataPacket[6];
			userIdByteArray[1]=empImagePullDataPacket[7];
			userIdByteArray[2]=empImagePullDataPacket[8];
			userIdByteArray[3]=empImagePullDataPacket[9];
			
			//Log.e("useridsent",""+ UTILConstants.byteArrayToInt(userIdInBytes));
			//Log.e("useridsent","actual value"+new String(userIdByteArray)+"  byte 0:"+ userIdByteArray[0]+"  byte 1:"+userIdByteArray[1]+"  byte 2:"+userIdByteArray[2]+"  byte 3:"+userIdByteArray[3]);
			for (int i = 0; i < 32 && i<authTokenByteArray.length; i++) {			
				empImagePullDataPacket[i+10]=authTokenByteArray[i];
			}
			
			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(empImagePullDataPacket));
			new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,empImagePullDataPacket,new MODELReqPacket(seqNum,activity,System.currentTimeMillis(),imageViewHandle)).execute("");
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Employee image msg sent");
		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;

	}







}
