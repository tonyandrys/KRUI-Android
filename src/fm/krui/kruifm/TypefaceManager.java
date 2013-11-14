/*
 * fm.krui.kruifm.TypefaceManager - TypefaceManager.java
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
