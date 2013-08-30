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
 * All user preferences can be set or retrieved through this class.
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

}
