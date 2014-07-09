package pb.example.myvideoplayer;

import pb.example.myvideoplayer.util.MyTime;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ItemPlaylistAdapter extends SimpleCursorAdapter{

	private static final String TAG = "MyVP.ItemPlaylistAdapter";
	
	public ItemPlaylistAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tn = (TextView) view.findViewById(R.id.vidListItem_name);
		TextView td = (TextView) view.findViewById(R.id.vidListItem_dura);
		ImageView im = (ImageView) view.findViewById(R.id.vidListItem_thumb);
		
		String[] projection = {MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.DURATION,
				MediaStore.Video.Media.SIZE};	
		
		String path = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.T_KEY_PATH));
		path = path.substring(1, path.length()-1);


		String selection = MediaStore.Video.VideoColumns.DATA + " LIKE ?";
		String selectionArgs[] = new String[] { path };
		Cursor c = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
		c.moveToFirst();
		
		String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
		tn.setText(name);
		
		Long dura = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
		MyTime tt = new MyTime(dura);
		td.setText("Délka: "+tt.toString());

		Bitmap bbb = ThumbnailUtils.createVideoThumbnail(
				c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)),
				Thumbnails.MICRO_KIND);
		if (bbb != null)
			im.setImageBitmap(bbb);
		
		ImageView btn = (ImageView) view.findViewById(R.id.vidListitem_dia);
		btn.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});
		
		c.close();
		super.bindView(view, context, cursor);
	}

}
