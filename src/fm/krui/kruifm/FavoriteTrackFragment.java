/*
 * fm.krui.kruifm.FavoriteTrackFragment - FavoriteTrackFragment.java
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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * fm.krui.kruifm - FavoriteTrackFragment
 *
 * @author Tony Andrys
 *         Created: 08/22/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Fragment which displays the user's favorite tracks.
 */
public class FavoriteTrackFragment extends ListFragment {

    final private String TAG = FavoriteTrackFragment.class.getName();
    private View rootView;

    // Adapter cache.
    private FavoriteTrackAdapter adapter;
    private ArrayList<HashMap<String,String>> favoriteList;
    private FavoriteTrackManager favTrackManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorite_track_layout, container, false);
        rootView = view;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Retrieve user's favorited tracks as a list.
        favTrackManager = new FavoriteTrackManager(getActivity());
        favTrackManager.loadFavoriteTracks();
        favoriteList = favTrackManager.getFavoriteList();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load FavoriteTrackAdapter with track information.
        adapter = new FavoriteTrackAdapter(favoriteList, favTrackManager, getActivity());

        // Bind adapter to this list to display its data.
        setListAdapter(adapter);
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Whenever this fragment loses focus, store the new track data.
        favTrackManager.storeFavoriteTracks();
    }

}
