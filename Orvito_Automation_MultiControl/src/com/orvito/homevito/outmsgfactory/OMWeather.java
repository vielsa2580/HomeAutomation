package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELWeather;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class OMWeather {


	

	public MODELResultSet tcpGetCurrentWeather(Activity activity,Handler handler){	
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing weather report MSG ");
		MODELResultSet resultSetToReturn=new MODELResultSet();
		try {
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.WEATHER;
			byte[] seqNumArray=new byte[2];
			int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			//Log.e("sent byte[0]", ""+seqNumArray[0]);
			//Log.e("sent byte[1]", ""+seqNumArray[1]);
			Short portNum=new Integer(UTILConstants.TCPRECEIVERPORT).shortValue();
			
			byte[] regDataPacket=new byte[6];
			regDataPacket[0]=(byte)protocolVersion;
			regDataPacket[1]=(byte)msgType;		
			regDataPacket[2]=seqNumArray[0];
			regDataPacket[3]=seqNumArray[1];
			regDataPacket[4]=(byte)portNum.byteValue();
			regDataPacket[5]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();
			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(regDataPacket));
			new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,regDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");
			if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "weather report msg sent");

		} catch (Exception e) {
			e.printStackTrace();
			//Log.e("Exception at Class:DFSERVERDocChem, Method:getDocChem" , "Exception:"+e.toString());
			resultSetToReturn.setError("Error");
			resultSetToReturn.setMessage(e.toString());

		}
		return resultSetToReturn;	

	}

}
