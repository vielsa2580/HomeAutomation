package com.orvito.homevito.incmsgprocessors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.models.MODELWeather;
import com.orvito.homevito.presentors.ACTHomepage;
import com.orvito.homevito.utils.UTILConstants;

public class IMWeatherAck {

	Context context;
	Handler handler;

	public IMWeatherAck(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}

	public void processWeatherAck(byte[] msg, final MODELReqPacket reqPacket)throws Exception  {
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received weather Ack");
		if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTHomepage")) {
			//Log.e("crossed", "SeqNum");
			final Activity actHandle = (Activity) reqPacket.getActionHandle();
			final int errCode = (int) msg[4];
			if (errCode != 0) {
				handler.post(new Runnable() {
					public void run() {
						Toast.makeText(context, "Error Code" + errCode,Toast.LENGTH_LONG).show();
					}
				});

			} else {
				//if(UTILConstants.debugModeForLogs) Log.e("crossed", "Errorcode");
				byte[] jsonLengthTemp = new byte[4];
				jsonLengthTemp[0] = msg[5];
				jsonLengthTemp[1] = msg[6];
				jsonLengthTemp[2] = msg[7];
				jsonLengthTemp[3] = msg[8];

				int jsonLength = UTILConstants.byteArrayToInt(jsonLengthTemp);

				//Log.v("jsonLength received", "" + jsonLength);
				byte[] jsonResponseByteArray = new byte[jsonLength];
				for (int j = 9; j < jsonResponseByteArray.length + 9; j++) {
					jsonResponseByteArray[j - 9] = msg[j];
					// Log.v("jsonResponseByteArray j="+(j-9)+" msg["+(j)+"]",
					// "char:"+(char)msg[j]+"   byte"+msg[j]);
				}
				String jsonResponse = new String(jsonResponseByteArray);
				try {
					JSONObject serverResponse = new JSONObject(jsonResponse);
					final JSONObject jsonObject = serverResponse;
					if (serverResponse.length() < 0) {
						handler.post(new Runnable() {
							public void run() {
								Toast.makeText(context, "No Json Data",Toast.LENGTH_LONG).show();
							}
						});

					} else {

						JSONObject finalServerResponse = jsonObject.getJSONObject("data");

						JSONArray jsonArray = (JSONArray) finalServerResponse.get("current_condition");
						JSONObject currentWeatherJsonObject = (JSONObject) jsonArray.get(0);
						final MODELWeather modelWeather = new MODELWeather(actHandle);
						modelWeather.setTemperature(currentWeatherJsonObject.getString("temp_C"));

						jsonArray = (JSONArray) currentWeatherJsonObject.get("weatherDesc");
						JSONObject climateDescJsonObject = (JSONObject) jsonArray.get(0);
						modelWeather.setClimateDesc(climateDescJsonObject.getString("value"));
						//modelWeather.setClimateDesc("sunny");

						jsonArray = (JSONArray) finalServerResponse.get("request");
						JSONObject areaJsonObject = (JSONObject) jsonArray.get(0);
						modelWeather.setCity(areaJsonObject.getString("query"));

						jsonArray = (JSONArray) finalServerResponse.get("weather");
						JSONObject todaysWeatherJsonObject = (JSONObject) jsonArray.get(0);
						modelWeather.setMaxTemp(todaysWeatherJsonObject.getString("tempMaxC"));
						modelWeather.setMinTemp(todaysWeatherJsonObject.getString("tempMinC"));
						final TextView temperature = (TextView) actHandle.findViewById(R.id.temperature);
						final TextView maxmin = (TextView) actHandle.findViewById(R.id.maxmintemp);
						final TextView cityTv = (TextView) actHandle.findViewById(R.id.city);
						final TextView weatherdesc = (TextView) actHandle.findViewById(R.id.weatherdesc);
						final ImageView weatherImage = (ImageView) actHandle.findViewById(R.id.weatherimage);
						final ImageView naImage = (ImageView) actHandle.findViewById(R.id.na);
						final TextSwitcher timehour=(TextSwitcher) actHandle.findViewById(R.id.timehour);
						final TextSwitcher timeminute=(TextSwitcher) actHandle.findViewById(R.id.timeminute);
						final TextView ampm=(TextView) actHandle.findViewById(R.id.ampm);
						final TextView date=(TextView) actHandle.findViewById(R.id.date);
						final TextSwitcher timesecond=(TextSwitcher) actHandle.findViewById(R.id.timesecond);
						final LinearLayout weatherLayout = (LinearLayout) actHandle.findViewById(R.id.weatherlayout);

						final String currentTemp = modelWeather.getTemperature() + "°";
						final String minmax = modelWeather.getMinTemp()+ "° / " + modelWeather.getMaxTemp() + "°";
						final String climateDesc = modelWeather.getClimateDesc();
						final String city = modelWeather.getCity();
						actHandle.runOnUiThread(new Runnable() {
							public void run() {

								temperature.setText(currentTemp);
								temperature.setTextColor(modelWeather.getTextColorCode());
								maxmin.setText(minmax);
								maxmin.setTextColor(modelWeather.getTextColorCode());
								weatherdesc.setText(climateDesc);
								weatherdesc.setTextColor(modelWeather.getTextColorCode());
								cityTv.setText(city);
								cityTv.setTextColor(modelWeather.getTextColorCode());
								weatherImage.setBackgroundResource(modelWeather.getWeatherImage());
								//Log.e("weather", ""+modelWeather.getWeatherBackground());
								weatherLayout.setBackgroundResource(modelWeather.getWeatherBackground());
								naImage.setVisibility(View.GONE);
								UTILConstants.toastMsg(context, "Weather update succesful");
								
								((TextView)timehour.getCurrentView()).setTextColor(modelWeather.getTextColorCode());
								((TextView)timeminute.getCurrentView()).setTextColor(modelWeather.getTextColorCode());
								ampm.setTextColor(modelWeather.getTextColorCode());
								date.setTextColor(modelWeather.getTextColorCode());
								((TextView)timesecond.getCurrentView()).setTextColor(modelWeather.getTextColorCode());

								((ACTHomepage)actHandle).TVTimehour.setTextColor(modelWeather.getTextColorCode());
								((ACTHomepage)actHandle).TVTimeminute.setTextColor(modelWeather.getTextColorCode());
								((ACTHomepage)actHandle).TVTimesecond.setTextColor(modelWeather.getTextColorCode());
								ACTHomepage.lastColorCodeUsed=modelWeather.getTextColorCode();
							}
						});

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "completed weather Ack processing");
			}
		}// end of activity on top check
	}
}
