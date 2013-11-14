/*
 * fm.krui.kruifm.IntentManager - IntentManager.java
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
