package fm.krui.kruifm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ShowDetailActivity extends Activity {

	private static final String TAG = KRUIScheduleActivity.class.getName(); // Tag constant for logging purposes

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showdetail_layout);
		
		// enable back button in ActionBar
		// TODO: Modularize this pattern because it will be used in all child views.
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		// Retrieve info on this show
		DatabaseHandler dh = new DatabaseHandler(this);
		Intent intent = getIntent();
		String showId = intent.getExtras().getString("show_id");
		Show show = dh.getShow(showId);
		String title = show.get_title();
		int dayOfWeek = show.get_dayOfWeek();
		String dayOfWeekText = Utils.weekdayIntToStr(dayOfWeek);
		String description = show.get_description();
		String startTime = show.get_startTime();
		String endTime = show.get_endTime();
		
		// Get views and set retrieved values.
		TextView titleView = (TextView)findViewById(R.id.show_title_field);
		TextView dayOfWeekView = (TextView)findViewById(R.id.day_of_week_field);
		TextView timeView = (TextView)findViewById(R.id.show_time_field);
		TextView descriptionView = (TextView)findViewById(R.id.show_description_field);
		
		titleView.setText(title);
		dayOfWeekView.setText("Day of Week: " + dayOfWeekText);
		timeView.setText("Time: " + startTime + " - " + endTime);
		descriptionView.setText("Description: " + description);

	}
}