/*
 * fm.krui.kruifm.TweetAdapter - TweetAdapter.java
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
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * fm.krui.kruifm - TweetAdapter
 *
 * @author Tony Andrys
 *         Created: 08/30/2013
 *         (C) 2013 - Tony Andrys
 */
public class TweetAdapter extends BaseAdapter {

    private final String TAG = TweetAdapter.class.getName();

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Tweet> tweetList;
    private Bitmap profileImage;

    public TweetAdapter(Context context, ArrayList<Tweet> tweetList, Bitmap profileImage) {
        this.context = context;
        this.tweetList = tweetList;
        this.profileImage = profileImage;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return tweetList.size();
    }

    @Override
    public Object getItem(int position) {
        return tweetList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the tweet at this position
        Tweet tweet = tweetList.get(position);

        TweetViewHolder tweetHolder = new TweetViewHolder();
        View view;

        // If there is no convertView to use, we have to create one.
        if ((convertView == null) || (convertView.getTag() != tweetHolder)) {
            convertView = inflater.inflate(R.layout.tweet_listrow, null);
            tweetHolder = new TweetViewHolder();
            tweetHolder.tweetTextTextView = (TextView)convertView.findViewById(R.id.tweet_text_textview);
            tweetHolder.twitterProfileImageView = (ImageView)convertView.findViewById(R.id.twitter_profile_image_imageview);
            tweetHolder.tweetTimeTextView = (TextView)convertView.findViewById(R.id.tweet_time_textview);
            tweetHolder.tweetDateTextView = (TextView)convertView.findViewById(R.id.tweet_date_textview);

            convertView.setTag(tweetHolder);
            view = convertView;
        }

        // If there IS a convertView, just use its content to save resources.
        else {
            // Get the holder so we can set the data
            tweetHolder = (TweetViewHolder)convertView.getTag();
            view = convertView;
        }

        // Set the contents of the tweet views.
        tweetHolder.tweetTextTextView.setText(tweet.getText());
        tweetHolder.tweetDateTextView.setText(tweet.getDateOfTweet());
        tweetHolder.tweetTimeTextView.setText(tweet.getTimeOfTweet());
        tweetHolder.twitterProfileImageView.setImageBitmap(profileImage);

        return view;
    }

    /*
     * Holds location of views to avoid expensive findViewById operations.
     */

    static class TweetViewHolder {
        ImageView twitterProfileImageView;
        TextView tweetTextTextView;
        TextView tweetTimeTextView;
        TextView tweetDateTextView;

    }

}