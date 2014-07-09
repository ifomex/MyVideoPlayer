package pb.example.myvideoplayer;

import pb.example.myvideoplayer.util.MyTime;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class VideoListAdapter extends SimpleCursorAdapter {

	public VideoListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView im = (ImageView)view.findViewById(R.id.vidListItem_thumb);
		TextView tn = (TextView)view.findViewById(R.id.vidListItem_name);
		TextView td = (TextView)view.findViewById(R.id.vidListItem_dura);
		
		String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
		tn.setText(name);
		Long dura = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
		MyTime tt = new MyTime(dura);
		td.setText("Délka: "+tt.toString());

		Bitmap bbb = ThumbnailUtils.createVideoThumbnail(
				cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)),
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
	}

	
}
