/*
 * fm.krui.kruifm.Track - Track.java
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
 * fm.krui.kruifm - Track
 * Created by Tony Andrys on 07/11/{2013}.
 */


/**
 * Track objects encapsulate all information regarding played songs. They include song characteristics
 * AND a link back to the originating DJ.
 */
public class Track {

    // Song information
    private String title;
    private String artist;
    private String album;
    private String date;
    private String time;
    private boolean request;
    private String artUrl;

    // DJ information for this track
    private DJ playedBy;

    // JSON Keys
    public static final String KEY_SONG = "song";
    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_DATETIME = "time";
    public static final String KEY_REQUEST = "request";

    // Default Constructor, generates a blank Track object.
    public Track() {

    }

    // Partially parameterized constructor which automatically sets the request field to false.
    public Track(String title, String artist, String album, String date, String time, String artUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.date = date;
        this.time = time;
        this.request = false;
        this.artUrl = artUrl;
    }

    // Fully parameterized constructor used when all six fields are necessary.
    public Track(String title, String artist, String album,  String date, String time, boolean request, String artUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.date = date;
        this.time = time;
        this.request = request;
        this.artUrl = artUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
    }

    public DJ getPlayedBy() {
        return this.playedBy;
    }

    public void setPlayedBy(DJ playedBy) {
        this.playedBy = playedBy;
    }

    public String getArtUrl() {
        return this.artUrl;
    }

    public void setArtUrl(String url) {
        this.artUrl = url;
    }


}
