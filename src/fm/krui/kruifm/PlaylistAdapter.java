

package fm.krui.kruifm;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.*;

/**
 * fm.krui.kruifm - PlaylistAdapter
 * Created by Tony Andrys on 07/11/2013.
 */

public class PlaylistAdapter extends BaseAdapter {

    private final String TAG = PlaylistAdapter.class.getName();

    // Listrow ViewType Constants
    private final int VIEW_COUNT = 2; // Date row + Track row = 2 total rows
    private final int ROW_TRACK = 0;
    private final int ROW_DATE = 1;

    private ArrayList<HashMap<String, Track>> trackList;
    private Activity activity;
    private LayoutInflater inflater;

    public PlaylistAdapter(ArrayList<HashMap<String, Track>> trackList, Activity activity) {
        this.trackList = trackList;
        this.activity = activity;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return trackList.size();
    }

    @Override
    public Object getItem(int position) {
        return trackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_COUNT;
    }

    @Override
    public int getItemViewType(int position) {

        // Check if the map at this position has a spacer.
        HashMap<String, Track> trackMap = trackList.get(position);
        boolean isSpacer = trackMap.containsKey("spacer");

        // If the date of this row does not match the stored date, display a date banner.
        if (isSpacer) {
            return ROW_DATE;
        }
        // Otherwise, display track information.
        else {
            return ROW_TRACK;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // first get the track and DJ object from data model.
        HashMap<String, Track> trackMap = trackList.get(position);
        Track track = trackMap.get("track"); // Retrieve Track object for map at this position.

        // If the track at this position was played on a different day than the last track, setup a view for the date row
        if (getItemViewType(position) == ROW_DATE) {
            DateViewHolder dateHolder = new DateViewHolder();
            View view;

            // If no convertView exists, we're going to have to create a new one.
            if ((convertView == null) || (convertView.getTag() != dateHolder)) {
                convertView = inflater.inflate(R.layout.playlist_date_listrow, null);

                // Use ViewHolder pattern to reduce expensive findViewById lookups.
                dateHolder = new DateViewHolder();
                dateHolder.datePlaylistTextView = (TextView)convertView.findViewById(R.id.date_playlist_textview);
                convertView.setTag(dateHolder);
                view = convertView;
            }

            // If we DO have a convertView, just use its content to save resources.
            else {
                // Get the holder so we can set the data
                dateHolder = (DateViewHolder)convertView.getTag();
                view = convertView;
            }

            // Finally, set the contents of the date row.
            String[] curDate = track.getDate().split("&");
            dateHolder.datePlaylistTextView.setText(curDate[1] + " - " + curDate[0]);
            return view;
        }

        // MUST be ROW_TRACK!
        // If the track at this position is on the same day as the last, we need to display the whole track information.
        else if (getItemViewType(position) == ROW_TRACK){

            DJ dj = track.getPlayedBy(); // Retrieve DJ object from track object.

            TrackViewHolder trackHolder = new TrackViewHolder();
            View view;

            // If there is no convertView to use, we have to create one.
            if ((convertView == null) || (convertView.getTag() != trackHolder)) {
                convertView = inflater.inflate(R.layout.playlist_listrow, null);
                trackHolder = new TrackViewHolder();
                trackHolder.songTextView = (TextView)convertView.findViewById(R.id.track_name_textview);
                trackHolder.artistTextView = (TextView)convertView.findViewById(R.id.artist_name_textview);
                trackHolder.djNameTextView = (TextView)convertView.findViewById(R.id.played_by_textview);
                trackHolder.timePlayedTextView = (TextView)convertView.findViewById(R.id.time_played_textview);
                convertView.setTag(trackHolder);
                view = convertView;
            }

            // If there IS a convertView, just use its content to save resources.
            else {
                // Get the holder so we can set the data
                trackHolder = (TrackViewHolder)convertView.getTag();
                view = convertView;
            }

            // Set the contents of the playlist row.
            trackHolder.songTextView.setText(track.getTitle());
            trackHolder.artistTextView.setText(track.getArtist());
            trackHolder.djNameTextView.setText(activity.getResources().getString(R.string.played_by) + " " + dj.getFirstName() + " " + dj.getLastName());
            trackHolder.timePlayedTextView.setText(track.getTime());
            return view;
        }
        return null;
    }

    /*
     * Holds location of views to avoid expensive findViewById operations.
     */
    static class DateViewHolder {
        TextView datePlaylistTextView;
    }

    static class TrackViewHolder {
        TextView artistTextView;
        TextView songTextView;
        TextView djNameTextView;
        TextView timePlayedTextView;
        ImageView albumArtImageView;
    }

}
