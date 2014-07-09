package pb.example.myvideoplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import pb.example.myvideoplayer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PlaybackActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private static final String PREF_NAME = "MyVP_pref";
	private static final String PREF_RAND = "p_random";
	private static final String PREF_LOOP = "p_loop";
	private static final String TAG = "MyVP.PlaybackAct";
	
	VideoView mVideo;
	ImageButton mPPbtn, mPrevbtn, mNextbtn;
	ImageButton mRepebtn, mShufbtn;
	SeekBar mPBar;
	private boolean mIsPlaying;
	private boolean mIsRandom = false;
	private boolean mIsLoop = false;
	private int mCurPos = -1;
	private Handler mHandler = new Handler();
	sbUpdater mUpdater = new sbUpdater();
	DBAdapter mDB;
	Cursor mCur;
	
	ArrayList<Integer> nextlist = new ArrayList<Integer>();

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);

		SharedPreferences pref = getSharedPreferences(PREF_NAME, 0);//getPreferences(0);
		mIsRandom = pref.getBoolean(PREF_RAND, false);
		mIsLoop = pref.getBoolean(PREF_LOOP, false);
		
		mDB = new DBAdapter(this);
		mDB.open();
		mVideo = (VideoView) findViewById(R.id.videoView1);
		
		final View controlsView = findViewById(R.id.video_controls);
		//final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, mVideo,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		mVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
		/**
		 * Dokonèení pøehrávání videa
		 */
		mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mIsPlaying = false;
				mUpdater.stopIt();
				nextfile();
			}
		}); 
		/**
		 * Pøíprava pøehrávání
		 */
		mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				mPBar.setMax(mp.getDuration());
				mPBar.setProgress(0);
				mIsPlaying = true;
				mHandler.postDelayed(mUpdater, 500);
				Log.d(TAG,String.valueOf("Video Duration: "+mVideo.getDuration()) );
			}
		});

		/**
		 * Tlaèítko play/pause
		 */
		mPPbtn = (ImageButton) findViewById(R.id.playpause_button);
		mPPbtn.setOnTouchListener(
				mDelayHideTouchListener);
		mPPbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsPlaying){
					mVideo.pause();
					mIsPlaying = false;
					//mPPbtn.setText(R.string.play_button);
					mPPbtn.setImageResource(R.drawable.apollo_holo_light_play);
				}else {
					mVideo.start();
					mIsPlaying = true;
					mPBar.setMax(mVideo.getDuration());
					//mPPbtn.setText(R.string.pause_button);
					mPPbtn.setImageResource(R.drawable.apollo_holo_light_pause);
				}
			}
		});
		/**
		 * Tlaèítko pøedchozí
		 */
		mPrevbtn = (ImageButton) findViewById(R.id.prev_button);
		mPrevbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				prevfile();
			}
		});
		/**
		 * Tlaèítko další
		 */
		mNextbtn = (ImageButton) findViewById(R.id.next_button);
		mNextbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextfile();
			}
		});
		/**
		 * Tlaèítko opakování
		 */
		mRepebtn = (ImageButton) findViewById(R.id.repeat_button);
		mRepebtn.setImageResource(mIsLoop ? R.drawable.apollo_holo_light_repeat_all : 
			R.drawable.apollo_holo_light_repeat_normal);
		mRepebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsLoop) {
					mRepebtn.setImageResource(R.drawable.apollo_holo_light_repeat_normal);
					mIsLoop = false;
				} else {
					mRepebtn.setImageResource(R.drawable.apollo_holo_light_repeat_all);
					mIsLoop = true;
				}
			}
		});
		/**
		 * Tlaèítko náhodnì
		 */
		mShufbtn = (ImageButton) findViewById(R.id.shuffle_button);
		mShufbtn.setImageResource(mIsRandom ? R.drawable.apollo_holo_light_shuffle_on : 
			R.drawable.apollo_holo_light_shuffle_normal);
		mShufbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsRandom) { //vypnuti randomu TODO nedela to spravne
					mShufbtn.setImageResource(R.drawable.apollo_holo_light_shuffle_normal);
					mIsRandom = false;
					mCurPos = nextlist.get(mCurPos);
					setlist(); //znovu vygeneruje seznam
				} else { //zapnuti randomu
					mShufbtn.setImageResource(R.drawable.apollo_holo_light_shuffle_on);
					mIsRandom = true;
					setlist();
					fileList();
				}
			}
		});
		/**
		 * Posuvník videa
		 */
		mPBar = (SeekBar) findViewById(R.id.progress);
		mPBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mVideo.seekTo(seekBar.getProgress());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser){
					seekBar.setProgress(progress);
				}
			}
		});
		
		
		Bundle b = getIntent().getExtras();
		int plid = b.getInt("plid", -1);
		int idx = b.getInt("fileId", -1);
		mCur = mDB.getListFiles(plid);
		Log.d(TAG, "plid:"+plid);
		
		setlist();		
		if (idx >=0) {
			mCurPos = idx;
			openfile(idx);
		}else
			frstfile();

	}//oncreate
	
	/**
	 * Metoda naète z kurzoru cestu k souboru videa a pøedí ji pøehrávaèi
	 * @param index Index na pozici do kursoru
	 */
	private void openfile(int index){
		Log.d(TAG, "mcurpos:"+index);
		mCur.moveToPosition(index);
		String p = mCur.getString(mCur.getColumnIndexOrThrow(DBAdapter.T_KEY_PATH));
		p = p.substring(1, p.length()-1);
		Log.d(TAG, "open file:"+p);
		
		File f = new File(p);
		Uri viduri = Uri.fromFile(f);
		
		mVideo.setVideoURI(viduri);
	}
	/**
	 * Nastavení pozice na první video v seznamu
	 */
	public void frstfile() {
		mCurPos = 0;
		openfile(nextlist.get(mCurPos));
	}
	/**
	 * Nastavení pozice na další video v seznamu
	 */
	public void nextfile() {
		if (mCurPos == nextlist.size()-1){ //je posledni
			if (mIsLoop)	//opakovani zapnuto
				mCurPos = 0;
			else {			//opakovani vypnuto
				mVideo.stopPlayback();	//zastavi prehravani
				//mIsPlaying = false;
				mPPbtn.performClick();
				Toast.makeText(getBaseContext(), "Konec seznamu", Toast.LENGTH_SHORT).show();
				return;
			}
		}else				//neni posledni
			mCurPos++;		//spusti dalsi
		openfile(nextlist.get(mCurPos));
		
	}
	/**
	 * Nastavení pozice na pøedchozí video v seznamu
	 */
	public void prevfile() {
		if (mCurPos > 0)
			mCurPos--;
		openfile(nextlist.get(mCurPos));
	}
	/**
	 * Vytvoøení seznamu pro pøehrávání
	 */
	public void setlist(){
		mCur.moveToFirst();
		do {
			nextlist.add(mCur.getPosition());
		} while(mCur.moveToNext());
		Log.d(TAG, "delka seznamu:"+nextlist.size());
		
		if (mIsRandom){
			Random gen = new Random();
			gen.nextInt();
			for (int i=1, n=nextlist.size(); i<n; i++){  //TODO prvni je vzdy prvni
				int idx = i + gen.nextInt(n-i);
				swap(nextlist, i, idx);
			}
		}
		mCur.moveToFirst();
	}
	/**
	 * Pøehození prvkù v seznamu, využíváno pro náhodné pøehrávání
	 * @param ll Seznam pøehrávání
	 * @param a Prvek 1
	 * @param b Prvek 2
	 */
	private void swap(ArrayList<Integer> ll, int a, int b) {
		int hel = ll.get(a);
		ll.set(a, ll.get(b));
		ll.set(b, hel);
	}
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mVideo.isPlaying())
			mVideo.stopPlayback();
		mDB.deleteList(DBAdapter.PL_DEF);
	}
	
	@Override
	protected void onStop() {
		SharedPreferences pref = getSharedPreferences(PREF_NAME, 0);
		Editor ed = pref.edit();
		ed.putBoolean(PREF_RAND, mIsRandom);
		ed.putBoolean(PREF_LOOP, mIsLoop);
		ed.commit();
		super.onStop();
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	/**
	 * Tøída pro obhovování pozice progresbaru
	 */
	private class sbUpdater implements Runnable {
		private boolean stop;

		public void stopIt() {
			stop = true;
		}
		@Override
		public void run() {
			mPBar.setProgress(mVideo.getCurrentPosition());
			if(!stop)
				mHandler.postDelayed(mUpdater, 500);
		}
	}
}

