/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;


/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {

	private static final int NEST_CLOSE_INTERVAL = 20;
	//Telnet
	private Socket socketNest;
	private Socket socketDoor;

	private static final int NESTPORT = 23;
	private static final String NEST_IP = "18.111.24.207";
	private static final String DOOR_IP = "18.111.24.207"; //get ip from app

	// Amarino 

	private static final String TOUCHONE_ADD =  "00:06:66:6C:AB:E4";
	//	private static final String NEST_ADD =  "00:06:66:08:E7:D0"; 
	private static final String BENDONE_ADD =  "00:06:66:02:CB:DA";
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private Context currentContext;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// Stopwatch
	private TextView textTimer;
	private Button startButton;
	private Button pauseButton;
	private Button manualControl; 
	private Button pauseEnterButton;
	private Button startEnterButton;
	private long startTime = 0L;
	private Handler myHandler = new Handler();
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	boolean log1Flag = false;
	boolean log2Flag = false;
	boolean log3Flag = false;

	//For View Switching jugaad
	boolean pauseFlag = false; 
	boolean manualMode = false;
	boolean start = false;

	//Record
	private Button weekRecord;
	private Button backWeek;
	long dayMs = 0L;
	long lastLogTime = 0L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.e(TAG, "+++ ON CREATE +++");

		//Keep Screen On in Glass
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Set up the window layout
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		setContentView(R.layout.main);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText("JogCall");
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		currentContext = this;

		//Telnet thread

		new Thread(new TelnetClientThread()).start();
		new Thread(new DoorClientThread()).start();
	}

	@Override
	public void onStart() {
		super.onStart();
		if(D) Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null) setupChat();
		}

		// BluetoothChat is setup with the default window. Time to switch the window now....
		setContentView(R.layout.stopwatch);

		textTimer = (TextView) findViewById(R.id.textTimer);

		// For resuming from switching: if pause button is called before, then make sure that the correct value is displayed
		if(pauseFlag)
		{
			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (finalTime % 1000);
			textTimer.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
		}

		startButton = (Button) findViewById(R.id.btnStart);
		startEnterButton = (Button) findViewById(R.id.btnStartEnter);
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startTime = SystemClock.uptimeMillis();
				myHandler.postDelayed(updateTimerMethod, 0);
				pauseFlag = false;
				startButton.setVisibility(View.INVISIBLE);
				startEnterButton.setVisibility(View.VISIBLE);
				pauseEnterButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
			}
		});
		startEnterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				updateStopWatchLog();
			}
		});
		if(!manualMode)
		{
			startButton.setVisibility(View.INVISIBLE);
			startEnterButton.setVisibility(View.INVISIBLE);
		}
		else if(pauseFlag)
		{
			startButton.setVisibility(View.VISIBLE);
			startEnterButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			startButton.setVisibility(View.INVISIBLE);
			startEnterButton.setVisibility(View.VISIBLE);
		}


		pauseButton = (Button) findViewById(R.id.btnPause);
		pauseEnterButton = (Button) findViewById(R.id.btnPauseEnter);
		pauseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				timeSwap += timeInMillies;
				myHandler.removeCallbacks(updateTimerMethod);
				pauseFlag = true;
				pauseButton.setVisibility(View.INVISIBLE);
				pauseEnterButton.setVisibility(View.VISIBLE);
				startEnterButton.setVisibility(View.INVISIBLE);
				startButton.setVisibility(View.VISIBLE);
			}
		});
		pauseEnterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				updateStopWatchLog();
			}
		});

		if(!manualMode)
		{
			pauseButton.setVisibility(View.INVISIBLE);
			pauseEnterButton.setVisibility(View.INVISIBLE);
		}
		else if(pauseFlag)
		{	
			pauseButton.setVisibility(View.INVISIBLE);
			pauseEnterButton.setVisibility(View.VISIBLE);
		}
		else 
		{ 
			pauseButton.setVisibility(View.VISIBLE);
			pauseEnterButton.setVisibility(View.INVISIBLE);
		}


		//If week record button is pressed, change view to Week's record
		weekRecord = (Button) findViewById(R.id.btnweekRecord);
		weekRecord.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setContentView(R.layout.weekrecord);

				// Update today's timer
				//Log.v("Debugging", ""+dayMs);
				TextView todayTimer = (TextView) findViewById(R.id.w0Timer);
				int seconds = (int) (dayMs / 1000);
				int minutes = seconds / 60;
				int hours = minutes / 60;
				minutes = minutes % 60;
				seconds = seconds % 60;
				todayTimer.setText("" + hours + "h"
						+ " " + String.format("%02d", minutes) + "m");

				//If back button from week record button is pressed, change view to stopwatch
				backWeek = (Button) findViewById(R.id.btnbackWeek);
				backWeek.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						setContentView(R.layout.stopwatch);
						log1Flag = log2Flag = log3Flag = false;
						onStart();
					}
				});
			}
		});

		manualControl = (Button) findViewById(R.id.manual);
		manualControl.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				new AlertDialog.Builder(currentContext)
				.setTitle("Switch to manual mode?")
				.setMessage("App will stop updating automatically")
				.setPositiveButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						// continue with delete
					}
				})
				.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						manualControl.setVisibility(View.INVISIBLE);
						startButton.setVisibility(View.VISIBLE);
						pauseEnterButton.setVisibility(View.VISIBLE);
						manualMode = true;
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			}
		});

		if(manualMode)
			manualControl.setVisibility(View.INVISIBLE);

		//JogCall is setup. Attempt connection to arduino now

		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

		// this is how you tell Amarino to connect to a specific BT device from within your own code
		//Amarino.connect(this, BENDONE_ADD);
		//Amarino.connect(this, NEST_ADD);
	} 



	@Override
	public synchronized void onResume() {
		super.onResume();
		if(D) Log.e(TAG, "+ ON RESUME +");


		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if(D) Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if(D) Log.e(TAG, "-- ON STOP --");

		// if you connect in onStart() you must not forget to disconnect when your app is closed
		//Amarino.disconnect(this, BENDONE_ADD);
		//Amarino.disconnect(this, NEST_ADD);

		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null) mChatService.stop();
		if(D) Log.e(TAG, "--- ON DESTROY ---");
