package com.orvito.homevito.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UTILSharedPreference {

	


	public static String getPreference(Activity activity,String prefKey){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		return sharedPreferences.getString(prefKey, null);
	}
	
	public static String getPreference(Context context,String prefKey){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(prefKey, null);
	}


	public static void setPreference(Activity activity, String prefKey, String prefValue) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(prefKey,prefValue);
		editor.commit();
	}
	
	public static void setPreference(Context context, String prefKey, String prefValue) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(prefKey,prefValue);
		editor.commit();
	}
	
	

}
