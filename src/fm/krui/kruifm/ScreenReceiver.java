package fm.krui.kruifm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * fm.krui.kruifm - ScreenReceiver
 *
 * @author Tony Andrys
 *         Created: 11/01/2013
 *         (C) 2013 - Tony Andrys
 */

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // When screen is off, do not perform track/UI updates. Store favorited track if it was favorited!
            wasScreenOn = false;

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // When screen turns back on, we need to refresh the UI.
            /* Case 1: We haven't received new track information since the screen was disabled.
            * If this is the case, we need not change the album art or song text, but we DO need to refresh
            * the favorite track star. In other words, if a user favorited a track and locked the screen, we need to
            * restore that state so the user isn't confused and adds the favorited track again. */


            /* Case 2: There is new track information. We need to perform an album art/track text refresh and RESET the favorite
            * star so the user can add this new track to their favorites if desired. */
            wasScreenOn = true;
        }
    }

}