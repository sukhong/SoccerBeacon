package com.shh.soccerbeacon;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shh.soccerbeacon.adapter.CalibrateListAdapter;
import com.shh.soccerbeacon.adapter.CalibrationPaneAdapter;
import com.shh.soccerbeacon.dto.BeaconListItem;
import com.shh.soccerbeacon.dto.BeaconLocationItem;
import com.shh.soccerbeacon.view.FieldView;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;

public class StartBeaconsActivity extends ActionBarActivity implements BeaconConsumer
{	
	FieldView fvFieldView;
	
	ArrayList<BeaconLocationItem> beaconLocationsList;
	
	private BeaconManager beaconManager;
	
	ListView lvCalibrationPane;
	CalibrationPaneAdapter calibrationPaneAdapter;
	
	boolean isPlaying = false;
	boolean showingCalibrationPane = false;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		lvCalibrationPane = (ListView) findViewById(R.id.lvCalibrationPane);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		// load settings
		int scanInterval = sharedPref.getInt("ScanInterval", 400);
		int runningSumCount = sharedPref.getInt("RunningSumCount", -1);
		float outlierTrimDistance = sharedPref.getFloat("OutlierTrimDistance", 2);
		float outlierTrimFactor = sharedPref.getFloat("OutlierTrimFactor", 0.5f);
		boolean useClosestBeacon = sharedPref.getBoolean("UseClosestBeacon", true);
		
		int displayMargin = sharedPref.getInt("DisplayMargin", 10);
		
		float fieldWidth = sharedPref.getFloat("FieldWidth", -1);
		float fieldHeight = sharedPref.getFloat("FieldHeight", -1);
				
		fvFieldView = (FieldView) findViewById(R.id.fvFieldView);
		fvFieldView.setMargin(displayMargin);	
		fvFieldView.setOutlierTrimDistance(outlierTrimDistance);
		fvFieldView.setOutlierTrimFactor(outlierTrimFactor);	
		fvFieldView.setUseClosestBeacon(useClosestBeacon);
		fvFieldView.setFieldWidth(fieldWidth);
		fvFieldView.setFieldHeight(fieldHeight);
		
		String beaconLocationsJSON = sharedPref.getString("BeaconLocations", "[]");
		
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<BeaconLocationItem>>(){}.getType();
		beaconLocationsList = (ArrayList<BeaconLocationItem>) gson.fromJson(beaconLocationsJSON, collectionType);
		
		// initialize running sum count for each beacon
		for (int i = 0; i < beaconLocationsList.size(); i++)
		{
			beaconLocationsList.get(i).setRunningSumCount(runningSumCount);
		}
		
		// sort by coordinates first
		Collections.sort(beaconLocationsList, new Comparator<BeaconLocationItem>()
    			{
    	            public int compare(BeaconLocationItem b1, BeaconLocationItem b2) 
    	            {
    	            	if (b1.getY() > b2.getY())
    	    			{
    	    				return 1;
    	    			}
    	    			else if (b1.getY() == b2.getY())
    	    			{
    	    				if (b1.getX() > b2.getX() )
    	    					return 1;
    	    				else if (b1.getX()  == b2.getX() )
    	    					return 0;
    	    				else
    	    					return -1;
    	    			}
    	    			else
    	    			{
    	    				return -1;
    	    			}		   	
    	            }
    	        });
		
		
		ArrayList<BeaconLocationItem> clonedList = (ArrayList<BeaconLocationItem>) beaconLocationsList.clone();
		
		Collections.sort(clonedList, new Comparator<BeaconLocationItem>()
    			{
    	            public int compare(BeaconLocationItem b1, BeaconLocationItem b2) 
    	            {
    	            	if (b1.getY() > b2.getY())
    	    			{
    	    				return 1;
    	    			}
    	    			else if (b1.getY() == b2.getY())
    	    			{
    	    				if (b1.getX() > b2.getX() )
    	    					return 1;
    	    				else if (b1.getX()  == b2.getX() )
    	    					return 0;
    	    				else
    	    					return -1;
    	    			}
    	    			else
    	    			{
    	    				return -1;
    	    			}		   	
    	            }
    	        });
		
		calibrationPaneAdapter = new CalibrationPaneAdapter(getApplicationContext(),  clonedList);
		lvCalibrationPane.setAdapter(calibrationPaneAdapter);
				
		fvFieldView.setBeaconLocationsList(beaconLocationsList);	
		
		// start listening to the beacons...
		beaconManager = BeaconManager.getInstanceForApplication(this);
		
