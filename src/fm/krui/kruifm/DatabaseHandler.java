package fm.krui.kruifm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	
		private static final String TAG = DatabaseHandler.class.getName(); // Tag constant for logging purposes

		// Database Attributes
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "KRUI_DB";
		private static final String TABLE_SHOWS = "shows";


		// Column Names
		// Show (String id, int Station, String title, String startTime, String endTime, String htmlLink, String description, int music, int talk, int sports, int special) {
		private static final String KEY_ID = "id";
		private static final String KEY_SHOWID = "show_id";
		private static final String KEY_STATION = "station";
		private static final String KEY_TITLE = "title";
		private static final String KEY_DAYOFWEEK = "day_of_week";
		private static final String KEY_STARTTIME = "start_time";
		private static final String KEY_ENDTIME = "end_time";
		private static final String KEY_LINK = "html_link";
		private static final String KEY_DESCRIPTION = "description";
		private static final String KEY_MUSIC = "is_music";
		private static final String KEY_TALK = "is_talk";
		private static final String KEY_SPORTS = "is_sports";
		private static final String KEY_SPECIAL = "is_special";

		public DatabaseHandler(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Build the database based on above schema 
			String CREATE_SHOWS_TABLE = "CREATE TABLE " + TABLE_SHOWS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SHOWID + " TEXT," + KEY_STATION + " INTEGER," + KEY_TITLE + " TEXT," + KEY_DAYOFWEEK + " TEXT," + KEY_STARTTIME + " TEXT," + KEY_ENDTIME + " TEXT," + KEY_LINK + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_MUSIC + " INTEGER," + KEY_TALK + " INTEGER," + KEY_SPORTS + " INTEGER," + KEY_SPECIAL + " INTEGER" + ")";
			db.execSQL(CREATE_SHOWS_TABLE);
			Log.v(TAG, "** Created new database: " + DATABASE_NAME);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Drop existing table and rebuild.
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
			onCreate(db);
			Log.v(TAG, "** Updated database: " + DATABASE_NAME);
		}
		
		public void truncate() {

			// Wipe DB WITHOUT upgrading. Unnecessary in final build. Remove. FIXME
			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
			onCreate(db);
			Log.v(TAG, "** Truncated database: " + DATABASE_NAME);
		}

		/*
		 * Add/Retrieval operations
		 */
		void addShow(Show show) {
			SQLiteDatabase db = this.getWritableDatabase();

			// Grab all values and format for insertion
			ContentValues values = new ContentValues();
			values.put(KEY_SHOWID, show.get_id());
			values.put(KEY_STATION, show.get_station());
			values.put(KEY_TITLE, show.get_title());
			values.put(KEY_DAYOFWEEK, show.get_dayOfWeek());
			values.put(KEY_STARTTIME, show.get_startTime());
			values.put(KEY_ENDTIME, show.get_endTime());
			values.put(KEY_LINK, show.get_htmlLink());
			values.put(KEY_DESCRIPTION, show.get_description());
			values.put(KEY_MUSIC, show.get_music());
			values.put(KEY_TALK, show.get_talk());
			values.put(KEY_SPORTS, show.get_sports());
			values.put(KEY_SPECIAL, show.get_special());

			// Insert the row and close connection.
			db.insert(TABLE_SHOWS, null, values);
			db.close();
		}
		
		/**
		 * Returns the full database record of a show when given its alphanumeric key.
		 */
		public Show getShow(String show_id) {
			SQLiteDatabase db = this.getWritableDatabase();

			Cursor cursor = db.query(TABLE_SHOWS, new String[] { KEY_SHOWID, KEY_STATION, KEY_TITLE, KEY_DAYOFWEEK, KEY_STARTTIME, KEY_ENDTIME, KEY_LINK, KEY_DESCRIPTION, KEY_MUSIC, KEY_TALK, KEY_SPORTS, KEY_SPECIAL }, KEY_SHOWID + "=?", new String[] { show_id }, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
			}

			// Create show and return the object for use. 
			// String id, int station, String title, int dayOfWeek, String startTime, String endTime, String htmlLink, String description, int music, int talk, int sports, int special
			if (cursor.getCount() > 0) {
				Show show = new Show( cursor.getString(0), Integer.parseInt(cursor.getString(1)), cursor.getString(2), Integer.parseInt(cursor.getString(3)), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)), Integer.parseInt(cursor.getString(9)), Integer.parseInt(cursor.getString(10)), Integer.parseInt(cursor.getString(11)));
				db.close();
				cursor.close();
				return show;
			}

			// If no rows returned, log and return an empty show object.
			Show empty = new Show();
			Log.e(TAG, "Returned no database entry for input: " + show_id);
			db.close();
			cursor.close();
			return empty;
		}
		
		/*
		 *  Convenience class for db calls outside of BuildDatabase
		 *  readDB(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
		 */
		public Cursor readDB(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
			
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			if (cursor != null) {
				cursor.moveToFirst();
			} 
			return cursor;
		}
	
	}
