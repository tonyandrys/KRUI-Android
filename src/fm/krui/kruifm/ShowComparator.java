/*
 * fm.krui.kruifm.ShowComparator - ShowComparator.java
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

import java.util.Comparator;

/**
 * fm.krui.kruifm - ShowComparator
 *
 * @author Tony Andrys
 *         Created: 10/28/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * A Comparator which provides comparison of arbitrary Show objects, based on the day they air.
 * Used for processing Show objects in a Priority Queue and displaying them on the correct day of the week in ScheduleFragment.
 */
public class ShowComparator implements Comparator<Show> {

    public ShowComparator() {

    }

    @Override
    public int compare(Show lhs, Show rhs) {

        // Get day of the week values for both objects
        int x = lhs.getDayOfWeek();
        int y = rhs.getDayOfWeek();

        // If x < y, the difference of x and y is negative. Therefore, if x - y < 0, x is the larger value.
        if (x<y) {
            return -1;
        }

        // If x > y, the difference of x and y is positive. Therefore, if x - y > 0, y is the larger value.
        else if (x>y) {
            return 1;
        }

        // Finally, if the difference of x and y is zero, given the fact that x and y are both integers, we know that
        // they contain the same value.
        else {
            return 0;
        }
    }
}
