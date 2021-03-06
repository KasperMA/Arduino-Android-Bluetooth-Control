package com.example.anouar.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Control extends AppCompatActivity {

    ConnectedThread mConnectedThread = null;
    //final UUID mUUID = UUID.fromString("ea8f8174-3dd7-4ff8-844f-783f64682691");
    final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Handler connectThreadHandler;
    BluetoothSocket mBTSocket = null;
    TextView buzzerStateTextView;
    TextView windowStateTextView;
    TextView fanStateTextView;
    boolean buzzerDeactivatedPermanently = false;
    boolean fansDeactivatedPermanently = false;
    boolean windowsDeactivatedPermanently = false;
    Runnable myRunnableTextSetter;
    //List<String> dataToBeSent;
    int DataSendPending = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);


        findAndAssignTextViews();

        CreateNewHandler();
        connectToAdevice(ClientActivity.selectedDeviceForConnection);
        //  SendData("SendAllData###");


        Button BuzzerOffButton = findViewById(R.id.BuzzerOFF);
        BuzzerOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("BZOFF");
            }
        });

        Button ShutWindowButton = findViewById(R.id.ShutWindow);
        ShutWindowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("WDOFF");
            }
        });

        Button StopFanButton = findViewById(R.id.StopFan);
        StopFanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("FNOFF");
            }
        });


        final Button DeactivateBuzzer = findViewById(R.id.DeactivateBuzzer);
        DeactivateBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buzzerDeactivatedPermanently) {
                    sendData("BZONP");
                } else {
                    sendData("BZOFFP");
                }

            }
        });

        Button DeactivateFans = findViewById(R.id.DeactivateFans);
        DeactivateFans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fansDeactivatedPermanently) {
                    sendData("FNONP");
                } else {
                    sendData("FNOFFP");
                }

            }
        });
        Button DeactivateWindows = findViewById(R.id.DeactivateWindows);
        DeactivateWindows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (windowsDeactivatedPermanently) {
                    sendData("WDONP");
                } else {
                    sendData("WDOFFP");
                }

            }
        });
    }

    void processReceivedDataAndRefreshDisplay(final String str) {
        Log.d("ReceivedFromArduino", str);
        final String stringToBeApplied;
        final TextView tempTextView;
        final Button tempButton;
        boolean ChangeButtons = false;

        if (str.contains("WDS")) {
            tempTextView = findViewById(R.id.WindowState);
            tempButton = new Button(this);

        } else if (str.contains("BZS")) {
            tempTextView = findViewById(R.id.BuzzerState);
            tempButton = new Button(this);
        } else if (str.contains("FNS")) {
            tempTextView = findViewById(R.id.FanState);
            tempButton = new Button(this);
        } else if (str.contains("ARN")) {
            //alarm notification impelemnt it later
            tempButton = new Button(this);
            tempTextView = new TextView(this);
            return;
        } else if (str.contains("BZ") && str.contains("P")) {
            tempButton = findViewById(R.id.DeactivateBuzzer);

            tempTextView = new TextView(this);
            ChangeButtons = true;
        } else if (str.contains("FN") && str.contains("P")) {

            tempButton = findViewById(R.id.DeactivateFans);
            tempTextView = new TextView(this);
            ChangeButtons = true;
        } else if (str.contains("WD") && str.contains("P")) {

            tempButton = findViewById(R.id.DeactivateWindows);
            tempTextView = new TextView(this);
            ChangeButtons = true;
        } else if (android.text.TextUtils.isDigitsOnly(str)) {
            tempTextView = findViewById(R.id.SensorValue);
            tempButton = new Button(this);
        } else {
            //tempTextView=findViewById(R.id.SensorValue);//must find a solution to remove these
            // tempButton=findViewById(R.id.DeactivateWindows);//must find a solution to remove these
            tempTextView = new TextView(this);
            tempButton = new Button(this);


        }

        if (str.contains("ON")) {
            if (ChangeButtons) {
                stringToBeApplied = "Deactivate";

            } else {
                stringToBeApplied = "ON";
            }

        } else if (str.contains("OF")) {
            if (ChangeButtons) {
                stringToBeApplied = "Activate";
            } else {
                stringToBeApplied = "OFF";
            }

        } else {
            stringToBeApplied = str;
        }

    /* tempTextView=findViewById(R.id.SensorValue);
     stringToBeApplied=str;*/
        if (ChangeButtons) {
            myRunnableTextSetter = new Runnable() {
                @Override
                public void run() {
                    if (stringToBeApplied == "Activate") {
                        String ButtonText = tempButton.getText().toString();
                        switch (tempButton.getId()) {
                            case R.id.DeactivateBuzzer:
                                buzzerDeactivatedPermanently = true;
                                ButtonText = "Activate Buzzer";
                                break;
                            case R.id.DeactivateWindows:
                                windowsDeactivatedPermanently = true;
                                ButtonText = "Activate Windows";
                                break;
                            case R.id.DeactivateFans:
                                fansDeactivatedPermanently = true;
                                ButtonText = "Activate Fans";
                                break;

                        }
                        tempButton.setText(ButtonText);
                    } else if (stringToBeApplied == "Deactivate") {
                        String ButtonText = tempButton.getText().toString();

                        switch (tempButton.getId()) {
                            case R.id.DeactivateBuzzer:
                                buzzerDeactivatedPermanently = false;
                                ButtonText = "Deactivate Buzzer";
                                break;
                            case R.id.DeactivateWindows:
                                windowsDeactivatedPermanently = false;
                                ButtonText = "Deactivate Windows";
                                break;
                            case R.id.DeactivateFans:
                                fansDeactivatedPermanently = false;
                                ButtonText = "Deactivate Fans";
                                break;
                        }
                        tempButton.setText(ButtonText);
                    }
                }
            };
            tempButton.post(myRunnableTextSetter);
        } else {
            myRunnableTextSetter = new Runnable() {
                @Override
                public void run() {
                    tempTextView.setText(stringToBeApplied);
                }
            };
            tempTextView.post(myRunnableTextSetter);
        }


    }

    public void CreateNewHandler() {
        connectThreadHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                Log.d("Handler", record.getMessage());
                processReceivedDataAndRefreshDisplay(record.getMessage());

            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }

        };
    }


    public void sendData(final String str) {

        incrementDataSendPending();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                decrementDataSendPending();
                mConnectedThread.Write(str);
            }
        };
        Timer tempTimer = new Timer();
        tempTimer.schedule(task, 1000 * DataSendPending);


    }

    void decrementDataSendPending() {
        Log.d("DelaySend", "SendData: " + DataSendPending);
        if (DataSendPending > 0) {
            DataSendPending = DataSendPending - 1;
        }
    }

    void incrementDataSendPending() {
        //  Log.d("DelaySend", "SendData: "+DataSendPending);
        DataSendPending = DataSendPending + 1;
    }

    public void connectToAdevice(BluetoothDevice SelectedDeviceForConnection) {

        try {
            mBTSocket = SelectedDeviceForConnection.createInsecureRfcommSocketToServiceRecord(mUUID);
        } catch (IOException e) {

        }

        try {
            mBTSocket.connect();
        } catch (IOException e) {

        }
        mConnectedThread = new ConnectedThread();
        mConnectedThread.start();
    }

    public class ConnectedThread extends Thread {
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread() {
            InputStream mTmpInputStream = null;
            OutputStream mTmpOutputStream = null;
            try {
                mTmpInputStream = mBTSocket.getInputStream();
                mTmpOutputStream = mBTSocket.getOutputStream();
            } catch (IOException e) {
            }
            mInputStream = mTmpInputStream;
            mOutputStream = mTmpOutputStream;
        }

        public void run() {
            // Keep looping to listen for received messages
            while (true) {
                try {
                    byte[] buffer = new byte[256000];
                    int bytes;
                    bytes = mInputStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    connectThreadHandler.publish(new LogRecord(Level.ALL, readMessage));
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void Write(String input) {
            byte[] buffer = input.getBytes();
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
            }
        }
    }

    void findAndAssignTextViews() {
        buzzerStateTextView = findViewById(R.id.BuzzerState);

        windowStateTextView = findViewById(R.id.WindowState);

        fanStateTextView = findViewById(R.id.FanState);

    }
}
