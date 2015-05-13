package com.orvito.homevito.helpers;

import java.util.List;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.outmsgfactory.OMTabSync;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class HLPRDeviceControl {
	
	/*public List<MODELRoom> getAndSetGroupAndDeviceData(Activity activity,ViewPager viewPager,LinearLayout layout){
		
		DFGetUserDevices userDeviceLogin=new DFGetUserDevices();
		String authToken=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN);
		MODELResultSet response=userDeviceLogin.getDevices(authToken);
		if(response.getError()==null){
			List<MODELRoom> roomsList=(List<MODELRoom>)response.getDataList();
			if(roomsList==null){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(activity, "No files assigned to you", Toast.LENGTH_LONG).show();
				return null;
			}else if(roomsList.size()<1){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(activity, "No files assigned to you", Toast.LENGTH_LONG).show();
				return null;
			}else{
				viewPager.setVisibility(View.VISIBLE);
				viewPager.setAdapter(new ADPTDeviceControlPager(activity,layout,roomsList));
				return roomsList;
			}

		}else{
			Toast.makeText(activity, response.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}		
	}*/
	
	

}
