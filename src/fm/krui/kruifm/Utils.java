package fm.krui.kruifm;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private static final String TAG = Utils.class.getName();

    /**
     * Converts a datetime of arbitrary format and TimeZone to a human readable string. Can convert to a local timezone
     * if desired.
     * @param dateTime Input string to parse and convert
     * @param inputTemplate Format of input string
     * @param inputTimeZone TimeZone of input string. Nullify for UTC.
     * @param outputTemplate Desired output format of the input string
     * @param outputTimeZone Desired timezone of output string.
     * @return
     */
    public static String convertTime(String dateTime, String inputTemplate, TimeZone inputTimeZone, String outputTemplate, TimeZone outputTimeZone) {
        String result = null;
        try {
            SimpleDateFormat df1 = new SimpleDateFormat(inputTemplate, Locale.US);
            if (inputTimeZone != null) {
                df1.setTimeZone(inputTimeZone);
            } else {
                // If inputZone is nullified, assume UTC.
                df1.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            SimpleDateFormat df2 = new SimpleDateFormat(outputTemplate, Locale.US);
            if (outputTimeZone != null) {
                df2.setTimeZone(outputTimeZone);
            } else {
                // If nothing is passed, do not alter timezone from UTC.
                df2.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            return result = df2.format(df1.parse(dateTime));
        }
        catch (ParseException e) {
            Log.v(TAG,"Error when parsing time: "+ result);
            e.printStackTrace();
            return result = "-1";
        }
    }

    /**
     * Returns the textual representation of the day of the week when given its integer representation.
     * @arg k Day of week value as an integer.
     */
    public static String weekdayIntToStr(int k) {
        String weekday = null;
        switch (k) {
            case 1:
                weekday = "Sunday";
                break;
            case 2:
                weekday = "Monday";
                break;
            case 3:
                weekday = "Tuesday";
                break;
            case 4:
                weekday = "Wednesday";
                break;
            case 5:
                weekday = "Thursday";
                break;
            case 6:
                weekday = "Friday";
                break;
            case 7:
                weekday = "Saturday";
        }

        return weekday;
    }
    /**
     * Converts a non URL safe string into a URL safe string.
     * @param s String to be encoded
     * @return URL safe string
     */
    public static String webEncodeString(String s) {
        try {
            String safe = URLEncoder.encode(s, "UTF-8");
            return safe;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode string: ");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Checks for a valid network connection on any active interface.
     * @param activity Calling activity (to access CONNECTIVITY_SERVICE).
     * @return true if a valid connection is found, false if no usable connection found.
     * NOTE: Thanks to a user on StackOverflow for this one-- when I find your name again I'll give you credit.
     */
    public static boolean checkForNetworkConnection(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
