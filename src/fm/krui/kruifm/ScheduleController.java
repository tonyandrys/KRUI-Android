package fm.krui.kruifm;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ScheduleController extends Fragment {

    // Station Constants
	final public int MAIN_STATE = 1;
	final public int LAB_STATE = 2;
	
	private int tabState;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set ActionBar Navigation Parameters
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Define each action bar option as a list entry.
        String[] stationList = getResources().getStringArray(R.array.station_dropdown_list);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.station_spinner_layout, R.id.station_spinner_textview, stationList);

        // Define actions to be performed when each dropdown item is selected.
        ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                switch (position) {
                    case 0:
                        displayMainSchedule();
                        break;
                    case 1:
                        displayLabSchedule();
                }
                return true;
            }

        };

        // Apply dropdown listener and actionbar items
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(listAdapter, navListener);

        // Finally, inflate schedule_tab linearlayout to display content.
        // FIXME: Figure out what the hell this does.
        View view = inflater.inflate(R.layout.schedule_tab, container, false);
		return view;
	}
	
	public void displayMainSchedule() {

        // If the 89.7 schedule is currently displayed, do nothing.
		if (tabState != MAIN_STATE) {

            // If the 89.7 schedule is not visible, display it.
			tabState = MAIN_STATE;
			
			FragmentManager fm = getFragmentManager();
			if (fm != null) {
				FragmentTransaction ft = fm.beginTransaction();
				// Replace the current content with 89.7 schedule.
				ft.replace(R.id.fragment_content, new ScheduleFragment());
				ft.commit();
			}
			
		}
	}
	
	public void displayLabSchedule() {

        // If the Lab schedule is currently displayed, do nothing.
		if (tabState != LAB_STATE) {

            // if the Lab schedule is not visible, display it.
			tabState = LAB_STATE;
			
			FragmentManager fm = getFragmentManager();
			if (fm != null) {
				FragmentTransaction ft = fm.beginTransaction();
				// Replace the current content The Lab schedule.
				ft.replace(R.id.fragment_content, new ScheduleFragment());
				ft.commit();
			}
		}
		
	}
	
	
}
