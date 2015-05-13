package com.orvito.homevito.helpers;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELUser;
import com.orvito.homevito.outmsgfactory.OMEmpImagePull;
import com.orvito.homevito.utils.UTILConstants;


public class ADPTUserGrid  extends ArrayAdapter {

	Activity activity;
	List<MODELUser> residentsList;
	List<View> viewsList=null;
	Handler handler;
	

	public ADPTUserGrid(Activity activity,Handler handler,List<MODELUser> residentList) {

		super(activity.getBaseContext(),R.layout.icon_user,residentList);
		this.residentsList = residentList;
		this.activity=activity;
		viewsList=new ArrayList<View>(this.residentsList .size());
		this.handler=handler;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		try{
			View bufferedView=viewsList.get(position);
			if(bufferedView!=null){
				return bufferedView;
			}
		}catch(Exception e){}

		LayoutInflater inflator=activity.getLayoutInflater();
		final View row=inflator.inflate(R.layout.icon_user, null);

		RelativeLayout layout=(RelativeLayout) row.findViewById(R.id.image);
		layout.setBackgroundColor(UTILConstants.getUsersColor(position, activity));
		TextView _UITVName = (TextView)row.findViewById(R.id.smallresidentname);
		TextView _UITVAddress = (TextView)row.findViewById(R.id.smallresidentaddress);
		ImageView _UIIVResidentImage=(ImageView) row.findViewById(R.id.empimage);
		ImageView _UIIVResidentOriginalImage=(ImageView) row.findViewById(R.id.originalImage);

		_UITVName.setText(residentsList.get(position).getFirstName()+" "+residentsList.get(position).getLastName().substring(0,1));
		//_UITVAddress.setText(residentsList.get(position).getBlockName()+" , "+residentsList.get(position).getFlatNo()+" , "+residentsList.get(position).getAddress());
		_UITVAddress.setText(residentsList.get(position).getDesignation()+", "+residentsList.get(position).getDepartment());

		new Thread(new Runnable() {			
			@Override
			public void run() {
				new OMEmpImagePull().requestImage(activity, handler,residentsList.get(position).getUserId(), row);				
				
			}
		}).start();
		
			viewsList.add(position, row);
		return(row);
		
	}
	
	
}
