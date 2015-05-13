package com.orvito.homevito.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.outmsgfactory.OMCtrlDeviceList;
import com.orvito.homevito.utils.UTILConstants;

public class ADPTDeviceMasterPager extends PagerAdapter implements OnClickListener,OnItemClickListener{

	Activity _activity;
	ViewFlipper _viewFlipper;
	LinearLayout _UILLResidentDetail;
	List<MODELRoom> _modelRoomList,modelRoomsFilteredList;
	GridView devicesGrid;
	LinearLayout alertView,addRoom;
	TextView roomName;
	protected boolean isFaceUp=false;
	protected Timer faceTimer;
	TimerTask faceAnimationSchedule;
	View v;
	Handler handler;
	Button _BTmasterOff,_BTmasterOn,_BTkingOff,_BTkingOn;
	int listPosition;

	public ADPTDeviceMasterPager(Activity activity,Handler handler,RelativeLayout _UILLDeviceControlLayout,List<MODELRoom> modelGroupList) {

		this._activity = activity;
		this.handler=handler;
		this._modelRoomList=modelGroupList;
		devicesGrid=(GridView) _UILLDeviceControlLayout.findViewById(R.id.devicesGrid);
		alertView=(LinearLayout) _UILLDeviceControlLayout.findViewById(R.id.alertView);
		
		roomName=(TextView) _UILLDeviceControlLayout.findViewById(R.id.roomName);
		
		_BTmasterOff = (Button) _UILLDeviceControlLayout.findViewById(R.id.btMasterSwitchOff);
		_BTmasterOn = (Button) _UILLDeviceControlLayout.findViewById(R.id.btMasterSwitchOn);

		_BTmasterOff.setOnClickListener(this);
		_BTmasterOn.setOnClickListener(this);

	}

	public int getCount() {
		return 1;
	}

	public Object instantiateItem(final View collection, int position) {//LHS ViewPager construction

		LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = null;//inflater.inflate(R.layout.plain_temp_layout, null);

		if(position == 0){

			view = roomsGridPopulate(inflater);

		}else {//the else will not be called as the size is right now 1 coz for now we dont need to flip view on LHS

			//view = inflater.inflate(R.layout.gridrow, null);

		}
		final View finalview=view;
		_activity.runOnUiThread(new Runnable() {

			public void run() {
				((ViewPager) collection).addView(finalview, 0);

			}
		});


		return view;
	}

	private View roomsGridPopulate(LayoutInflater inflater) {
		View view= inflater.inflate(R.layout.list_rooms, null);
		
		ListView groupsGrid = (ListView)view.findViewById(R.id.roomslist);
		addRoom = (LinearLayout)view.findViewById(R.id.llAddRoom);
		
		groupsGrid.setOnItemClickListener(this);
		groupsGrid.setAdapter(new ADPTRoomGrid(_activity,_modelRoomList));
		populateDevicesInARoom(_modelRoomList.get(0),UTILConstants.getRoomsColor(0, _activity));

		_BTkingOff = (Button)view.findViewById(R.id.btKingControlOFF);
		_BTkingOn= (Button)view.findViewById(R.id.btKingControlOn);


		_BTkingOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				KingControl("0");
			}
		});
		_BTkingOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				KingControl("1");
			}
		});
		
		
		addRoom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				addNewRoomDialog();
			}
		});

		if(_modelRoomList == null) return null; 
		modelRoomsFilteredList=new ArrayList<MODELRoom>(_modelRoomList);

		return view;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);

	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);

	}

	@Override
	public Parcelable saveState() {
		return null;
	}


	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btMasterSwitchOff:

			masterControl("0");
			break;

		case R.id.btMasterSwitchOn:

			masterControl("1");
			break;

		default:
			break;
		}

	}

	private void addNewRoomDialog(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
		
		EditText editText = new EditText(_activity);
		builder.setView(editText);
		
		builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
				dialog.cancel();
			}
		});
		
		builder.show();
		
	}
	
	private void masterControl(String state) {

		List<MODELNode> nodeList = _modelRoomList.get(listPosition).getNodeList();
		HashSet<String> hashSet=new HashSet<String>();
		for (int i = 0; i < nodeList.size(); i++) {
			hashSet.add(nodeList.get(i).getIpAddress());
		}
		int numOFIPAdress=hashSet.size();

		Object strings[]= hashSet.toArray();
		for (int i = 0; i < strings.length; i++) {
			List<MODELNode> list=new ArrayList<MODELNode>();
			for (int j = 0; j < nodeList.size(); j++) {

				if(((String)strings[i]).equals(nodeList.get(j).getIpAddress())){
					nodeList.get(j).setUiObjectID(devicesGrid.getChildAt(j).getId());
					nodeList.get(j).setState(state);	
					list.add(nodeList.get(j));
				}
			}
			new OMCtrlDeviceList().ctrlDevice(_activity,handler, list,list.get(0).getIpAddress(),Integer.valueOf(list.get(0).getPort()), list.get(0).getDevAuthToken());//(_activity,selectedNode,ctrlcommandToSend);
		}
	}

	private void KingControl(String state) {

		List<MODELNode> nodeList = new ArrayList<MODELNode>();
		for (int i = 0; i < _modelRoomList.size(); i++) {
			for (int j = 0; j < _modelRoomList.get(i).getNodeList().size(); j++) {
				nodeList.add(_modelRoomList.get(i).getNodeList().get(j));
			} 
		}

		HashSet<String> hashSet=new HashSet<String>();
		for (int i = 0; i < nodeList.size(); i++) {
			hashSet.add(nodeList.get(i).getIpAddress());
		}

		Object strings[]= hashSet.toArray();
		for (int i = 0; i < strings.length; i++) {
			List<MODELNode> list=new ArrayList<MODELNode>();
			for (int j = 0; j < nodeList.size(); j++) {
				if(((String)strings[i]).equals(nodeList.get(j).getIpAddress())){
					list.add(nodeList.get(j));
					nodeList.get(j).setState(state);
				}
			}
			new OMCtrlDeviceList().ctrlDevice(_activity,handler, list,list.get(0).getIpAddress(),Integer.valueOf(list.get(0).getPort()), list.get(0).getDevAuthToken());//(_activity,selectedNode,ctrlcommandToSend);
		}

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		//on click of a group grid update device grid view on right

		listPosition = position;

		if(isFaceUp){
			//v.setAnimation(scaleFaceDownAnimation(500));
			isFaceUp = false;
		}
		this.v=arg1;
		//arg1.setAnimation(scaleFaceUpAnimation());
		isFaceUp = true;
		populateDevicesInARoom(_modelRoomList.get(position),UTILConstants.getRoomsColor(position, _activity));

	}

	void populateDevicesInARoom(final MODELRoom selectedRoom,final int gridcolor){
		_activity.runOnUiThread(new Runnable() {
			public void run() {
				alertView.setVisibility(View.GONE);
				if(selectedRoom.getNodeList().size()<1){
					alertView.setVisibility(View.VISIBLE);
				}
				roomName.setText(selectedRoom.getName());
				devicesGrid.setAdapter(new ADPTDevicesGrid(_activity,handler, selectedRoom,gridcolor));
			}
		});
	}

}