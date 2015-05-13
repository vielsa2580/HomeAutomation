package com.orvito.homevito.outmsgfactory;

import java.math.BigInteger;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.presentors.ACTUserLogin;
import com.orvito.homevito.socketprogramming.TCPSender;
import com.orvito.homevito.utils.UTILConstants;

public class OMTabReg {

	final String dataInvalidMsg="Invalid input data";	


	public MODELResultSet tcpRegisterTablet(final Activity activity,final Handler handler,EditText username,EditText password){
		//if(UTILConstants.debugModeForLogs) Log.v("OUT_MSG", "preparing Tab Reg MSG ");

		if(username.getText().toString().length()==0 || password.getText().toString().length()==0){
			((ACTUserLogin)activity).loginProgress.setVisibility(View.GONE);
			((ACTUserLogin)activity).clearTimers();
			Toast.makeText(activity, "Please provide valid data..!!", 1).show();
			return null;	
		}

		MODELResultSet resultSet=new MODELResultSet();
		try{
			char protocolVersion=UTILConstants.PROTOCOLVERSION;
			char msgType=UTILConstants.TABREG;
			byte[] seqNumArray=new byte[2];
			final int seqNum=UTILConstants.getRandomSeqNum();
			seqNumArray=BigInteger.valueOf(seqNum).toByteArray();
			char devType=UTILConstants.DEVTYPE;
			String macId=getDeviceMAC(activity).replace(":", "");
			Short portNum=new Integer(UTILConstants.TCPRECEIVERPORT).shortValue();
			int givenUsernameLength=username.getText().toString().length();
			String givenUsername=username.getText().toString();
			int givenPasswordLength=password.getText().toString().length();
			String givenPassword=password.getText().toString();



			final byte[] regDataPacket=new byte[15+givenUsernameLength+givenPasswordLength];

			/*----------------------HEADER------------------------------------------------*/
			regDataPacket[0]=(byte)protocolVersion;
			regDataPacket[1]=(byte)msgType;		
			regDataPacket[2]=seqNumArray[0];
			regDataPacket[3]=seqNumArray[1];

			/*-------------------------BODY--------------------------------------------*/
			regDataPacket[4]=(byte)devType;        

			byte[] macIDInDec=new byte[12];
			for (int i = 0; i < macId.length(); i++) {				
				macIDInDec[i]=(byte)Integer.parseInt(""+macId.charAt(i),16);
			}

			int temp=0;//convertion of macid into 6 byte array
			for (int i = 0; i < 6; i++) {
				byte x=macIDInDec[temp];
				byte y=macIDInDec[++temp];
				regDataPacket[i+5]=(byte) ((x<<4)|y);
				temp++;
			}


			//convertion of port number into two byte array
			regDataPacket[11]=(byte)portNum.byteValue();
			regDataPacket[12]=(byte)new Short(Short.reverseBytes(portNum)).byteValue();

			regDataPacket[13]=(byte)givenUsernameLength;			
			for (int i = 0; i < givenUsername.length(); i++) {
				regDataPacket[i+14]=(byte)givenUsername.charAt(i);
			}

			regDataPacket[14+givenUsernameLength]=(byte)givenPasswordLength;
			for (int i = 0; i < givenPassword.length(); i++) {
				regDataPacket[i+15+givenUsernameLength]=(byte)givenPassword.charAt(i);
			}




			//if(UTILConstants.debugModeForLogs) Log.e("byte array",new String(regDataPacket));
			handler.post(new Runnable() {				
				@Override
				public void run() {
					new TCPSender(UTILConstants.fieldServerIP,UTILConstants.fieldServerPort,activity,handler,regDataPacket,new MODELReqPacket(seqNum, activity,System.currentTimeMillis())).execute("");					
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

	private String getDeviceMAC(Activity activity){
		WifiManager wimanager = (WifiManager)activity.getSystemService(Activity.WIFI_SERVICE);
		return wimanager.getConnectionInfo().getMacAddress();
	}


}
