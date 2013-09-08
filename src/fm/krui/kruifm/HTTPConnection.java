package fm.krui.kruifm;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * fm.krui.kruifm - HTTPConnection
 *
 * @author Tony Andrys
 *         Created: 08/30/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Executes an HTTP Connection using the passed client and request type (POST, GET, PUT, DELETE).
 * When operation is finished, the response is sent via callback to the passed HTTPConnectionListener.
 */
public class HTTPConnection extends AsyncTask<Void, String, String> {

    final static String TAG = HTTPConnection.class.getName();
    protected AndroidHttpClient httpClient;
    protected HttpRequestBase httpRequest;
    protected HTTPConnectionListener callbackListener;

    public HTTPConnection(AndroidHttpClient httpClient, HttpRequestBase httpRequest, HTTPConnectionListener listener) {
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.callbackListener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String serverResponse = null;
        String callbackName = callbackListener.getClass().getName();
        Log.v(TAG, "** Callback listener is set to " + callbackName);

        try {
            Log.i(TAG, "Executing the HTTP " + httpRequest.getMethod() + " request to " + httpRequest.getURI().toString());
            HttpResponse response;
            response = httpClient.execute(httpRequest);

            // Grab the returned string as it is returned and make it a String to save memory.
            StringBuilder stringBuilderResponse = inputStreamToString(response.getEntity().getContent());
            serverResponse = stringBuilderResponse.toString();

            // Log the response.
            Log.i(TAG, "** HTTP Response returned: " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            Log.i(TAG, "** " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            Log.i(TAG, "Response body: " + serverResponse);
            httpClient.close();
            return serverResponse;

        } catch (IOException e) {
            Log.e(TAG, "Error when executing HTTP " + httpRequest.getMethod() + " request!");
            e.printStackTrace();
            httpClient.close();
            return serverResponse;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callbackListener.onConnectionFinish(result);
    }

    /**
     * Convenience method for converting InputStreams to human readable strings.
     *
     * @param is InputStream with text to convert
     * @return String containing InputStream's content
     */
    private StringBuilder inputStreamToString(InputStream is) {

        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the passed InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read entire response until there is no more to parse.
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caught when translating InputStream to String!");
            e.printStackTrace();
            return total;
        }

        // Finally, return the full string.
        return total;

    }
}
