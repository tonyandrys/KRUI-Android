package fm.krui.kruifm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * fm.krui.kruifm - IntentManager
 *
 * @author Tony Andrys
 *         Created: 09/27/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Intents which interact with other applications/OS components are launched from this class.
 */
public class IntentManager {

    private Activity activity;

    public IntentManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Pushes KRUI studio phone number to user's phone. Only dials, does not make calls (so the user can back out if needed).
     */
    public void dialStudio() {
        String studioNumber = "319-335-8970";
        String uri = "tel:" + studioNumber.trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        activity.startActivity(intent);
    }
}
