package fm.krui.kruifm;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

/**
 * fm.krui.kruifm - DJInfoActivity
 *
 * @author Tony Andrys
 *         Created: 08/29/2013
 *         (C) 2013 - Tony Andrys
 */
/**
 * Root container for all radio fragments. This includes streaming, extended playlist, DJ info, and more.
 */
public class DJInfoActivity extends FragmentActivity implements ActionBar.TabListener, DJInfoListener {

    private static String TAG = DJInfoActivity.class.getName();

    private DJ dj;
    private Fragment currentFragment;

    // Fragment reference constants
    private final int BIO_TAB = 0;
    private final int TWITTER_TAB = 1;

    // Current fragment state
    protected int TAB_STATE = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dj_info_container_layout);
        showLoadingScreen(true);

        // Allow custom home button actions
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* For each component, create a tab for navigation */
        // Stream
        ActionBar.Tab bioTab = actionBar.newTab().setText(getResources().getString(R.string.dj_bio_tab)).setTabListener(this);
        // Extended Playlist
        ActionBar.Tab twitterTab= actionBar.newTab().setText(getResources().getString(R.string.dj_twitter_tab)).setTabListener(this);

        // Then apply the tabs to the ActionBar.
        actionBar.addTab(bioTab);
        actionBar.addTab(twitterTab);

        // Download DJ object attached to the last played song to display information.
        Log.v(TAG, "Grabbing DJ information...");
        DJInfoFetcher fetcher = new DJInfoFetcher(this, this);
        fetcher.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

        int position = tab.getPosition();

        // Ensure the selected tab is not the tab that is currently open. If it is, do nothing.
        // FIXME: This check might be redundant with onTabReselected...
        if (position == TAB_STATE) {
            return;
        } else {
            Fragment fragment;
            // Based on the position of the selected tab, replace the current fragment in the container with the desired fragment.
            switch (position) {
                case BIO_TAB:
                    fragment = new DJInfoFragment(dj);
                    fragment.setHasOptionsMenu(true); // Enable action bar menu modification from this fragment.
                    fragment.setMenuVisibility(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.dj_info_fragment_container, fragment, Integer.toString(BIO_TAB)).commit();
                    TAB_STATE = BIO_TAB;
                    currentFragment = fragment;
                    break;
                case TWITTER_TAB:
                    fragment = new TwitterFragment(dj);
                    getSupportFragmentManager().beginTransaction().replace(R.id.dj_info_fragment_container, fragment, Integer.toString(TWITTER_TAB)).commit();
                    TAB_STATE = TWITTER_TAB;
                    currentFragment = fragment;
                    break;
            }
        }
    }

    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)findViewById(R.id.djinfo_container_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onFinish(DJ dj) {

        // Once the DJ object has been downloaded, store it for retrieval by child fragments.
        this.dj = dj;
        Log.v(TAG, "DJ object has been stored.");

        // Instantiate the bio fragment and hide the loading screen.
        Fragment fragment = new DJInfoFragment(dj);
        fragment.setHasOptionsMenu(true);
        fragment.setMenuVisibility(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.dj_info_fragment_container, fragment, Integer.toString(BIO_TAB)).commit();
        currentFragment = getSupportFragmentManager().findFragmentById(BIO_TAB);
        showLoadingScreen(false);
    }
}