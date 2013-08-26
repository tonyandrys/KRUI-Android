package fm.krui.kruifm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * fm.krui.kruifm - StreamService
 *
 * @author Tony Andrys
 *         Created: 08/25/2013
 *         (C) 2013 - Tony Andrys
 */

/**
 * Audio streaming functionality as a service.
 */
public class StreamService extends Service implements MediaPlayer.OnErrorListener {

    final private String TAG = StreamService.class.getName();

    // Intent command constants
    public static final String ACTION_PLAY = "fm.krui.kruifm.PLAY";
    public static final String ACTION_PAUSE = "fm.krui.kruifm.PAUSE";
    public static final String ACTION_RECONFIGURE = "fm.krui.kruifm.RECONFIG";

    // Default stream to play is the 89.7 128kb/s stream
    private String streamUrl = "http://krui.student-services.uiowa.edu:8200";

    private MediaPlayer mp;
    private Activity activity;
    private NotificationManager notificationManager;

    // Stream bools
    private boolean isPrepared;
    private boolean isPaused;

    public StreamService(Activity activity, String streamUrl) {
        this.activity = activity;
        this.streamUrl = streamUrl;
        this.isPrepared = false;
        this.isPaused = false;

        // Build the Audio Player and configure it when building the service.
        buildAudioPlayer();
    }

    // legacy api support method
    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get intent action
        String intentAction = intent.getAction();

        // If play command is received, prepare and play audio.
        if (intentAction.equals(ACTION_PLAY)) {
            prepareAudio();
        } else if (intentAction.equals(ACTION_PAUSE)) {
        }

        // The audio streaming service should persist until it is explicitly stopped, so return sticky status.
        return START_STICKY;
    }

    /**
     * Pauses the MediaPlayer
     */
    public void pauseAudio() {

        mp.pause();
        isPaused = true;
        Log.i(TAG, "Stream paused.");
    }

    /**
     * Plays audio, preparing and buffering the stream if necessary.
     */
    public void playAudio() {

        // If media player is playing, pause the audio.
        if (mp.isPlaying()) {
            pauseAudio();
        }

        // If no sound is playing, check if the player is prepared.
        else {
            // If the media player is prepared, quickly resume the audio.
            if (isPrepared) {
                resumeAudio();
            }
            // If the media player is NOT prepared, it should be prepared before playing.
            else {
                prepareAudio();
            }
        }

    }

    public void resumeAudio() {

        // Resume the audio
        mp.start();
        isPaused = false;
        Log.i(TAG, "Stream resumed.");
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
                Toast.makeText(activity, "Failed to load the stream. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
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

        // Rebuild player with new stream URL.
        mp.reset();
        mp = buildAudioPlayer();
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
                    Log.i(TAG, "Stream playback started.");
                }
            });

            // Prepares stream without blocking UI Thread
            mp.prepareAsync();

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
    }

    private String getStreamURL() {
        return streamUrl;
    }

    /**
     * All MediaPlayer errors will call this method with details.
     * @param mp MediaPlayer object that threw the error
     * @param what int identifier of error type
     * @param extra int identifier of error code
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        // Log the error
        Log.e(TAG, "*** MediaPlayer has encountered a fatal error.");
        String errorType = "";
        String errorCode = "";
        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            errorType = "UNKNOWN";
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
        isPrepared = false;
        return false;
    }
}
