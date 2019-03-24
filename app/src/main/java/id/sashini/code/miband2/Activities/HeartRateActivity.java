package id.sashini.code.miband2.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import id.sashini.code.miband2.R;
import id.sashini.code.miband2.Services.HeartRateService;

public class HeartRateActivity extends AppCompatActivity {

    //Default Mi band 2 bluetooth address
    private static final String DEFAULT_MIBAND2 ="C5:C1:C3:6D:84:0F";

    //Views in layout
    //Heart rate view
    private TextView heraRateText;
    //Show mi band id and connect status
    private TextView deviceText;
    //Buttons
    private Button connectButton;
    private Button startButton;
    private Button stopButton;
    
    //Set max hr
    private EditText thresholdRate;

    private Intent serviceIntent;


    //Final variables
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_HEART_RATE_THRESHOLD = "MAX_HR";
    //Default hr
    public static final int DEFAULT_HR = 90;

    //Device(mobile phone's) bluetooth adapter
    BluetoothAdapter bluetoothAdapter;

    //Mi band 2's name and bluetooth address
    private String mDeviceName;
    private String mDeviceAddress;


    //Called when view is created on phone
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Set view from layout xml file
        setContentView(R.layout.activity_heart_rate);
        
        //get tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        //set tool bar title
        toolbar.setTitle("Heart Rate Monitor");

        //show tool bar
        setSupportActionBar(toolbar);

        //when activity is started with intent get sent data to activity
        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //initialse
        initializeObjects();
        initViews();
        initListeners();

        //check connected miband2 device availble
        if(mDeviceAddress==null)
        {
            //if not availble use default device
            mDeviceAddress=DEFAULT_MIBAND2;
            //show connection status on display
            deviceText.setText(mDeviceAddress +" Connected");
        }

    }

    private void initViews()
    {
        //find view ids on layout file and initialise
        heraRateText=findViewById(R.id.textView_rate);
        deviceText=findViewById(R.id.textView_device);
        connectButton=findViewById(R.id.button_connect);
        startButton=findViewById(R.id.button_start);
        stopButton=findViewById(R.id.button_stop);
        thresholdRate=findViewById(R.id.editText_treashold);

        thresholdRate.setText("90");
    }

    //Register button listners
    private void initListeners()
    {
        //Execute when connect buttun is clicked
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBoundedDevice();
            }
        });

        //start service when button is clicked
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        //stop service when stop button is clicked
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(serviceIntent);
            }
        });
    }

    
    private void startService()
    {

        //set default rate
        int rate=DEFAULT_HR;
        try {
            //convert string to integer from editText
            rate=Integer.parseInt(thresholdRate.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        //Create intent to start heartRate service
        serviceIntent =new Intent(this, HeartRateService.class);
        
        //Start service only if device (mi band 2) available and bluetooth is on on your phone
        if(mDeviceAddress!=null && bluetoothAdapter.isEnabled())
        {
            //Show status on screen
            Toast.makeText(this,"Connecting",Toast.LENGTH_SHORT).show();
            
            //Add extradetails to send servise class to start service. Service class will get this information
            serviceIntent.putExtra(EXTRAS_DEVICE_ADDRESS,mDeviceAddress);
            serviceIntent.putExtra(EXTRAS_HEART_RATE_THRESHOLD,rate);
            //Start service class
            startService(serviceIntent);
        }
        else {
            //If device is not available show warninig
            Toast.makeText(this,"No Mi band connected, Pair device first",Toast.LENGTH_SHORT).show();
        }
    }

    //Get bluetooth adapter
    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    void getBoundedDevice() {


        Toast.makeText(this,"Checking available devices",Toast.LENGTH_SHORT).show();

        if(!bluetoothAdapter.isEnabled())
        {
            deviceText.setText("Turn on the bluetooth");
            Toast.makeText(this,"Turn on the bluetooth to scan",Toast.LENGTH_SHORT).show();
            return;
        }

        //Get Mi band 2 device of availble list of all bluetooth device
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                mDeviceAddress=bd.getAddress();
                deviceText.setText(bd.getName()+" connected");
            }
        }
    }

    //Run when activity is running
    @Override
    protected void onResume() {
        super.onResume();
        
        //Get details from Service class
        IntentFilter filter = new IntentFilter(HeartRateService.HR_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister local broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        //When service send the heart rate details show in activities heart rate text view
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get rate
            String rate = intent.getStringExtra(HeartRateService.HEART_RATE);
            //Set text
            heraRateText.setText(rate);
        }
    };


    //Action menu on tool bar (Go to Scan activity)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan_d) {
            //Start scan activity
            Intent intent =new Intent(this,DeviceScanActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
