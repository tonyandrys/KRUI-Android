package fm.krui.kruifm;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Displays KRUI's Programming Schedule.
 */


public class ScheduleFragment extends Fragment {

	private static final String TAG = ScheduleFragment.class.getName(); // Tag constant for logging purposes
    private static final String MAIN_SCHEDULE_FILENAME = "main-studio-json";
    private static final String LAB_SCHEDULE_FILENAME = "lab-json";
    protected ArrayList<Show> showList;

    private View rootView;
    private JSONObject calendarObj;

    /**
     * ScheduleFragments are constucted with a list of shows to display. Fragment handles the positioning and rendering
     * of the objects in its UI.
     * @param showList ArrayList of Show objects.
     */
    public ScheduleFragment(ArrayList<Show> showList) {
        this.showList = showList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_schedule_layout, container, false);
        rootView = view;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        for (int i=0; i<showList.size(); i++) {
            Show show = showList.get(i);
            // TODO: Include Show object in addShow method signature to show more information to the user on click
            addShow(show.getTitle(), show.getDescription(), show.getStartTimeMinutes(), show.getEndTimeMinutes(), show.getCategory());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Adds a show to the schedule.
     * @param title Title of show to display
     * @param description Description of show to display
     * @param startTime Start time of show in minutes from midnight
     * @param endTime End time of show in minutes from midnight
     * @param category Format of this show, required to correctly color the event.
     *
     * Valid settings for category include:
     *                 1 - Regular Rotation
     *                 2 - Music Speciality
     *                 3 - Sports
     *                 4 - News/Talk
     *                 5 - Specials
     */
    private void addShow(String title, String description, int startTime, int endTime, int category) {

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
        //Log.v(TAG, "Start time: " + startTime + " End time: " + endTime);
        //Log.v(TAG, "Setting parameters for " + title);
        RelativeLayout.LayoutParams rrLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPixels(difference));
        rrLayoutParams.leftMargin = dpToPixels(55);
        //Log.v(TAG, "Left margin: " + rrLayoutParams.leftMargin);
        rrLayoutParams.topMargin = dpToPixels(startTime);
        //Log.v(TAG, "Top margin: " + rrLayoutParams.topMargin);
        rrLayoutParams.rightMargin = dpToPixels(5);
        //Log.v(TAG, "Right margin: " + rrLayoutParams.rightMargin);


        /* Build LinearLayout and apply parameters */
        LinearLayout eventLL = new LinearLayout(getActivity());
        eventLL.setOrientation(LinearLayout.VERTICAL);
        eventLL.setPadding(dpToPixels(5), dpToPixels(2), dpToPixels(5), dpToPixels(5));

        // Get background for this event
        eventLL.setBackgroundResource(getEventBackground(category));

        /* Add title of event to LinearLayout */
        TextView titleTV = new TextView(getActivity());

        // Title of a show should be bolded
        titleTV.setText(title);
        titleTV.setTypeface(null, Typeface.BOLD);
        titleTV.setPadding(dpToPixels(5), 0, dpToPixels(5), 0);
        eventLL.addView(titleTV);

        /* Determine length of event to see if we have room to attach a description (if one was passed) */
        int length = endTime - startTime;

        /* Attach a description with a size that depends on the event length (longer events can hold more description text). */
        if (description != null) {
            TextView descriptionTV = new TextView(getActivity());
            descriptionTV.setPadding(dpToPixels(5), dpToPixels(5), dpToPixels(5), dpToPixels(5));
            descriptionTV.setText(description);

            // ~hour long shows can display 1 line of description
            if (difference >= 30 && difference <= 60) {
                descriptionTV.setMaxLines(1);
            }

            // 1:30 long shows can display 2 lines of description
            else if (difference >= 60 && difference <= 90) {
                descriptionTV.setMaxLines(2);
            }

            // 2:00 long shows can display 4 lines of description
            else if (difference >= 120) {
                descriptionTV.setMaxLines(4);
            }

            descriptionTV.setEllipsize(TextUtils.TruncateAt.END);
            descriptionTV.setPadding(dpToPixels(5), dpToPixels(5), dpToPixels(5), dpToPixels(5));
            eventLL.addView(descriptionTV);
        }

        /* Add this view to the schedule UI */
        RelativeLayout rl = (RelativeLayout)rootView.findViewById(R.id.schedule_container_relativelayout);
        rl.addView(eventLL, rrLayoutParams);
    }

    /**
     * Returns the resource ID for an event based on the show category.
     * @param category Show category as an integer (see addShow() for this mapping)
     * @return resID of background drawable
     */
    private int getEventBackground(int category) {
        int res = 0;
        switch (category) {
            case 1:
                // Regular Rotation
                res = R.drawable.blue_rounded_event;
                break;
            case 2:
                // Music Speciality
                res = R.drawable.orange_rounded_event;
                break;
            case 3:
                // Sports
                res = R.drawable.green_rounded_event;
                break;
            case 4:
                // News/Talk
                res = R.drawable.red_rounded_event;
                break;
            case 5:
                // Specials
                res = R.drawable.purple_rounded_event;
                break;
        }
        return res;
    }

    /**
     * Converts dp values to an appropriate amount of pixels based on screen density of this device.
     * @param dp value of dp to convert
     * @return equivalent pixel count
     */
    private int dpToPixels(int dp) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
