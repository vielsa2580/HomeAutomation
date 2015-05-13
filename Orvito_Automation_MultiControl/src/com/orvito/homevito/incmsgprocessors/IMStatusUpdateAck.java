package com.orvito.homevito.incmsgprocessors;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.presentors.ACTDeviceMaster;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMStatusUpdateAck {

	
	Context context;
	Handler handler;
	
	public IMStatusUpdateAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}
	
	public void processStatusUpdateAck(byte[] msg, final MODELReqPacket reqPacket)throws Exception  {
		//if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received Status Update Ack");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTDeviceMaster")) {

			if(UTILConstants.debugModeForLogs) Log.e("crossed", "SeqNum");
			ACTDeviceMaster actHandle = (ACTDeviceMaster) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode != 0) {				
				UTILConstants.toastMsg(handler,context," Aborting Status-Update-Ack Error Code" + errCode);			

			} else {
				if(UTILConstants.debugModeForLogs) Log.e("crossed", "Errorcode");				
				byte[] tabAuthTokenByteArray=new byte[32];//	
				for (int i = 0; i < tabAuthTokenByteArray.length; i++) {
					tabAuthTokenByteArray[i]=msg[i+5];
				}
				String tabAuth=UTILSharedPreference.getPreference(actHandle, UTILConstants.AUTHTOKEN);
				if(tabAuth.equals(new String(tabAuthTokenByteArray))){
					if(UTILConstants.debugModeForToasts)UTILConstants.toastMsg(handler,context,"STATUS UPDATED successfully in cloud");
					if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "STATUS UPDATED successfully in cloud");
//					actHandle.refreshData();
				}
			}
		}// end of activity on top check
	}
}
