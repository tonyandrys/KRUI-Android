package fm.krui.kruifm;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class StreamFragment extends Fragment implements TrackUpdateListener {

    private static String TAG = StreamFragment.class.getName();

    /* Constants */
    final private int MAIN_STUDIO = 0;
    final private int THE_LAB = 1;
    final private int MAX_VOLUME = 100;
    final private int TRACK_UPDATE_INTERVAL = 30000; // Time to wait before checking for track updates in milliseconds.

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
    private MediaPlayer mp;
    private boolean isPaused = false;
    private boolean isPrepared = false;
    ProgressDialog pd;

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
        final SeekBar volumeSeekBar = (SeekBar)getActivity().findViewById(R.id.volume_seekbar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // When seek bar progress is changed, change the audio of the media player appropriately.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

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

        // Build audio player using default settings.
        mp = buildAudioPlayer();
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

        // If audio is still playing, resume the update timer when this fragment comes back into focus.
        if (mp.isPlaying()) {
            setUpdateTimer(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // If audio is not playing, reset MediaPlayer object to prevent memory leaks.
        if (mp.isPlaying() == false) {
            mp.reset();
            isPaused = false;
            Log.v(TAG, "User leaving activity and player is stopped. Releasing MediaPlayer.");
        }

        // Stop updating track information
        setUpdateTimer(false);

        // Store favoriteMap into memory
        Log.v(TAG, "Attempting to save favorite tracks...");
        favTrackManager.storeFavoriteTracks();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop updating track information
        setUpdateTimer(false);
    }

    /**
     * Builds and returns a configured, unprepared MediaPlayer and attach an error handler.
     */
    public MediaPlayer buildAudioPlayer() {

        // Build MediaPlayer
        mp = new MediaPlayer();

        try {
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(streamUrl);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Caught IllegalArgumentException: ");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Caught IllegalStateException: ");
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.e(TAG, "Caught SecurityException: ");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Caught IOException: ");
            e.printStackTrace();
        }

        // Attach error handler to instance.
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {

                // If there is an error in playback, stop and inform the user.
                pd.dismiss();
                mp = buildAudioPlayer();
                Toast.makeText(getActivity(), "Failed to load the stream. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error in playback. onError was called.");
                return true;
            }

        });

        return mp;
    }

    protected void changeUrl(int spinnerPosition) {
        // URL depends on spinner position!
        // This turned out to be kind of cool, but this could cause problems if this order ever changes!
        String streamUrl;
        streamUrl = getStreamUrl(spinnerPosition);
        Log.v(TAG, "Stream URL changed to " + streamUrl);
        reconfigureStream(streamUrl);
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
        isPrepared = false;
        return URL;
    }

    /**
     * Changes stream source to passed URL.
     * @param newStreamUrl URL of the new target for player.
     */
    protected void reconfigureStream(String newStreamUrl) {
        streamUrl = newStreamUrl;

        // Stop stream if it is currently playing to prevent state exceptions
        if (mp.isPlaying()) {
            Log.v(TAG, "Stream source changed by user. Rebuilding stream.");
            mp.stop();
            Log.i(TAG, "Stream playback stopped.");
        }

        // Rebuild player with new stream URL.
        mp.reset();
        mp = buildAudioPlayer();
    }

    /**
     * Stops audio, drops connection to stream, and returns Media Player to an unprepared state. Called by a button onClick event.
     * @param v Button pressed by user.
     */
    public void stopAudio(View v) {
        mp.stop();
        Log.i(TAG, "Stream playback stopped.");
    }

    public void prepareAudio(ImageView playButton) {
        try {
            Log.v(TAG, "Attempting to play stream from " + streamUrl);
            playButton.setImageResource(R.drawable.pause_icon_white);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    // When the stream is buffered, kill prompt and start playing automatically.
                    pd.dismiss();
                    mp.start();
                    isPrepared = true;
                    Log.i(TAG, "Stream playback started.");

                    // Start the update timer.
                    setUpdateTimer(true);
                }
            });

            // Prepares stream without blocking UI Thread
            mp.prepareAsync();

        } catch (IllegalStateException e) {
            Log.e(TAG, "Caught IllegalStateException when preparing: ");
            playButton.setImageResource(R.drawable.play_icon_white);
            e.printStackTrace();
        }

        // Stop user input while buffering by displaying ProgressDialog
        pd = new ProgressDialog(getActivity());
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        pd = ProgressDialog.show(getActivity(), "Loading...", "Buffering Stream", true, true, new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                pd.dismiss();

            }

        });
    }

    public void pauseAudio(ImageView playButton) {
        // Pause the audio
        mp.pause();
        isPaused = true;

        // Stop the update timer
        setUpdateTimer(false);

        // Change image of playButton to play icon.
        playButton.setImageResource(R.drawable.play_icon_white);
        Log.i(TAG, "Stream paused.");
    }

    public void resumeAudio(ImageView playButton) {
        // Resume the audio
        mp.start();
        isPaused = false;

        // Start update timer
        setUpdateTimer(true);

        // Change image of playButton to pause icon.
        playButton.setImageResource(R.drawable.pause_icon_white);
        Log.i(TAG, "Stream resumed.");
    }

    /**
     * Handles changes in audio behavior when play button is pressed by user. Depending on the context, this function
     * will either resume paused audio, pause the audio, or buffer and play the stream.
     * @param playButton
     */
    public void handleAudio(ImageView playButton) {

        // If media player is playing, pause the audio.
        if (mp.isPlaying()) {
            pauseAudio(playButton);
        }

        // If no sound is playing, check if the player is prepared.
        else {

            // If the media player is prepared, quickly resume the audio.
            if (isPrepared) {
                resumeAudio(playButton);
            }
            // If the media player is NOT prepared, it should be prepared before playing.
            else {
                prepareAudio(playButton);
            }
        }
    }

    /**
     * Sets volume of the media player based on the value of the volume seek bar. Volume settings are logarithmic,
     * so conversion is required before setting volume.
     * @param soundVolume Current setting of seekBar. Valid parameters are in [0,100]
     */
    public void setVolume(int soundVolume) {
        if (isPrepared) {
            final float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
            mp.setVolume(volume, volume);
        }
    }

    /**
     * Refreshes the title, album name, art, and other info and updates the UI.
     */
    public void updateTrackInfo() {
        RelativeLayout container = (RelativeLayout)getActivity().findViewById(R.id.stream_album_art_container);
        TextView songNameTextView = (TextView)getActivity().findViewById(R.id.song_name_textview);
        TextView artistTextView = (TextView)getActivity().findViewById(R.id.artist_name_textview);
        TextView albumNameTextView = (TextView)getActivity().findViewById(R.id.album_name_textview);

        // Store current track info as an array
        String[] currentTrackInfo;
        try {
            currentTrackInfo = new String[] {songNameTextView.getText().toString(), artistTextView.getText().toString(), albumNameTextView.getText().toString()};
        } catch (NullPointerException e) {
            currentTrackInfo = new String[] {"", "", ""};
        }
        new TrackUpdateHandler(this, getActivity(), container, currentTrackInfo, prefManager.getAlbumArtDownloadPreference()).execute();
    }

    /**
     * Pushes KRUI studio phone number to user's phone. Only dials, does not make calls (so the user can back out if needed).
     */
    protected void dialStudio() {
        String studioNumber = "319-335-8970";
        String uri = "tel:" + studioNumber.trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    /**
     * Retrieves the user's streaming preferences and changes the player's settings accordingly.
     * @param prefManager PreferenceManager object to get/set changes.
     */
    private void applyStreamPreferences(PreferenceManager prefManager) {

        // Retrieve stream preferences from PreferenceManager
        int streamQuality = prefManager.getStreamQuality();
        boolean isArtDownloaded = prefManager.getAlbumArtDownloadPreference();



    }

    /**
     * Enables and disables the track update timer.
     * @param startTimer true to start the timer, false to stop.
     */
    private void setUpdateTimer(boolean startTimer) {

        // Build timer
        updateTimer = new Timer();

        // If the timer is to be enabled, assign the TimerTask and schedule updates.
        if (startTimer) {
            updateTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.v(TAG, "Timer task has been called! Updating track info...");
                    updateTrackInfo();
                }
            };
            updateTimer.scheduleAtFixedRate(updateTimerTask, 0, TRACK_UPDATE_INTERVAL);
            Log.v(TAG, "Timer started!");
            Log.v(TAG, "Update schedule is set at " + TRACK_UPDATE_INTERVAL + " milliseconds.");
        }

        // If the timer is to be disabled, cancel and purge the timer object.
        else {
            if (updateTimerTask != null && updateTimer != null) {
                updateTimerTask.cancel();
                updateTimer.cancel();
                Log.v(TAG, "Timer stopped and task has been purged.");
            }
        }
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

    private void toggleAudioNotification(boolean isPlaying) {
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity());
        notificationBuilder.setSmallIcon(R.drawable.play_icon_white);
        notificationBuilder.setContentTitle(getString(R.string.notification_title));
        notificationBuilder.setContentText(getString(R.string.notification_subtitle));

        // Create intent for activity in app
        Intent resultIntent = new Intent(getActivity(), StreamContainer.class);

        // Stack builder object contains an artificial back stack for the activity,
        // ensuring the user will hit the home screen if they press back.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());

        // Add back stack for intent
        stackBuilder.addParentStack(StreamContainer.class);

        // Add intent that starts activity to the stop of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        // Use NotificationManger to trigger the intent
        NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        //notificationManager.notify(NOTIFICATION_ID, notificationBuilder.notify());



    }
}