package com.orvito.homevito.incmsgprocessors;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELNodeStatus;
import com.orvito.homevito.models.MODELHardwareType;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELRoom;
import com.orvito.homevito.presentors.ACTDeviceMaster;
import com.orvito.homevito.utils.UTILConstants;

public class IMCloudStatusUpdate {


	Context context;
	Handler handler;

	public IMCloudStatusUpdate(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}


	public void processStatusUpdateFromCloud(byte[] msg) throws Exception  {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received Status Update From Cloud");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTDeviceMaster")) {
			Log.v("NUM OF BYTES RECEIVED", ""+msg.length);
			if (UTILConstants.PROTOCOLVERSION == (char) msg[0]) {

				byte[] jsonLengthArray=new byte[4];
				
				for (int i = 0; i < jsonLengthArray.length; i++) {
					jsonLengthArray[i]=msg[i+4];
				}

				int jsonLength = UTILConstants.byteArrayToInt(jsonLengthArray);

				if(UTILConstants.debugModeForLogs) Log.v("jsonLength received", "" + jsonLength);
				byte[] jsonResponseByteArray = new byte[jsonLength];
				for (int j = 9; j < jsonResponseByteArray.length + 9; j++) {
					jsonResponseByteArray[j - 9] = msg[j];
					// Log.v("jsonResponseByteArray j="+(j-9)+" msg["+(j)+"]",
					// "char:"+(char)msg[j]+"   byte"+msg[j]);
				}
				String jsonResponse = new String(jsonResponseByteArray);
				Log.v("COMPLETE INCOMING RESULT", ""+jsonResponse);
				try {
					JSONObject jsonObject = new JSONObject(jsonResponse);
					
					

					if (jsonObject.length() < 0) {
						Log.v("NO VALUE", "");
						UTILConstants.toastMsg(handler, context, "No Json Data");
					} else {
						
						

						/**/
						MODELResultSet resultSetToReturn = new MODELResultSet();
						try {
							if (jsonObject.get("error").toString().length() > 0) {

								resultSetToReturn.setError(jsonObject.get("error").toString());
								resultSetToReturn.setMessage(jsonObject.get("message").toString());

							} else {

								resultSetToReturn.setMessage(jsonObject.get("message").toString());
								JSONArray roomsJsonArray = (JSONArray) jsonObject.get("nodeList");
								List<MODELRoom> modelRoomsList = new ArrayList<MODELRoom>();
								JSONObject jsonObject2;
								for (int i = 0; i < roomsJsonArray.length(); i++) {
									jsonObject2 = (JSONObject) roomsJsonArray.get(i);
									MODELRoom modelRoom = new MODELRoom(jsonObject2.getString("grpId"),jsonObject2.getString("grpName"));
									JSONArray devicesJsonArray = (JSONArray) jsonObject2.get("groupNodes");
									List<MODELNode> modelNodesList = new ArrayList<MODELNode>();
									JSONObject jsonObject3;
									for (int j = 0; j < devicesJsonArray.length(); j++) {
										jsonObject3 = (JSONObject) devicesJsonArray.get(j);

										MODELNode modelNode = new MODELNode(jsonObject3.getString("nodeName"),jsonObject3.getString("IpAddr"),
												jsonObject3.getString("port"),jsonObject3.getString("devAuthToken"),jsonObject3.getString("nodeNum"),
												new MODELHardwareType("",jsonObject3.getString("nodeTypeName")));

										jsonObject3 = (JSONObject) jsonObject3.get("nodeStatusRec");

										MODELNodeStatus modelDeviceStatus = new MODELNodeStatus();
										modelDeviceStatus.setTimeStamp(jsonObject3.getString("timeStamp"));
										modelDeviceStatus.setStatus(jsonObject3.getString("status"));
										modelDeviceStatus.setState(jsonObject3.getString("state"));
										modelNode.setNodeStatus(modelDeviceStatus);

										modelNodesList.add(modelNode);
										modelNode = null;
									}
									modelRoom.setNodeList(modelNodesList);
									modelNodesList = null;
									modelRoomsList.add(modelRoom);
									modelRoom = null;

								}
								resultSetToReturn.setDataList(modelRoomsList);
								context.startActivity(new Intent(context,ACTDeviceMaster.class));
							}

						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}catch (Exception e) {
					e.printStackTrace();				
				}
			}
		}
	}
}



