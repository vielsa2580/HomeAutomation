package com.orvito.homevito.incmsgprocessors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.presentors.ACTHomepage;
import com.orvito.homevito.presentors.ACTUserLogin;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMTabRegAck {

	Context context;
	Handler handler;

	public IMTabRegAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}
	public void processTabRegAck(byte[] msg, final MODELReqPacket reqPacket) throws Exception {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received Tab Reg Ack");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTUserLogin")) {
			final ACTUserLogin actHandle = (ACTUserLogin) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode != 0) {

				handler.post(new Runnable() {
					public void run() {
						actHandle.loginProgress.setVisibility(View.GONE);
						actHandle.clearTimers();
						UTILConstants.toastMsg(handler, actHandle,  "Error Code" + errCode);		
					}
				});
				

			} else {
				actHandle.loginProgress.setVisibility(View.GONE);
				actHandle.clearTimers();

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
					// Log.v("jsonResponseByteArray j="+(j-9)+" msg["+(j)+"]",
					// "char:"+(char)msg[j]+"   byte"+msg[j]);
				}
				String jsonResponse = new String(jsonResponseByteArray);

				try {
					JSONObject serverResponse = new JSONObject(jsonResponse);
					final JSONObject finalServerResponse = serverResponse;
					if (serverResponse.getString("error").length() > 0) {						

						UTILConstants.toastMsg(handler, context, finalServerResponse.getString("message"));						

					} else {
						serverResponse = serverResponse.getJSONObject("userInfo");

						UTILSharedPreference.setPreference(actHandle,UTILConstants.AUTHTOKEN,serverResponse.getString("authToken"));
						UTILSharedPreference.setPreference(actHandle,UTILConstants.FIRSTNAME,serverResponse.getString("firstName"));
						UTILSharedPreference.setPreference(actHandle,UTILConstants.LASTNAME,serverResponse.getString("lastName"));
						UTILSharedPreference.setPreference(actHandle,UTILConstants.DOB,serverResponse.getString("dateOfBirth"));
						UTILSharedPreference.setPreference(actHandle,UTILConstants.EMAIL,serverResponse.getString("email"));
						UTILSharedPreference.setPreference(actHandle,UTILConstants.PHONENUM,serverResponse.getString("phoneNum"));
						//if(UTILConstants.debugModeForLogs) Log.e("authToken received",serverResponse.getString("authToken"));

//						byte[] decodedImageArray = Base64.decode(serverResponse.getString("photo").getBytes(),Base64.NO_PADDING);
//						Bitmap bmp = BitmapFactory.decodeByteArray(decodedImageArray, 0, decodedImageArray.length);

						/*
						 * try { File file=new
						 * File("/sdcard/imagedownloaded.jpg");
						 * file.createNewFile(); FileOutputStream out = new
						 * FileOutputStream(file);
						 * bmp.compress(Bitmap.CompressFormat.JPEG, 90, out); }
						 * catch (FileNotFoundException e) {
						 * e.printStackTrace(); } catch (IOException e) {
						 * e.printStackTrace(); }
						 */
						actHandle.loginProgress.setVisibility(View.GONE);
						actHandle.clearTimers();
						Intent intentToHomePage = new Intent(actHandle,ACTHomepage.class);
						actHandle.startActivity(intentToHomePage);
						actHandle.finish();
						
						if(UTILConstants.debugModeForToasts) Log.v("INC_MSG", "completed Tab Reg Ack processing");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}// end of activity on top check
	}


}
