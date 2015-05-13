package com.orvito.homevito.incmsgprocessors;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMEmpImageAck {


	Context context;
	Handler handler;

	public IMEmpImageAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}

	public void processEmpImageAck(byte[] msg, final MODELReqPacket reqPacket) throws Exception  {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received control device on admin Walkin from cloud");
		/*if(UTILConstants.debugModeForLogs) {
			for (int i = 37; i < 45; i++) {
				Log.e("msg", i+"  "+(char)msg[i]+"  "+msg[i]);
			}
		}*/

		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTUserDirectory")) {

			//if(UTILConstants.debugModeForLogs) Log.e("crossed", "SeqNum");
			Activity actHandle = (Activity) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode != 0) {				
				UTILConstants.toastMsg(handler,context," Aborting Emp-Image-Ack Error Code" + errCode);				

			} else {
				//if(UTILConstants.debugModeForLogs) Log.e("crossed", "Errorcode");				
				byte[] tabAuthTokenByteArray=new byte[32];//	
				for (int i = 0; i < tabAuthTokenByteArray.length; i++) {
					tabAuthTokenByteArray[i]=msg[i+5];
				}
				String tabAuth=UTILSharedPreference.getPreference(actHandle, UTILConstants.AUTHTOKEN);
				if(tabAuth.equals(new String(tabAuthTokenByteArray))){
					byte[] jsonLengthInArray=new byte[4];
					for (int i = 0; i < jsonLengthInArray.length; i++) {
						jsonLengthInArray[i]=msg[i+37];
					}


					Integer jsonLength=UTILConstants.byteArrayToInt(jsonLengthInArray);
					//if(UTILConstants.debugModeForLogs) Log.e("jsonLength", ""+jsonLength);
					byte[] jsonResponse=new byte[jsonLength];
					for (int i = 0; i < jsonLength; i++) {
						jsonResponse[i]=msg[i+41];
					}

					//if(UTILConstants.debugModeForLogs) Log.e("jsonresponse", new String(jsonResponse));
					try {
						JSONObject serverResponse = new JSONObject( new String(jsonResponse));
						final JSONObject finalServerResponse = serverResponse;

						if (serverResponse.getString("error").length() > 0) {
							UTILConstants.toastMsg(handler, context,finalServerResponse.getString("message"));
						} else {

							String photo=serverResponse.getString("photo");
							if(photo.length()>0){
								byte[] decodedImageArray = Base64.decode(photo.getBytes(),Base64.NO_PADDING);
								Bitmap bmp = BitmapFactory.decodeByteArray(decodedImageArray, 0, decodedImageArray.length);
								View view=(View) reqPacket.getObject();
								ImageView residentImage=(ImageView) view.findViewById(R.id.empimage);
								ImageView residentOriginalImage=(ImageView) view.findViewById(R.id.originalImage);
								residentOriginalImage.setBackgroundDrawable(new BitmapDrawable(bmp));
								bmp=Bitmap.createScaledBitmap(bmp, 64, 64, false);	//image is scaled here							
								residentImage.setBackgroundDrawable(new BitmapDrawable(bmp));

							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
					}


				}	
				if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "completed control device on admin Walkin from cloud processing");
			}
		}// end of activity on top check
	}
}
