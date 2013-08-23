package fm.krui.kruifm;

import android.app.ActionBar;
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

        // Prepare the ActionBar for this fragment
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(getString(R.string.favorite_tracks_title));
        actionBar.setSubtitle(getString(R.string.favorite_tracks_subtitle));

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
