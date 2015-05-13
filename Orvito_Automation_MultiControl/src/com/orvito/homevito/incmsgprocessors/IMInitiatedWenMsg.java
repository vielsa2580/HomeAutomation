package com.orvito.homevito.incmsgprocessors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.presentors.ACTUserLogin;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMInitiatedWenMsg {

	Activity activity;
	Handler handler;

	public IMInitiatedWenMsg(Activity activity,Handler handler){
		this.activity=activity;
		this.handler=handler;
		
	}
	public void processWenInitiation(byte[] msg, final MODELReqPacket reqPacket) throws Exception {
		
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received Tab Reg Ack");
		if (UTILConstants.isActivityOnTop(activity,"com.orvito.homevito.presentors.ACTUserLogin")) {
			final ACTUserLogin actHandle = (ACTUserLogin) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode == 0) {
				editIp("0");
				
			} else {

				editIp("1");
			}
		}// end of activity on top check
	}


	private void editIp(String errorType){
		final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

		TextView textView = new TextView(activity);
		
		if(errorType.equals("0")){
			
			textView.setText("Sorry there has been some problem initiating wen config");
			
		}else if(errorType.equals("1")){
			
			textView.setText("Successfully initiated wen config");
		}

		alert.setView(textView);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {


			}
		});

		alert.show();     
	}
	
}
