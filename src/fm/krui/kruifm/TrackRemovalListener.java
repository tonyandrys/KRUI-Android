package fm.krui.kruifm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * fm.krui.kruifm - TrackRemovalListener
 *
 * @author Tony Andrys
 *         Created: 08/22/2013
 *         (C) 2013 - Tony Andrys
 */
public interface TrackRemovalListener {
    void onTrackRemoved(ArrayList<HashMap<String,String>> newList);
}
