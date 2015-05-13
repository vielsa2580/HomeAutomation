package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class OMNodeStatusUpdate {	


	public MODELResultSet nodeStatusUpdate(Context context,Handler handler,byte[] pcbAuthTokenByteArray,List<MODELNode> nodeList){

		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.STATUSUPDATETOCLOUD;
			byte[] seqNumArray=new byte[2];
			int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			Short portNum=Integer.valueOf(UTILConstants.TCPRECEIVERPORT).shortValue();

			byte[] tabAuthTokenByteArray=UTILSharedPreference.getPreference(context, UTILConstants.AUTHTOKEN).getBytes();		


			byte[] statusUpdateDataPacket=new byte[71+(nodeList.size()*2)];
			statusUpdateDataPacket[0]=(byte)protocolVersion;
			statusUpdateDataPacket[1]=(byte)msgType;		
			statusUpdateDataPacket[2]=seqNumArray[0];
			statusUpdateDataPacket[3]=seqNumArray[1];
			//convertion of port number into two byte array
			statusUpdateDataPacket[4]=(byte)portNum.byteValue();
			statusUpdateDataPacket[5]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
			for (int i = 0; i < tabAuthTokenByteArray.length; i++) {
				statusUpdateDataPacket[i+6]=tabAuthTokenByteArray[i];
			}

			for (int i = 0; i < pcbAuthTokenByteArray.length; i++) {
				statusUpdateDataPacket[i+38]=pcbAuthTokenByteArray[i];
			}



			statusUpdateDataPacket[70]=(byte) nodeList.size();
			for (int i = 0,currentpos=71; i < nodeList.size(); i++,currentpos++) {
				statusUpdateDataPacket[currentpos]=(byte)(int)Integer.valueOf(nodeList.get(i).getNodeNum());
				statusUpdateDataPacket[++currentpos]=(byte)(int)Integer.valueOf(nodeList.get(i).getState());

			}



			//if(nodeList.size()==1){
		//	if(UTILConstants.debugModeForLogs) Log.e("statusupdate statebeingsent nodeList.size()"+nodeList.size(), nodeList.get(0).getState());
			//}
			//UTILConstants.toastMsg(activity.getBaseContext(), "sending status update to field server");


			new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,context,handler,statusUpdateDataPacket,new MODELReqPacket(seqNum, context,System.currentTimeMillis())).execute("");//.sendTCPMsg(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,context,statusUpdateDataPacket,new MODELReqPacket(seqNum, context,System.currentTimeMillis()));

		}catch (Exception e) {			
			resultSet.setError("Error");
			resultSet.setMessage(e.toString());
			e.printStackTrace();
		}
		return resultSet;

	}







}
