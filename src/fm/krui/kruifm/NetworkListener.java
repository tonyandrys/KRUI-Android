/*
 * fm.krui.kruifm.NetworkListener - NetworkListener.java
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
 * fm.krui.kruifm - NetworkListener
 *
 * @author Tony Andrys
 *         Created: 07/26/2013
 *         (C) 2013 - Tony Andrys
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Tightly coupled with NetworkManager -- the only purpose here is to receive callback messages from NetworkManager.
 */

// TODO: Also the layout teardown process should probably be moved somewhere else, but I really don't want to deal with that right now.

public class NetworkListener implements DialogInterface.OnClickListener {

    private Activity activity;
    private ViewGroup rootContainer;

    public NetworkListener(Activity activity, ViewGroup rootContainer) {
        this.activity = activity;
        this.rootContainer = rootContainer;
    }

    /**
     * On click, triggers the layout replacement on the cached context.
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {

        // Inflate the new layout file into a view
        LayoutInflater layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Strip all existing elements from the passed container
        rootContainer.removeAllViews();

        // Then add the new layout to the passed container.
        View viewGroup = layoutInflater.inflate(R.layout.no_network_connection_layout, rootContainer, true);
    }
}
