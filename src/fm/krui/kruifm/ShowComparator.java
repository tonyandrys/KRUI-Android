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
