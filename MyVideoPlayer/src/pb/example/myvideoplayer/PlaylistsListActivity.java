package pb.example.myvideoplayer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PlaylistsListActivity extends Activity implements OnItemClickListener{
										//List 
	private static final String TAG = "MyVP.PlaylistList";
	private static final int MI_SHOW = 11;
	private static final int MI_DEL = 12;
	
	private ListView mList;
	public DBAdapter mDB;
	private int mPlid;
	private Cursor mCur;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlists_list);
		
		mList = (ListView) findViewById(R.id.pl_list);
		
		mDB = new DBAdapter(this);
		mDB.open();
		//mCur = mDB.getAllLists();
		//mCur.moveToFirst();
		
		//initList();
		//mList = getListView();
		mList.setOnItemClickListener(this);
		mList.setOnCreateContextMenuListener(this);
	}
	
	@Override
	protected void onResume() {	
		initList();
		super.onResume();
	}
	/**
	 * Inicializace seznamu playlistù
	 */
	private void initList() {
		mCur = mDB.getAllLists();
		mCur.moveToFirst();
		
		String[] from = new String[]{DBAdapter.T_KEY_LNAME};
		int[] to = new int[]{R.id.pla_name};
		
		mList.setAdapter(new PlaylistListAdapter(this, R.layout.adapter_playlist_list, 
				mCur, from, to));
		/*setListAdapter(new PlaylistListAdapter(this, R.layout.adapter_playlist_list, 
				mCur, from, to));*/
	}
	
	/*@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(PlaylistsListActivity.this, PlaybackActivity.class);
		i.putExtra("plid", position+1);
		startActivity(i);
		super.onListItemClick(l, v, position, id);
	}*/
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		mCur.moveToPosition(position);
		Intent i = new Intent(PlaylistsListActivity.this, PlaybackActivity.class);
		i.putExtra("plid", position+2);	//+2 kvuli defaultnimu playlistu na indexu 1 , position zacina od 0
		Log.d(TAG, "plid:"+position+2);
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		menu.add(0, MI_SHOW, 0, "Zobrazit");
		menu.add(0, MI_DEL, 0, "Smazat");
		
		String title = mCur.getString(mCur.getColumnIndexOrThrow(DBAdapter.T_KEY_LNAME));
		menu.setHeaderTitle(title);
		//Log.d(TAG, "curpos:"+mCur.getPosition());
		mPlid = mCur.getInt(mCur.getColumnIndexOrThrow(DBAdapter.T_KEY_ID));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MI_SHOW:
			//Log.d(TAG, "cislo playlist: "+mPlid);
			Intent i = new Intent(PlaylistsListActivity.this, ItemsPlaylistActivity.class);
			i.putExtra("plid", mPlid);
			startActivity(i);
			break;
		case MI_DEL:
			mDB.deleteList(mPlid);	
			initList(); 
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.playlists_list, menu);
		return true;
	}

	@Override
	protected void onStop() {
		//mCur.close();
		super.onStop();
	}

	
	
}
