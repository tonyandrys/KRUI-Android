package fm.krui.kruifm;

/**
 * fm.krui.kruifm - TwitterManager
 *
 * @author Tony Andrys
 *         Created: 08/14/2013
 *         (C) 2013 - Tony Andrys
 */

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * All Twitter authorization/communication functionality is accessed from this class.
 */
public class TwitterManager {

    final private static String TAG = TwitterManager.class.getName();

    // Condition codes
    final public static String TIMELINE_SUCCESS = "timeline1";
    final public static String TIMELINE_FAILED = "timeline0";
    final public static String AUTHORIZATION_SUCCESS = "auth1";
    final public static String AUTHORIZATION_FAILED = "auth0";

    // API links
    final private String ROOT_URL = "https://api.twitter.com";
    final private String REQUEST_TOKEN_URL = "/oauth2/token?grant_type=client_credentials";
    final private String TIMELINE_URL = "/1.1/statuses/user_timeline.json";

    // JSON Keys
    final public static String KEY_ACCESS_TOKEN = "access_token";
    final public static String KEY_CREATED_AT = "created_at";
    final public static String KEY_TWEET_TEXT = "text";
    final public static String KEY_PROFILE_IMAGE_URL = "profile_image_url_https";
    final public static String OBJ_USER = "user";
    final public static String KEY_SCREEN_NAME = "screen_name";

    // Time hashmap constants (use after extractTimeValue())
    final public static String KEY_EXTRACT_DAY_OF_WEEK= "dayOfWeek";
    final public static String KEY_EXTRACT_MONTH = "month";
    final public static String KEY_EXTRACT_DAY_OF_MONTH = "dayOfMonth";
    final public static String KEY_EXTRACT_24HR_TIME = "24hrTime";
    final public static String KEY_EXTRACT_TIMEZONE_OFFSET = "timeZoneOffset";
    final public static String KEY_EXTRACT_YEAR = "year";

    private Context context;
    private String bearerToken;
    private HTTPConnectionListener callback;
    private HashMap<String, Integer> dateIntMap;

    public TwitterManager(Context context, HTTPConnectionListener listener) {
        this.context = context;
        this.callback = listener;
        dateIntMap = new HashMap<String, Integer>();
    }

    /**
     * Authorizes this device to communicate with Twitter REST API by exchanging private keys and storing token.
     */
    public void authorize() {

        // Retrieve consumer secret and key
        String consumerKey = context.getString(R.string.twitter_consumer_key);
        String consumerSecret = context.getString(R.string.twitter_consumer_secret);

        // Prepare credentials for authorization
        String encodedResult = encodeCredentials(consumerKey, consumerSecret);

        // POST authentication credentials to get a bearer token
        postAuth(encodedResult);
    }

