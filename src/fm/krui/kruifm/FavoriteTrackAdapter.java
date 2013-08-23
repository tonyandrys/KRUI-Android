package fm.krui.kruifm;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * fm.krui.kruifm - FavoriteTrackAdapter
 *
 * @author Tony Andrys
 *         Created: 08/22/2013
 *         (C) 2013 - Tony Andrys
 */
public class FavoriteTrackAdapter extends BaseAdapter {

    private final String TAG = PlaylistAdapter.class.getName();

    final private String KEY_ARTIST = "artist";
    final private String KEY_TRACK = "name";
    final private String KEY_ALBUM = "album";

    private ArrayList<HashMap<String, String>> trackList;
    private Activity activity;
    private LayoutInflater inflater;
    private FavoriteTrackManager favTrackManager;

    public FavoriteTrackAdapter(ArrayList<HashMap<String, String>> trackList, FavoriteTrackManager favTrackManager, Activity activity) {
        this.trackList = trackList;
        this.activity = activity;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.favTrackManager = favTrackManager;
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
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the track at this position
        HashMap<String, String> trackMap = trackList.get(position);

            TrackViewHolder trackHolder = new TrackViewHolder();
            View view;

            // If there is no convertView to use, we have to create one.
            if ((convertView == null) || (convertView.getTag() != trackHolder)) {
                convertView = inflater.inflate(R.layout.favorite_track_listrow, null);
                trackHolder = new TrackViewHolder();
                trackHolder.songTextView = (TextView)convertView.findViewById(R.id.fav_track_name_textview);
                trackHolder.artistTextView = (TextView)convertView.findViewById(R.id.fav_artist_name_textview);
                trackHolder.albumTextView = (TextView)convertView.findViewById(R.id.fav_album_name_textview);
                trackHolder.deleteTrackImageView = (ImageView)convertView.findViewById(R.id.delete_fav_track_imageview);
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
            trackHolder.songTextView.setText(trackMap.get(KEY_TRACK));
            trackHolder.artistTextView.setText(trackMap.get(KEY_ARTIST));
            trackHolder.albumTextView.setText(trackMap.get(KEY_ALBUM));

            // Build delete button listener
            trackHolder.deleteTrackImageView.setTag(position);
            trackHolder.deleteTrackImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove the track from the LOCAL copy of data to remove it from the displayed list
                    trackList.remove(position);
                    notifyDataSetChanged();

                    // Then remove it from the copy on the internal storage
                    favTrackManager.removeThisTrackFromFavorites(position);
                }
            });

        return view;
    }

    /*
     * Holds location of views to avoid expensive findViewById operations.
     */

    static class TrackViewHolder {
        TextView artistTextView;
        TextView songTextView;
        TextView albumTextView;
        ImageView deleteTrackImageView;
    }

}


