package fm.krui.kruifm;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DJInfoFragment extends Fragment implements DJInfoListener, ImageListener {

    private static String TAG = DJInfoFragment.class.getName();
    protected View rootView;
    protected String DEFAULT_DJ_IMAGE = "http://staff.krui.fm/assets/images/default.png";
    protected ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dj_info_layout, container, false);
        rootView = view;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare for network operations
        ViewGroup rootContainer = (RelativeLayout)getActivity().findViewById(R.id.djinfo_fragment_container_relativelayout);
        NetworkListener networkListener = new NetworkListener(getActivity(), rootContainer);
        NetworkManager networkManager = new NetworkManager(getActivity(), networkListener);

        // Check for network access
        boolean isConnected = networkManager.checkForNetworkConnection();

        if (!isConnected) {
            // Strip layout container and display alert dialog.
            rootContainer.removeAllViews();
            networkManager.showConnectionAlert();
        } else {
            // Get DJ information from staff.krui.fm API
            DJInfoFetcher fetcher = new DJInfoFetcher(getActivity(), this);
            fetcher.execute();
        }
    }

    @Override
    public void onFinish(DJ dj) {

        // Instantiate layout objects to be updated.
        TextView djNameTextView = (TextView) rootView.findViewById(R.id.dj_info_name_textview);
        TextView djUrlTextView = (TextView) rootView.findViewById(R.id.dj_info_url_textview);
        TextView djBioTextView = (TextView) rootView.findViewById(R.id.dj_bio_content_textview);
        TextView djTwitterTextView = (TextView) rootView.findViewById(R.id.dj_twitter_content);

        // Update objects
        djNameTextView.setText(dj.getFirstName() + " " + dj.getLastName());
        djUrlTextView.setText(dj.getUrl());
        djBioTextView.setText(dj.getBio());
        djTwitterTextView.setText(dj.getTwitter());

        // Update DJ Image if one was provided.
        if (!dj.getImageURL().equals(DEFAULT_DJ_IMAGE)) {
            ImageDownloader dl = new ImageDownloader(this);
            dl.execute(dj.getImageURL());
        }

    }

    @Override
    public void onImageDownloaded(Bitmap bitmap) {

        // If this method is called, a valid image has been returned. Update the imageView.
        ImageView djImageView = (ImageView)rootView.findViewById(R.id.dj_image_imageView);
        djImageView.setImageBitmap(bitmap);
    }
}
