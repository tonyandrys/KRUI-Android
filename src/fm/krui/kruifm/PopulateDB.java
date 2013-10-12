package fm.krui.kruifm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class PopulateDB extends AsyncTask<Void, Integer, Void> {
	
	// Retain to allow interaction with parent activity
	private Context context;
	private Activity parent;
	private ProgressDialog pd;
    private DBListener dbListener;

    // Web resources constants
    private String rootUrl = "https://www.googleapis.com/calendar/v3/calendars/";
    private String mainCalendar = "krui.fm_3165kjuskfgpafro155137000s@group.calendar.google.com";
    private String labCalendar = "krui.fm_9p7f17segeguatpg2k66v3l61o@group.calendar.google.com";
    private String timeMin = "2013-07-07T00:00:00.000-05:00";
    private String timeMax = "2013-07-14T05:00:00.000-05:00";

    // Calendar HashMap constants
    final public String TIME_MIN = "timeMin";
    final public String TIME_MAX = "timeMax";

    private static final int MAIN = 0x1;
	private static final int LAB = 0x2;
	private static final String TAG = StreamActivity.class.getName(); // Tag constant for logging purposes
	
	public PopulateDB(Context context, Activity parent, DBListener listener) {
		this.context = context;
		this.parent = parent;
        this.dbListener = listener;
	}
	
	@Override
	protected void onPreExecute() {

	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		
		// Connect to database
		DatabaseHandler dh = new DatabaseHandler(context);
		
		// TODO: Remove truncation of the database on every build-- ONLY necessary for testing.
		dh.truncate();
		
		// Get 89.7 shows, then get Lab shows.
        // FIXME: Lab shows do not exist due to lack of calendar entries over the summer.
		buildShowDB(dh, MAIN);
		//buildShowDB(dh, LAB);
		return null;
	}
	
	@Override
	protected void onPostExecute(final Void result) {
        dbListener.onDBFinish();
	}

    /**
     * Depending on the current date, this calculates the lower and upper bound of the events such that
     * we will only retrieve events that occur this week.
     */
    protected void calculateDate() {

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int decValue = cal.get(Calendar.DAY_OF_WEEK);
        
        HashMap<String, String> dateMap = new HashMap<String, String>();
    }

	
	public void buildShowDB(DatabaseHandler d, int station) {
		
		// Depending on input, get the 89.7 calendar or the Lab calendar
		String apiQuery = "";
		if (station == MAIN) { 
		apiQuery = "http://krui.fm/kruiapp/json/main_studio.txt";
		} else if (station == LAB) {
		apiQuery = "http://krui.fm/kruiapp/json/the_lab.txt";
		}
		JSONObject calendarObj  = new JSONObject();
		calendarObj = JSONFunctions.getJSONObjectFromURL(apiQuery);
		
		// Map used to convert text weekday values to integers
		HashMap<String, Integer> weekdayToIntMap = new HashMap<String, Integer>();
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
				int dayOfWeekInt = weekdayToIntMap.get(dayOfWeekText);
				
				// Since SQLite can only handle integers, let Main Studio = 1 and a Lab show = 2.
				int stationFlag = 0; // If zero is added, 
				if (station == MAIN) {
					stationFlag = 1;
				} else if (station == LAB) {
					stationFlag = 2;
				}
				
				// If the station flag is zero, we have a serious problem.
				assert stationFlag != 0;
				
				// Add information to the database
				Show show = new Show(calId, stationFlag, title, dayOfWeekInt, startTime, endTime, link, description, 1, 0, 0, 0);
				d.addShow(show);
				Log.i(TAG, show.get_title() + " has been added to the database.");
				dbAddCount++;
			}
		
		if (station == MAIN) { 
			Log.i(TAG, "** Main studio shows added to database.");
		} else if (station == LAB) {
			Log.i(TAG, "** Lab shows added to database.");
		}
		Log.i(TAG, Integer.toString(dbAddCount) + " entries added to database.");
			
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSONArray & adding show to database: ");
			e.printStackTrace();
		}
	}
}
