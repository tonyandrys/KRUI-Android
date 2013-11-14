package fm.krui.kruifm;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
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

    // Cache file names to store schedule information
    private static final String MAIN_SCHEDULE_FILENAME = "main-studio-json";
    private static final String MAIN_SCHEDULE_MS_FILENAME = "main-studio-ms-json";
    private static final String MAIN_SCHEDULE_RR_FILENAME = "main-studio-rr-json";
    private static final String MAIN_SCHEDULE_S_FILENAME = "main-studio-s-json";
    private static final String MAIN_SCHEDULE_NT_FILENAME = "main-studio-nt-json";
    private static final String MAIN_SCHEDULE_SP_FILENAME = "main-studio-sp-json";
    private static final String LAB_SCHEDULE_FILENAME = "lab-json";

    // URL locations of show JSON data
    private static final String ROOT_URL = "http://krui.fm/kruiapp/json/";
    private static final String MAIN_SCHEDULE_RR_URL = "main_studio_rr.txt";
    private static final String MAIN_SCHEDULE_MS_URL = "main_studio_ms.txt";
    private static final String MAIN_SCHEDULE_S_URL = "main_studio_s.txt";
    private static final String MAIN_SCHEDULE_NT_URL = "main_studio_nt.txt";
    private static final String MAIN_SCHEDULE_SP_URL = "main_studio_sp.txt";

    // Show storage by weekday
    private ArrayList<Show> sunday;
    private ArrayList<Show> monday;
    private ArrayList<Show> tuesday;
    private ArrayList<Show> wednesday;
    private ArrayList<Show> thursday;
    private ArrayList<Show> friday;
    private ArrayList<Show> saturday;

    // Show storage used as cache before saving to showList
    PriorityQueue<Show> pq;

    private ViewPager pager;
    private FragmentPagerAdapter pagerAdapter;
    private JSONObject calendarObj;
    private GregorianCalendar cal;
    private ArrayList<String> dateList;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity_layout);

        // Enable back button in this activity
        ActionBar actionbar = getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setSubtitle("Our 7 Day Program Schedule");

        showLoadingScreen(true);
        cal = new GregorianCalendar();

        // Download show data
        downloadShowData();
    }

    /**
     * Downloads JSON-based show data from KRUI's servers, caches it, and parses it into Show objects to be displayed.
     * Filenames of each cache file are stored as member constants
     * Categories are represented as integers which determine event coloring for all shows pulled from this text file.
     *                 1 - Regular Rotation
     *                 2 - Music Speciality
     *                 3 - Sports
     *                 4 - News/Talk
     *                 5 - Special Programming
     */
    private void downloadShowData() {

        // Download all 5 text files, each one stores data from one category of show.
        String[] urls = {ROOT_URL + MAIN_SCHEDULE_RR_URL, ROOT_URL + MAIN_SCHEDULE_MS_URL, ROOT_URL + MAIN_SCHEDULE_S_URL, ROOT_URL + MAIN_SCHEDULE_NT_URL, ROOT_URL + MAIN_SCHEDULE_SP_URL};

        // Store each text file onto internal storage to avoid having to download information every time this activity is called.
        String[] filenames = {MAIN_SCHEDULE_RR_FILENAME, MAIN_SCHEDULE_MS_FILENAME, MAIN_SCHEDULE_S_FILENAME, MAIN_SCHEDULE_NT_FILENAME, MAIN_SCHEDULE_SP_FILENAME};

        // Execute process
        Log.v(TAG, "About to launch TextFetcher to grab JSON files");
        try {
            TextFetcher tf = new TextFetcher(this, urls, filenames, this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                tf.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
            } else {
                tf.execute((Void[])null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTextDownloaded() {

        String[] urls = {MAIN_SCHEDULE_RR_URL, MAIN_SCHEDULE_MS_URL, MAIN_SCHEDULE_S_URL, MAIN_SCHEDULE_NT_URL, MAIN_SCHEDULE_SP_URL};
        String[] filenames = {MAIN_SCHEDULE_RR_FILENAME, MAIN_SCHEDULE_MS_FILENAME, MAIN_SCHEDULE_S_FILENAME, MAIN_SCHEDULE_NT_FILENAME, MAIN_SCHEDULE_SP_FILENAME};

        // Initialize a Priority Queue which will cache show objects before sorting into the final showList.
        pq = new PriorityQueue<Show>(11, new ShowComparator());

        // For each day of the week, scan all five event categories
        // For each text file we downloaded, parse the show data inside and store them in showList.
        for (int k=0; k<filenames.length; k++) {
            Log.v(TAG, "* Beginning scan of category: " + filenames[k]);

            // Read text file
            try {
                // Construct a JSONObject from the stored text file containing events of this category
                JSONObject calObj = new JSONObject(readTextFile(new File(getFilesDir(), filenames[k])));

                // Store Sun-Sat events from this category in the Priority Queue
                parseShowData(calObj, k+1);

            } catch (NullPointerException e) {
                Log.e(TAG, "No shows found for category: " + filenames[k]);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException thrown when processing shows!");
            }

        }

        Log.v(TAG, "All shows have been parsed, now sort into their proper place in showList.");

        // Initialize show storage
        sunday = new ArrayList<Show>();
        monday = new ArrayList<Show>();
        tuesday = new ArrayList<Show>();
        wednesday = new ArrayList<Show>();
        thursday = new ArrayList<Show>();
        friday = new ArrayList<Show>();
        saturday = new ArrayList<Show>();

        // Pull every show off the Priority Queue and store them
        int size = pq.size();
        for (int i=0; i<size; i++) {
            storeShow(pq.poll());
            Log.v(TAG, "PQ Count:" + pq.size());
        }

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

    /**
     * Stores the passed Show into its correct day-of-week list for use by ScheduleFragments.
     * @param show Show to store
     */
    private void storeShow(Show show) {
        Log.v(TAG, "Trying to store " + show.getTitle());
        int w = show.getDayOfWeek();
        String log = "SOMETHING BROKE"; //FIXME: DELETE ME I AM USELESS IF NOT FOR DEBUGGING
        switch (w) {
            case 1:
                sunday.add(show);
                log = "sunday";
                break;
            case 2:
                monday.add(show);
                log = "monday";
                break;
            case 3:
                tuesday.add(show);
                log = "tuesday";
                break;
            case 4:
                wednesday.add(show);
                log = "wednesday";
                break;
            case 5:
                thursday.add(show);
                log = "thursday";
                break;
            case 6:
                friday.add(show);
                log = "friday";
                break;
            case 7:
                saturday.add(show);
                log = "saturday";
                break;
        }
        Log.v(TAG, show.getTitle() + " stored into " + log);
    }

    /**
     * Shows or hides the loading indicator which covers the entire activity.
     * @param isLoading true to show, false to hide.
     */
    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)findViewById(R.id.schedule_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.GONE);
        }

    }

    /**
     * Parses a Google Calendar JSONObject and builds a list of Shows, which are assigned the passed category value.
     * @param calObj Google Calendar JSONObject to parse
     * @param category category value as an int
     */
    private void parseShowData(JSONObject calObj, int category) {


        /* When dayOfWeek != dayCache, we have changed weekdays, so we must change storage location in the list. Initial
        value will match with Sunday shows */
        int dayCache = 1;

        /* dayList contains all of the shows for the current weekday. When weekday is changed, dayList is stored
        into the master showList, and the list is wiped clean to be used again. */
        ArrayList<Show> dayList = new ArrayList<Show>();

        // Map used to convert text weekday values to integers
        HashMap< String, Integer> weekdayToIntMap = new HashMap<String, Integer>();
        String[] weekdays = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int j=0; j<7; j++) {
            weekdayToIntMap.put(weekdays[j], j+1);
        }

        try {
            JSONArray calendarArray = calObj.getJSONArray("items"); // "items" array stored here

            // For each JSON Object in the array, extract all necessary data
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

                // Set this show object's category value to color it correctly in ScheduleFragment.
                // Using the key in downloadShowData, the correct category is always the k+1th value.
                show.setCategory(category);

                // Enqueue this show onto the Priority Queue
                Log.v(TAG, "Adding " + show.getTitle() + " to the priority queue.");
                Log.v(TAG, "Show category is: " + show.getCategory());
                pq.add(show);
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "No events were available to parse.");

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing Programming Information from JSON. ");
            e.printStackTrace();
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
        ArrayList<Show> l = new ArrayList<Show>();
        switch (i) {
            case 0:
                l = sunday;
                break;
            case 1:
                l = monday;
                break;
            case 2:
                l = tuesday;
                break;
            case 3:
                l = wednesday;
                break;
            case 4:
                l = thursday;
                break;
            case 5:
                l = friday;
                break;
            case 6:
                l = saturday;
                break;
        }
        return new ScheduleFragment(l);
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