		// add iBeacon Layout
		beaconManager.getBeaconParsers().add(new BeaconParser().
	               setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
		
		// scan for new updates every 500 milliseconds
		// default is 1100 milliseconds...
		beaconManager.setForegroundScanPeriod(scanInterval);
		beaconManager.setForegroundBetweenScanPeriod(0);
		
		beaconManager.setBackgroundScanPeriod(scanInterval);
		beaconManager.setBackgroundBetweenScanPeriod(0);
		
		beaconManager.bind(this);	
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(!mBluetoothAdapter.isEnabled())
        {
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, 1);
        }
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		beaconManager.unbind(this);
			
		//reset information
		for (int i = 0; i < beaconLocationsList.size(); i++)
		{
			BeaconLocationItem item = beaconLocationsList.get(i);		
			
			item.clearRSSI();
			item.setDistance(-1);
			item.setRunningSumCount(-1);
		}
		
		// save beacon calibration information		
		String beaconLocationsJSON = new Gson().toJson(beaconLocationsList);
				
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("BeaconLocations", beaconLocationsJSON);
		editor.apply();		
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (isPlaying)
		{
			menu.getItem(0).setTitle("STOP");
		}
		else
		{
			menu.getItem(0).setTitle("START");
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startbeacons_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_start)
		{
			isPlaying = !isPlaying;
			
			if (isPlaying)
				fvFieldView.start();
			else
				fvFieldView.stop();
			
			invalidateOptionsMenu();			
		}
		else if (id == R.id.action_screenshot)
		{
			takeScreenshot();
			
			Toast toast = Toast.makeText(this, "Screenshot saved", Toast.LENGTH_SHORT);
			toast.show();
		}
		else if (id == R.id.option_show_circles) 
		{
			fvFieldView.setShowRange(!item.isChecked());
			item.setChecked(!item.isChecked());
			
			return true;
		}
		else if (id == R.id.option_show_beacon_info) 
		{
			fvFieldView.setShowBeaconInfo(!item.isChecked());
			item.setChecked(!item.isChecked());
			
			return true;
		}
		else if (id == R.id.action_calibrationpane)
		{
			if (showingCalibrationPane)
				lvCalibrationPane.setVisibility(View.GONE);
			else
				lvCalibrationPane.setVisibility(View.VISIBLE);
			
			showingCalibrationPane = !showingCalibrationPane;
		}
				
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBeaconServiceConnect() {
		beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override 
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) 
            {                 	            	
        		Log.i("BEACON", "Beacon STARTED with size: " + beacons.size());

            	if (beacons.size() > 0) 
                {            		
                	for (Beacon current_beacon : beacons)
                	{
                		//Log.i("BEACON", "Beacon ID1: " + current_beacon.getId1() + " ID2: " + current_beacon.getId2() + " ID3: " + current_beacon.getBluetoothName() + " RSSI: " + current_beacon.getRssi());
                		int major = current_beacon.getId2().toInt();
                		int minor = current_beacon.getId3().toInt();
                		int RSSI = current_beacon.getRssi();
                		
                		// search through beacon locations to see if this beacon is registered in the field
                		for (int i = 0; i < beaconLocationsList.size(); i++)
                		{
                			BeaconLocationItem beaconLocation = beaconLocationsList.get(i);
                			if (beaconLocation.getMajor() == major && beaconLocation.getMinor() == minor)
                			{         	          				
                				//Log.i("BEACON", "RSSI: " + major + ", " + minor + ": " + RSSI);
                				
                				beaconLocation.setRSSI(RSSI);
                				
                				break;
                			}                			
                		}       		
                	} 
                	
                	// search through beacon locations to see if this beacon is registered in the field
            		for (int i = 0; i < beaconLocationsList.size(); i++)
            		{
            			BeaconLocationItem beaconLocation = beaconLocationsList.get(i);
            			beaconLocation.setDistance(beaconLocation.calculateDistance());            			
            		}     
                }
      	
            	runOnUiThread(new Runnable() {
          		     @Override
          		     public void run() {
          		    	fvFieldView.invalidate();
          		     }
            	});
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.shh.soccerbeaconRegion", null, null, null));
        } catch (RemoteException e)
        {
    		Log.e("BEACON", "REMOTE ERROR" + e.getMessage());
        }		
	}
	
	private void takeScreenshot() {
	    Date now = new Date();
	    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

	    try {
	        // image naming and path  to include sd card  appending name you choose for file
	        String mPath = Environment.getExternalStorageDirectory().toString() + "/Pictures/Screenshots/" + now + ".png";

	        // create bitmap screen capture
	        View v1 = getWindow().getDecorView().getRootView();
	        v1.setDrawingCacheEnabled(true);
	        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
	        v1.setDrawingCacheEnabled(false);

	        File imageFile = new File(mPath);

	        FileOutputStream outputStream = new FileOutputStream(imageFile);
	        int quality = 100;
	        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
	        outputStream.flush();
	        outputStream.close();
	    } catch (Throwable e) {
	        // Several error may come out with file handling or OOM
	        e.printStackTrace();
	    }
	}
}