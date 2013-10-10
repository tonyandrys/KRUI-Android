package fm.krui.kruifm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * fm.krui.kruifm - StreamService
 *
 * @author Tony Andrys
 *         Created: 08/25/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Provides background streaming of KRUI audio streams as a foreground service.
 */
public class StreamService extends Service implements MediaPlayer.OnErrorListener, TrackUpdateListener {

    final private String TAG = StreamService.class.getName();

    final int NOTIFICATION_ID = 1492; // in 1492 Columbus sailed the ocean blue.
    final private int MAX_VOLUME = 100;
    final private int TRACK_UPDATE_INTERVAL = 30000; // Time to wait before checking for track updates in milliseconds.

    // Pref constants
    public static final String PREFS_NAME = "KRUI-CurrentTrack";
    public static final String PREFKEY_TRACK = "track";
    public static final String PREFKEY_ARTIST = "artist";
    public static final String PREFKEY_ALBUM = "album";

    // Intent broadcast constants
    public static final String BROADCAST_MESSAGE = "streamMessage";
    public static final String BROADCAST_KEY = "broadcastCommand";
    public static final String BROADCAST_COMMAND_PLAY = "playStream";
    public static final String BROADCAST_COMMAND_PAUSE = "pauseStream";
    public static final String BROADCAST_COMMAND_STOP = "stopStream";
    public static final String BROADCAST_COMMAND_UPDATE = "updateStream";
    public static final String BROADCAST_COMMAND_STATUS_MESSAGE = "statusMessage";
    public static final String BROADCAST_COMMAND_STATUS_HIDE = "hideStatus";
    public static final String STREAM_STATUS_KEY = "newStatus";
    public static final String STREAM_STATUS_DISPLAY_LENGTH = "isIndefinite";

    // Intent command constants
    public static final String INTENT_STREAM_URL = "streamUrl";
    public static final String ACTION_PLAY = "fm.krui.kruifm.PLAY";
    public static final String ACTION_PAUSE = "fm.krui.kruifm.PAUSE";
    public static final String ACTION_STOP = "fm.krui.kruifm.STOP";
    public static final String ACTION_CHANGE_URL = "fm.krui.kruifm.CHANGEURL";

    // Default stream to play is the 89.7 128kb/s stream
    private String streamUrl = "http://krui.student-services.uiowa.edu:8200";

    // Timer members
    private Timer updateTimer;
    private TimerTask updateTimerTask;

    SharedPreferences prefs;
    private MediaPlayer mp;
    private Notification.Builder notificationBuilder;

    // Stream bools
    private boolean isPrepared;
    private boolean isPaused;

