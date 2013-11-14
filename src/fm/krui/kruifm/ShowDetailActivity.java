/*
 * fm.krui.kruifm.ShowDetailActivity - ShowDetailActivity.java
 *
 * (C) 2013 - Tony Andrys
 * http://www.tonyandrys.com
 *
 * Created: 11/14/2013
 *
 * ---
 *
 * This file is part of KRUI.FM.
 *
 * KRUI.FM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * KRUI.FM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KRUI.FM.  If not, see <http://www.gnu.org/licenses/>.
 */

package fm.krui.kruifm;

import android.app.Activity;
import android.os.Bundle;

public class ShowDetailActivity extends Activity {

	private static final String TAG = KRUIScheduleActivity.class.getName(); // Tag constant for logging purposes

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showdetail_layout);

        /*

            ***Temporarily Disabled until I complete removal of database storage***

		// enable back button in ActionBar
		// TODO: Modularize this pattern because it will be used in all child views.
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		// Retrieve info on this show
		Intent intent = getIntent();
		String showId = intent.getExtras().getString("show_id");
		Show show = dh.getShow(showId);
		String title = show.getTitle();
		int dayOfWeek = show.getDayOfWeek();
		String dayOfWeekText = Utils.weekdayIntToStr(dayOfWeek);
		String description = show.getDescription();
		String startTime = show.getStartTimeUTC();
		String endTime = show.getEndTimeUTC();
		
		// Get views and set retrieved values.
		TextView titleView = (TextView)findViewById(R.id.show_title_field);
		TextView dayOfWeekView = (TextView)findViewById(R.id.day_of_week_field);
		TextView timeView = (TextView)findViewById(R.id.show_time_field);
		TextView descriptionView = (TextView)findViewById(R.id.show_description_field);
		
		titleView.setText(title);
		dayOfWeekView.setText("Day of Week: " + dayOfWeekText);
		timeView.setText("Time: " + startTime + " - " + endTime);
		descriptionView.setText("Description: " + description);*/

	}
}