    /**
     * POST authentication credentials to get a bearer token
     */
    private void postAuth(String preparedKey) {

        // Configure HTTP Request
        bearerToken = "";
        String userAgent = "Android"; //FIXME: Use more specific identifier here
        AndroidHttpClient androidHttpClient = AndroidHttpClient.newInstance(userAgent);
        HttpPost httpPost = new HttpPost(ROOT_URL + REQUEST_TOKEN_URL);

        // Apply headers and entity to request
        httpPost.setHeader("Authorization", "Basic " + preparedKey);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        // Build callback listener
        HTTPConnectionListener localListener = new HTTPConnectionListener() {
            @Override
            public void onConnectionFinish(String result) {
                JSONObject response;
                String accessToken = "";
                try {
                    // Convert response to JSON and store bearer token.
                    response = new JSONObject(result);

                    // FIXME: Turn off response body logging before release, bearer token cannot leak!
                    bearerToken = response.getString(KEY_ACCESS_TOKEN);

                    if (bearerToken.length() >= 1) {
                        callback.onConnectionFinish(AUTHORIZATION_SUCCESS);
                    } else {
                        callback.onConnectionFinish(AUTHORIZATION_FAILED);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not convert server response to JSON!");
                    e.printStackTrace();
                }
            }
        };

        // Execute HTTP request
        new HTTPConnection(androidHttpClient, httpPost, localListener).execute();
    }

    /**
     * Returns the (public) timeline of a specific user.
     * @param screenName Username of account to get timeline (with or without leading @)
     * @param tweetCount Number of tweets to pull as an integer
     * @param trimUser true to strip user objects down to only user id, false to return full user object.
     */
    public void getTimeline(String screenName, int tweetCount, boolean trimUser) {

        // URL Parameter constants
        final String SCREEN_NAME = "screen_name";
        final String TWEET_COUNT = "count";
        final String TRIM_USER = "trim_user";

        String url = ROOT_URL + TIMELINE_URL + "?";
        String username = validateUsername(screenName);

        // Encode POST parameters in the URL
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair(SCREEN_NAME, username));
        params.add(new BasicNameValuePair(TWEET_COUNT, Integer.toString(tweetCount)));
        if (trimUser) {
            params.add(new BasicNameValuePair(TRIM_USER, Integer.toString(1)));
        }

        String paramString = URLEncodedUtils.format(params, "UTF-8");
        url += paramString;

        // Build connection environment and execute
        String userAgent = "Android"; //FIXME: Use more specific identifier here
        AndroidHttpClient androidHttpClient = AndroidHttpClient.newInstance(userAgent);
        HttpGet httpGet = new HttpGet(url);

        // Build listener
        HTTPConnectionListener localListener = new HTTPConnectionListener() {
            @Override
            public void onConnectionFinish(String result) {
                callback.onConnectionFinish(result);
            }
        };

        // Apply authorization header with bearer token
        httpGet.setHeader("Authorization", "Bearer " + bearerToken);
        Log.v(TAG, "Set Authentication Header using bearerToken " + bearerToken);
        new HTTPConnection(androidHttpClient, httpGet, localListener).execute();
    }

    /**
     * Builds an ArrayList of tweets from a JSON timeline representation
     * @param JSONTimeline Timeline data as String in JSON format
     * @return ArrayList of Tweets
     */
    public ArrayList<Tweet> processJSONTimeline(String JSONTimeline) {

        ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
        try {
            // Convert string to JSON Array
            JSONArray a = new JSONArray(JSONTimeline);

            for (int i=0; i<a.length(); i++) {
                Tweet tweet = new Tweet();

                // Extract values from JSON Object
                JSONObject o = a.getJSONObject(i);
                Log.v(TAG, "Adding tweet:");
                tweet.setText(o.getString(KEY_TWEET_TEXT));
                Log.v(TAG, "Text: " + o.getString(KEY_TWEET_TEXT));
                JSONObject userObj = o.getJSONObject(OBJ_USER);
                tweet.setProfileImageUrl(userObj.getString(KEY_PROFILE_IMAGE_URL));
                Log.v(TAG, "Profile Image URL: " + userObj.getString(KEY_PROFILE_IMAGE_URL));
                tweet.setScreenName(userObj.getString(KEY_SCREEN_NAME));
                Log.v(TAG, "Screen name: " + userObj.getString(KEY_SCREEN_NAME));

                // Extract time values from string and add relevant values to object
                HashMap<String, String> timeMap = extractTimeValue(o.getString(KEY_CREATED_AT));
                tweet.setTimeOfTweet(timeMap.get(KEY_EXTRACT_24HR_TIME));

                // Change month to integer representation for cleanliness
                String monthStr = timeMap.get(KEY_EXTRACT_MONTH);
                Log.v(TAG, "Trying to convert month: " + monthStr);
                String monthInt = Integer.toString(convertMonthToInt(monthStr));
                tweet.setDateOfTweet(monthInt + "/" + timeMap.get(KEY_EXTRACT_DAY_OF_MONTH));
                tweet.setTimeZoneOffset(timeMap.get(KEY_EXTRACT_TIMEZONE_OFFSET));

                // Add this tweet to list
                tweetList.add(tweet);
                Log.v(TAG, "Tweet added!");
            }

        } catch (JSONException e) {
            Log.e(TAG, "Could not build JSON from input string!");
            e.printStackTrace();
        }

        return tweetList;
    }

