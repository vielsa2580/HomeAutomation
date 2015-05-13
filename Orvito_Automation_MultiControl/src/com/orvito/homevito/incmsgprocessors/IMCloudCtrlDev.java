package com.orvito.homevito.incmsgprocessors;

import java.math.BigInteger;
import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.socketprogramming.UDPSender;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMCloudCtrlDev {


	Context context;
	Handler handler;

	public IMCloudCtrlDev(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}


	public void processCtrlDevFromCloud(byte[] msg)throws Exception  {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received control device from cloud");
		if (UTILConstants.PROTOCOLVERSION == (char) msg[0]) {

			byte[] tabAuth=new byte[32];
			for (int i = 0; i < tabAuth.length; i++) {
				tabAuth[i]=msg[i+4];
			}
			if(UTILSharedPreference.getPreference(context, UTILConstants.AUTHTOKEN).equals(new String(tabAuth))){
				char protocolVersion=UTILConstants.PROTOCOLVERSION;
				char msgType=UTILConstants.CTRLDEV;
				byte[] seqNumArray=new byte[2];
				int seqNum=new Random().nextInt(32767);
				seqNumArray=BigInteger.valueOf(seqNum).toByteArray();

				byte[] ipAddressArray=UTILConstants.getIPAddressAsArray();
				//Log.e("ipaddress", ipAddressArray[0]+"  "+ipAddressArray[1]+"  "+ipAddressArray[2]+"  "+ipAddressArray[3]);		
				Short portNum=Integer.valueOf(UTILConstants.UDPRECEIVERPORT).shortValue();		
				
				byte[] pcbAuthTokenByteArray=new byte[32];
				
				String LOGSTRING = "";
				
				for (int i = 0; i < pcbAuthTokenByteArray.length; i++) {
					pcbAuthTokenByteArray[i]=msg[i+36];
					LOGSTRING = LOGSTRING+pcbAuthTokenByteArray[i];
				}
				
				byte pcbNodeNum=msg[68];
				byte pcbCtrlCmd=msg[69];
				
				String pcbIpAddress=""+((int )msg[70] & 0xff)+"."+((int )msg[71] & 0xff)+"."+((int )msg[72] & 0xff)+"."+((int )msg[73] & 0xff);//+":"+(int)msg[71]+":"+(int)msg[72]+":"+(int)msg[73];
				byte[] pcbPortArray=new byte[2];
				pcbPortArray[0]=msg[74];
				pcbPortArray[1]=msg[75];
				
				

				byte[] devCtrlDataPacket=new byte[45];
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
				for (int i = 10; i < pcbAuthTokenByteArray.length+10 && i<42; i++) {
					devCtrlDataPacket[i]=pcbAuthTokenByteArray[i-10];
				}
				
				devCtrlDataPacket[42]=1;//no of nodes
				devCtrlDataPacket[43]=pcbNodeNum;
				devCtrlDataPacket[44]=pcbCtrlCmd;
				
				
				if(UTILConstants.debugModeForToasts)UTILConstants.toastMsg(handler, context, "Cloud control... forwarding to WEN... pcbNodeNum: "+pcbNodeNum+"  pcbCtrlCmd: "+pcbCtrlCmd);
				if(UTILConstants.debugModeForLogs) Log.e("cloudctrl", "Cloud control... forwarding to WEN...  pcbNodeNum: "+pcbNodeNum+"  pcbCtrlCmd: "+pcbCtrlCmd);
				new UDPSender(pcbIpAddress,UTILConstants.byteArrayToInt(pcbPortArray),context,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, context,System.currentTimeMillis())).execute("");//sendUDPMsg(pcbIpAddress,UTILConstants.byteArrayToInt(pcbPortArray),context,devCtrlDataPacket,new MODELReqPacket(seqNum, context,System.currentTimeMillis()));
				
			}
		}	

	}


}
