package com.orvito.homevito.helpers;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.utils.UTILConstants;


public class ADPTRoomGrid  extends ArrayAdapter{

	Activity activity;
	static List<MODELRoom> roomsList;
	Typeface tf ;
	

	public ADPTRoomGrid(Activity activity, List<MODELRoom> roomList) {

		super(activity,R.layout.icon_room,roomList);
		this.roomsList = roomList;
		this.activity=activity;
		tf = Typeface.createFromAsset(activity.getAssets(),"futorallightdbnormal.ttf");
	}
	public ADPTRoomGrid(Activity activity) {

		super(activity,R.layout.icon_room,ADPTRoomGrid.roomsList);
		this.activity=activity;
		tf = Typeface.createFromAsset(activity.getAssets(),"futorallightdbnormal.ttf");
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflator=activity.getLayoutInflater();
		View row=inflator.inflate(R.layout.icon_room, null);
		RelativeLayout layout=(RelativeLayout) row.findViewById(R.id.roomParentLL);
		layout.setBackgroundColor(UTILConstants.getRoomsColor(position,activity));

		TextView roomName = (TextView)row.findViewById(R.id.roomname);		
		roomName.setTypeface(tf);
		roomName.setText(roomsList.get(position).getName().toUpperCase());


		TextView noOfDevices = (TextView)row.findViewById(R.id.TVDevAvailVal);		
		noOfDevices.setTypeface(tf);
		int numOfDevices=roomsList.get(position).getNodeList().size();
		noOfDevices.setText("Devices: "+numOfDevices);

		TextView activeDevices = (TextView)row.findViewById(R.id.TVDevOnValue);
		activeDevices.setTypeface(tf);
		int temp=0;
		for (int i = 0; i < roomsList.get(position).getNodeList().size(); i++) {
			if(Integer.valueOf(roomsList.get(position).getNodeList().get(i).getNodeStatus().getState())==1) {
				++temp;
			}
		}
		activeDevices.setText("On: "+temp);

		

		return(row);
	}
	
	
	
	

	
}
