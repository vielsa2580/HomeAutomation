package com.orvito.homevito.incmsgprocessors;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.presentors.ACTUserLogin;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMSmartHubDisc {

	Context context;
	Handler handler;
	String receivedShIp;

	public IMSmartHubDisc(Context context,Handler handler,String receivedShIp){
		this.context=context;
		this.handler=handler;
		this.receivedShIp = receivedShIp;

		if(receivedShIp != null){

			Log.v("SH IP ADDRESS RECEIVED", ""+this.receivedShIp);
			UTILSharedPreference.setPreference(context, UTILConstants.RECEIVEDSHIP,receivedShIp);

		}

	}
	public void processSHDisc(byte[] msg, final MODELReqPacket reqPacket) throws Exception {

		if(UTILConstants.debugModeForLogs) Log.v("INC_SHACK", "received SH found Ack");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTUserLogin")) {
			final ACTUserLogin actHandle = (ACTUserLogin) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode == 1) { //----- here 1 means there is some error

			} else if(errCode == 0){

				//if(UTILConstants.debugModeForLogs) Log.e("crossed", "Errorcode");
				byte[] msgBody = new byte[5];
				msgBody[0] = msg[5];//--- authtoken part 1
				msgBody[1] = msg[6];//--- authtoken part 2
				msgBody[2] = msg[7];//--- devId
				msgBody[3] = msg[8];//---- port part 1
				msgBody[4] = msg[9];//---- port part 2

				byte [] authSwapped = new byte[2];
				authSwapped[0] = msgBody[1];
				authSwapped[1] = msgBody[0];

				byte [] shPort = new byte[2];
				shPort[0] = msgBody[4];
				shPort[1] = msgBody[3];

				byte [] authFromReqPacket = new byte[2];
				authFromReqPacket[0] = reqPacket.getDataPacketSent()[4];
				authFromReqPacket[1] = reqPacket.getDataPacketSent()[5];
				if(new String(authSwapped).equals(new String(authFromReqPacket))){

					if(msgBody[2] == UTILConstants.DEVIDSH){

						int shportNumber = UTILConstants.byteArrayToInt(shPort);
						Log.v("SH PORT", ""+shportNumber);
						UTILConstants.fieldServerPort = shportNumber;
					}
				}
			}
		}
	}
}
