package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.UDPSender;
import com.orvito.homevito.utils.UTILConstants;

public class OMCtrlDevice {

	final String dataInvalidMsg="Invalid input data";	


	public MODELResultSet ctrlDevice(Activity activity,Handler handler,MODELNode node,int ctrlCmd){
		if(ctrlCmd==-1) return null;
		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.CTRLDEV;
			byte[] seqNumArray=new byte[2];
			int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			if(UTILConstants.debugModeForLogs) Log.e("sent byte[0]", ""+seqNumArray[0]);
			if(UTILConstants.debugModeForLogs) Log.e("sent byte[1]", ""+seqNumArray[1]);
			byte[] ipAddressArray=UTILConstants.getIPAddressAsArray();
			//Log.e("ipaddress", ipAddressArray[0]+"  "+ipAddressArray[1]+"  "+ipAddressArray[2]+"  "+ipAddressArray[3]);		
			Short portNum=Integer.valueOf(UTILConstants.UDPRECEIVERPORT).shortValue();		
			byte[] authTokenByteArray=node.getDevAuthToken().getBytes();

			

			byte[] devCtrlDataPacket=new byte[44];
			devCtrlDataPacket[0]=(byte)protocolVersion;
			devCtrlDataPacket[1]=(byte)msgType;		
			devCtrlDataPacket[2]=seqNumArray[0];
			devCtrlDataPacket[3]=seqNumArray[1];
			//convertion of port number into two byte array
			for (int i = 0; i < ipAddressArray.length; i++) {
				devCtrlDataPacket[i+4]=ipAddressArray[i];
			}
			devCtrlDataPacket[8]=(byte)portNum.byteValue();
			devCtrlDataPacket[9]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
			for (int i = 10; i < authTokenByteArray.length+10 && i<42; i++) {
				devCtrlDataPacket[i]=authTokenByteArray[i-10];
			}

			devCtrlDataPacket[42]=(byte)1;
			devCtrlDataPacket[43]=(byte)(int)new Integer(node.getNodeNum());
			devCtrlDataPacket[44]=(byte)ctrlCmd;


			if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(devCtrlDataPacket));
			Toast.makeText(activity, ""+ctrlCmd, Toast.LENGTH_SHORT).show();//24425 1344867795332
			new UDPSender(node.getIpAddress(),Integer.valueOf(node.getPort()),activity,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis(),node)).execute("");//sendUDPMsg(node.getIpAddress(),Integer.valueOf(node.getPort()),activity,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis(),node));

		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;

	}







}
