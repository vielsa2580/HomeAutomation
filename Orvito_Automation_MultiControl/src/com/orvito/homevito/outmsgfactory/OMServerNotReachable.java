package com.orvito.homevito.outmsgfactory;

import android.app.Activity;
import android.view.View;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.presentors.ACTDeviceMaster;
import com.orvito.homevito.presentors.ACTSensorControl;
import com.orvito.homevito.presentors.ACTUserLogin;
import com.orvito.homevito.utils.UTILConstants;

public class OMServerNotReachable {


	public OMServerNotReachable(MODELReqPacket reqPacket,final MODELResultSet resultSet) {
		if (UTILConstants.isActivityOnTop((Activity) reqPacket.getActionHandle(),"com.orvito.homevito.presentors.ACTUserLogin")){
			final ACTUserLogin actHandle=(ACTUserLogin) reqPacket.getActionHandle();

			actHandle.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					actHandle.loginProgress.setVisibility(View.GONE);
					actHandle.clearTimers();	
					UTILConstants.toastMsg(actHandle.getBaseContext(),resultSet.getMessage());
				}
			});

		}else if (UTILConstants.isActivityOnTop((Activity) reqPacket.getActionHandle(),"com.orvito.homevito.presentors.ACTDeviceMaster")){
			final ACTDeviceMaster actHandle=(ACTDeviceMaster) reqPacket.getActionHandle();

			actHandle.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					actHandle.loadingLayout.setVisibility(View.GONE);	
					actHandle._UIBTReload.setVisibility(View.VISIBLE);
					actHandle.clearTimers();
					UTILConstants.toastMsg(actHandle.getBaseContext(),resultSet.getMessage());
				}
			});

		}else if (UTILConstants.isActivityOnTop((Activity) reqPacket.getActionHandle(),"com.orvito.homevito.presentors.ACTSensorControl")){
			final ACTSensorControl actHandle=(ACTSensorControl) reqPacket.getActionHandle();

			actHandle.runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					actHandle.loadingLayout.setVisibility(View.GONE);	
					actHandle._UIBTReload.setVisibility(View.VISIBLE);
					actHandle.clearTimers();
					UTILConstants.toastMsg(actHandle.getBaseContext(),resultSet.getMessage());
				}
			});

		}

		UTILConstants.removeReqPacketFromQueue(reqPacket);
	}





}
