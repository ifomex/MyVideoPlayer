package pb.example.myvideoplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	public static final String TABLE_LISTS = "VidPlaylists";
	public static final String TABLE_FILES = "VidFiles";
	public static final String T_KEY_ID = "_id";
	public static final String T_KEY_LNAME = "listname";
	public static final String T_KEY_PATH = "path";
	public static final String T_KEY_CNT = "cnt";
	public static final String T_KEY_PLID = "plid";
	
	public static final int PL_DEF = 1;
	
	private static final String TAG = "MyVP.DBAdapter";
	
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, "data", null, 4);
		}
		/**
		 * Vytvoøení databáze
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table "+ TABLE_LISTS+
					"(_id integer primary key autoincrement, "+
					"listname text not null, "+
					"cnt integer );");
			
			db.execSQL("create table "+ TABLE_FILES+
					"(_id integer primary key autoincrement, "+
					"plid integer not null, "+
					"path string not null);" );
			ContentValues v = new ContentValues();
			v.put(T_KEY_LNAME, "Nový");
			v.put(T_KEY_CNT, 0);
			long aa = db.insert(TABLE_LISTS, null, v);
			Log.d(TAG, "default playlist:"+String.valueOf(aa));
		}
		/**
		 * Update databáze
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists "+TABLE_FILES);
			db.execSQL("drop table if exists "+TABLE_LISTS);
			onCreate(db);
		}
					
	}
		
	public DBAdapter(Context ctx) {
    	this.mCtx = ctx;
    }
    /**
     * Otevøení databáze
     * @return Vrací odkaz se databázi
     * @throws SQLException
     */
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    /**
     * Zavøení databáze
     */
    public void close() {
        mDbHelper.close();
    }
	/**
	 * Vytvoøení playlistu
	 * @param name Název Playlistu
	 * @return Vrací index vytvoøeného playlistu v databázi
	 */
    public int createList(String name) {
    	ContentValues v = new ContentValues();
		v.put(T_KEY_LNAME, name);
		v.put(T_KEY_CNT, 0);
		return (int) mDb.insert(TABLE_LISTS, null, v);
    }
    /**
     * Pøedání video souboru do databáze 
     * @param list Playlist do kterého soubor patøí
     * @param path Cesta k souboru
     */
	public void addFile(int list, String path) {
		//vlozi novy soubor
		ContentValues v = new ContentValues();
		v.put(T_KEY_PLID, list);
		v.put(T_KEY_PATH, "\'"+path+"\'");
		mDb.insert(TABLE_FILES, null, v);

		Cursor cc = getListFiles(list);
		Log.d(TAG, "pocet v cursoru"+String.valueOf(cc.getCount()));
		v = new ContentValues();
		v.put(T_KEY_CNT, cc.getCount());
		mDb.update(TABLE_LISTS, v, T_KEY_ID+" = "+list, null);
		//mDb.execSQL("update "+TABLE_LISTS+" set "+T_KEY_CNT +" = "+T_KEY_CNT +" + 1 where "+T_KEY_ID+" = "+list);
	}
	/**
	 * Smazání souboru z databáze
	 * @param id Identifikátor souboru
	 * @param plid Identfikátor playlistu
	 */
	public void deleteFile(int id, int plid) {
		mDb.delete(TABLE_FILES, T_KEY_ID+" = "+id, null);
		mDb.execSQL("update "+TABLE_LISTS+" set "+T_KEY_CNT +" = "+T_KEY_CNT +" - 1 where "+T_KEY_ID+" = "+plid);
	}
	/**
	 * Naètení všech playalistù
	 * @return Databázový kurzor se seznamem playlistù 
	 */
	public Cursor getAllLists() {// vynechat první (defaultni) playlist
		return mDb.query(TABLE_LISTS, new String[] {T_KEY_ID, T_KEY_LNAME, T_KEY_CNT}, T_KEY_ID+" != "+PL_DEF, null, null, null, null);
	}
	/**
	 * Naètení jména playlistu
	 * @param plid identifikátor playlistu
	 * @return Jméno playlistu
	 */
	public String getListName(int plid) {
		Cursor c = mDb.query(TABLE_LISTS, new String[] {T_KEY_ID, T_KEY_LNAME}, T_KEY_ID+" = "+plid , null, null, null, null);
		c.moveToFirst();
		return c.getString(c.getColumnIndexOrThrow(T_KEY_LNAME));
	}
	/**
	 * naètení všech souborù playlistu
	 * @param plid Identifikátor playlistu
	 * @return Databázový kurzor se seznamem souborù
	 */
	public Cursor getListFiles(int plid) { 
		return mDb.query(TABLE_FILES, new String[] {T_KEY_ID, T_KEY_PATH},
				T_KEY_PLID +" = "+plid, null, null, null, null);
	}
	/** 
	 * Odstranìní playlistu
	 * @param plid Identifikátor playlistu
	 */
	public void deleteList(int plid) {
		mDb.delete(TABLE_FILES, T_KEY_PLID+" = "+plid, null);
		if (plid != PL_DEF)
			mDb.delete(TABLE_LISTS, T_KEY_ID+" = "+plid, null);
	}
}
