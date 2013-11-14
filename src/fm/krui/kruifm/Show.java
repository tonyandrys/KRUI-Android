/*
 * fm.krui.kruifm.Show - Show.java
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

public class Show {
	
	private String id;
	private int station;
	private String title;
    private String startTimeUTC;
	private String endTimeUTC;
    private int startTimeMinutes;
    private int endTimeMinutes;
	private String htmlLink;
	private String description;
	private int dayOfWeek;

    // 1 - RR, 2 - MS, 3 - Sports, 4 - News/Talk, 5 - Special
    private int category;

	// default constructor
	Show() {
		
	}

    public Show(String id, int station, String title, String startTimeUTC, String endTimeUTC, int startTimeMinutes, int endTimeMinutes, String htmlLink, String description, int dayOfWeek) {
        this.id = id;
        this.station = station;
        this.title = title;
        this.startTimeUTC = startTimeUTC;
        this.endTimeUTC = endTimeUTC;
        this.startTimeMinutes = startTimeMinutes;
        this.endTimeMinutes = endTimeMinutes;
        this.htmlLink = htmlLink;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTimeUTC() {
        return startTimeUTC;
    }

    public void setStartTimeUTC(String startTimeUTC) {
        this.startTimeUTC = startTimeUTC;
    }

    public String getEndTimeUTC() {
        return endTimeUTC;
    }

    public void setEndTimeUTC(String endTimeUTC) {
        this.endTimeUTC = endTimeUTC;
    }

    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    public void setStartTimeMinutes(int startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }

    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public void setEndTimeMinutes(int endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    public String getHtmlLink() {
        return htmlLink;
    }

    public void setHtmlLink(String htmlLink) {
        this.htmlLink = htmlLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
