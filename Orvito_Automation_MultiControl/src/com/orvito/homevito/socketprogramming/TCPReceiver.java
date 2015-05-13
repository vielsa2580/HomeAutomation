package com.orvito.homevito.socketprogramming;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.orvito.homevito.incmsgprocessors.IMCloudCtrlDev;
import com.orvito.homevito.incmsgprocessors.IMCloudCtrlDevOnAdminWalkin;
import com.orvito.homevito.incmsgprocessors.IMCloudStatusUpdate;
import com.orvito.homevito.incmsgprocessors.IMCtrlDevAck;
import com.orvito.homevito.incmsgprocessors.IMEmpImageAck;
import com.orvito.homevito.incmsgprocessors.IMInitiatedWenMsg;
import com.orvito.homevito.incmsgprocessors.IMSmartHubDisc;
import com.orvito.homevito.incmsgprocessors.IMStatusUpdateAck;
import com.orvito.homevito.incmsgprocessors.IMTabRegAck;
import com.orvito.homevito.incmsgprocessors.IMTabSyncAck;
import com.orvito.homevito.incmsgprocessors.IMUserDirAck;
import com.orvito.homevito.incmsgprocessors.IMWeatherAck;
import com.orvito.homevito.models.MODELReqPacket;
import com.orvito.homevito.utils.UTILConstants;

public class TCPReceiver extends Thread {

	Context context;
	Handler handler;
	ServerSocket serverSocket=null;
	String tcpIp;
	Socket socket;
	
