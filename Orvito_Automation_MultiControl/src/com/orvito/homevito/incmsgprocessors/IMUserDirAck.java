package com.orvito.homevito.incmsgprocessors;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orvito.homevito.models.MODELBlock;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.models.MODELUser;
import com.orvito.homevito.presentors.ACTUserDirectory;
import com.orvito.homevito.utils.UTILConstants;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class IMUserDirAck {


	Context context;
	Handler handler;

	public IMUserDirAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}

	public void processUserDirAck(byte[] msg, final MODELReqPacket reqPacket)throws Exception  {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received user dir Ack");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTUserDirectory")) {

			//if(UTILConstants.debugModeForLogs) Log.e("crossed", "SeqNum");
			final ACTUserDirectory actHandle = (ACTUserDirectory) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode != 0) {
				handler.post(new Runnable() {
					public void run() {
						Toast.makeText(context, "Error Code" + errCode,Toast.LENGTH_LONG).show();
						actHandle.loadingLayout.setVisibility(View.GONE);	
						actHandle._UIBTReload.setVisibility(View.VISIBLE);
						actHandle.clearTimers();
					}
				});

			} else {
				
//				handler.post(new Runnable() {
//					public void run() {					
//						actHandle.loadingLayout.setVisibility(View.GONE);
//						actHandle._UIBTReload.setVisibility(View.GONE);
//						actHandle.clearTimers();
//					}
//				});
				//if(UTILConstants.debugModeForLogs) Log.e("crossed", "Errorcode");
				byte[] jsonLengthTemp = new byte[4];
				jsonLengthTemp[0] = msg[5];
				jsonLengthTemp[1] = msg[6];
				jsonLengthTemp[2] = msg[7];
				jsonLengthTemp[3] = msg[8];

				int jsonLength = UTILConstants.byteArrayToInt(jsonLengthTemp);

				//if(UTILConstants.debugModeForLogs) Log.v("jsonLength received", "" + jsonLength);
				byte[] jsonResponseByteArray = new byte[jsonLength];
				for (int j = 9; j < jsonResponseByteArray.length + 9; j++) {
					jsonResponseByteArray[j - 9] = msg[j];
				}
				String jsonResponse = new String(jsonResponseByteArray);
				//if(UTILConstants.debugModeForLogs) Log.v("empdirjsonResponse", jsonResponse);
				try {
					JSONObject jsonObject = new JSONObject(jsonResponse);

					if (jsonObject.length() < 0) {
						handler.post(new Runnable() {
							public void run() {
								Toast.makeText(context, "Invalid server response",Toast.LENGTH_LONG).show();
							}
						});

					} else {

						MODELResultSet resultSetToReturn = new MODELResultSet();
						try {
							if (jsonObject.get("error").toString().length() > 0) {

								resultSetToReturn.setError(jsonObject.get("error").toString());
								resultSetToReturn.setMessage(jsonObject.get("message").toString());

							} else {
								resultSetToReturn.setMessage(jsonObject.get("message").toString());
								JSONArray jsonArray = (JSONArray) jsonObject.get("employeeDir");

								List<MODELBlock> blockList = new ArrayList<MODELBlock>();
								MODELBlock modelBlock = new MODELBlock();
								modelBlock.setBlockName("");
								List<MODELUser> userList = new ArrayList<MODELUser>();
								for (int i = 0; i < jsonArray.length(); i++) {

									jsonObject = (JSONObject) jsonArray.get(i);

									MODELUser modelUser = new MODELUser();

									modelUser.setUserId(jsonObject.getString("userId"));
									modelUser.setFirstName(jsonObject.getString("firstName"));
									modelUser.setLastName(jsonObject.getString("lastName"));
									//modelUser.setDob(jsonObject
									//.getString("DOB"));
									modelUser.setEmail(jsonObject.getString("email"));
									modelUser.setPhoneNum(jsonObject.getString("phoneNum"));
									modelUser.setAddress(jsonObject.getString("address"));
									modelUser.setDepartment(jsonObject.getJSONObject("department").getString("name"));
									modelUser.setDesignation(jsonObject.getJSONObject("designation").getString("name"));
									modelUser.setBlockName("");
									modelUser.setFlatNo("");									
									modelUser.setSkills(jsonObject.getString("skills"));
									modelUser.setHobbies(jsonObject.getString("hobbies"));
									userList.add(modelUser);
									modelUser = null;
								}
								modelBlock.setModelUserList(userList);
								blockList.add(modelBlock);
								modelBlock = null;
								resultSetToReturn.setDataList(blockList);
								actHandle.populateUserData(resultSetToReturn);
								
								handler.post(new Runnable() {
									public void run() {										
										actHandle.loadingLayout.setVisibility(View.GONE);
										actHandle._UIBTReload.setVisibility(View.GONE);
										actHandle.clearTimers();
									}
								});

							}

						} catch (JSONException e) {
							e.printStackTrace();
							//Log.e("Exception at Class:DFSERVERDocChem, Method:getDocChem","Exception:" + e.toString());
							resultSetToReturn.setError("Error");
							resultSetToReturn.setMessage(e.toString());
						}

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "completed user dir Ack processing");
			}
		}// end of activity on top check
	}
}
