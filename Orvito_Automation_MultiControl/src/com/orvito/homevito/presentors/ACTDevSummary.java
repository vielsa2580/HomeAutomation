package com.orvito.homevito.presentors;

import java.util.ArrayList;
import java.util.List;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.outmsgfactory.OMCtrlDeviceList;
import com.orvito.homevito.utils.UTILConstants;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ACTDevSummary extends Activity{

	TextView pirName;
	Button pirToggle;
	MODELNode pirNodeData=null;
	public static ACTDevSummary currentInstance=null;
	Handler handler;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devsummary);
		handler=new Handler();
		pirName=(TextView) findViewById(R.id.pirname);
		pirToggle=(Button) findViewById(R.id.pirtoggle);

		pirNodeData=UTILConstants.pirSensorData;

		if(pirNodeData!=null){
			String imageToPick = pirNodeData.getHardwareType().getName()+ pirNodeData.getNodeStatus().getState();
			pirToggle.setBackgroundResource(getResources().getIdentifier(imageToPick, "drawable",getPackageName()));			
			
			pirName.setText(pirNodeData.getName());
		}
		
		pirToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Integer ctrlcommandToSend=null;

				if(Integer.valueOf(pirNodeData.getNodeStatus().getState())==0){
					ctrlcommandToSend=1;
				}else if(Integer.valueOf(pirNodeData.getNodeStatus().getState())==1){
					ctrlcommandToSend=0;
				}else{
					ctrlcommandToSend=-1;
				}			


				if(ctrlcommandToSend!=-1){
					pirNodeData.setUiObjectID(pirToggle.getId());
					pirNodeData.setState(""+ctrlcommandToSend);
					List<MODELNode> nodeList=new ArrayList<MODELNode>();
					nodeList.add(pirNodeData);
					if(UTILConstants.debugModeForLogs) Log.e("devauthbeingsent", pirNodeData.getDevAuthToken());
					new OMCtrlDeviceList().ctrlDevice(ACTDevSummary.this,handler, nodeList,pirNodeData.getIpAddress(),Integer.valueOf(pirNodeData.getPort()), pirNodeData.getDevAuthToken());//(_activity,pirNodeData,ctrlcommandToSend);
				}
				
			}
		});
				
			
				
			
	}
	
	public void refreshData(String newState){
		pirNodeData.getNodeStatus().setState(newState);
		String imageToPick = pirNodeData.getHardwareType().getName()+ pirNodeData.getNodeStatus().getState();
		pirToggle.setBackgroundResource(getResources().getIdentifier(imageToPick, "drawable",getPackageName()));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		currentInstance=ACTDevSummary.this;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		ACTDevSummary.currentInstance=null;
	}
}