//		socketNest.close();
//		socketDoor.close();
	}

	private void ensureDiscoverable() {
		if(D) Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * @param message  A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener =
			new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			// If the action is a key-up event on the return key, send the message
			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if(D) Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mConversationArrayAdapter.clear();

					//Now that we are connected, start the timer if not in the manual mode...
					if(!manualMode)
					{
						startTime = SystemClock.uptimeMillis(); 
						myHandler.postDelayed(updateTimerMethod, 0);
					}

					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);

					//log the value, reset the timer and pause the clock
					if(!manualMode && start)
					{
						updateStopWatchLog();
						startTime = 0L;
						timeInMillies = 0L;
						timeSwap = 0L;
						finalTime = 0L;
						myHandler.removeCallbacks(updateTimerMethod);
					}
					start = true;
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras()
						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	private Runnable updateTimerMethod = new Runnable() {
		public void run() {
			timeInMillies = SystemClock.uptimeMillis() - startTime;
			finalTime = timeSwap + timeInMillies;

			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (finalTime % 1000);
			textTimer.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
			myHandler.postDelayed(this, 0);
		}
	};

	private void updateStopWatchLog()
	{
		TextView logTimer = null;
		TextView logTime = null;
		if(!log1Flag)
		{
			logTime = (TextView) findViewById(R.id.log1Time);
			logTimer = (TextView) findViewById(R.id.log1Timer);
			log1Flag = true;
		}
		else if(!log2Flag)
		{
			logTime = (TextView) findViewById(R.id.log2Time);
			logTimer = (TextView) findViewById(R.id.log2Timer);
			log2Flag = true;
		}
		else
		{
			logTime = (TextView) findViewById(R.id.log3Time);
			logTimer = (TextView) findViewById(R.id.log3Timer);
			log3Flag = true;
		}

		int hour = Calendar.getInstance().get(Calendar.HOUR);
		int min =  Calendar.getInstance().get(Calendar.MINUTE);
		int am_pm = Calendar.getInstance().get(Calendar.AM_PM);
		String AM = "AM";
		if(am_pm == 1)
			AM = "PM";
		//Log.v("Debugging", hour + ":");

		logTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", min) + AM);

		int seconds = (int) (finalTime / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int milliseconds = (int) (finalTime % 1000);
		logTimer.setText("" + minutes + ":"
				+ String.format("%02d", seconds) + ":"
				+ String.format("%03d", milliseconds));

		logTime.setVisibility(View.VISIBLE);
		logTimer.setVisibility(View.VISIBLE);

		//Update day's total time count
		dayMs += finalTime;

		if((int)((dayMs - lastLogTime)/1000) >= NEST_CLOSE_INTERVAL)
		{
			Log.v("Debugging", "dayMS" + dayMs);
			Log.v("Debugging", "lastLogTime" + lastLogTime);

			//Amarino.sendDataToArduino(getApplicationContext(), NEST_ADD, 'C', true);
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socketNest.getOutputStream())),
						true);
				out.println('B');
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			lastLogTime = dayMs;
		}
	}

	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;

			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			//final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

			// we only expect String data though, but it is better to check if really string was sent
			// later Amarino will support differnt data types, so far data comes always as string and
			// you have to parse the data to the type you have sent from Arduino, like it is shown below
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

				//Log.v("Debugging", "On receiving from sensors, directly sending Data to Nest for chirping if not null");

				if (data != null){
					try {
						PrintWriter out = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(socketNest.getOutputStream())),
								true);
						out.println('C');
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

					//Amarino.sendDataToArduino(getApplicationContext(), NEST_ADD, 'B', true);
				}
			}
		}
	}


	class TelnetClientThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress nestServerAddr = InetAddress.getByName(NEST_IP);

				socketNest = new Socket(nestServerAddr, NESTPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	class DoorClientThread implements Runnable {

		@Override
		public void run() {
			BufferedReader input=null;
			InetAddress doorServerAddr;
			try {
				doorServerAddr = InetAddress.getByName(DOOR_IP);
				socketDoor = new Socket(doorServerAddr, NESTPORT);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {
				try {
					input = new BufferedReader(new InputStreamReader(socketDoor.getInputStream()));
                    String read = input.readLine();

					if (read != null){
						PrintWriter out = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(socketNest.getOutputStream())),
								true);
						out.println('C');
					}

				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}


}