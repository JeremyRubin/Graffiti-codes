package ch.rubinte.graffiti;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	final Activity activity = this;
	
	//Sensors
	private SensorManager sensorManager;
	private SensorEventListener sensorEventListener;
	private Sensor accelerometer;
	
	//Data Stores
	private JSONArray accDataX;
	private JSONArray accDataY;
	private JSONArray accDataZ;
	private JSONArray timeData;
	
	private boolean scanState;
	private boolean currentlyScanning;
	
	private TextView text;
	private AlertDialog.Builder alert;
////////////////////////////////
//Data Parsing BLOCK//
///////////////////////////////

	private void wipeAllData(){
		accDataX = new JSONArray();
		accDataY = new JSONArray();
		accDataZ = new JSONArray();
		timeData = new JSONArray();
	}
	private void grabData(){
		wipeAllData();
		sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		Log.i("called", "grabData()");
	}
	private JSONObject assembleScanJSON(){
		JSONObject json = new JSONObject();
		try{
			json.put("timestamps", timeData);
			json.put("dataX", accDataX);
			json.put("dataY", accDataY);
			json.put("dataZ", accDataZ);
		}catch (JSONException j){
			Log.w("Error", "Write Failed on ", j);
		}
		return json;
	}
	private JSONObject assembleCreateJSON(String message){
		JSONObject json = new JSONObject();
		try{
			json.put("timestamps", timeData);
			json.put("dataX", accDataX);
			json.put("dataY", accDataY);
			json.put("dataZ", accDataZ);
			json.put("msg", message);
		}catch (JSONException j){
			Log.w("Error", "Write Failed on " + message, j);
		}
		return json;
	}
////////////////////////////////
//Webview BLOCK//
///////////////////////////////

	 private class MyWebViewClient extends WebViewClient {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return false;
	        }
	    }
	 private class MyChromeClient extends WebChromeClient{
		 public void onProgressChanged(WebView view, int progress){
			 activity.setProgress(progress * 100);
		 }
	 }
////////////////////////////////
	 //SENDING BLOCK//
///////////////////////////////
	private class BGPost extends AsyncTask<String, Void, String>{
		protected void onPostExecute(String result){
			WebView web = (WebView) findViewById(R.id.webview);
		    web.getSettings().setJavaScriptEnabled(true);
		    web.setWebChromeClient(new MyChromeClient());
		    web.getSettings().setLoadWithOverviewMode(true);
		    web.getSettings().setUseWideViewPort(true);
		    web.getSettings().setBuiltInZoomControls(true);
		    web.setWebViewClient(new MyWebViewClient());
			web.loadUrl(result);
		}
		protected String doInBackground(String...jsons){
			String whole = "";
			for(String json : jsons){
				
				try{
					URL url = new URL("http://rubinte.ch/scribbles");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					try{
						String msg = "data="+json;
						conn.setDoOutput(true);
						conn.setFixedLengthStreamingMode(msg.length());
						OutputStream out = new BufferedOutputStream(conn.getOutputStream());
						out.write(msg.getBytes());
						out.flush();
						
						BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						StringBuilder total = new StringBuilder();
						String line;
						while ((line = r.readLine()) != null) {
						    total.append(line);
						}
						whole = total.toString() ;
						out.close();
						r.close();
						conn.disconnect();
					}catch (Exception e){
					}
				}catch (Exception e){

				}
			}
			return whole;
		}

	}

	private class BGCreate extends AsyncTask<String, Void, Void>{
		protected Void doInBackground(String...jsons){
			for(String json : jsons){
				try{
					URL url = new URL("http://rubinte.ch/scribbles");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					
					try{
						String msg = "data="+json;
						conn.setDoOutput(true);
						conn.setFixedLengthStreamingMode(msg.length());
						OutputStream out = new BufferedOutputStream(conn.getOutputStream());
						out.write(msg.getBytes());
						out.flush();
						
						/*BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						StringBuilder total = new StringBuilder();
						String line;
						while ((line = r.readLine()) != null) {
						    total.append(line);
						}*/
						out.close();
						//r.close();
						conn.disconnect();
					}catch (Exception e){
					}
				}catch (Exception e){

				}
			}
			return null;
		}

	}
////////////////////////////////
//ButtonPress BLOCK//
///////////////////////////////
	private void scanGraffiti(boolean on){
		if (on){
			grabData();
			text.setText("Recording");
			Log.i("pressed", "scan on");
		}else{
			text.setText("Press Volume to Record");
			sensorManager.unregisterListener(sensorEventListener);
			try{
				new BGPost().execute(assembleScanJSON().toString());
			}catch (Exception e){	
			}
			Log.i("called", "scan off");
		}
	}
	private void createGraffiti(boolean on){
		if (on){
			grabData();
			text.setText("Recording");
			
			Log.i("called", "create on");
		}else{
			text.setText("Press Volume to Record");
			sensorManager.unregisterListener(sensorEventListener);
			alert = new AlertDialog.Builder(this);
			alert.setTitle("Add a URL");
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			input.setText("http://");
			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  String url = input.getText().toString();
				  // Do something with value!
				  try{
						new BGCreate().execute(assembleCreateJSON(url).toString());
					}catch (Exception e){	
					}
				  }
				});
			alert.show();
			Log.i("called", "create off");
		}

	}
	public void updateScanState(View view){
		boolean on = ((CompoundButton) view).isChecked();
		if (on){
			scanState = false;
		}else{
			scanState = true;
		}
		Log.d("state",Boolean.toString(scanState));
	}
	private void switchState(){
		if (scanState){
			scanGraffiti(!currentlyScanning);

		}else{
			createGraffiti(!currentlyScanning);

		}
		currentlyScanning = !currentlyScanning;
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		
		switch(keyCode){
		case KeyEvent.KEYCODE_VOLUME_UP:
			if(action == KeyEvent.ACTION_DOWN){
				switchState();
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if(action == KeyEvent.ACTION_UP){
				switchState();
			}
			return true;
		default:
			return true;
		}
	}
////////////////////////////////
//Main BLOCK//
///////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_main);

		scanState = true;
		currentlyScanning = false;
		text = (TextView) findViewById(R.id.status);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorEventListener = new SensorEventListener(){
			public void onSensorChanged(SensorEvent event){
				accDataX.put(Float.toString(event.values[0]));
				accDataY.put(Float.toString(event.values[1]));
				accDataZ.put(Float.toString(event.values[2]));
				timeData.put(Long.toString(event.timestamp));
			}
			public void onAccuracyChanged(Sensor sensor, int accuracy){

			}
		};

	}


}
