package com.orvito.homevito.incmsgprocessors;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.orvito.homevito.models.MODELHardwareType;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELNodeStatus;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.presentors.ACTDeviceMaster;
import com.orvito.homevito.presentors.ACTSensorControl;
import com.orvito.homevito.utils.UTILConstants;

public class IMTabSyncAck {


	Context context;
	Handler handler;

	public IMTabSyncAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}

	public void processTabSyncAck(byte[] msg, final MODELReqPacket reqPacket)throws Exception  {


		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received Tab sync Ack");
		ACTDeviceMaster actDeviceMasterHandle=null;
		ACTSensorControl actSensorControlHandle=null;
		try{ actDeviceMasterHandle = (ACTDeviceMaster) reqPacket.getActionHandle();}catch (Exception e) {}
		try { actSensorControlHandle=(ACTSensorControl) reqPacket.getActionHandle();} catch (Exception e) {}
		final ACTDeviceMaster finalactDeviceMasterHandle=actDeviceMasterHandle;
		final ACTSensorControl finalactSensorControlHandle=actSensorControlHandle;
		if(finalactDeviceMasterHandle!=null){
			handler.post(new Runnable() {
				public void run() {
					if(finalactDeviceMasterHandle!=null){
						finalactDeviceMasterHandle.loadingLayout.setVisibility(View.GONE);	
						finalactDeviceMasterHandle._UIBTReload.setVisibility(View.VISIBLE);
						finalactDeviceMasterHandle.clearTimers();
					}

					if(finalactSensorControlHandle!=null){
						finalactSensorControlHandle.loadingLayout.setVisibility(View.GONE);	
						finalactSensorControlHandle._UIBTReload.setVisibility(View.VISIBLE);
						finalactSensorControlHandle.clearTimers();
					}
				}
			});
		}

		final int errCode = (int) msg[4];
		if (errCode != 0) {
			if(actDeviceMasterHandle!=null) UTILConstants.toastMsg(handler, actDeviceMasterHandle, "Aborting Tab-Sync-Ack Error Code" );//(actHandle, msg)
			if(actSensorControlHandle!=null) UTILConstants.toastMsg(handler, actSensorControlHandle, "Aborting Tab-Sync-Ack Error Code" );//(actHandle, msg)
		} else {
			byte[] jsonLengthTemp = new byte[4];
			jsonLengthTemp[0] = msg[5];
			jsonLengthTemp[1] = msg[6];
			jsonLengthTemp[2] = msg[7];
			jsonLengthTemp[3] = msg[8];

			int jsonLength = UTILConstants.byteArrayToInt(jsonLengthTemp);

			byte[] jsonResponseByteArray = new byte[jsonLength];
			for (int j = 9; j < jsonResponseByteArray.length + 9; j++) {
				jsonResponseByteArray[j - 9] = msg[j];
				// Log.v("jsonResponseByteArray j="+(j-9)+" msg["+(j)+"]",
				// "char:"+(char)msg[j]+"   byte"+msg[j]);
			}
			String jsonResponse = new String(jsonResponseByteArray);
			try {
				JSONObject jsonObject = new JSONObject(jsonResponse);

				if (jsonObject.length() < 0) {
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(context, "Invalid server response",Toast.LENGTH_LONG).show();
						}
					});

				} else {

					/**/
					MODELResultSet resultSetToReturn = new MODELResultSet();
					try {
						if (jsonObject.get("error").toString().length() > 0) {

							resultSetToReturn.setError(jsonObject.getString("error"));
							resultSetToReturn.setMessage(jsonObject.getString("message"));
							UTILConstants.toastMsg(handler, context, jsonObject.getString("message"));
							

						} else {
							resultSetToReturn.setMessage(jsonObject.getString("message"));
							JSONArray roomsJsonArray = (JSONArray) jsonObject.get("nodeList");
							List<MODELRoom> modelRoomsList = new ArrayList<MODELRoom>();
							List<MODELRoom> modelRoomsWithSensors = new ArrayList<MODELRoom>();
							JSONObject jsonObject2;
							for (int i = 0; i < roomsJsonArray.length(); i++) {
								jsonObject2 = (JSONObject) roomsJsonArray.get(i);
								MODELRoom modelRoom = new MODELRoom(jsonObject2.getString("grpId"),jsonObject2.getString("grpName"));
								MODELRoom modelRoomForSensors=new MODELRoom(jsonObject2.getString("grpId"),jsonObject2.getString("grpName"));
								JSONArray devicesJsonArray = (JSONArray) jsonObject2.get("groupNodes");
								List<MODELNode> modelNodesList = new ArrayList<MODELNode>();
								List<MODELNode> sensorNodeList=new ArrayList<MODELNode>();
								JSONObject jsonObject3;
								for (int j = 0; j < devicesJsonArray.length(); j++) {//loop to retrieve nodes in a room
									jsonObject3 = (JSONObject) devicesJsonArray.get(j);
									MODELNode modelNode = new MODELNode(
											jsonObject3.getString("nodeName"),
											jsonObject3.getString("IpAddr"),
											jsonObject3.getString("port"),
											jsonObject3.getString("devAuthToken"),
											jsonObject3.getString("nodeNum"),
											new MODELHardwareType("",jsonObject3.getString("nodeTypeName")));

									jsonObject3 = (JSONObject) jsonObject3.get("nodeStatusRec");

									MODELNodeStatus modelDeviceStatus = new MODELNodeStatus();
									modelDeviceStatus.setTimeStamp(jsonObject3.getString("timeStamp"));
									modelDeviceStatus.setStatus(jsonObject3.getString("status"));
									modelDeviceStatus.setState(jsonObject3.getString("state"));
									modelNode.setNodeStatus(modelDeviceStatus);

									if(modelNode.getHardwareType().getName().equals("pirsensor")){												
										sensorNodeList.add(modelNode);
									}else{
										modelNodesList.add(modelNode);
									}

									modelNode = null;
								}
								modelRoom.setNodeList(modelNodesList);
								modelRoomForSensors.setNodeList(sensorNodeList);

								modelNodesList = null;
								sensorNodeList=null;

								//if(modelRoom.getNodeList().size()>0)
									modelRoomsList.add(modelRoom);	
								if(modelRoomForSensors.getNodeList().size()>0)	modelRoomsWithSensors.add(modelRoomForSensors);

								modelRoom = null;
								modelRoomForSensors=null;
							}
							UTILConstants.sensorsData=null;
							UTILConstants.sensorsData=modelRoomsWithSensors;
							resultSetToReturn.setDataList(modelRoomsList);
							
							
							if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTDeviceMaster") && finalactDeviceMasterHandle!=null) {
								actDeviceMasterHandle.populateDeviceControl(resultSetToReturn);

								handler.post(new Runnable() {
									public void run() {			
										if(finalactDeviceMasterHandle!=null){
											finalactDeviceMasterHandle.loadingLayout.setVisibility(View.GONE);
											finalactDeviceMasterHandle._UIBTReload.setVisibility(View.GONE);
											finalactDeviceMasterHandle.clearTimers();
										}
									}
								});


							}else if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTSensorControl") && finalactSensorControlHandle!=null) {
								actSensorControlHandle.populateSensorControl(UTILConstants.sensorsData);

								handler.post(new Runnable() {
									public void run() {	
										if(finalactSensorControlHandle!=null){
											finalactSensorControlHandle.loadingLayout.setVisibility(View.GONE);
											finalactSensorControlHandle._UIBTReload.setVisibility(View.GONE);
											finalactSensorControlHandle.clearTimers();
										}
									}
								});


							}
							
							if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "completed Tab sync Ack processing");
						}

					} catch (JSONException e) {
						e.printStackTrace();
						//Log.e("error","" + e.toString());
						resultSetToReturn.setError("Error");
						resultSetToReturn.setMessage(e.toString());
					}
					
					

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}
}
