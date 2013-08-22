package fm.krui.kruifm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;

/*
FIXME: I AM A DEPRECATED CLASS. I SHOULD BE COMPLETELY REPLACED BY AN IDENTICAL FRAGMENT. DO NOT RELEASE WITH ME INCLUDED. I SUCK.
 */
public class StreamActivity extends Activity implements DBListener {

	static private MediaPlayer mp;
	static private boolean isPaused = false;
	static ProgressDialog pd;
	static String streamUrl = "http://krui.student-services.uiowa.edu:8200"; // Default value is 128kb/s 89.7 stream.
	private static final String TAG = StreamActivity.class.getName(); // Tag constant for logging purposes

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stream_activity_menu, menu);
		return true;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream_layout);
		
		// Build Database
		// TODO: Make this not build on startup in a more elegant way.
		// TODO: Modularize this so it doesn't build on every startup of this activity.
		ProgressDialog pd = new ProgressDialog(this);
		new PopulateDB(this, this, this).execute();

		// Build station selection spinner
		ArrayAdapter<CharSequence> stationAdapter = ArrayAdapter.createFromResource(this, R.array.station_string_array, android.R.layout.simple_spinner_item);
		stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Build audio player using default settings.
		mp = buildAudioPlayer();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// If audio is not playing, reset MediaPlayer object to prevent memory leaks.
		if (mp.isPlaying() == false) {
			mp.reset();
			isPaused = false;
			Log.v(TAG, "User leaving activity and player is stopped. Releasing MediaPlayer.");
		}
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
		mp.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				
				// If there is an error in playback, stop and inform the user.
				pd.dismiss();
				mp = buildAudioPlayer();
				Toast.makeText(getBaseContext(), "Failed to load the stream. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
				Log.e(TAG, "Error in playback. onError was called.");
				return true;
			}
			
		});

		return mp;
	}

	protected void changeUrl(int spinnerPosition) {
		String newStreamUrl;
		switch (spinnerPosition) {
		case 0:
			// 128kb 89.7 stream
			// Default case, always executed on activity creation.
			newStreamUrl = "http://krui.student-services.uiowa.edu:8200";
			reconfigureStream(newStreamUrl);
			break;

		case 1:
			// 320kb 89.7 stream
			newStreamUrl = "http://krui.student-services.uiowa.edu:8000";
			reconfigureStream(newStreamUrl);
			break;

		case 2:
			// 128kb Lab stream
			newStreamUrl = "http://krui.student-services.uiowa.edu:8105";
			reconfigureStream(newStreamUrl);
			break;

		case 3:
			// 320kb Lab stream
			newStreamUrl = "http://krui.student-services.uiowa.edu:8103";
			reconfigureStream(newStreamUrl);
		}
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
	/**
	 * Pauses audio with no change to connection or Media Player. Called by a button onClick event.
	 * @param v Button pressed by user.
	 */
	public void pauseAudio(View v) {
		mp.pause();
		isPaused = true;
		Log.i(TAG, "Stream playback paused.");
	}

	/**
	 * Prepares Media Player asynchronously. Displays prompt while buffering and automatically starts when finished. 
	 * @param v Button pressed by user.
	 */
	public void playAudio(View v) {
		
		// If audio is paused, resume playback without rebuffering.
		if (isPaused) {
			mp.start();
			isPaused = false;
			Log.i(TAG, "Stream resumed.");
		} else if (isPaused == false && mp.isPlaying() == false) {

			// If audio is NOT playing, we need to prepare and buffer.
			try {
				Log.v(TAG, "Attempting to play stream from " + streamUrl);
				mp.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {

						// When the stream is buffered, kill prompt and start playing automatically.
						pd.dismiss();
						mp.start();
						Log.i(TAG, "Stream playback started.");
					}
				});

				// Prepares stream without blocking UI Thread
				mp.prepareAsync(); 

			} catch (IllegalStateException e) {
				Log.e(TAG, "Caught IllegalStateException when preparing: ");
				e.printStackTrace();
			} 

			// Stop user input while buffering by displaying ProgressDialog
			pd = new ProgressDialog(this);
			pd.setCancelable(true);
			pd.setCanceledOnTouchOutside(false);
			pd = ProgressDialog.show(this, "Loading...", "Buffering Stream", true, true, new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					pd.dismiss();

				}
				
			});
		}
	}
	
	/**
	 * Refreshes the title, album name, art, and other info and updates the UI.
	 */
	public void updateTrackInfo(View v) {
		ProgressDialog pd = new ProgressDialog(this);
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

    @Override
    public void onDBFinish() {
    }
}
