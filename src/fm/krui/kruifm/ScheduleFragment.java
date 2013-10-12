package fm.krui.kruifm;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Displays KRUI's Programming Schedule.
 */


public class ScheduleFragment extends Fragment implements TextListener {

	private static final String TAG = ScheduleFragment.class.getName(); // Tag constant for logging purposes
    private static final String MAIN_SCHEDULE_FILENAME = "main-studio-json";
    private static final String LAB_SCHEDULE_FILENAME = "lab-json";

    private View rootView;
    private JSONObject calendarObj;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_schedule_layout, container, false);
        rootView = view;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLoadingScreen(true);

        // Download all events for Monday (temporarily for now, will eventually change to all events).
        String apiQuery = "http://krui.fm/kruiapp/json/main_studio.txt";

        // Download JSON text file
        try {
            TextFetcher tf = new TextFetcher(getActivity(), apiQuery, MAIN_SCHEDULE_FILENAME, this);
            tf.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)rootView.findViewById(R.id.schedule_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.GONE);
        }

    }

    @Override
    public void onTextDownloaded() {

        // Read text file
        try {
            calendarObj = new JSONObject(readTextFile(new File(getActivity().getFilesDir(), MAIN_SCHEDULE_FILENAME)));
        } catch (JSONException e) {
            Log.e(TAG, "Error converting text file to JSON!");
            e.printStackTrace();
        }

        // Map used to convert text weekday values to integers
        HashMap< String, Integer> weekdayToIntMap = new HashMap<String, Integer>();
        String[] weekdays = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int j=0; j<7; j++) {
            weekdayToIntMap.put(weekdays[j], j+1);
        }

        try {
            JSONArray calendarArray = calendarObj.getJSONArray("items"); // "items" array stored here

            // For each JSON Object in the array, extract all necessary data
            // TODO: Integrate music/news/sports/special. Currently music is auto selected (by passing hard coded values in the show constructor).
            int dbAddCount = 0;
            for (int i=0; i<calendarArray.length(); i++) {
                JSONObject o = calendarArray.getJSONObject(i);
                JSONObject startObject = o.getJSONObject("start");
                JSONObject endObject = o.getJSONObject("end");
                String calId = o.getString("id");
                String title = o.getString("summary");
                String description = "";
                if (o.isNull("description") == false) {
                    description = o.getString("description");
                }
                String link = o.getString("htmlLink");
                String startUTC = startObject.getString("dateTime");
                String endUTC = endObject.getString("dateTime");

                // Parse UTC results to get day of week and human readable time
                String startTime = Utils.convertTime(startUTC, "yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC"), "hh a", TimeZone.getTimeZone("America/Chicago"));
                String endTime = Utils.convertTime(endUTC, "yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC"), "hh a", TimeZone.getTimeZone("America/Chicago"));
                String dayOfWeekText = Utils.convertTime(startUTC, "yyyy-MM-dd'T'hh:mm:ssZ", TimeZone.getTimeZone("UTC"), "E", TimeZone.getTimeZone("America/Chicago"));

                // Get number of minutes from midnight to the start of this show to determine top margin of event
                // Formula:
                // 24 hour start time n is in [1,24], * 60 minutes in an hour to determine hour many minutes until the top of the hour
                // Check minutes of starting time to see if the event starts somewhere other than the top of the hour. If minutes > 0, add it to the hour calculation.
                String startMinuteString = Utils.convertTime(startUTC, "yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC"), "HH m", TimeZone.getTimeZone("America/Chicago"));

                // Split result on whitespace to separate hours and minutes. a[0] = minutes until the top of the hour this show starts, a[1] contains offset minutes if they exist.
                String[] a = startMinuteString.split("\\s+");
                int startMinutes = (Integer.parseInt(a[0]) * 60) + Integer.parseInt(a[1]);
                //Log.v(TAG, "Start time for show " + title + "is " + startMinutes);

                // Do the same for end time
                String endMinuteString = Utils.convertTime(endUTC, "yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC"), "HH m", TimeZone.getTimeZone("America/Chicago"));
                String[] b = endMinuteString.split("\\s+");
                int endMinutes = (Integer.parseInt(b[0]) * 60) + Integer.parseInt(b[1]);
                //Log.v(TAG, "End time for show " + title + " is " + endMinutes);

                int dayOfWeekInt = weekdayToIntMap.get(dayOfWeekText);

                // Add this event to the schedule if it is a monday
                if (i<6) {
                    Log.v(TAG, "** About to add show: " + title);
                    addShow(title, description, startMinutes, endMinutes);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Programming Information from JSON. ");
            e.printStackTrace();
        }

        // Hide loading screen
        showLoadingScreen(false);

    }

    /**
     * Adds a show to the schedule.
     * @param title Title of show to display
     * @param description Description of show to display
     * @param startTime Start time of show in minutes from midnight
     * @param endTime End time of show in minutes from midnight
     */
    private void addShow(String title, String description, int startTime, int endTime) {

        /* Build the LinearLayout to function as the container for this show. Since the size of the container is to represent
        the length of the show, its height must be proportional (1dp = 1 minute) to the length. Determine length by finding the difference
        between the start and end times. */

        // Fix for corner case of shows ending at midnight.
        if (endTime == 0) {
            endTime = 1440;
        }
        int difference = endTime - startTime;

        /* Define the margins of this show. All shows must not overlap the displayed times, which are 50dp in width.
        Add 5 more (to the right and left) to see the schedule lines for clarity. Push the show down to align with the appropriate time marker using the top margin value set to the
        difference (in minutes) between midnight and the start of the show. */
        
        Log.v(TAG, "Configuring " + title);
        Log.v(TAG, "Start time: " + startTime + " End time: " + endTime);
        Log.v(TAG, "Setting parameters for " + title);
        RelativeLayout.LayoutParams rrLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPixels(difference));
        rrLayoutParams.leftMargin = dpToPixels(55);
        Log.v(TAG, "Left margin: " + rrLayoutParams.leftMargin);
        rrLayoutParams.topMargin = dpToPixels(startTime);
        Log.v(TAG, "Top margin: " + rrLayoutParams.topMargin);
        rrLayoutParams.rightMargin = dpToPixels(5);
        Log.v(TAG, "Right margin: " + rrLayoutParams.rightMargin);


        /* Build LinearLayout and apply parameters */
        LinearLayout eventLL = new LinearLayout(getActivity());
        eventLL.setOrientation(LinearLayout.VERTICAL);
        eventLL.setPadding(dpToPixels(5), dpToPixels(5), dpToPixels(5), dpToPixels(5));
        eventLL.setBackgroundResource(R.drawable.orange_rounded_event);

        /* Add title of event to LinearLayout */
        TextView titleTV = new TextView(getActivity());
        titleTV.setText(title);
        eventLL.addView(titleTV);

        /* Determine length of event to see if we have room to attach a description (if one was passed) */
        int length = endTime - startTime;
        if (length >= 60 && description != null) {
            TextView descriptionTV = new TextView(getActivity());
            descriptionTV.setText(description);
            eventLL.addView(descriptionTV);
        }

        /* Add this view to the schedule UI */
        RelativeLayout rl = (RelativeLayout)getActivity().findViewById(R.id.schedule_container_relativelayout);
        rl.addView(eventLL, rrLayoutParams);
        Log.v(TAG, "RelativeLayout now has " + rl.getChildCount() + " children.");
    }

    /**
     * Reads text file object and returns it as a string.
     * @return Text file as a string
     */
    private String readTextFile(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream (file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not read text file! " + file.getName());
            e.printStackTrace();
            return "";
        }

        StringBuffer fileContent = new StringBuffer("");
        byte[] buffer = new byte[1024];

        try {
            while (fis.read(buffer) != -1) {
                fileContent.append(new String(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return fileContent.toString();
    }

    /**
     * Converts dp values to an appropriate amount of pixels based on screen density of this device.
     * @param dp value of dp to convert
     * @return equivalent pixel count
     */
    private int dpToPixels(int dp) {
        Resources r = getActivity().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
