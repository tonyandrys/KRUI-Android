/*
 * fm.krui.kruifm.NetworkManager - NetworkManager.java
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
 * fm.krui.kruifm - NetworkManager
 *
 * @author Tony Andrys
 *         Created: 07/26/2013
 *         (C) 2013 -
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class to encapsulate all network operations.
 */
public class NetworkManager {

    final private String TAG = NetworkManager.class.getName();

    private Activity activity;
    private DialogInterface.OnClickListener listener;

    public NetworkManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Class requires the calling activity's context and a listener to send messages to if necessary.
     * @param activity Calling activity
     * @param listener Callback class to receive messages.
     */
    public NetworkManager(Activity activity, DialogInterface.OnClickListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    /**
     * Checks for a valid network connection on any active interface.
     * @return true if a valid connection is found, false if no usable connection found.
     * NOTE: Thanks to a user on StackOverflow for this one-- when I find your name again I'll give you credit.
     */
     public boolean checkForNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * Displays a connection alert screen when called, notifying the user that no network connection is available.
     * The user is given an option to retry or abort the connection.
     * @return true if connection retry is requested, false if abort is requested.
     */
    public void showConnectionAlert() {

            // Build AlertDialog object
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setTitle(activity.getResources().getString(R.string.no_network_connection_title));
            alertDialogBuilder.setMessage(activity.getResources().getString(R.string.no_network_connection_message));
            alertDialogBuilder.setNeutralButton(activity.getResources().getString(R.string.ok), listener);

            // Display the dialog on the calling activity's context. Calls are sent to the listener passed on class instantiation.
            alertDialogBuilder.show();
    }

}
