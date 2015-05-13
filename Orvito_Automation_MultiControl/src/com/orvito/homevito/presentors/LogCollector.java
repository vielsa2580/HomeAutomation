package com.orvito.homevito.presentors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LogCollector {
	public final static String LINE_SEPARATOR = System.getProperty("line.separator");//$NON-NLS-1$
	private static final String LOG_TAG = LogCollector.class.getSimpleName();
	private Context context;
	private String mPackageName;
	private Pattern mPattern;
	private SharedPreferences mPrefs;
	private ArrayList<String> mLastLogs;
	String logData;

	public LogCollector(Context context) {
		this.context = context;
		mPackageName = context.getPackageName();
		String pattern = String.format("(.*)E\\/AndroidRuntime\\(\\s*\\d+\\)\\:\\s*at\\s%s.*", 
				mPackageName.replace(".", "\\."));
		mPattern = Pattern.compile(pattern);
		mPrefs = context.getSharedPreferences("LogCollector", Context.MODE_PRIVATE);
		mLastLogs = new ArrayList<String>();
	}

	private void collectLog(List<String> outLines, String format, String buffer, String[] filterSpecs){

		outLines.clear();

		ArrayList<String> params = new ArrayList<String>();

		if (format == null){
			format = "time";
		}

		params.add("-v");
		params.add(format);

		if (buffer != null){
			params.add("-b");
			params.add(buffer);
		}

		if (filterSpecs != null){
			for (String filterSpec : filterSpecs){
				params.add(filterSpec);
			}
		}

		try{
			ArrayList<String> commandLine = new ArrayList<String>();
			commandLine.add("logcat");//$NON-NLS-1$
			commandLine.add("-d");//$NON-NLS-1$
			commandLine.addAll(params);

			Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null){ 
				outLines.add(line);

				logData=bufferedReader.readLine();
				Log.v("LOG DATA", ""+outLines);	
				TestTwo(line);
			}
		} 
		catch (IOException e){
			Log.e(LOG_TAG, String.format("collectAndSendLog failed - format:%s, buffer:%s, filterSpecs:%s", 
					format, buffer, filterSpecs), e);
		} 

	}

	public void TestTwo (String logData){

		String path = "/sdcard/ErrorLog/";
		FileOutputStream fos;

		final File mediaDir = new File(path);
		if (!mediaDir.exists()){
			mediaDir.mkdir();
		}

		try {
			fos = new FileOutputStream(path+"ErrorLog.txt",true);
			fos.write(logData.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public boolean hasForceCloseHappened() {
		String[] filterSpecs = {"*:E"};  //{"AndroidRuntime:E"}; // for some reason, AndroidRuntime:E lists all logs
		ArrayList<String> lines = new ArrayList<String>();
		collectLog(lines, "time", null, filterSpecs);

		//TestTwo(logData);
		if (lines.size()>0) {
			boolean forceClosedSinceLastCheck = false;
			for (String line:lines) {
				final Matcher matcher = mPattern.matcher(line);
				boolean isMyStackTrace = matcher.matches();
				SharedPreferences prefs = mPrefs;
				if (isMyStackTrace) {
					String timestamp = matcher.group(1); 
					boolean appeared = prefs.getBoolean(timestamp , false);
					//					Log.d(LOG_TAG, lineKey);
					if (!appeared) {
						//						Log.d(LOG_TAG, "!appeared");
						forceClosedSinceLastCheck = true;
						prefs.edit().putBoolean(timestamp, true).commit();
					}
				}
			}
			return forceClosedSinceLastCheck;
		} else
			return false;
	}

	public boolean collect() {
		ArrayList<String> lines = mLastLogs;
		collectLog(lines, null, null, null);
		return lines.size()>0;
	}

	public void sendLog(String email, String subject, String preface) {

//		Mail mail = new Mail("vielsa2580@gmail.com", "Saraselan@123"); 
//
//		try { 
//
//			String[] toArr = {"vielsa2580@gmail.com"};
//			mail.set_from("vielsa2580@gmail.com");
//			mail.set_to(toArr);
//			mail.set_subject("HEllo");
//			mail.setBody("I receive this I'm super happy");
//			mail.addAttachment("/sdcard/ErrorLog/ErrorLog.txt");
//
//			//	        m.addAttachment("/sdcard/filelocation"); 
//
//			if(mail.send()) { 
//				File file = new File("/sdcard/ErrorLog/ErrorLog.txt");
//				file.delete();
//				Toast.makeText(context, "Email was sent successfully.", Toast.LENGTH_LONG).show();
//
//			} else { 
//				Toast.makeText(context, "Email was not sent.", Toast.LENGTH_LONG).show(); 
//			} 
//		} catch(Exception e) { 
//			//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
//			Log.e("MailApp", "Could not send email", e); 
//		}
		
		new CheckForceCloseTask().execute();
		
	}


	class CheckForceCloseTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			try { 

				Mail mail = new Mail("vielsa2580@gmail.com", "Saraselan@123"); 
				String[] toArr = {"vielsa2580@gmail.com"};
				mail.set_from("vielsa2580@gmail.com");
				mail.set_to(toArr);
				mail.set_subject("HEllo");
				mail.setBody("I receive this I'm super happy");
				mail.addAttachment("/sdcard/ErrorLog/ErrorLog.txt");

				return mail.send();
			}
				catch(Exception e) { 
					//Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show(); 
					return false; 
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				
				if(result) { 
					File file = new File("/sdcard/ErrorLog/ErrorLog.txt");
					file.delete();
					Toast.makeText(context, "Email was sent successfully.", Toast.LENGTH_LONG).show();

				} else { 
					Toast.makeText(context, "Email was not sent.", Toast.LENGTH_LONG).show(); 
				} 
				
		}
	}


		public class Mail extends javax.mail.Authenticator { 

			private String _user,_pass; 

			private String[] _to; 
			private String _from,_port; 
			private String _sport; 

			private String _host; 

			private String _subject,_body; 

			public String[] get_to() {
				return _to;
			}

			public void set_to(String[] _to) {
				this._to = _to;
			}

			public String get_from() {
				return _from;
			}

			public void set_from(String _from) {
				this._from = _from;
			}

			public String get_subject() {
				return _subject;
			}

			public void set_subject(String _subject) {
				this._subject = _subject;
			}

			public String getBody() { 
				return _body; 
			} 

			public void setBody(String _body) { 
				this._body = _body; 
			} 

			public Mail(String user, String pass) { 
				this(); 

				_user = user; 
				_pass = pass; 
			} 

			private boolean _auth; 

			private boolean _debuggable; 

			private Multipart _multipart; 

			public Mail() { 
				_host = "smtp.gmail.com"; // default smtp server 
				_port = "465"; // default smtp port 
				_sport = "465"; // default socketfactory port 

				_user = ""; // username 
				_pass = ""; // password 
				_from = ""; // email sent from 
				_subject = ""; // email subject 
				_body = ""; // email body 

				_debuggable = false; // debug mode on or off - default off 
				_auth = true; // smtp authentication - default on 

				_multipart = new MimeMultipart(); 

				// There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
				MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
				mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
				mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
				mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
				mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
				mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
				CommandMap.setDefaultCommandMap(mc); 
			} 

			public boolean send() throws Exception { 
				Properties props = _setProperties(); 

				if(!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("") && !_body.equals("")) { 
					Session session = Session.getInstance(props, this); 

					MimeMessage msg = new MimeMessage(session); 

					msg.setFrom(new InternetAddress(_from)); 

					InternetAddress[] addressTo = new InternetAddress[_to.length]; 
					for (int i = 0; i < _to.length; i++) { 
						addressTo[i] = new InternetAddress(_to[i]); 
					} 
					msg.setRecipients(MimeMessage.RecipientType.TO, addressTo); 

					msg.setSubject(_subject); 
					msg.setSentDate(new Date()); 

					// setup message body 
					BodyPart messageBodyPart = new MimeBodyPart(); 
					messageBodyPart.setText(_body); 
					_multipart.addBodyPart(messageBodyPart); 

					// Put parts in message 
					msg.setContent(_multipart); 

					// send email 
					Transport.send(msg); 

					return true; 
				} else { 
					return false; 
				} 
			} 

			public void addAttachment(String filename) throws Exception { 
				BodyPart messageBodyPart = new MimeBodyPart(); 
				DataSource source = new FileDataSource(filename); 
				messageBodyPart.setDataHandler(new DataHandler(source)); 
				messageBodyPart.setFileName(filename); 

				_multipart.addBodyPart(messageBodyPart); 
			} 

			@Override 
			public PasswordAuthentication getPasswordAuthentication() { 
				return new PasswordAuthentication(_user, _pass); 
			} 

			private Properties _setProperties() { 
				Properties props = new Properties(); 

				props.put("mail.smtp.host", _host); 

				if(_debuggable) { 
					props.put("mail.debug", "true"); 
				} 

				if(_auth) { 
					props.put("mail.smtp.auth", "true"); 
				} 

				props.put("mail.smtp.port", _port); 
				props.put("mail.smtp.socketFactory.port", _sport); 
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
				props.put("mail.smtp.socketFactory.fallback", "false"); 

				return props; 
			} 

		}
	}
