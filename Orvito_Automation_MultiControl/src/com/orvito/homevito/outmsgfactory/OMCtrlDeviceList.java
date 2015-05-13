package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Timer;

import java.util.TimerTask;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.UDPSender;
import com.orvito.homevito.utils.UTILConstants;


public class OMCtrlDeviceList {



	public MODELResultSet ctrlDevice(final Context activity,final Handler handler,final List<MODELNode> nodeList,final String pcbIPAddress,final int pcbPort,String pcbAuthToken ){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing control device MSG");
		MODELResultSet resultSet=new MODELResultSet();
		try{

			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.CTRLDEV;
			final int seqNum=UTILConstants.getRandomSeqNum();
			byte[] seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			byte[] tabIPAddressArray=UTILConstants.getIPAddressAsArray();	
			Short tabPortNum=Integer.valueOf(UTILConstants.UDPRECEIVERPORT).shortValue();				
			byte[] authTokenByteArray=pcbAuthToken.getBytes();	


			final byte[] devCtrlDataPacket=new byte[(43+nodeList.size()*2)];
			devCtrlDataPacket[0]=(byte)protocolVersion;
			devCtrlDataPacket[1]=(byte)msgType;		
			devCtrlDataPacket[2]=seqNumArray[0];
			devCtrlDataPacket[3]=seqNumArray[1];
			//convertion of port number into two byte array
			for (int i = 0; i < tabIPAddressArray.length; i++) {
				devCtrlDataPacket[i+4]=tabIPAddressArray[i];
			}
			devCtrlDataPacket[8]=(byte)tabPortNum.byteValue();
			devCtrlDataPacket[9]=(byte)new Short(Short.reverseBytes(tabPortNum)).byteValue();
			for (int i = 10; i < authTokenByteArray.length+10 && i<42; i++) {
				devCtrlDataPacket[i]=authTokenByteArray[i-10];
			}
			devCtrlDataPacket[42]=(byte)nodeList.size();


			for (int i = 0,currentNode=43; i < nodeList.size(); i++,currentNode++) {

				devCtrlDataPacket[currentNode]=(byte)(int)Integer.valueOf(nodeList.get(i).getNodeNum());					
				devCtrlDataPacket[++currentNode]=(byte)(int)Integer.valueOf(nodeList.get(i).getState());

			}


			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(devCtrlDataPacket));
			if(nodeList.size()==1){					
				TimerTask timerTask=new TimerTask() {

					@Override
					public void run() {

						new UDPSender(pcbIPAddress,Integer.valueOf(pcbPort),activity,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis(),nodeList.get(0))).execute("");//sendUDPMsg(pcbIPAddress,Integer.valueOf(pcbPort),activity,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis(),nodeList.get(0)));

					}
				};

				Timer timer=new Timer();
				//timer.schedule(timerTask, 1,5000);
				timer.schedule(timerTask, 1);


				if(UTILConstants.debugModeForToasts)UTILConstants.toastMsg(activity," pcbIPAddress:  "+pcbIPAddress+"  pcbPort:"+Integer.valueOf(pcbPort)+"  command:"+nodeList.get(0).getState()+"   pcbAuthToken:"+pcbAuthToken);
				//if(UTILConstants.debugModeForLogs)Log.e("Device control", "pcbIPAddress:  "+pcbIPAddress+"  pcbPort:"+Integer.valueOf(pcbPort)+"  command:"+nodeList.get(0).getState()+"   pcbAuthToken:"+pcbAuthToken);

			}else{
				new UDPSender(pcbIPAddress,Integer.valueOf(pcbPort),activity,handler,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");//sendUDPMsg(pcbIPAddress,Integer.valueOf(pcbPort),activity,devCtrlDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis()));
			}
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "Control device msg sent");

		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;

	}







}
