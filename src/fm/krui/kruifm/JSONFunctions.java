package fm.krui.kruifm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/*
 * JSONfunctions.java
 */

public class JSONFunctions {
	
	private static final String TAG = JSONFunctions.class.getName(); // Tag constant for logging purposes

	public static JSONObject getJSONObjectFromURL(String url) {
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httppost = new HttpGet(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}catch(Exception e){
			Log.e(TAG, "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e(TAG, "Error converting result "+e.toString());
		}

		try{

			jArray = new JSONObject(result);            
		}catch(JSONException e){
			Log.e(TAG, "Error parsing data "+e.toString());
		}

		return jArray;
	}
	
	public static JSONArray getJSONArrayFromURL(String url) {
		InputStream is = null;
		String result = "";
		JSONArray jArray = null;

		//http post
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httppost = new HttpGet(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		} catch(Exception e) {
			Log.e(TAG, "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e(TAG, "Error converting result "+e.toString());
		}

		try{

			jArray = new JSONArray(result);            
		}catch(JSONException e){
			Log.e(TAG, "Error parsing data "+e.toString());
		}

		return jArray;
	}
	
	// Stores a JSON object in memory to internal storage.
	public static void storeJSONObject(String filename, JSONObject o) {
		
	}
	
	
	
}
