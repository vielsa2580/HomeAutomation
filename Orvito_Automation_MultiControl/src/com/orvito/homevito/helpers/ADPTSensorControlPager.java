package com.orvito.homevito.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.utils.UTILConstants;

public class ADPTSensorControlPager extends PagerAdapter implements OnClickListener,OnItemClickListener{

	Activity _activity;
	ViewFlipper _viewFlipper;
	LinearLayout _UILLResidentDetail;
	List<MODELRoom> roomsListWithSensors,modelRoomsFilteredList;
	GridView devicesGrid;
	TextView roomName;
	protected boolean isFaceUp=false;
	protected Timer faceTimer;
	TimerTask faceAnimationSchedule;
	View v;
	Handler handler;

	public ADPTSensorControlPager(Activity activity,Handler handler,RelativeLayout _UILLDeviceControlLayout,List<MODELRoom> roomsListWithSensors) {

		this._activity = activity;
		this.handler=handler;
		this.roomsListWithSensors=roomsListWithSensors;
		devicesGrid=(GridView) _UILLDeviceControlLayout.findViewById(R.id.devicesGrid);
		roomName=(TextView) _UILLDeviceControlLayout.findViewById(R.id.roomName);

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
		groupsGrid.setOnItemClickListener(this);
		TextView textView=(TextView) view.findViewById(R.id.TVResidentDirectory);
		textView.setText("SENSOR CONTROL");
		groupsGrid.setAdapter(new ADPTRoomGrid(_activity,roomsListWithSensors));
		populateDevicesInARoom(roomsListWithSensors.get(0),UTILConstants.getRoomsColor(0, _activity));

		EditText _UIEDSearch = (EditText)view.findViewById(R.id.EDSearch);


		if(roomsListWithSensors == null) return null; 
		modelRoomsFilteredList=new ArrayList<MODELRoom>(roomsListWithSensors);


		/*_UIEDSearch.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length()<1){
					populateDevicesInARoom(roomsListWithSensors.get(0),UTILConstants.getRoomsColor(0, _activity));
					return;
				}

				modelRoomsFilteredList=null;
				modelRoomsFilteredList=new ArrayList<MODELRoom>();		
				


				int  m=0;
				List<String> userProvidedStringList=new ArrayList<String>(); 
				for (int j=0; j < s.length(); j++){

					if(new String(""+s.charAt(j)).equals(" ")){
						userProvidedStringList.add(new String(s.toString()).substring(m,j).toLowerCase());
						m=j+1;
					}						
				}



				String tempString = new String(s.toString()).substring(m,s.length());
				if(tempString.length()!=0){

					// Add "tempstring" to "userProvidedStringList" only when value is added and neglect adding to "tempString" if space is encountered   // 

					userProvidedStringList.add(tempString);

				}
				//userProvidedStringList.add(new String(s.toString()).substring(m,s.length()));

				for (int i = 0; i < userProvidedStringList.size(); i++) {
					for (int j = 0; j < roomsListWithSensors.size(); j++) {


						if(roomsListWithSensors.get(j).getName().toLowerCase().startsWith(userProvidedStringList.get(i))) {

							Boolean found=false;
							for (int k = 0; k < modelRoomsFilteredList.size(); k++) {
								if(modelRoomsFilteredList.get(k).equals(roomsListWithSensors.get(j))){
									found=true;
								}
							}

							if(!found) modelRoomsFilteredList.add(roomsListWithSensors.get(j));

						}

					}
				}
				if(modelRoomsFilteredList.size()<1){

				}
				populateDevicesInARoom(roomsListWithSensors.get(0),UTILConstants.getRoomsColor(0, _activity));


			}


			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}


			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub

			}	
		});*/


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

	}


	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		//on click of a group grid update device grid view on right

		if(isFaceUp){
			//v.setAnimation(scaleFaceDownAnimation(500));
			isFaceUp = false;
		}
		this.v=arg1;
		//arg1.setAnimation(scaleFaceUpAnimation());
		isFaceUp = true;
		populateDevicesInARoom(roomsListWithSensors.get(position),UTILConstants.getRoomsColor(position, _activity));

	}

	void populateDevicesInARoom(final MODELRoom selectedRoom,final int gridcolor){
		_activity.runOnUiThread(new Runnable() {
			public void run() {
				roomName.setText(selectedRoom.getName());
				devicesGrid.setAdapter(new ADPTSensorsGrid(_activity, handler,selectedRoom,gridcolor));
			}
		});
	}

}