	public TCPReceiver(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public void run() {
		startTCPServer(UTILConstants.TCPRECEIVERPORT);
	}

	private void startTCPServer(int receiverPort) {

		try {
			Boolean end = false;
			serverSocket = new ServerSocket(receiverPort);

			UTILConstants.TCPRECEIVERPORT = receiverPort;
			if(UTILConstants.debugModeForLogs) Log.d("Socket_programming", "Started TCP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort);
			while (!end) {
				// Server is waiting for client here, if needed
				 socket = serverSocket.accept();
				InputStream br = socket.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte buffer[] = new byte[1024];
				socket.setSoTimeout(20000);
				//if(UTILConstants.debugMode) Log.e("tcpserver", "received data will read it now");
				for (int s; (s=br.read(buffer, 0, buffer.length))!=-1;) {
					baos.write(buffer, 0, s);
				}

				final byte result[] = baos.toByteArray();
				socket.close();


				new Thread(new Runnable() {
					public void run() {
						//char msgType=(char) result[1];
						//if(msgType!=UTILConstants.STATUSUPDATEFROMCLOUD){
						try {
							processMsg(result);
						} catch (Exception e) {
							e.printStackTrace();
						}
						//}
					}
				}).start();
			}
			serverSocket.close();

		} catch (InterruptedIOException e) {// this exception occurs when the socket times out so we need to restart the server
			//e.printStackTrace();
			if(serverSocket!=null){
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(UTILConstants.debugModeForLogs) Log.e("Socket_programming", "Stopped TCP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort+"...restarting now..   ");
			startTCPServer((receiverPort));
		} catch (IOException e) {// this exception occurs when the needed port
			// is unvailable so we need to restart with
			// a new port
			//e.printStackTrace();
			if(UTILConstants.debugModeForLogs) Log.e("Socket_programming", "Failed to start TCP server at "+UTILConstants.getLocalIpAddress()+" on port:"+receiverPort+"...restarting now on"+(receiverPort+2));
			startTCPServer((receiverPort + 2));
		} catch (Exception e) {
			e.printStackTrace();
			startTCPServer((receiverPort));
		}
	}

	public void closeExistingSocket(){
		if(serverSocket!=null){
			try {
				Log.v("socket_programming", "tcp server socket is not null so closing");
				//serverSocket.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else{
			Log.v("socket_programming", "tcp server socket is already null");
		}
	}

	private void processMsg(final byte[] msg) throws Exception{



		//-----------------------the following two messages originate from cloud so we need not check for
		//-----------------------seq num or modelreqpacket in the static list
		char msgType=(char) msg[1];

		if(msgType==UTILConstants.CTRLDEVFROMCLOUD){
			//UTILConstants.toastMsg(handler,context,"Received ctrl device from cloud");
			new IMCloudCtrlDev(context, handler).processCtrlDevFromCloud(msg);
			return;
		}else if(msgType==UTILConstants.STATUSUPDATEFROMCLOUD){
			//UTILConstants.toastMsg(handler,context,"Received STATUS UPDATE from cloud");
			new IMCloudStatusUpdate(context, handler).processStatusUpdateFromCloud(msg);
			return;
		}else if(msgType==UTILConstants.AUTOCTRLDEVACK){
			//UTILConstants.toastMsg(handler,context,"Received AUTOCTRLDEVACK from cloud");
			new IMCloudCtrlDevOnAdminWalkin(context, handler).processctrlDevOnAdminWalkin(msg);
			return;
		}


		//-------------------the following msgs are the acks for msgs sent from the tablet


		final MODELReqPacket reqPacket = processHeader(msg);
		if (reqPacket == null) {
			if(UTILConstants.debugModeForToasts) UTILConstants.toastMsg(handler,context,"Unidentifed Data msg length:"+msg.length+"  msgTypechar:"+msgType+"  msgtypebyte"+msg[1]);
			if(UTILConstants.debugModeForLogs) Log.e("TCP_Server", "Unidentifed Data msg length:"+msg.length+"  msgTypechar:"+msgType+"  msgtypebyte"+msg[1]);
			return;
		}



		final Activity activity = (Activity) reqPacket.getActionHandle();

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try{

					switch (reqPacket.getMsgType()) {

					case UTILConstants.TABREGACK:
						new IMTabRegAck(context,handler).processTabRegAck(msg, reqPacket); 
						break;

					case UTILConstants.WEATHERACK:	
						new IMWeatherAck(context,handler).processWeatherAck(msg, reqPacket);
						break;

					case UTILConstants.USERDIRACK:
						new IMUserDirAck(context,handler).processUserDirAck(msg, reqPacket);
						break;

					case UTILConstants.TABSYNCACK:
						new IMTabSyncAck(context,handler).processTabSyncAck(msg, reqPacket);
						break;

					case UTILConstants.CTRLDEVACK:
						new IMCtrlDevAck(context,handler).processCtrlDevAck(msg, reqPacket);
						break;


					case UTILConstants.STATUSUPDATEACK:					
						new IMStatusUpdateAck(context,handler).processStatusUpdateAck(msg, reqPacket);
						break;

					case UTILConstants.USERIMAGEACK:						
						new IMEmpImageAck(context,handler).processEmpImageAck(msg, reqPacket);
						break;

					case UTILConstants.SHTOTABDISC:
						tcpIp = socket.getRemoteSocketAddress().toString();
						new IMSmartHubDisc(context, handler,tcpIp).processSHDisc(msg, reqPacket);
						break;
						
					case UTILConstants.INWENDISC:						
						
						new IMInitiatedWenMsg(activity, handler).processWenInitiation(msg, reqPacket);
						break;

					default:
						if(UTILConstants.debugModeForToasts)UTILConstants.toastMsg(handler,context,"Invalid message received.Received msg type:"+ reqPacket.getMsgType());
						if(UTILConstants.debugModeForLogs)Log.e("TCP_Server","Invalid message received.Received msg type:"+ reqPacket.getMsgType());
						break;

					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});

	}

	private MODELReqPacket processHeader(byte[] msg) throws Exception{
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
			MODELReqPacket reqPacket = null;
			for (int i = UTILConstants.reqPacketList.size()-1; i >= 0; i--) {

				if (seqNumInt == UTILConstants.reqPacketList.get(i).getSequenceNumber()) {
					//Log.e("crossed", "SeqNum");
					reqPacket = new MODELReqPacket(UTILConstants.reqPacketList.get(i));
					reqPacket.setMsgType((char) msg[1]);
					UTILConstants.reqPacketList.remove(i);
					break;
				}


			}
			return reqPacket;
		}

		return null;
	}





}