    public StreamService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SharedPrefs file used to cache track info
        prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);

        // Set initial parameters and build audio player
        this.isPaused = false;
        this.isPrepared = false;
        buildAudioPlayer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handleIntent(intent);

        // The audio streaming service should persist until it is explicitly stopped, so return sticky status.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Broadcast closing intent to update UI

        // Stop and release media player resources
        Log.v(TAG, "Audio service has received signal to shutdown. Stopping audio and freeing resources...");
        mp.stop();
        mp.release();
        isPaused = false;
        isPrepared = false;
        Log.v(TAG, "Resources freed.");

    }

    private void handleIntent(Intent intent) {

        // Get intent action
        String intentAction = intent.getAction();

        // If play command is received, resume or prepare/play audio.
        if (intentAction.equals(ACTION_PLAY)) {
            if (isPaused && isPrepared) {
                resumeAudio();
            } else {
                streamUrl = intent.getStringExtra(INTENT_STREAM_URL);
                prepareAudio();
            }
        }

        // If pause command is received, pause the audio.
        else if (intentAction.equals(ACTION_PAUSE)) {
            pauseAudio();
        }

        else if (intentAction.equals(ACTION_STOP)) {
            stopSelf();
        }

        // If change url command is received, update stream url and flag player to
        // rebuild on next play.
        else if (intentAction.equals(ACTION_CHANGE_URL)) {
            String newUrl = intent.getStringExtra(INTENT_STREAM_URL);
            if (newUrl != null) {
                setStreamURL(newUrl);
                this.isPrepared = false;
                this.isPaused = false;
            } else {
                Log.e(TAG, "ERROR: Change URL requested, but no URL was passed in intent!");
            }
        }
    }

    /**
     * Pauses the MediaPlayer and stops update timer.
     */
    public void pauseAudio() {

        mp.pause();
        isPaused = true;

        // Rebuild notification with resume button
        String[] currentTrackInfo = getCurrentTrackInfo();
        updateNotification(currentTrackInfo[1], currentTrackInfo[0], false);

        // Send broadcast informing all interested components that streaming has paused
        broadcastMessage(BROADCAST_COMMAND_PAUSE);

        // Stop timer and clear track information to force a refresh on next play
        setUpdateTimer(false);
        setCurrentTrackInfo("", "", "");
        Log.i(TAG, "Stream paused.");

    }

    /**
     * Resumes a prepared audio feed and starts update timer.
     */
    public void resumeAudio() {

        // Start playing audio
        mp.start();
        isPaused = false;

        // Build base notification, retrieve current track info, and update the notification when it is available.
        startForegoundService();
        String[] currentTrackInfo = getCurrentTrackInfo();
        updateNotification(currentTrackInfo[1], currentTrackInfo[0], true);

        // Send broadcast informing all interested components that streaming has started
        broadcastMessage(BROADCAST_COMMAND_PLAY);

        // Start updating track information
        setUpdateTimer(true);
        Log.i(TAG, "Stream resumed.");

    }

    /**
     * Stops streaming audio and kills audio service.
     */
    public void stopAudio() {

        // Broadcast service closing intent
        broadcastMessage(BROADCAST_COMMAND_STOP);

        // Kill the service
        stopSelf();
    }

    /**
     * Sends a local broadcast with passed BROADCAST_COMMAND.
     * @param broadcastCommand BROADCAST_COMMAND String constant.
     */
    private void broadcastMessage(final String broadcastCommand) {
        Intent intent = new Intent(BROADCAST_MESSAGE);
        intent.putExtra(BROADCAST_KEY, broadcastCommand);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.v(TAG, "Sent local broadcast: " + intent.getStringExtra(BROADCAST_KEY));
    }

    /**
     * Updates and displays the stream status bar. Updates require a broadcast message of a
     * different format, which includes the status to display as well as its lifespan.
     * @param status String to be displayed in the status bar
     * @param displayUntilCancelled if this is true, the status message will be displayed until it is explicitly
     *                              cancelled by a BROADCAST_COMMAND_HIDE_STATUS message.
     */
    private void updateStreamStatus(String status, boolean displayUntilCancelled) {

        // Send a
        // TODO: If this method or broadcastMessage() get much bigger, it would be more correct to make a separate class dedicated to broadcasts.

        Intent intent = new Intent(BROADCAST_MESSAGE);
        intent.putExtra(BROADCAST_KEY, BROADCAST_COMMAND_STATUS_MESSAGE);
        intent.putExtra(STREAM_STATUS_KEY, status);
        intent.putExtra(STREAM_STATUS_DISPLAY_LENGTH, displayUntilCancelled);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.v(TAG, "Sent new status message: " + intent.getStringExtra(STREAM_STATUS_KEY));
        Log.v(TAG, "Indefinite status: " + intent.getBooleanExtra(STREAM_STATUS_DISPLAY_LENGTH, false));
    }

    public IBinder onBind(Intent intent) {
        return null;
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
                mp = buildAudioPlayer();
                // FIXME: This should be a status bar message! Update this when that is fully implemented
                Toast.makeText(getApplicationContext(), "Failed to load the stream. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error in playback. onError was called.");
                return true;
            }

        });
        return mp;
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


    }

    /**
     * Prepares the player for streaming and plays the audio upon completion.
     */
    private void prepareAudio() {
        try {
            Log.v(TAG, "Attempting to play stream from " + streamUrl);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    // When the stream is buffered, kill prompt and start playing automatically.
                    mp.start();
                    isPrepared = true;

                    // Clear cached track information and start audio service.
                    setCurrentTrackInfo("", "", "");
                    updateNotification("", "", true);
                    setUpdateTimer(true);

                    // Since we're not buffering anymore, hide the status bar from the user
                    broadcastMessage(BROADCAST_COMMAND_STATUS_HIDE);
                    Log.i(TAG, "Stream playback started.");

                }
            });

            // Prepares stream without blocking UI Thread
            mp.prepareAsync();
            updateStreamStatus(getString(R.string.stream_status_buffering), true);

        } catch (IllegalStateException e) {
            Log.e(TAG, "Caught IllegalStateException when preparing: ");
            e.printStackTrace();
        }
    }

    /**
     * Allows a component to change the URL played without restarting the service.
     * @param streamUrl
     */
    private void setStreamURL(String streamUrl) {
        this.streamUrl = streamUrl;

        // Stop MediaPlayer
        if (mp.isPlaying()) {
            mp.stop();
        }
        Log.v(TAG, "Stream source changed by user. Stream playback stopped.");

        // Rebuild player with new stream URL.
        mp.reset();
        mp = buildAudioPlayer();
        Log.v(TAG, "Media Player has been rebuilt with new source.");
    }

    private String getStreamURL() {
        return streamUrl;
    }

    /**
     * All MediaPlayer errors will call this method with details.
     * @param mp MediaPlayer object that threw the error
     * @param what int identifier of error type
     * @param extra int identifier of error code
     * @return true if error was handled by this method, false if not.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        // Log the error
        Log.e(TAG, "*** MediaPlayer has encountered a fatal error.");
        String errorType = "";
        String errorCode = "";
        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            errorType = "UNKNOWN ERROR";
        } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            errorType = "SERVER DIED";
        }
        Log.e(TAG, "*** Error Type: " + errorType);

        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
            errorCode = "IO ERROR";
        } else if (extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            errorCode = "MALFORMED MEDIA";
        }  else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            errorCode = "UNSUPPORTED MEDIA";
        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            errorCode = "MEDIA TIMED OUT";
        }
        Log.e(TAG, "*** Error Code: " + errorCode);
        // FIXME: UGLY. Clean this up.
        updateStreamStatus("Error Type: " + errorType + " / Error Code: " + errorCode, false);
        isPrepared = false;
        return false;
    }

    /**
     * Launches a foreground service with minimal information -- should be updated using updateNotification as soon as
     * track information is available.
     */
    private void startForegoundService() {

        // Build the notification to be displayed during foreground execution
        notificationBuilder.setSmallIcon(R.drawable.play_icon_white);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
        Log.v(TAG, "Started foreground streaming service.");
    }

    /**
     * Updates the foreground notification icon with track information
     * @param artistName Name of artist to display
     * @param trackName Name of track to display
     * @param isPlaying true if audio is playing, false if not. This param modifies the notification actions appropriately.
     */
    private void updateNotification(String artistName, String trackName, boolean isPlaying) {
        PendingIntent actionPI;
        notificationBuilder = new Notification.Builder(getApplicationContext());

        // Set static notification elements (title, icon, etc)
        notificationBuilder.setContentTitle(getString(R.string.notification_title));
        notificationBuilder.setSmallIcon(R.drawable.play_icon_white);

        // PendingIntent is executed when user selects the notification.
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), StreamContainer.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // If non-blank track information has been passed, add it to the notification.
        if ((!artistName.equals("")) && (!trackName.equals(""))) {
            notificationBuilder.setContentText(artistName + " - " + trackName);
        } else {
            notificationBuilder.setContentText(getString(R.string.notification_subtitle));
        }

        // If audio is playing, add action to pause audio
        if (isPlaying) {
            Intent pauseIntent = new Intent(this, StreamService.class);
            pauseIntent.setAction(ACTION_PAUSE);
            actionPI = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(R.drawable.pause_icon_white, getString(R.string.pause_audio), actionPI);
        }

        // If audio is NOT playing, add action to resume audio.
        else {
            Intent playIntent = new Intent(this, StreamService.class);
            playIntent.setAction(ACTION_PLAY);
            actionPI = PendingIntent.getService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(R.drawable.play_icon_white, getString(R.string.play_audio), actionPI);
        }

        // Regardless of state, add a stop button to allow the user to stop the streaming service.
        Intent stopIntent = new Intent(this, StreamService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPI = PendingIntent.getService(getApplicationContext(), 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.stop_button_white, getString(R.string.stop_audio), stopPI);

        // FOR FUN AND EXPERIMENTING, add album art as large icon if it has been stored
        // FIXME: This would work better if small album art was retrieved...
        Bitmap bitmap = retrieveAlbumArt();
        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap);
        }

        // Build the notification and launch foreground service.
        Notification notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
        Log.v(TAG, "Notification built. Text: " + artistName + " " + trackName);
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
     * Returns the stored currently playing track information as a String array
     * @return String[] of information in the format {Track Name, Artist Name, Album Name}
     */
    private String[] getCurrentTrackInfo() {
        // {songName, artistName, albumName}
        String[] currentTrackInfo = {prefs.getString(PREFKEY_TRACK, ""), prefs.getString(PREFKEY_ARTIST, ""), prefs.getString(PREFKEY_ALBUM, "")};
        Log.v(TAG, "Track information requested!");
        Log.v(TAG, "Stored track name: " + prefs.getString(PREFKEY_TRACK, ""));
        Log.v(TAG, "Stored artist: " + prefs.getString(PREFKEY_ARTIST, ""));
        Log.v(TAG, "Stored album: " + prefs.getString(PREFKEY_ALBUM, ""));
        return currentTrackInfo;

    }

    private void setCurrentTrackInfo(String trackName, String artistName, String albumName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFKEY_TRACK, trackName);
        editor.putString(PREFKEY_ARTIST, artistName);
        editor.putString(PREFKEY_ALBUM, albumName);
        editor.commit();
        Log.v(TAG, "Stored current track info into SharedPreferences.");
    }

    private void updateTrackInfo() {

        // Store current track info as an array
        String[] currentTrackInfo = getCurrentTrackInfo();

        // Start track update task
        new TrackUpdateHandler(this, this, currentTrackInfo, new PreferenceManager(this).getAlbumArtDownloadPreference()).execute();
    }

    @Override
    public void onTrackUpdate() {

        // Get current track info
        String[] currentTrackInfo = getCurrentTrackInfo();
        String trackName = currentTrackInfo[0];
        String artistName = currentTrackInfo[1];
        String albumName = currentTrackInfo[2];

        // Update the foreground notification
        updateNotification(artistName, trackName, true);

        // Send a broadcast to trigger UI updates from player
        broadcastMessage(BROADCAST_COMMAND_UPDATE);
    }

    /**
     * Retrieves album art of currently playing song from cache.
     * @return Album art as a bitmap or null if it could not be retrieved.
     */
    private Bitmap retrieveAlbumArt() {
        Bitmap bitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir() + "/" + TrackUpdateHandler.ALBUM_ART_FILENAME);
        if (bitmap == null) {
            Log.w(TAG, "Album art could not be retrieved from storage, returning null...");
            return null;
        } else {
            return bitmap;
        }
    }
}
