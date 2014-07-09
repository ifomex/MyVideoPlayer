package pb.example.myvideoplayer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PlaylistListAdapter extends SimpleCursorAdapter {
	
	public PlaylistListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tn = (TextView) view.findViewById(R.id.pla_name);
		TextView tc = (TextView) view.findViewById(R.id.pla_cnt);
		ImageView ic = (ImageView) view.findViewById(R.id.pla_con);
		
		tn.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.T_KEY_LNAME)));
		
		String cnt = "Poèet: "+String.valueOf( cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.T_KEY_CNT)));
		tc.setText(cnt);
		
		ic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});
		
		super.bindView(view, context, cursor);
	}
}
