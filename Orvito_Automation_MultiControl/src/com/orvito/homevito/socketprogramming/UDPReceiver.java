package com.orvito.homevito.socketprogramming;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.incmsgprocessors.IMCtrlDevAck;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.utils.UTILConstants;

public class UDPReceiver extends Thread {

	Context context;
	Handler handler;
	public static DatagramSocket ds = null;

	public UDPReceiver(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void run() {
		startUDPServer(UTILConstants.UDPRECEIVERPORT);
	}

	private void startUDPServer(int receiverPort) {
		DatagramSocket ds = null;
		try {	
			Boolean end = false;			
			while (!end) {
				final byte[] lMsg = new byte[15000];
				DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
				ds = new DatagramSocket(receiverPort);
				UTILConstants.UDPRECEIVERPORT=receiverPort;
				if(UTILConstants.debugModeForLogs) Log.d("Socket_programming", "Started UDP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort);
				ds.setBroadcast(true);
				
				ds.receive(dp);
				ds.setSoTimeout(5000);
				//Log.e("UDP packet received", new String(lMsg, 0, dp.getLength()));							
				ds.close();
				new Thread(new Runnable() {
					public void run() {
							try {
								processMsg(lMsg);	
							} catch (Exception e) {
								e.printStackTrace();
							}						
					}
				}).start();
			}

		} catch (InterruptedIOException e) {// this exception occurs when the socket times out so we need to restart the server
			//e.printStackTrace();
			if(ds!=null){
				try {
					ds.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			if(UTILConstants.debugModeForLogs) Log.e("Socket_programming", "Stopped UDP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort+"...restarting now..   ");
			startUDPServer((receiverPort));
		} catch (IOException e) {// this exception occurs when the needed port
			// is unvailable so we need to restart with
			// a new port
			//e.printStackTrace();
			if(UTILConstants.debugModeForLogs) Log.e("Socket_programming", "Failed to start UDP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort+"...restarting now on"+(receiverPort+2));
			startUDPServer((receiverPort + 2));
		} catch (Exception e) {
			e.printStackTrace();
			startUDPServer((receiverPort));
		}
	}
	
	public void closeExistingSocket(){
		if(ds!=null){
			Log.v("socket_programming", "udp server socket is not null so closing");
			try {
				//ds.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else{
			Log.v("socket_programming", "udp server socket is already null");
		}
	}

	private void processMsg(final byte[] msg) {

		final MODELReqPacket reqPacket = processHeader(msg);
		if (reqPacket == null) {
			if(UTILConstants.debugModeForToasts) UTILConstants.toastMsg(handler,context,"Unidentifed Data");
			if(UTILConstants.debugModeForLogs) Log.v("UDP_Server", "Unidentified Data");
			return;
		}

		//Log.e("RECEIVED MSG TYPE", "" + reqPacket.getMsgType());

		//Activity activity = (Activity) reqPacket.getActionHandle();


		handler.post(new Runnable() {
			public void run() {
				try{
				switch (reqPacket.getMsgType()) {


				case UTILConstants.CTRLDEVACK:
					//Log.e("crossed", "CTRLDEVACK");
					//UTILConstants.toastMsg(handler,context,"received ctrl dev ack");
					new IMCtrlDevAck(context,handler).processCtrlDevAck(msg, reqPacket);
					break;

				default:
					if(UTILConstants.debugModeForLogs) Log.v("unidentified", "msgtype unmatch");
					UTILConstants.toastMsg(handler,context,"MessageType doesnt match. Received:"+ reqPacket.getMsgType());
					break;

				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});


	}

	private MODELReqPacket processHeader(byte[] msg) {
		if (msg == null) {
			return null;
		} else if (msg.length == 0) {
			return null;
		}

		if (UTILConstants.PROTOCOLVERSION == (char) msg[0]) {
			//Log.e("crossed", "PROTOCOLVERSION");

			byte[] seqNumArray = new byte[2];
			seqNumArray[0] = msg[2];
			seqNumArray[1] = msg[3];

			int seqNumInt = UTILConstants.byteArrayToInt(seqNumArray);
			//Log.e("seqnum received", "" + seqNumInt);
			UTILConstants.removeTimeLapsedRequestPackets();
			for (int i = 0; i < UTILConstants.reqPacketList.size(); i++) {

				if (seqNumInt == UTILConstants.reqPacketList.get(i).getSequenceNumber()) {
					//Log.e("crossed", "SeqNum");
					MODELReqPacket reqPacket = new MODELReqPacket(UTILConstants.reqPacketList.get(i));
					reqPacket.setMsgType((char) msg[1]);
					UTILConstants.reqPacketList.remove(i);
					return reqPacket;
				}
			}
		}

		return null;
	}





}
