package com.orvito.homevito.helpers;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELNodeStatus;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.outmsgfactory.OMCtrlDeviceList;
import com.orvito.homevito.utils.UTILConstants;


public class ADPTDevicesGrid extends BaseAdapter implements OnClickListener {
	private Activity _activity;
	private MODELRoom _selectedRoom;
	boolean status=false;
	int gridColor;
	Handler handler;

	public ADPTDevicesGrid(Activity activity,Handler handler,MODELRoom selectedRoom,int gridcolor) {
		_activity=activity;
		_selectedRoom=selectedRoom;
		this.gridColor=gridcolor;
		activity.getBaseContext();
		this.handler=handler;

	}

	public int getCount() {

		return _selectedRoom.getNodeList().size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater li = _activity.getLayoutInflater();
		View v = li.inflate(R.layout.icon_device, null);
		MODELNode currentNode=_selectedRoom.getNodeList().get(position);

		LinearLayout layout=(LinearLayout) v.findViewById(R.id.layout);
		//layout.setBackgroundColor(gridColor);
		

		//LinearLayout originalcolorcode=(LinearLayout) v.findViewById(R.id.originalcolorcode);
		//originalcolorcode.setDrawingCacheBackgroundColor(gridColor);

		
		
		
		TextView devicename = (TextView)v.findViewById(R.id.devicename);
		devicename.setText(currentNode.getName());	


		TextView devicestatus = (TextView)v.findViewById(R.id.devicestatus);
		MODELNodeStatus latestDeviceStatus=currentNode.getNodeStatus();
		devicestatus.setText(latestDeviceStatus.getStatus());


		
		ImageView iv = (ImageView) v.findViewById(R.id.deviceimage);
		String imageToPick = currentNode.getHardwareType().getName()+ currentNode.getNodeStatus().getState();
		int imageID=_activity.getResources().getIdentifier(imageToPick, "drawable", _activity.getPackageName());
		if(imageID==0){
			imageToPick = "unknown"+ currentNode.getNodeStatus().getState();
			imageID=_activity.getResources().getIdentifier(imageToPick, "drawable", _activity.getPackageName());
		}
		iv.setBackgroundResource(imageID);
		
		
		if(Integer.valueOf(latestDeviceStatus.getState())==1){
			//layout.setBackgroundColor(Color.GREEN);			
		}
		v.setOnClickListener(this);
		v.setId(position);
		return v;

	}

	public void onClick(View v) {
		int position=v.getId();

		MODELNode selectedNode=_selectedRoom.getNodeList().get(position);
		Integer ctrlcommandToSend=null;


		if(new Integer(selectedNode.getNodeStatus().getState())==0){
			ctrlcommandToSend=1;
		}else if(new Integer(selectedNode.getNodeStatus().getState())==1){
			ctrlcommandToSend=0;
		}else{
			ctrlcommandToSend=-1;
		}

		if(ctrlcommandToSend!=-1){
			selectedNode.setUiObjectID(position);
			selectedNode.setState(""+ctrlcommandToSend);
			List<MODELNode> nodeList=new ArrayList<MODELNode>();
			nodeList.add(selectedNode);
			//if(UTILConstants.debugModeForLogs) Log.e("devauthbeingsent", selectedNode.getDevAuthToken());
			new OMCtrlDeviceList().ctrlDevice(_activity,handler, nodeList,selectedNode.getIpAddress(),Integer.valueOf(selectedNode.getPort()), selectedNode.getDevAuthToken());//(_activity,selectedNode,ctrlcommandToSend);
			if(nodeList!=null){
				if(nodeList.size()==1){
					if(UTILConstants.debugModeForToasts) UTILConstants.toastMsg(handler,_activity,"DEV CTRL@"+selectedNode.getIpAddress()+" : "+Integer.valueOf(selectedNode.getPort())+"    control command:  "+ctrlcommandToSend);
				}
			}
		}
		/*
		if(!status){
			//switch on
			iv.setBackgroundResource(R.drawable.bulb1);
			sendMsg("on".getBytes(), _selectedRoom.getNodeList().get(position).getIpAddress(), _selectedRoom.getNodeList().get(position).getPort());
			status=true;
		}else{
			iv.setBackgroundResource(R.drawable.bulb0);
			sendMsg("off".getBytes(), _selectedRoom.getNodeList().get(position).getIpAddress(), _selectedRoom.getNodeList().get(v.getId()).getPort());
			status=false;
		}*/

	}

	private static Boolean sendMsg(byte[] msg,String serverIp,String serverPort){

		DatagramSocket ds = null;


		try {
			ds = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName(serverIp);
			DatagramPacket dp= new DatagramPacket(msg, msg.length, serverAddr, new Integer(serverPort));
			ds.send(dp);
			if(UTILConstants.debugModeForLogs) Log.v("Message", "sent successfully");
			return true;

		} catch (Exception e) {
			if(UTILConstants.debugModeForLogs) Log.v("Exception", "SocketException");
			e.printStackTrace();
			return false;
		}finally {
			if (ds != null) {
				ds.close();
			}
		}
	}


}
