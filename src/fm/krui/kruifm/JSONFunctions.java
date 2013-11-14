/*
 * fm.krui.kruifm.JSONFunctions - JSONFunctions.java
 *
 * (C) 2013 - Tony Andrys
 * http://www.tonyandrys.com
 *
 * Created: 11/14/2013
 *
 * ---
 *
 * This file is part of KRUI.FM.
 *
 * KRUI.FM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * KRUI.FM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KRUI.FM.  If not, see <http://www.gnu.org/licenses/>.
 */

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
