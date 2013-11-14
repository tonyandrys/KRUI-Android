/*
 * fm.krui.kruifm.PreferenceManager - PreferenceManager.java
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

/**
 * fm.krui.kruifm - PreferenceManager
 *
 * @author Tony Andrys
 *         Created: 08/21/2013
 *         (C) 2013 - Tony Andrys
 */

import android.content.Context;
import android.content.SharedPreferences;

/**
 * All preferences can be set or retrieved through this class.
 */
public class PreferenceManager {

    /* Constants */
    public final String PREFS_NAME = "KRUI-Prefs";

    // Stream Quality
    public final String STREAM_QUALITY = "streamQuality";
    public final int HIGH_QUALITY = 1;
    public final int LOW_QUALITY = 0;

    // Album Art Download Preferences
    public final String DOWNLOAD_ALBUM_ART = "dlAlbumArt";

    // Welcome Message Preference
    public final String WELCOME_MESSAGE = "welcomeMessage";

    // Player State Preference
    public final String IS_PLAYING = "isPlaying";

    SharedPreferences prefs;
    Context context;

    public PreferenceManager(Context context) {
        this.context = context;

        // Restore user prefs
        prefs = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public void setStreamQuality(int quality) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(STREAM_QUALITY, quality);
        editor.commit();
    }

    public void setWelcomePreference(boolean showWelcomeMessage) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(WELCOME_MESSAGE, showWelcomeMessage);
        editor.commit();
    }

    public int getStreamQuality() {
        // Stream in high quality by default unless otherwise set by user.
        return prefs.getInt(STREAM_QUALITY, 1);
    }

    public void setAlbumArtDownloadPreference(boolean isArtDownloaded) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DOWNLOAD_ALBUM_ART, isArtDownloaded);
        editor.commit();
    }

    public boolean getAlbumArtDownloadPreference() {
        // Download album art by default unless turned off by user.
        return prefs.getBoolean(DOWNLOAD_ALBUM_ART, true);
    }

    public boolean getWelcomePreference() {
        return prefs.getBoolean(WELCOME_MESSAGE, true);
    }

    public boolean getPlayerState() {
        return prefs.getBoolean(IS_PLAYING, false);
    }

    public void setPlayerState(boolean playerState) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IS_PLAYING, playerState);
        editor.commit();
    }



}
