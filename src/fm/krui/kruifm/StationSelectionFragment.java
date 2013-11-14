/*
 * fm.krui.kruifm.StationSelectionFragment - StationSelectionFragment.java
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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * fm.krui.kruifm - StationSelectionFragment
 *
 * @author Tony Andrys
 *         Created: 10/31/2013
 *         (C) 2013 - Tony Andrys
 */

public class StationSelectionFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_selection_layout, container, false);
        rootView = view;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Determine if welcome window should be displayed or hidden
        final PreferenceManager pm = new PreferenceManager(getActivity());

        // If preference key indicates user has hidden the welcome window, do not display it.
        if (!pm.getWelcomePreference()) {
            setWelcomeMessage(false);
        } else {
            // If we are displaying the welcome message, we must build a listener for the "hide" button
            setWelcomeMessage(true);
            Button hideButton = (Button)rootView.findViewById(R.id.hide_welcome_message_button);
            hideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When hide button is clicked, hide the window and set pref key to never display again.
                    pm.setWelcomePreference(false);
                    setWelcomeMessage(false);
                    rootView.invalidate();
                }
            });
        }

        // Assign button listeners
        RelativeLayout mainStudio = (RelativeLayout)rootView.findViewById(R.id.main_studio_stream_button);
        mainStudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start streaming 89.7
                Intent intent = new Intent(getActivity(), StreamActivity.class);
                intent.putExtra(StreamActivity.KEY_STATION_TAG, StreamActivity.MAIN_STUDIO);
                startActivity(intent);
            }
        });

        RelativeLayout labButton = (RelativeLayout)rootView.findViewById(R.id.the_lab_stream_button);
        labButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start streaming The Lab
                Intent intent = new Intent(getActivity(), StreamActivity.class);
                intent.putExtra(StreamActivity.KEY_STATION_TAG, StreamActivity.THE_LAB);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Controls the display of the welcome message on this page.
     * @param showWelcomeMessage true to display, false to hide
     */
    private void setWelcomeMessage(boolean showWelcomeMessage) {
        RelativeLayout welcomeWindow = (RelativeLayout)rootView.findViewById(R.id.welcome_container_relativelayout);
        if (showWelcomeMessage) {
            welcomeWindow.setVisibility(View.VISIBLE);
        } else {
            welcomeWindow.setVisibility(View.GONE);
        }
    }


}
