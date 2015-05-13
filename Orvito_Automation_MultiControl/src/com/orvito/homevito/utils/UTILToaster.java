package com.orvito.homevito.utils;

import android.app.Activity;
import android.widget.Toast;

public class UTILToaster {
	
	public static void showLongToast(Activity activity,String message){
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();		
	}

}
