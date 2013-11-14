/*
 * fm.krui.kruifm.PlaylistFragment - PlaylistFragment.java
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

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistFragment extends ListFragment implements PlaylistListener {

    private static String TAG = PlaylistFragment.class.getName();

    private View rootView;

    // Cache the listAdapter to allow a quick fast resume.
    protected PlaylistAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_layout, container, false);
        rootView = view;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize track list and play count. For now this is arbitrary, this would be a cool user setting.
        showLoadingScreen(true);
        int playCount = 100;

        // Prepare for network operations
        ViewGroup rootContainer = (RelativeLayout)getActivity().findViewById(R.id.playlist_fragment_container_relativelayout);
        NetworkListener networkListener = new NetworkListener(getActivity(), rootContainer);
        NetworkManager networkManager = new NetworkManager(getActivity(), networkListener);

        // Check for an internet connection
        boolean isConnected = networkManager.checkForNetworkConnection();
        if (!isConnected) {
            // If no connection is detected, show a connection alert.
            rootContainer.removeAllViews();
            networkManager.showConnectionAlert();
        } else {
            // When we hit this point, we can finally grab playlist tracks.
            PlaylistFetcher fetcher = new PlaylistFetcher(getActivity(), this);
            fetcher.execute(playCount);
        }
    }

    @Override
    public void onPlaylistFinish(ArrayList <HashMap<String, Track>> returnedTracks) {

        // Load PlaylistAdapter with track information returned
        adapter = new PlaylistAdapter(returnedTracks, getActivity());

        // Bind ScheduleAdapter to this list to display its data.
        setListAdapter(adapter);

        showLoadingScreen(false);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }

    /**
     * Enables and disables the loading screen on this fragment
     * @param isLoading true to show, false to hide
     */
    private void showLoadingScreen(boolean isLoading) {
        FrameLayout frameLayout = (FrameLayout)rootView.findViewById(R.id.playlist_loading_framelayout);
        if (isLoading) {
            frameLayout.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.INVISIBLE);
        }
    }

}