package fm.krui.kruifm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class KRUIScheduleActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_fragment_layout);

		// All tab switching and rendering logic has been pushed into the fragments. 
		FragmentManager fm = getSupportFragmentManager();
		ScheduleController scheduleController = (ScheduleController) fm.findFragmentById(R.id.fragment_tab);
		
		// Display 89.7 schedule by default
		scheduleController.displayMainSchedule();
	}
}
