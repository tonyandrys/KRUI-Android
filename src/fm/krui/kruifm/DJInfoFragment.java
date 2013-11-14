/*
 * fm.krui.kruifm.DJInfoFragment - DJInfoFragment.java
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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DJInfoFragment extends Fragment implements ImageListener {

    private static String TAG = DJInfoFragment.class.getName();
    protected View rootView;
    protected String DEFAULT_DJ_IMAGE = "http://staff.krui.fm/assets/images/default.png";
    protected DJ dj;

    public DJInfoFragment(DJ dj) {
        this.dj = dj;
    }

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

// Check for network access
        /*boolean isConnected = networkManager.checkForNetworkConnection();

        if (!isConnected) {
            // Strip layout container and display alert dialog.
            rootContainer.removeAllViews();
            networkManager.showConnectionAlert();
        } else {
            // Get DJ information from staff.krui.fm API
            DJInfoFetcher fetcher = new DJInfoFetcher(getActivity(), this);
            fetcher.execute();
        }*/