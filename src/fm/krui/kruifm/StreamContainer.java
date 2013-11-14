/*
 * fm.krui.kruifm.StreamContainer - StreamContainer.java
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

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import pl.verdigo.libraries.drawer.Drawer;

/**
 * fm.krui.kruifm - StreamContainer
 * Created by Tony Andrys on 07/10/2013
 */


/**
 * Root container for all radio fragments. This includes streaming, extended playlist, DJ info, and more.
 */
public class StreamContainer extends FragmentActivity {

    private static String TAG = StreamContainer.class.getName();

    // KRUI Wordpress JSON Feeds
    final String musicURL = "http://krui.fm/category/music/?json=1";
    final String newsURL = "http://krui.fm/category/news/?json=1";
    final String sportsURL = "http://krui.fm/category/sports/?json=1";

    Drawer drawer;
    public static Fragment currentFragment;

    // Fragment reference constants
    private final int STREAM_TAB = 0;
    private final int STATION_SELECT_TAB = 1;
    private final int PLAYLIST_TAB = 1;
    private final int DJ_TAB = 2;
    private final int FAVORITE_TRACKS_TAB = 3;
    private final int MUSIC_ARTICLES_TAB = 4;
    private final int NEWS_ARTICLES_TAB = 5;
    private final int SPORTS_ARTICLES_TAB = 6;

    // Current fragment state
    protected int TAB_STATE = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showDrawer();
                break;

        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_container_layout);

        // Allow custom home button actions
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        // Instantiate the Station Selection Fragment
        Fragment fragment = new StationSelectionFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(STATION_SELECT_TAB)).commit();
        applyActionBarParameters(STATION_SELECT_TAB);
        currentFragment = (StationSelectionFragment)getSupportFragmentManager().findFragmentById(STATION_SELECT_TAB);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    private void showDrawer() {
        if (drawer == null) {
            drawer = Drawer.createLeftDrawer(this, R.layout.drawer_content_layout);
            drawer.init();
            drawer.setDrawerWidth(200f);
            drawer.setReuse(true);
        }

        drawer.show();
    }

    /**
     * Styles the ActionBar appropriately based on the fragment to be loaded
     * @param fragmentId Integer identifier of fragment
     */
    private void applyActionBarParameters(int fragmentId) {
        // Default params
        ActionBar actionBar = getActionBar();
        String title = "";
        String subTitle = "";
        int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;

        // Set params based on opening tab
        switch (fragmentId) {
            case STREAM_TAB:
                //navigationMode = ActionBar.NAVIGATION_MODE_LIST;
                break;
            case PLAYLIST_TAB:
                title = getString(R.string.extended_playlist_title);
                subTitle = getString(R.string.extended_playlist_subtitle);
                break;
            case DJ_TAB:
                title = getString(R.string.dj_info_tab);
                break;
            case FAVORITE_TRACKS_TAB:
                title = getString(R.string.favorite_tracks_title);
                subTitle = getString(R.string.favorite_tracks_subtitle);
                break;
            case MUSIC_ARTICLES_TAB:
                title = getString(R.string.music_content_sidebar);
                break;
            case NEWS_ARTICLES_TAB:
                title = getString(R.string.news_content_sidebar);
                break;
            case SPORTS_ARTICLES_TAB:
                title = getString(R.string.sports_content_sidebar);
                break;
        }

        // Apply parameters
        actionBar.setNavigationMode(navigationMode);
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
    }

    /* Start sidebar controller functions (one for each sidebar item) */

    // Listen Fragment
    public void showStream(View view) {
        drawer.cancel();
        Fragment fragment = new StationSelectionFragment();
        fragment.setHasOptionsMenu(true); // Enable action bar menu modification from this fragment.
        fragment.setMenuVisibility(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(STREAM_TAB)).commit();
        TAB_STATE = STREAM_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    // Playlist Fragment
    public void showPlaylist(View view) {
        drawer.cancel();
        Fragment fragment = new PlaylistFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(PLAYLIST_TAB)).commit();
        TAB_STATE = PLAYLIST_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    // DJ Info Fragment
    public void showDJInfo(View view) {
        Intent intent = new Intent(this, DJInfoActivity.class);
        startActivity(intent);
        //drawer.cancel();
        //Fragment fragment = new DJInfoFragment();
        //getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(DJ_TAB)).commit();
        //TAB_STATE = DJ_TAB;
        //applyActionBarParameters(TAB_STATE);
        //currentFragment = fragment;
    }

    // Favorite Tracks Fragment
    public void showFavoriteTracks(View view) {
        drawer.cancel();
        Fragment fragment = new FavoriteTrackFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(FAVORITE_TRACKS_TAB)).commit();
        TAB_STATE = FAVORITE_TRACKS_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    // THESE SHOULD ALL BRING BACK THE STREAM CONTROLLER
    public void showMusicArticles(View view) {
        drawer.cancel();
        String title = getString(R.string.music_content_sidebar);
        Fragment fragment = new WordpressViewer(title, musicURL);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(MUSIC_ARTICLES_TAB)).commit();
        TAB_STATE = MUSIC_ARTICLES_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    public void showNewsArticles(View view) {
        drawer.cancel();
        String title = getString(R.string.news_content_sidebar);
        Fragment fragment = new WordpressViewer(title, newsURL);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(NEWS_ARTICLES_TAB)).commit();
        TAB_STATE = NEWS_ARTICLES_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    public void showSportsArticles(View view) {
        drawer.cancel();
        String title = getString(R.string.sports_content_sidebar);
        Fragment fragment = new WordpressViewer(title, sportsURL);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(SPORTS_ARTICLES_TAB)).commit();
        TAB_STATE = SPORTS_ARTICLES_TAB;
        applyActionBarParameters(TAB_STATE);
        currentFragment = fragment;
    }

    // Schedule Activity
    public void showSchedule(View view) {
        drawer.cancel();
        Intent transIntent = new Intent(this, KRUIScheduleActivity.class);
        startActivity(transIntent);
    }

}