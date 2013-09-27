package fm.krui.kruifm;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.util.Timer;
import java.util.TimerTask;


public class StreamFragment extends Fragment implements TrackUpdateListener {

    private static String TAG = StreamFragment.class.getName();

    /* Constants */
    final private int MAIN_STUDIO = 0;
    final private int THE_LAB = 1;

    // HashMap Constants
    final private String KEY_ARTIST = "artist";
    final private String KEY_TRACK = "name";
    final private String KEY_ALBUM = "album";

    /* Class members */
    private String streamUrl = "http://krui.student-services.uiowa.edu:8200"; // Default value is 128kb/s 89.7 stream.
    private Timer updateTimer = new Timer();
    private TimerTask updateTimerTask;
    private PreferenceManager prefManager;
    private FavoriteTrackManager favTrackManager;
    private int stationSpinnerPosition;
    private boolean isPlaying = false;
    ProgressDialog pd;

    // FIXME: This member isn't efficient
    SharedPreferences trackPrefs;


    // Service broadcast receiver
    private BroadcastReceiver broadcastReceiver;

    // Temporary
    private boolean trackIsFavorite = false;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.stream_activity_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call_studio_button:
                dialStudio();
                break;

        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Instantiate class members
        prefManager = new PreferenceManager(getActivity());
        favTrackManager = new FavoriteTrackManager(getActivity());

        // FIXME: Move these to PreferenceManager after expanding its scope for cleaner code
        trackPrefs = getActivity().getSharedPreferences(StreamService.PREFS_NAME, 0);

