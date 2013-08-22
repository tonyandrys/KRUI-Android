package fm.krui.kruifm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Displays KRUI's Programming Schedules.
 */
public class ScheduleFragment extends ListFragment implements DBListener {

	private static final String TAG = KRUIScheduleActivity.class.getName(); // Tag constant for logging purposes
    protected ProgressDialog pd;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Build Database
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading shows, please wait..."); //TODO: String resource
        pd.show();
        new PopulateDB(getActivity(), getActivity(), this).execute();


	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// On click, show detailed information in new activity
		
		@SuppressWarnings("unchecked")
		HashMap<String, String>o = (HashMap<String, String>) l.getItemAtPosition(position);
		String passId = o.get("show_id");
		Intent intent = new Intent(getActivity(), ShowDetailActivity.class);
		intent.putExtra("show_id", passId);
		startActivity(intent);
	}

    @Override
    public void onDBFinish() {
        pd.dismiss();

        DatabaseHandler dh = new DatabaseHandler(getActivity());

        // Read show info from DB and send results to adapter
        // TODO: Check efficiency difference between pulling ALL INFO from db vs. creating Show object.

        Cursor result = dh.readDB("shows", new String[] { "show_id" }, null, null, null, null, null, null);
        ArrayList<HashMap<String, String>> parsedList = new ArrayList<HashMap<String, String>>();

        // Events start on Sunday by default.
        int currentDay = 0;

        // Store positions where day of week changes to be used when displaying the schedule.
        ArrayList<Integer> dayOfWeekChanges = new ArrayList<Integer>();

        // Zero will always start the week, so add it by default.
        dayOfWeekChanges.add(0);

        int loopCount = result.getCount();
        for (int i=0; i<loopCount; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            Show show = dh.getShow(result.getString(0));
            String showId = show.get_id();
            String title = show.get_title();
            String startTime = show.get_startTime();
            int dayOfWeek = show.get_dayOfWeek();

            // When the day of the week of events changes, we need to change the layout in the list.
            // TODO: Inefficient hacky implementation of day of week change... this should be cleaned up.
            if (dayOfWeek != currentDay) {

                // Add this location to the list of day/week changes.
                dayOfWeekChanges.add(i);
                map.put("day_of_week", Integer.toString(dayOfWeek));
                parsedList.add(map);
                currentDay = dayOfWeek;
                loopCount++; // Iterate again because no show was printed in favor of a banner.
            }

            // Compile show info into hash map.
            else {
                map.put("show_id", showId);
                map.put("title", title);
                map.put("start_time", startTime);
                map.put("day_of_week", Integer.toString(dayOfWeek));
                parsedList.add(map);
                result.moveToNext();
            }

        }
        result.close();

        // Load ScheduleAdapter with the information collected from DB
        ScheduleAdapter adapter = new ScheduleAdapter(getActivity(), parsedList, getActivity(), dayOfWeekChanges);

        // Bind ScheduleAdapter to this list to display its data.
        setListAdapter(adapter);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);

    }
}