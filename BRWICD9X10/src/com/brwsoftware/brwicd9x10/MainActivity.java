package com.brwsoftware.brwicd9x10;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private int mDatabaseAction = ICD9X10Database.DATABASE_ACTION_NONE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		
        GridView gridview = (GridView) findViewById(R.id.dashboard_grid);
        gridview.setAdapter(new ImageAdapter(this));
        
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                switch(position){
                case 0:
                case 1:
                	Intent intent = new Intent(getApplicationContext(), ICDActivity.class);
                	intent.putExtra(AppValue.INTENT_EXTRA_ICDORDINAL, position);
                	startActivity(intent);
                	break;
                case 2:
                	startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                	break;
                case 3:
                	startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                	break;
                }
            }
        });
        
        // Hack to disable GridView scrolling
        gridview.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });
        
		// Perform database setup in the background
		InitializeDatabaseTask task = new InitializeDatabaseTask();
		task.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	void OnDatabaseInitialized() {
		
		//Upon first create or upgrade initialize folder related items to known values
		if (mDatabaseAction == ICD9X10Database.DATABASE_ACTION_CREATE ||
				mDatabaseAction == ICD9X10Database.DATABASE_ACTION_UPGRADE) {
			
			boolean needCommit = false;
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editPref = sharedPref.edit();
			
			//ICD9 Folder
			if(sharedPref.getInt(AppValue.PREFKEY_CURRENT_ICD9_FOLDER_ID, 0) == 0) {
				editPref.putInt(AppValue.PREFKEY_CURRENT_ICD9_FOLDER_ID, AppValue.MY_FAVORITES_FOLDER_ID);
				editPref.putString(AppValue.PREFKEY_CURRENT_ICD9_FOLDER_NAME, AppValue.MY_FAVORITES_FOLDER_NAME);
				needCommit = true;
			}
			
			//ICD10 Folder
			if(sharedPref.getInt(AppValue.PREFKEY_CURRENT_ICD10_FOLDER_ID, 0) == 0) {
				editPref.putInt(AppValue.PREFKEY_CURRENT_ICD10_FOLDER_ID, AppValue.MY_FAVORITES_FOLDER_ID);
				editPref.putString(AppValue.PREFKEY_CURRENT_ICD10_FOLDER_NAME, AppValue.MY_FAVORITES_FOLDER_NAME);
				needCommit = true;
			}
			
			if (needCommit) {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
					editPref.commit();
				} else {
					editPref.apply();
				}
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private class InitializeDatabaseTask extends AsyncTask<Void, String, Void> {

		ProgressDialog mDlg;
		ICD9X10Database.OpenHelper mDbHelper;
		RuntimeException mRuntimeException = null;

		protected class MyListener implements
				ICD9X10Database.OpenHelper.Listener {

			@Override
			public void OnProgress(String msg) {
				publishProgress(msg);
			}

			@Override
			public void OnError(String msg, Exception e) {
				//Capture exception info to be thrown later
				mRuntimeException = new RuntimeException(msg, e);
				
				publishProgress(msg);				
			}

			@Override
			public void OnAction(int action) {
				mDatabaseAction = action;
			}
		}

		private void lockScreenOrientation() {
		    int currentOrientation = getResources().getConfiguration().orientation;
		    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		    } else {
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    }
		}
		 
		private void unlockScreenOrientation() {
		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
		
		@Override
		protected void onPreExecute() {
			lockScreenOrientation();
			
			mDbHelper = ICD9X10Database.OpenHelper.getInstance(MainActivity.this);
			mDbHelper.setListener(new MyListener());

			mDlg = new ProgressDialog(MainActivity.this);
			mDlg.setTitle("Initializing");
			mDlg.setIndeterminate(true);
			mDlg.setCancelable(false);
			mDlg.show();
		}

		@Override
		protected Void doInBackground(Void... param) {
			// This will trigger first time create
			mDbHelper.getWritableDatabase();
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if (mDlg != null) {
				mDlg.setMessage(values[0]);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mDbHelper != null) {
				mDbHelper.setListener(null);
			}
			if (mDlg != null) {
				mDlg.dismiss();
			}
			unlockScreenOrientation();
			
			//If an error occurred during the create or upgrade process 
			//the database is most likely in an incomplete or corrupt state.
			//I'm going to re-throw the exception that was captured and force the app to shutdown.
			//Note: onPostExecute operates on the UI thread, so we can throw the exception here and kill the app
			if(mRuntimeException != null) {
				throw mRuntimeException;
			} else {
				OnDatabaseInitialized();
			}			
		}
	}

    static class DashboardIcon {
        final String text;
        final int imgId;
   
        public DashboardIcon(int imgId, String text) {
            super();
            this.imgId = imgId;
            this.text = text;
        }
     }
 
	static final DashboardIcon[] ICONS = {
			new DashboardIcon(R.drawable.ic_menu_icd9, "ICD9"),
			new DashboardIcon(R.drawable.ic_menu_icd10, "ICD10"),
			new DashboardIcon(R.drawable.ic_menu_help, "Help"),
			new DashboardIcon(R.drawable.ic_menu_preferences, "Settings"), };
	
	static class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return ICONS.length;
		}

		@Override
		public DashboardIcon getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		static class ViewHolder {
			public ImageView icon;
			public TextView text;
		}

		// Create a new ImageView for each item referenced by the Adapter
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				v = vi.inflate(R.layout.dashboard_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) v.findViewById(R.id.item_text);
				holder.icon = (ImageView) v.findViewById(R.id.item_img);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}

			holder.icon.setImageResource(ICONS[position].imgId);
			holder.text.setText(ICONS[position].text);

			return v;
		}
	}
}
