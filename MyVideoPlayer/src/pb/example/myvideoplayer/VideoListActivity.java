package pb.example.myvideoplayer;

import java.io.File;
import java.util.ArrayList;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class VideoListActivity extends Activity implements OnItemClickListener {
							//ListActivity
    private final int PLAY_ALL = 6;
    private final int ADD_TO_PLAYLIST = 7;
    
    private final int DIAL_PL = 11;
    private final int DIAL_NWPL = 12;
	
	public static final String TAG = "MyVP.VideoList";
	ListView mList;
	Cursor mCursorVideo;
	private DBAdapter mDB;
	String mNewplname;
	String mSelPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_list);
		
		mList = (ListView) findViewById(R.id.vid_list);
		
		mDB = new DBAdapter(this);
		mDB.open();
	}
	
	@Override
	protected void onResume() {
		initList();
		super.onResume();
	}

	@Override
	protected void onStop() {
		mDB.close();
		super.onStop();
	}
	/**
	 * Inicializace seznamu videí
	 */
	public void initList() {
		new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String[] projection = {MediaStore.Video.Media._ID,
						MediaStore.Video.Media.DATA,
						MediaStore.Video.Media.DISPLAY_NAME,
						MediaStore.Video.Media.DURATION,
						MediaStore.Video.Media.SIZE};
				//String selection = MediaStore.Video.VideoColumns.MIME_TYPE + "=?";
				//String selectionArgs[] = new String[] { "video/mp4", "video/3gp" };
				mCursorVideo = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
				
				String[] from = new String[]{MediaStore.Video.Media.DISPLAY_NAME};
				int[] to = new int[]{R.id.vidListItem_name};
				VideoListAdapter vList = new VideoListAdapter(VideoListActivity.this, R.layout.adapter_videolist, mCursorVideo, from, to);
				
				//setListAdapter(vList);
				//mList = this.getListView();
				
				mList.setAdapter(vList);
			}
		}.run();
		
		mList.setOnItemClickListener(VideoListActivity.this);
		mList.setOnCreateContextMenuListener(VideoListActivity.this);
	}


	
	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		mDB.deleteList(DBAdapter.PL_DEF);
		mCursorVideo.moveToPosition(position);
		String path = mCursorVideo.getString(mCursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
		mDB.addFile(DBAdapter.PL_DEF, path);
		//File f = new File(path);
		Intent i = new Intent(VideoListActivity.this, PlaybackActivity.class);
		//i.setDataAndType(Uri.fromFile(f), "video/*");
		i.putExtra("plid", DBAdapter.PL_DEF);
		startActivity(i);		
	}
	/*	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mCursorVideo.moveToPosition(position);
		String path = mCursorVideo.getString(mCursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
		File f = new File(path);
		Intent i = new Intent(VideoListActivity.this, PlaybackActivity.class);
		i.setDataAndType(Uri.fromFile(f), "video/*");
		startActivity(i);
		super.onListItemClick(l, v, position, id);
	}
	*/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, PLAY_ALL, 0, "Pøehrát vše");//getResources().getString(R.string.play_all)
        menu.add(0, ADD_TO_PLAYLIST, 0, "Pøidat do playlistu");//getResources().getString(R.string.add_to_playlist)
        
        String title = mCursorVideo.getString(mCursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
        menu.setHeaderTitle(title);
        
        mSelPath = mCursorVideo.getString(mCursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PLAY_ALL:
			mDB.deleteList(DBAdapter.PL_DEF);
			mCursorVideo.moveToFirst();
			do {
				mDB.addFile(DBAdapter.PL_DEF,
						mCursorVideo.getString(mCursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
			}while (mCursorVideo.moveToNext());
			Intent i = new Intent(VideoListActivity.this, PlaybackActivity.class);
			i.putExtra("plid", DBAdapter.PL_DEF);
			startActivity(i);
			break;
		case ADD_TO_PLAYLIST:
			showDialog(DIAL_PL);
			
			break;
		}
		return super.onContextItemSelected(item);
	}

	
	
	public Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case DIAL_PL:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Pøidat do playlistu");
			builder.setNeutralButton("Nový", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showDialog(DIAL_NWPL);
				}
			});
			builder.setCursor(mDB.getAllLists(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					/*case 0: // odstranit
						showDialog(DIAL_NWPL);
						Log.d(TAG, "novy pl:"+mNewplname);
						break;*/
					default:
						Log.d(TAG, "starsi pl:"+(2+which));
						mDB.addFile(which+2, mSelPath);		//+2 kvuli defaultnimu playlistu a rozdílu indexù o 1
					}
					
				}
			},DBAdapter.T_KEY_LNAME);
			dialog = builder.create();
			break;
		case DIAL_NWPL:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Nový playlist");
			final EditText plname = new EditText(getBaseContext()); 
			builder.setView(plname);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mNewplname = plname.getText().toString();
					int ii = mDB.createList(mNewplname);
					Log.d(TAG, "novej pl:"+ii);
					mDB.addFile(ii, mSelPath);
					Toast.makeText(getBaseContext(), "Vytvoøen playlist "+mNewplname, Toast.LENGTH_SHORT).show();
					mNewplname = "";
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = super.onCreateDialog(id); 
		}
		return dialog;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video_list, menu);
		return true;
	}

	

}
