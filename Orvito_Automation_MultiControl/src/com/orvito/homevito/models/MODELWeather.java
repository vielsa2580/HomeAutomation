package com.orvito.homevito.models;

import java.util.Calendar;

import com.orvito.homevito.presentors.ACTHomepage;

import android.app.Activity;
import android.graphics.Color;


public class MODELWeather {

	String city,temperature,maxTemp,minTemp,climateDesc;
	int weatherImage;
	int weatherBackground;
	int textColorCode;


	public int getTextColorCode() {
		return textColorCode;
	}
	public void setTextColorCode(int textColorCode) {
		this.textColorCode = textColorCode;
	}
	public int getWeatherBackground() {
		return weatherBackground;
	}
	public void setWeatherBackground(int weatherBackground) {
		this.weatherBackground = weatherBackground;
	}
	Activity activity;



	public MODELWeather(Activity activity) {
		this.activity = activity;
	}
	public String getCity() {
		return city;
	}
	public String getClimateDesc() {
		return climateDesc;
	}
	public void setClimateDesc(String climateDesc) {
		this.climateDesc = climateDesc;

		/*Sunny
Snowing/Ice/Sleet
Raining
Cloudy/Overcast
Drizzle
Fog/Mist
Thunder
Blizzard
		 */
		String nameOfImage="na";

		if(climateDesc.toLowerCase().contains("sunny") || climateDesc.toLowerCase().contains("clear")){
			nameOfImage="clear";
		}else if(climateDesc.toLowerCase().contains("snow") || climateDesc.toLowerCase().contains("ice") || climateDesc.toLowerCase().contains("sleet")){
			nameOfImage="snow";
		}else if(climateDesc.toLowerCase().contains("rain") || climateDesc.toLowerCase().contains("drizzle")){
			nameOfImage="rain";
		}else if(climateDesc.toLowerCase().contains("cloud") || climateDesc.toLowerCase().contains("overcast")){
			nameOfImage="cloudy";
		}else if(climateDesc.toLowerCase().contains("fog") || climateDesc.toLowerCase().contains("mist")){
			nameOfImage="fog";
		}else if(climateDesc.toLowerCase().contains("thunder")){
			nameOfImage="thunder";
		}else if(climateDesc.toLowerCase().contains("blizzard")  || climateDesc.toLowerCase().contains("wind")){
			nameOfImage="wind";
		}

		//nameOfImage="fog";
		setTextColorCode(Color.WHITE);
		ACTHomepage.lastColorCodeUsed=Color.WHITE;
		Calendar calendar=Calendar.getInstance();
		if(calendar.get(Calendar.HOUR_OF_DAY)>=6 && calendar.get(Calendar.HOUR_OF_DAY)<19){
			setWeatherImage( activity.getResources().getIdentifier("day"+nameOfImage, "drawable", "com.orvito.homevito"));
			setWeatherBackground( activity.getResources().getIdentifier("day"+nameOfImage+"bg", "drawable", "com.orvito.homevito"));

			if(nameOfImage.equals("fog") || nameOfImage.equals("thunder")){
				setTextColorCode(Color.BLACK);
				ACTHomepage.lastColorCodeUsed=Color.BLACK;
			}
		}else{
			setWeatherImage( activity.getResources().getIdentifier("night"+nameOfImage, "drawable", "com.orvito.homevito"));
			setWeatherBackground( activity.getResources().getIdentifier("night"+nameOfImage+"bg", "drawable", "com.orvito.homevito"));
		}




	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getMaxTemp() {
		return maxTemp;
	}
	public void setMaxTemp(String maxTemp) {
		this.maxTemp = maxTemp;
	}
	public String getMinTemp() {
		return minTemp;
	}
	public void setMinTemp(String minTemp) {
		this.minTemp = minTemp;
	}
	public int getWeatherImage() {
		return weatherImage;
	}
	public void setWeatherImage(int weatherImage) {
		this.weatherImage = weatherImage;
	}

}
