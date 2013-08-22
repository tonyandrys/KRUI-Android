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

    Drawer drawer;
    Fragment currentFragment;

    // Fragment reference constants
    private final int STREAM_TAB = 0;
    private final int PLAYLIST_TAB = 1;
    private final int DJ_TAB = 2;

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

        // Instantiate the Streaming Fragment
        Fragment fragment = new StreamFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setMenuVisibility(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(STREAM_TAB)).commit();
        currentFragment = (StreamFragment)getSupportFragmentManager().findFragmentById(STREAM_TAB);
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

    /* Start sidebar controller functions (one for each sidebar item) */

    // Listen Fragment
    public void showStream(View view) {
        drawer.cancel();
        Fragment fragment = new StreamFragment();
        fragment.setHasOptionsMenu(true); // Enable action bar menu modification from this fragment.
        fragment.setMenuVisibility(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(STREAM_TAB)).commit();
        TAB_STATE = STREAM_TAB;
        currentFragment = fragment;
    }

    // Playlist Fragment
    public void showPlaylist(View view) {
        drawer.cancel();
        Fragment fragment = new PlaylistFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(PLAYLIST_TAB)).commit();
        TAB_STATE = PLAYLIST_TAB;
        currentFragment = fragment;
    }

    // DJ Info Fragment
    public void showDJInfo(View view) {
        drawer.cancel();
        Fragment fragment = new DJInfoFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.stream_fragment_container, fragment, Integer.toString(DJ_TAB)).commit();
        TAB_STATE = DJ_TAB;
        currentFragment = fragment;
    }

    // Schedule Activity
    public void showSchedule(View view) {
        drawer.cancel();
        Intent transIntent = new Intent(this, KRUIScheduleActivity.class);
        startActivity(transIntent);
    }
}