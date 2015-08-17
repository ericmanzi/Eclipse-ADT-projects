package com.example.testwifi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class MainActivity extends ActionBarActivity {

	Handler updateConversationHandler;
	private TextView text;
	private EditText nestIP;
	private EditText doorIP;
	private Button connectBtn;

	private Socket socketNest;
	private Socket socketDoor;

	Thread serverThread=null;
	Thread doorThread;
	Thread nestThread;

	private static final int SERVERPORT = 6000;
	private static final String SERVER_IP="";
	private static final int NESTPORT = 23;
	private static final int DOORPORT = 23;
	private String NEST_IP = "18.111.3.52";  
	private String DOOR_IP = "18.111.6.131"; 

	//amarino
	private static final String TOUCHONE_ADD =  "00:06:66:6C:AB:E4";
	//	private static final String NEST_ADD =  "00:06:66:08:E7:D0"; 
	//	private static final String BENDONE_ADD =  "00:06:66:02:CB:DA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		text = (TextView) findViewById(R.id.text);
		nestIP = (EditText) findViewById(R.id.etNestIP);
		doorIP = (EditText) findViewById(R.id.etDoorIP);
		connectBtn = (Button) findViewById(R.id.button1);

		updateConversationHandler = new Handler();

		doorThread = new Thread(new DoorThread());
//		nestThread = new Thread(new NestThread());


	}

	public void connect() {
		updateConversationHandler.post(new updateUIThread("Connecting..."));
		if (!nestIP.getText().toString().equals("")) NEST_IP = nestIP.getText().toString();
		if (!doorIP.getText().toString().equals("")) DOOR_IP = doorIP.getText().toString();
		updateConversationHandler.post(new updateUIThread("Nest IP is "+NEST_IP));
		updateConversationHandler.post(new updateUIThread("Door IP is "+DOOR_IP));

		doorThread.start();
		updateConversationHandler.post(new updateUIThread("Starting door thread"));

//		nestThread.start();
//		updateConversationHandler.post(new updateUIThread("Start nest thread"));

		connectBtn.setEnabled(false);
	}


	@Override
	protected void onStart() {
		super.onStart();
		connectBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class NestThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress nestServerAddr = InetAddress.getByName(NEST_IP);
				updateConversationHandler.post(new updateUIThread("Connecting to nest..."));
				socketNest = new Socket(nestServerAddr, NESTPORT);
				if (socketNest!=null) {
					updateConversationHandler.post(new updateUIThread("Connected to nest!"));
				} else {
					updateConversationHandler.post(new updateUIThread("Can't connect to nest at IP:"+
							NEST_IP+" PORT: "+NESTPORT));
				}
				PrintWriter out = new PrintWriter(new BufferedWriter(
				 new OutputStreamWriter(socketNest.getOutputStream())),
												true);
				out.println("BBBBBB");
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				updateConversationHandler.post(new updateUIThread("HostException - Can't connect to nest at IP:"+
						NEST_IP+" PORT: "+NESTPORT));

			} catch (IOException e1) {
				e1.printStackTrace();
				updateConversationHandler.post(new updateUIThread("IOException - Can't connect to nest at IP:"+
						NEST_IP+" PORT: "+NESTPORT));

			}

		}

	}

	class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			text.setText(text.getText().toString() + msg + "\n");
		}
	}

	class DoorThread implements Runnable {

		@Override
		public void run() {
			BufferedReader input=null;
			InetAddress doorServerAddr;
			
			updateConversationHandler.post(new updateUIThread("Connecting to door..."));

			try {

				updateConversationHandler.post(new updateUIThread("Connecting to door 2..."));
				doorServerAddr = InetAddress.getByName(DOOR_IP);
				updateConversationHandler.post(new updateUIThread("Connecting to door 3..."));
				socketDoor = new Socket(doorServerAddr, DOORPORT);
				updateConversationHandler.post(new updateUIThread("Connecting to door 4..."));
				input = new BufferedReader(new InputStreamReader(socketDoor.getInputStream()));
			} catch (UnknownHostException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			if (socketDoor!=null) {
				updateConversationHandler.post(new updateUIThread("Connected to door!"));
			} else {
				updateConversationHandler.post(new updateUIThread("Can't connect to door at IP:"+
						DOOR_IP+" PORT: "+DOORPORT));
			}

			

			int read;
			updateConversationHandler.post(new updateUIThread("doorthread state: "+Thread.currentThread().getState().toString()));

			try {
				updateConversationHandler.post(new updateUIThread("Connecting to door 4..."));
				while (true) {
					read = input.read();
					updateConversationHandler.post(new updateUIThread(read+". Printing 'C' to Nest"));
					
				} 
				//updateConversationHandler.post(new updateUIThread("Out of loop"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		/*	if (input.ready()) {
						updateConversationHandler.post(new updateUIThread("input ready"));

						while ((read = input.readLine()) != null) {
							updateConversationHandler.post(new updateUIThread("dt state while loop: "+Thread.currentThread().getState().toString()));
							updateConversationHandler.post(new updateUIThread(read+". Printing 'C' to Nest"));
							//PrintWriter out = new PrintWriter(new BufferedWriter(
							// new OutputStreamWriter(socketNest.getOutputStream())),
							//								true);
							//out.println('C');

					} */



		//				input.close();
		//				socketDoor.close();


	}

}

