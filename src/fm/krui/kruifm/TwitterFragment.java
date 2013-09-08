package fm.krui.kruifm;

/**
 * fm.krui.kruifm - TwitterFragment
 *
 * @author Tony Andrys
 *         Created: 08/30/2013
 *         (C) 2013 - Tony Andrys
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Displays tweets of a user as a listview.
 */
public class TwitterFragment extends ListFragment implements HTTPConnectionListener, ImageListener {

    private final String TAG = TwitterFragment.class.getName();

    // Twitter parameters
    final int TWEET_COUNT = 20;

    // List of tweets to display
    private ArrayList<Tweet> tweetList;
    private TweetAdapter adapter;
    private Bitmap profileBitmap;

    private DJ dj;
    private Context context;
    private TwitterManager twitterManager;
    private View rootView;

    public TwitterFragment(DJ dj) {
        this.dj = dj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.twitter_layout, container, false);
        rootView = view;
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate twitter manager
        showLoadingScreen(true);
        twitterManager = new TwitterManager(getActivity(), this);
        twitterManager.authorize();
    }

    @Override
    public void onListItemClick(ListView lv, View view, int position, long id) {

        // Get the tweet for this position
        //Tweet tweet = (Tweet)lv.getAdapter().getItem(position);

        // Pass it to the viewer activity as an intent
        //Intent intent = new Intent(getActivity(), ArticleViewActivity.class);
        //intent.putExtra(article.ARTICLE_INTENT, article);
        //startActivity(intent);

    }

    /**
     * Authenticates with Twitter and retrieves timeline of current DJ.
     */
    private void getTwitterTimeline() {

        // Authenticate with twitter
        showLoadingScreen(true);

        // Get timeline of user
        if (dj.getTwitter().length() >= 1) {
            Log.v(TAG, "Requesting timeline... (user: " + twitterManager + ")");
            twitterManager.getTimeline(dj.getTwitter(), TWEET_COUNT, false);
        } else {
            Log.e(TAG, "No twitter username found!");
        }
    }

    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingScreen = (FrameLayout)rootView.findViewById(R.id.twitter_loading_framelayout);
        if (isLoading) {
            loadingScreen.setVisibility(View.VISIBLE);
        } else {
            loadingScreen.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onConnectionFinish(String result) {

        // If return code indicates authorization was successful, retrieve the timeline of the user.
        if (result.equals(TwitterManager.AUTHORIZATION_SUCCESS)) {
            Log.v(TAG, "Now authorized with twitter!");
            getTwitterTimeline();

        } else if (result.equals(TwitterManager.AUTHORIZATION_FAILED)) {
            Log.e(TAG, "Bearer token seems invalid. Cannot continue.");
            return;

        } else {
            Log.v(TAG, "Timeline request reached!");
            tweetList = new ArrayList<Tweet>();
            tweetList = twitterManager.processJSONTimeline(result);

            // Get the profile picture to apply to all tweets
            String imageUrl = tweetList.get(0).getProfileImageUrl();
            ImageDownloader downloader = new ImageDownloader(this);
            downloader.execute(imageUrl);
        }

    }

    @Override
    public void onImageDownloaded(Bitmap bitmap) {

        Log.v(TAG, "Twitter profile picture saved.");

        // Build adapter using downloaded bitmap
        adapter = new TweetAdapter(getActivity(), tweetList, bitmap);

        // Bind adapter to this list to display its data.
        setListAdapter(adapter);
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        showLoadingScreen(false);
    }
}
