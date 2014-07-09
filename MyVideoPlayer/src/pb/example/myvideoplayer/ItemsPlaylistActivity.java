package pb.example.myvideoplayer;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ItemsPlaylistActivity extends ListActivity {

	private static final int MI_DEL = 12;
	
	private static final String TAG = "MyVP.ItemPlaylist";
	private DBAdapter mDB;
	private int mPlid = -1;
	private Cursor mCur;
	private int mId;
	private ListView mList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items_playlist);
		
		Bundle b= getIntent().getExtras();
		mPlid = b.getInt("plid");
		
		mDB = new DBAdapter(this);
		mDB.open();
		
		//ActionBar bar = getActionBar();
		//bar.setBackgroundDrawable(new ColorDrawable(Color.CYAN));
		String title = mDB.getListName(mPlid);
		setTitle(title);
		//Log.d(TAG, "curs size:"+mCur.getCount());
		
		mList = getListView();
		mList.setOnCreateContextMenuListener(this);
		
	}

	@Override
	protected void onResume() {
		initList();
		super.onResume();
	}
	/**
	 * Inicializace seznamu souborù
	 */
	private void initList() {
		mCur = mDB.getListFiles(mPlid);
		mCur.moveToFirst();
		
		String[] from = new String[]{DBAdapter.T_KEY_PATH};
		int[] to = new int[]{android.R.id.text1}; //R.id.vidListItem_name
		/*ItemPlaylistAdapter adapter = new ItemPlaylistAdapter(this, R.layout.adapter_videolist, 
				mCur , from, to);
				*/
		setListAdapter(new ItemPlaylistAdapter(this, R.layout.adapter_videolist, 
				mCur , from, to));
	}
		
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(ItemsPlaylistActivity.this, PlaybackActivity.class);
		i.putExtra("plid", mPlid);
		i.putExtra("fileId", position);
		startActivity(i);
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, MI_DEL, 0, "Smazat");
		
		//String title = mCur.getString(mCur.getColumnIndexOrThrow(DBAdapter.T_KEY_LNAME));
		//menu.setHeaderTitle(title);
		
		mId = mCur.getInt(mCur.getColumnIndexOrThrow(DBAdapter.T_KEY_ID));
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MI_DEL:
			mDB.deleteFile(mId, mPlid); 
			initList();
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.items_playlist, menu);
		return true;
	}

}