        // Instantiate broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processBroadcastCommand(intent);
            }
        };

        // Build station selection spinner
        ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.station_string_array, android.R.layout.simple_spinner_item);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Build station selection listener
        ActionBar.OnNavigationListener stationListener = new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                stationSpinnerPosition = itemPosition;
                changeUrl(itemPosition);
                return false;
            }
        };

        // Prepare ActionBar
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setListNavigationCallbacks(stationAdapter, stationListener);

        // Build play button listener
        final ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleAudio(playButton);
            }
        });

        // Build volume seek bar listener
        // ** DISABLED FOR NOW -- This might be completely thrown out. **
        /*final SeekBar volumeSeekBar = (SeekBar)getActivity().findViewById(R.id.volume_seekbar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // When seek bar progress is changed, change the audio of the media player appropriately.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Send new volume via intent? Will this be slow?
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }); */

        // Build settings button listener and apply it to settings icon and submit button
        View.OnClickListener flipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Card flip animation which toggles between stream controls and settings views
                flipCard();
            }
        };

        final ImageView settingsButton = (ImageView)getActivity().findViewById(R.id.stream_settings_imageview);
        final Button saveSettingsButton = (Button)getActivity().findViewById(R.id.set_stream_settings_button);
        settingsButton.setOnClickListener(flipListener);
        saveSettingsButton.setOnClickListener(flipListener);

        // Build favorite button listener
        final ImageView favoriteButton = (ImageView)getActivity().findViewById(R.id.stream_favorite_imageview);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackIsFavorite) {
                    favoriteButton.setImageResource(R.drawable.star_unfilled_white);
                    trackIsFavorite = false;
                    favTrackManager.removeTrackFromFavorites();
                } else {
                    favoriteButton.setImageResource(R.drawable.star_filled_white);
                    trackIsFavorite = true;
                    addTrackToFavorites();
                }
            }
        });

        // Build test buttons ** REMOVE ME **
        final Button showButton = (Button)getActivity().findViewById(R.id.show_status_button_TEST);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStreamStatusBar(true, "Hello");
            }
        });
        final Button hideButton = (Button)getActivity().findViewById(R.id.hide_status_button_TEST);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStreamStatusBar(false, null);
            }
        });


        // Build settings switches
        final Switch streamQualitySwitch = (Switch)getActivity().findViewById(R.id.stream_quality_switch);
        final Switch albumArtSwitch = (Switch)getActivity().findViewById(R.id.stream_album_art_switch);

        // Set initial state of switches
        albumArtSwitch.setChecked(prefManager.getAlbumArtDownloadPreference());
        if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
            streamQualitySwitch.setChecked(true);
        } else {
            streamQualitySwitch.setChecked(false);
        }

        // Assign listeners to switches
        streamQualitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefManager.setStreamQuality(prefManager.HIGH_QUALITY);
                    Log.v(TAG, "Stream quality setting is now: " + prefManager.getStreamQuality());
                } else {
                    prefManager.setStreamQuality(prefManager.LOW_QUALITY);
                    Log.v(TAG, "Stream quality setting is now: " + prefManager.getStreamQuality());
                }
                changeUrl(stationSpinnerPosition);
            }
        });

        albumArtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefManager.setAlbumArtDownloadPreference(true);
                    Log.v(TAG, "Album Art Download setting is now " + prefManager.getAlbumArtDownloadPreference());
                } else {
                    prefManager.setAlbumArtDownloadPreference(false);
                    Log.v(TAG, "Album Art Download setting is now " + prefManager.getAlbumArtDownloadPreference());
                }
            }
        });

    }

    /**
     * Performs animation between control view and settings view.
     */
    private void flipCard() {
        View rootLayout = (View)getActivity().findViewById(R.id.stream_functions_container_framelayout);
        View cardFace = (View)getActivity().findViewById(R.id.player_controls_tablelayout);
        View cardBack = (View)getActivity().findViewById(R.id.player_settings_tablelayout);

        FlipAnimation flipAnimation = new FlipAnimation(cardFace, cardBack);

        if (cardFace.getVisibility() == View.GONE)
        {
            flipAnimation.reverse();
        }
        rootLayout.startAnimation(flipAnimation);
    }

    private void addTrackToFavorites() {
        String trackName = "";
        String albumName = "";
        String artistName = "";
        TextView trackNameTextView = (TextView)getActivity().findViewById(R.id.song_name_textview);
        TextView albumNameTextView = (TextView)getActivity().findViewById(R.id.album_name_textview);
        TextView artistNameTextView = (TextView)getActivity().findViewById(R.id.artist_name_textview);

        // Extract text from their views
        try {
            trackName = trackNameTextView.getText().toString();
            albumName = albumNameTextView.getText().toString();
            artistName = artistNameTextView.getText().toString();
        } catch (NullPointerException e) {
            Log.e(TAG, "Caught NullPointerException when extracting values from StreamFragment textviews!");
            e.printStackTrace();
        }

        // Build a Track from currently playing track.
        Track track = new Track();
        track.setTitle(trackName);
        track.setAlbum(albumName);
        track.setArtist(artistName);

        // Add this track to favorites
        favTrackManager.addTrackToFavorites(track);
    }

    private void removeTrackFromFavorites() {
        favTrackManager.removeTrackFromFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve favorites list from memory
        favTrackManager.loadFavoriteTracks();

        // Register broadcast receiver to receive updates
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(StreamService.BROADCAST_MESSAGE));

    }

    @Override
    public void onPause() {
        super.onPause();

        // Store favoriteMap into memory
        Log.v(TAG, "Attempting to save favorite tracks...");
        favTrackManager.storeFavoriteTracks();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * Handles all broadcast commands received from audio service
     * @param intent Broadcasted intent
     */
    private void processBroadcastCommand(Intent intent) {

        // Get the specific command from intent
        String broadcastCommand = intent.getStringExtra(StreamService.BROADCAST_KEY);

        if (broadcastCommand != null) {

            // If play command is received, store state and update UI appropriately.
            if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_PLAY)) {
                isPlaying = true;

                // Change image of button to pause icon.
                ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                playButton.setImageResource(R.drawable.pause_icon_white);
            }

            // If pause command is received, store state and update UI appropriately.
            else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_PAUSE)) {
                isPlaying = false;

                // Change image of button to play icon.
                ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                playButton.setImageResource(R.drawable.play_icon_white);
            }

            // If stop command is received, store state and clear UI elements.
            // FIXME: Is having a separate stop command necessary when a pause command exists? Maybe or maybe not. Right now it just does the same thing...
            else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_STOP)) {
                isPlaying = false;

                // Change image of button to play icon.
                ImageView playButton = (ImageView)getActivity().findViewById(R.id.play_audio_imageview);
                playButton.setImageResource(R.drawable.play_icon_white);
            }

            // If track update command is received, retrieve playing track information and apply it to UI.
            else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_UPDATE)) {
                // Update UI with new information
                updateTrackInfo();
            }

            else if (broadcastCommand.equals(StreamService.BROADCAST_COMMAND_STATUS_MESSAGE)) {
                // Extract message and determine if this message should stay until cancelled.
                String streamStatus = intent.getStringExtra(StreamService.STREAM_STATUS_KEY);
                boolean indefiniteMessage = intent.getBooleanExtra(StreamService.STREAM_STATUS_DISPLAY_LENGTH, false);
                showStreamStatusBar(false, null);
            }
        } else {
            Log.e(TAG, "ERROR: Broadcast received but could not extract command.");
        }

    }

    protected void changeUrl(int spinnerPosition) {
        // URL depends on spinner position!
        // This turned out to be kind of cool, but this could cause problems if this order ever changes!
        streamUrl = getStreamUrl(spinnerPosition);

        Log.v(TAG, "Requesting audio service to change url to " + streamUrl);
        Intent intent = new Intent(getActivity(), StreamService.class);
        intent.putExtra(StreamService.INTENT_STREAM_URL, streamUrl);
        intent.setAction(StreamService.ACTION_CHANGE_URL);
        getActivity().startService(intent);
    }

    /**
     * Determines the URL of the correct quality (based on user prefs) when passed a station identifier.
     * @param station Integer tag representing KRUI (0) or The Lab (1)
     * @return URL of correct stream
     */
    private String getStreamUrl(int station) {
        String URL = "http://krui.student-services.uiowa.edu:";
        String mainLowPort = "8200";
        String mainHighPort = "8000";
        String labLowPort = "8105";
        String labHighPort = "8103";

        // Based on station passed,
        prefManager = new PreferenceManager(getActivity());
        switch (station) {
            case MAIN_STUDIO:
                if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
                    URL += mainHighPort;
                    Toast.makeText(getActivity(), "Main Studio - High Quality stream selected", Toast.LENGTH_SHORT).show();
                } else {
                    URL += mainLowPort;
                    Toast.makeText(getActivity(), "Main Studio, Low Quality stream selected", Toast.LENGTH_SHORT).show();
                }
                break;

            case THE_LAB:
                if (prefManager.getStreamQuality() == prefManager.HIGH_QUALITY) {
                    URL += labHighPort;
                    Toast.makeText(getActivity(), "The Lab, High Quality stream selected", Toast.LENGTH_SHORT).show();
                } else {
                    URL += labLowPort;
                    Toast.makeText(getActivity(), "The Lab, Low Quality stream selected", Toast.LENGTH_SHORT).show();
                }
        }
        return URL;
    }

    public void pauseAudio(ImageView playButton) {

        // Request service to pause audio
        Intent intent = new Intent(getActivity(), StreamService.class);
        intent.setAction(StreamService.ACTION_PAUSE);
        getActivity().startService(intent);
        isPlaying = false;

        // Change image of button to play icon.
        playButton.setImageResource(R.drawable.play_icon_white);
    }

    public void startAudio(ImageView playButton) {

        // Request service to start audio
        Intent intent = new Intent(getActivity(), StreamService.class);
        intent.putExtra(StreamService.INTENT_STREAM_URL, streamUrl);
        intent.setAction(StreamService.ACTION_PLAY);
        getActivity().startService(intent);
        isPlaying = true;

        // Change image of button to pause icon.
        playButton.setImageResource(R.drawable.pause_icon_white);
    }

    /**
     * Handles changes in audio behavior when play button is pressed by user. Depending on the context, this function
     * will either resume paused audio, pause the audio, or buffer and play the stream.
     * @param playButton
     */
    public void handleAudio(ImageView playButton) {

        // If media player is playing, pause the audio.
        if (isPlaying) {
            pauseAudio(playButton);
        } else {
            startAudio(playButton);
        }
    }

    /**
     * Refreshes the title, album name, art, and other info from SharedPreferences and updates the UI.
     */
    public void updateTrackInfo() {
        TextView songNameTextView = (TextView)getActivity().findViewById(R.id.song_name_textview);
        TextView artistTextView = (TextView)getActivity().findViewById(R.id.artist_name_textview);
        TextView albumNameTextView = (TextView)getActivity().findViewById(R.id.album_name_textview);
        ImageView albumArtImageView = (ImageView)getActivity().findViewById(R.id.album_art_pane);

        // Show loading indicator
        setLoadingIndicator(true);

        // Get current track information and write it to textviews
        String trackName = trackPrefs.getString(StreamService.PREFKEY_TRACK, "");
        String artistName = trackPrefs.getString(StreamService.PREFKEY_ARTIST, "");
        String albumName = trackPrefs.getString(StreamService.PREFKEY_ALBUM, "");
        songNameTextView.setText(trackName);
        artistTextView.setText(artistName);
        albumNameTextView.setText(albumName);

        // Get album art if it has been saved
        Bitmap bitmap = BitmapFactory.decodeFile(getActivity().getFilesDir() + "/" + TrackUpdateHandler.ALBUM_ART_FILENAME);
        if (bitmap != null) {
            // If art was retrieved, load it into the album art pane.
            albumArtImageView.setImageBitmap(bitmap);
        } else {
            // If nothing could be retrieved, load the placeholder album art image.
            albumArtImageView.setImageResource(R.drawable.noalbumart);
            Log.w(TAG, "Album art could not be decoded from SharedPreferences");
        }

        // Hide the loading indicator
        setLoadingIndicator(false);
    }

    /**
     * Enables and disables the loading indicator for UI views.
     * @param showLoadingIndicator True to enable, false to disable.
     */
    private void setLoadingIndicator(final boolean showLoadingIndicator) {
        // Instantiate views
        ProgressBar progressBar = (ProgressBar)getActivity().findViewById(R.id.album_art_progressbar);
        ImageView albumArtPaneImageView = (ImageView)getActivity().findViewById(R.id.album_art_pane);
        ImageView albumArtLoadingPaneImageView = (ImageView)getActivity().findViewById(R.id.album_art_loading_pane);
        if (showLoadingIndicator) {
            // Show loading indicators
            progressBar.setVisibility(View.VISIBLE);
            albumArtLoadingPaneImageView.setVisibility(View.VISIBLE);
            albumArtPaneImageView.setVisibility(View.INVISIBLE);

        } else {
            // Hide progressBar and placeholder image and re-show album art pane.
            progressBar.setVisibility(View.INVISIBLE);
            albumArtLoadingPaneImageView.setVisibility(View.INVISIBLE);
            albumArtPaneImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pushes KRUI studio phone number to user's phone. Only dials, does not make calls (so the user can back out if needed).
     * FIXME: Move this to its own intent class combined with all other intents this application will make
     */
    protected void dialStudio() {
        String studioNumber = "319-335-8970";
        String uri = "tel:" + studioNumber.trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }


    /**
     * Called whenever a new track is displayed on the screen.
     */
    @Override
    public void onTrackUpdate() {
        // Since a new track is now on the screen, reset the status of the favorite button so the new track is addable.
        final ImageView favoriteButton = (ImageView)getActivity().findViewById(R.id.stream_favorite_imageview);
        trackIsFavorite = false;
        favoriteButton.setImageResource(R.drawable.star_unfilled_white);
        Log.v(TAG, "New track has been updated. Favorite status has been reset.");
    }

    /**
     * Moves the stream status bar into view to show messages regarding stream status. Use indefinite time constraints
     * @param message String to display in the status bar.
     * @param isIndefinite true if the message should be displayed until explicitly cancelled by a broadcast message.
     */
    // FIXME: Rewrite to support indefinite messages and set a default display time constant if not indefinite
    public void showStreamStatusBar(String message, boolean isIndefinite) {
        LinearLayout statusContainer = (LinearLayout)getActivity().findViewById(R.id.stream_status_container_linearlayout);
        if (!showStatus) {
            // Hide the status bar and do nothing else.
            final Animation animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_down);
            statusContainer.startAnimation(animOut);
        } else {
            // Apply label text, then bring the status bar into view
            TextView statusLabel = (TextView)getActivity().findViewById(R.id.stream_status_label_textview);
            statusLabel.setText(message);
            final Animation animIn = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_up);
            statusContainer.startAnimation(animIn);
        }
    }

    /**
     * Manually hides the stream status bar from view.
     */
    public void hideStatusBar() {
        LinearLayout statusContainer = (LinearLayout)getActivity().findViewById(R.id.stream_status_container_linearlayout);
        final Animation animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_down);
        statusContainer.startAnimation(animOut);
    }

}