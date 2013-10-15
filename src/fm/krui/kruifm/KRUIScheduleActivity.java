package fm.krui.kruifm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class KRUIScheduleActivity extends FragmentActivity implements TextListener {

    private static final String TAG = KRUIScheduleActivity.class.getName();
    private static final int FRAGMENT_COUNT = 7; // seven days of the week, seven fragments
    private static final String MAIN_SCHEDULE_FILENAME = "main-studio-json";
    private static final String LAB_SCHEDULE_FILENAME = "lab-json";

    // Show storage (shows by weekday are stored by index)
    private ArrayList<ArrayList<Show>> showList;

    private ViewPager pager;
    private FragmentPagerAdapter pagerAdapter;
    private JSONObject calendarObj;
    private GregorianCalendar cal;
    private ArrayList<String> dateList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_activity_layout);
        showLoadingScreen(true);
        cal = new GregorianCalendar();

        // Download all events for Monday (temporarily for now, will eventually change to all events).
                String apiQuery = "http://krui.fm/kruiapp/json/main_studio.txt";

        // Download JSON text file
        try {
            TextFetcher tf = new TextFetcher(this, apiQuery, MAIN_SCHEDULE_FILENAME, this);
            tf.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    @Override
    public void onTextDownloaded() {

        // Initialize show list
        // showList = [ sunday, monday, tuesday, wednesday, thursday, friday, saturday ]
        showList = new ArrayList<ArrayList<Show>>();

        // Read text file
        try {
            calendarObj = new JSONObject(readTextFile(new File(getFilesDir(), MAIN_SCHEDULE_FILENAME)));
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

        /* When dayOfWeek != dayCache, we have changed weekdays, so we must change storage location in the list. Initial
           value will match with Sunday shows */
        int dayCache = 1;
        /* dayList contains all of the shows for the current weekday. When weekday is changed, dayList is stored
           into the master showList, and the list is wiped clean to be used again. */
        ArrayList<Show> dayList = new ArrayList<Show>();

        try {
            JSONArray calendarArray = calendarObj.getJSONArray("items"); // "items" array stored here

            // For each JSON Object in the array, extract all necessary data
            // TODO: Integrate music/news/sports/special. Currently music is auto selected (by passing hard coded values in the show constructor).
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

                // Construct a show object and write this event to the show list
                Show show = new Show(calId, 1, title, startTime, endTime, startMinutes, endMinutes, link, description, dayOfWeekInt);

                // Determine how to store this show
                if (dayOfWeekInt == dayCache) {
                    // If the weekday hasn't changed, store this in the existing cache
                    Log.v(TAG, "Adding show (" + title + ") to show list.");
                    dayList.add(show);
                } else {
                    Log.v(TAG, "Day of week change detected! New day of week is " + dayOfWeekInt);
                    // If the weekday has changed, store the cache in the master list
                    showList.add(dayList);

                    // Clear the cache list and add this show
                    Log.v(TAG, "List has been cleared.");
                    dayList = new ArrayList<Show>();
                    Log.v(TAG, "Adding show (" + title + ") to show list.");
                    dayList.add(show);

                    // Then update dayCache's value so this will not be triggered until the NEXT weekday
                    dayCache = dayOfWeekInt;
                }
            }

            Log.v(TAG, "Last event has been created. Adding final list. ");
            showList.add(dayList);

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Programming Information from JSON. ");
            e.printStackTrace();
        }

        // Hide loading screen
        showLoadingScreen(false);

        // Print all values of shows
        //Log.v(TAG, "----------");
        //for (int i=0; i<showList.size(); i++) {
        //    ArrayList<Show> s = showList.get(i);
        //    Log.v(TAG, "* Printing all shows with day of week value: " + (i+1));
        //    for (int j=0; j<s.size(); j++) {
        //        Show show = s.get(j);
        //        Log.v(TAG, "Title: " + show.getTitle());
        //    }
        //    Log.v(TAG, "----------");
        //}

        // Fill date list
        dateList = new ArrayList<String>();
        cal.get(Calendar.DAY_OF_WEEK);
        //dateList.add();

        // Hook up pager and pagerAdapter and hide loading screen
        pager = (ViewPager)findViewById(R.id.schedule_pager);
        pagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        showLoadingScreen(false);

    }

    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)findViewById(R.id.schedule_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.GONE);
        }

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

    private class SchedulePagerAdapter extends FragmentPagerAdapter {

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return new ScheduleFragment(showList.get(i));
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch(position) {
                case 0: title = getString(R.string.sunday);
                    break;
                case 1: title = getString(R.string.monday);
                    break;
                case 2: title = getString(R.string.tuesday);
                    break;
                case 3: title = getString(R.string.wednesday);
                    break;
                case 4: title = getString(R.string.thursday);
                    break;
                case 5: title = getString(R.string.friday);
                    break;
                case 6: title = getString(R.string.saturday);
                    break;
            }
            return title;
        }
    }
}
