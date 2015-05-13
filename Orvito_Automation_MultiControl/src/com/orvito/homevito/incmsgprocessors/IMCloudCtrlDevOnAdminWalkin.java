package com.orvito.homevito.incmsgprocessors;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.models.MODELNode;
import com.orvito.homevito.outmsgfactory.OMCtrlDeviceList;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;

public class IMCloudCtrlDevOnAdminWalkin {


	Context context;
	Handler handler;

	public IMCloudCtrlDevOnAdminWalkin(Context context,Handler handler){
		this.context=context;
		this.handler=handler;
	}


	public void processctrlDevOnAdminWalkin(byte[] msg)throws Exception  {
		//if (UTILConstants.isActivityOnTop(context,"com.orvito.homevito.presentors.ACTDeviceControl")) {		
		/*
		MsgHeader ( 4 bytes ),  tabletAuthToken( 32 bytes ), 
   		number of PCBs ( 1 byte )
		[ PcbIp( 4 bytes ),  pcbPort( 2 bytes ), PcbAuth( 32 bytes ), 
		numberOfNodes( 1 bytes ), Node1( 1 byte ), State1( 1 byte ), Node2( 1 byte ), State2( 1byte ).....  ]*/
		if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "received control device on admin Walkin from cloud");
		if (UTILConstants.PROTOCOLVERSION == (char) msg[0]) {
			

			byte[] tabAuth=new byte[32];
			for (int i = 0; i < tabAuth.length; i++) {
				tabAuth[i]=msg[i+4];
			}
			if(UTILSharedPreference.getPreference(context, UTILConstants.AUTHTOKEN).equals(new String(tabAuth))){
				
				int numOfPcb=(int)msg[36];
				int currentNode=37;
				for (int i = 0; i < numOfPcb; i++) {
					int numOfNodesInthisPCB=msg[currentNode+38];
					byte[] pcbDataByteArray= new byte[39+(2*numOfNodesInthisPCB)];
					for (int j = 0; j < pcbDataByteArray.length; j++) {
						pcbDataByteArray[i]=msg[currentNode++];
					}
					
					String pcbIpAddress=""+((int )msg[0] & 0xff)+"."+((int )msg[1] & 0xff)+"."+((int )msg[2] & 0xff)+"."+((int )msg[3] & 0xff);//+":"+(int)msg[71]+":"+(int)msg[72]+":"+(int)msg[73];
					byte[] pcbPortArray=new byte[2];
					pcbPortArray[0]=msg[4];
					pcbPortArray[1]=msg[5];
					
					byte[] pcbAuth=new byte[32];
					for (int j = 6; j <=37; j++) {
						pcbAuth[j-6]=msg[j];
					}
					
					List<MODELNode> nodeList=new ArrayList<MODELNode>();
					for (int j = 0,nodepos=76; j < numOfNodesInthisPCB; j++,nodepos++) {
						MODELNode modelNode=new MODELNode();
						modelNode.setNodeNum(""+msg[nodepos]);
						modelNode.setState(""+msg[++nodepos]);
						nodeList.add(modelNode);
						
					}
					new OMCtrlDeviceList().ctrlDevice(context,handler, nodeList, pcbIpAddress, Integer.valueOf(new String(pcbPortArray)), ""+pcbAuth);
					if(UTILConstants.debugModeForLogs) Log.v("INC_MSG", "completed received control device on admin Walkin from cloud processing");
				}				
			}
		}	
		
		//}// end of activity on top check
	}


}