    /**
     * Handles time elements from "created_at" field from tweet
     * @return String array of each component
     */
    private HashMap<String, String> extractTimeValue(String createdAt) {
        String[] split = createdAt.split("\\s+");
        HashMap<String, String> timeMap = new HashMap<String, String>();
        timeMap.put(KEY_EXTRACT_DAY_OF_WEEK, split[0]);
        Log.v(TAG, "Adding " + split[0] + " to day of week key");
        timeMap.put(KEY_EXTRACT_MONTH, split[1]);
        Log.v(TAG, "Adding " + split[1] + " to month key");
        timeMap.put(KEY_EXTRACT_DAY_OF_MONTH, split[2]);
        Log.v(TAG, "Adding " + split[2] + " to day of month key");
        timeMap.put(KEY_EXTRACT_24HR_TIME, split[3]);
        Log.v(TAG, "Adding " + split[3] + " to 24 hour time key");
        timeMap.put(KEY_EXTRACT_TIMEZONE_OFFSET, split[4]);
        Log.v(TAG, "Adding " + split[4] + " to timezone offset key");
        timeMap.put(KEY_EXTRACT_YEAR, split[5]);
        Log.v(TAG, "Adding " + split[5] + " to year key");

        return timeMap;
    }

    /**
     * Encodes consumer key and consumer secret using RFC 1783 and formats the result according
     * to Twitter's requirements for authorization.
     * @param consumerKey Consumer Key
     * @param consumerSecret Consumer Secret
     * @return Processed string
     */
    private String encodeCredentials(String consumerKey, String consumerSecret) {

        // Encode credentials according to RFC 1739
        String consumerKey1379 = null;
        String consumerSecret1379 = null;
        try {
            consumerKey1379 = URLEncoder.encode(consumerKey, "UTF-8");
            consumerSecret1379 = URLEncoder.encode(consumerSecret, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Could not encode auth credentials!");
            e.printStackTrace();
        }

        // Concatenate the encoded consumer key and secret with a colon
        String singleKey = consumerKey1379 + ":" + consumerSecret1379;

        // Encode single key using base64 and return result
        byte[] keyBytes = singleKey.getBytes();
        String finalKey = Base64.encodeToString(keyBytes, Base64.NO_WRAP);
        Log.v(TAG, "Final key created: " + finalKey);
        return finalKey;
    }

    /**
     * Strips junk from twitter username and outputs a validated username.
     * TODO: Turn this into a URL parser as well
     * @return clean username as a String
     */
    private String validateUsername(String screenName) {

        // Check user name for leading @ and strip it if it exists.
        String verifiedUsername = "";
        String subString = screenName.substring(0,1);
        if (subString.equals("@")) {
            Log.v(TAG, "Stripping leading @ from screen name (" + screenName + ")");
            verifiedUsername = screenName.substring(1);
        } else {
            verifiedUsername = screenName;
        }
        Log.v(TAG, "Verified username is " + verifiedUsername);
        return verifiedUsername;
    }

    /**
     * Get bearer token used for signing API requests
     * @return KRUI bearer token as a String
     */
    private String getBearerToken() {
        return this.bearerToken;
    }

    private int convertMonthToInt(String text) {

        // If map has not been built yet, build it.
        if (dateIntMap.size() == 0) {
            // Build date/int map
            dateIntMap.put("Jan", 1);
            dateIntMap.put("Feb", 2);
            dateIntMap.put("Mar", 3);
            dateIntMap.put("Apr", 4);
            dateIntMap.put("May", 5);
            dateIntMap.put("Jun", 6);
            dateIntMap.put("Jul", 7);
            dateIntMap.put("Aug", 8);
            dateIntMap.put("Sep", 9);
            dateIntMap.put("Oct", 10);
            dateIntMap.put("Nov", 11);
            dateIntMap.put("Dec", 12);
        }

        // Convert the value and return it.
        return dateIntMap.get(text);
    }

}