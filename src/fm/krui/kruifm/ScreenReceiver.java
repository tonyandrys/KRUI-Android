/*
 * fm.krui.kruifm.ScreenReceiver - ScreenReceiver.java
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