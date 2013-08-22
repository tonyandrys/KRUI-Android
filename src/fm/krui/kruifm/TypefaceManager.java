package fm.krui.kruifm;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * fm.krui.kruifm - TypefaceManager
 *
 * @author Tony Andrys
 *         Created: 07/26/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Class which caches font objects to keep memory usage low.
 */
public class TypefaceManager {

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String name) {
        synchronized(cache) {
            if(!cache.containsKey(name)){
                Typeface t = Typeface.createFromAsset(
                        c.getAssets(),
                        String.format("fonts/%s.ttf", name)
                );
                cache.put(name, t);
            }
            return cache.get(name);
        }
    }
}
