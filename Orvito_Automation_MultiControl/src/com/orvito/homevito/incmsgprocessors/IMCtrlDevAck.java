package com.orvito.homevito.incmsgprocessors;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.orvito.homevito.R;
import com.orvito.homevito.helpers.ADPTRoomGrid;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.outmsgfactory.OMNodeStatusUpdate;
import com.orvito.homevito.presentors.ACTDeviceMaster;
import com.orvito.homevito.presentors.ACTSensorControl;
import com.orvito.homevito.utils.UTILConstants;

public class IMCtrlDevAck {
	Context context;
	Handler handler;

	public IMCtrlDevAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}


	public void processCtrlDevAck(byte[] msg, final MODELReqPacket reqPacket) throws Exception {	
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received control device ACK");
		//UTILConstants.toastMsg(handler,context,"received control device ACK");
		byte[] pcbAuthTokenByteArray=new byte[32];
		for (int i = 0; i < pcbAuthTokenByteArray.length; i++) {
			pcbAuthTokenByteArray[i]=msg[i+4];
		}
		int numOfNodes = (int) msg[36];
		List<MODELNode> nodeList=new ArrayList<MODELNode>();
		MODELNode modelNode = null;
		for (int i = 0,currentpos=37; i < numOfNodes; i++,currentpos++) {
			modelNode=new MODELNode();
			modelNode.setNodeNum(""+(int)msg[currentpos]);
			modelNode.setState(""+(int)msg[++currentpos]);
			//Log.e("ackfrompcb", modelNode.getState());
			nodeList.add(modelNode);
			//modelNode=null;
		}


		if(ACTDeviceMaster.currentInstance!=null){
			new OMNodeStatusUpdate().nodeStatusUpdate(ACTDeviceMaster.currentInstance,handler, pcbAuthTokenByteArray,nodeList);
		}else{
			new OMNodeStatusUpdate().nodeStatusUpdate((Context)reqPacket.getActionHandle(), handler,pcbAuthTokenByteArray,nodeList);
		}
		if(UTILConstants.debugModeForLogs)Log.v("INC_MSG", "Status sent to cloud");
		//UTILConstants.toastMsg(handler,context,"Status sent to cloud");
		Log.v("ctrldevack", "Status sent to cloud");


		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTDeviceMaster") && ACTDeviceMaster.currentInstance!=null) {
			try{
				MODELNode currentNode = (MODELNode) reqPacket.getObject();
				if(currentNode==null){//this will be an ack for the control generated from cloud
					if(ACTDeviceMaster.currentInstance!=null){
						ACTDeviceMaster.currentInstance.refreshData();
						Log.v("ctrldevack", "completed ack processing by refreshing page");
						//UTILConstants.toastMsg(handler,context,"completed ack processing by refreshing page");	
					}
				}else if(ACTDeviceMaster.currentInstance!=null){//this will be an ack for the control generated from the tablet
					View v = ACTDeviceMaster.currentInstance.findViewById(currentNode.getUiObjectID());
					ImageView iv = (ImageView) v.findViewById(R.id.deviceimage);
					String imageToPick = currentNode.getHardwareType().getName()+ modelNode.getState();//receivedState;
					int imageID=ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable", ACTDeviceMaster.currentInstance.getPackageName());
					if(imageID==0){
						imageToPick = "unknown"+ currentNode.getNodeStatus().getState();
						imageID=ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable", ACTDeviceMaster.currentInstance.getPackageName());
					}
					iv.setBackgroundResource(imageID);
					iv.setBackgroundResource(ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable",ACTDeviceMaster.currentInstance.getPackageName()));
					currentNode.getNodeStatus().setState(modelNode.getState());

					//the next two lines is to refresh the LHS i.e. the rooms list device status 
					ListView groupsGrid = (ListView)ACTDeviceMaster.currentInstance.findViewById(R.id.roomslist);
					groupsGrid.setAdapter(new ADPTRoomGrid(ACTDeviceMaster.currentInstance));

					//Log.v("PCBCtrlDevAck", "inside  ACTDeviceControl");
					//UTILConstants.toastMsg(handler,context,"completed ack processing by updating node state");	
					Log.v("ctrldevack", "completed ack processing by updating node state");
				}
				
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTSensorControl")){

			try{

				MODELNode currentNode = (MODELNode) reqPacket.getObject();
				if(currentNode!=null && ACTSensorControl.currentInstance!=null){  //this will be an ack for the control generated from cloud
					//this will be an ack for the control generated from the tablet					


					View v = ACTSensorControl.currentInstance.findViewById(currentNode.getUiObjectID());
					ImageView iv = (ImageView) v.findViewById(R.id.deviceimage);
					String imageToPick = currentNode.getHardwareType().getName()+ modelNode.getState();//receivedState;
					int imageID=ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable", ACTDeviceMaster.currentInstance.getPackageName());
					if(imageID==0){
						imageToPick = "unknown"+ currentNode.getNodeStatus().getState();
						imageID=ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable", ACTDeviceMaster.currentInstance.getPackageName());
					}
					iv.setBackgroundResource(imageID);
					iv.setBackgroundResource(ACTDeviceMaster.currentInstance.getResources().getIdentifier(imageToPick, "drawable",ACTDeviceMaster.currentInstance.getPackageName()));
					currentNode.getNodeStatus().setState(modelNode.getState());

					//the next two lines is to refresh the LHS i.e. the rooms list device status 
					ListView groupsGrid = (ListView)ACTDeviceMaster.currentInstance.findViewById(R.id.roomslist);
					groupsGrid.setAdapter(new ADPTRoomGrid(ACTDeviceMaster.currentInstance));

					//Log.v("PCBCtrlDevAck", "inside  ACTDeviceControl");

					/*	
					Button pirToggle = (Button) ((Activity)reqPacket.getActionHandle()).findViewById(currentNode.getUiObjectID());
					String imageToPick = currentNode.getHardwareType().getName()+ modelNode.getState();
					pirToggle.setBackgroundResource(((Activity)reqPacket.getActionHandle()).getResources().getIdentifier(imageToPick, "drawable",((Activity)reqPacket.getActionHandle()).getPackageName()));
					UTILConstants.pirSensorData.getNodeStatus().setState(modelNode.getState());*/


				}else if(ACTSensorControl.currentInstance!=null){
					ACTSensorControl.currentInstance.refreshData();

				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(UTILConstants.debugModeForLogs)Log.v("INC_MSG", "Completed ctrldevack processing");


	}

}
