package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;

public class OMSmartHubDisc {

	final String dataInvalidMsg="Invalid input data";	


	public MODELResultSet tcpDiscoverSH(final Activity activity,final Handler handler){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing Tab Reg MSG ");

		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.TABTOSHDISC;
			byte[] seqNumArray=new byte[2];
			final int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[0]", ""+seqNumArray[0]);
			//if(UTILConstants.debugModeForLogs) Log.e("sent byte[1]", ""+seqNumArray[1]);
			/*ByteBuffer wrapped = ByteBuffer.wrap(seqNum); // big-endian by default
			int senumINt = wrapped.getInt();*/
			//if(UTILConstants.debugModeForLogs) Log.e("seqnum sent",""+seqNum);
			Short portNum=new Integer(UTILConstants.TCPRECEIVERPORT).shortValue();

			byte[] authTokenArray=new byte[2];
			final int authToken=UTILConstants.getRandomSeqNum();
			authTokenArray = BigInteger.valueOf(authToken).toByteArray();
			final byte[] shDiscPacket=new byte[8];

			/*----------------------HEADER------------------------------------------------*/
			shDiscPacket[0]=(byte)protocolVersion;
			shDiscPacket[1]=(byte)msgType;		
			shDiscPacket[2]=seqNumArray[0];
			shDiscPacket[3]=seqNumArray[1];

			/*-------------------------BODY--------------------------------------------*/
			shDiscPacket[4]=(byte)authTokenArray[0];   
			shDiscPacket[5]=(byte)authTokenArray[1];

			shDiscPacket[6]=(byte)portNum.byteValue();
			shDiscPacket[7]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();

			
			
			
			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(regDataPacket));
			
					final int deviceIpLast = Integer.parseInt(UTILConstants.deviceIp.substring(UTILConstants.deviceIp.lastIndexOf(".")+1, UTILConstants.deviceIp.length()));
					final String otherIpAddressFull = UTILConstants.deviceIp.substring(0,UTILConstants.deviceIp.lastIndexOf(".")+1);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							
//							for (int i = 1; i < 255; i++) {
//								if(i!=deviceIpLast){
									String ipToSend = "192.168.1.158";	//otherIpAddressFull+""+i;
//									Log.v("IP ADDRESSES", ipToSend);
									try {	

										new TCPSender(ipToSend,9999,activity,handler,shDiscPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");
										Log.v("OH SYNC SEND", "sent");	
							
									} catch (Exception e) {
									
										Log.v("FAILED", e.toString());			
									}
									
//									
									
//								}
								
							}
							
//						}
					}).start();
				
										
				
			
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Tab reg MSG sent");
		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;


	}
	
}
