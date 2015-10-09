package com.brwsoftware.brwicd9x10;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;

import com.brwsoftware.brwicd9x10.AsyncQueryHandlerEx;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10FOLDER;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD9FOLDER;

public class FoldersActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;
    ListView mListView;
    int mICDType;
    AsyncQueryHandlerEx mAsyncQueryHandler;
    MyAsyncQueryListener mAsyncQueryListener;
    
    protected class MyAsyncQueryListener implements AsyncQueryHandlerEx.AsyncQueryListener {

		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		}

		@Override
		public void onInsertComplete(int token, Object cookie, Uri uri) {
		}

		@Override
		public void onUpdateComplete(int token, Object cookie, int result) {
			if(token == AppValue.ICDTYPE_9 || token == AppValue.ICDTYPE_10) {
				if(cookie != null && cookie.getClass() == MyAsyncCookie.class) {
					MyAsyncCookie myCookie = (MyAsyncCookie)cookie;
					Intent intent = new Intent(AppValue.INTENT_ACTION_FOLDER_UPDATE);
					intent.putExtra(AppValue.INTENT_EXTRA_ICDTYPE, token);
					intent.putExtra(AppValue.INTENT_EXTRA_FOLDER_ID, myCookie.getFolderID());
					intent.putExtra(AppValue.INTENT_EXTRA_FOLDER_NAME, myCookie.getFolderName());
					LocalBroadcastManager.getInstance(myCookie.getContext()).sendBroadcast(intent);
				}
			}
		}

		@Override
		public void onDeleteComplete(int token, Object cookie, int result) {
			if(token == AppValue.ICDTYPE_9 || token == AppValue.ICDTYPE_10) {
				if(cookie != null && cookie.getClass() == MyAsyncCookie.class) {
					MyAsyncCookie myCookie = (MyAsyncCookie)cookie;
					Intent intent = new Intent(AppValue.INTENT_ACTION_FOLDER_DELETE);
					intent.putExtra(AppValue.INTENT_EXTRA_ICDTYPE, token);
					intent.putExtra(AppValue.INTENT_EXTRA_FOLDER_ID, myCookie.getFolderID());
					LocalBroadcastManager.getInstance(myCookie.getContext()).sendBroadcast(intent);
				}
			}
		}
    }
    
    
    private class MyAsyncCookie {
		private int mFolderID;
		private String mFolderName;
		private Context mContext;
		
    	public MyAsyncCookie(int folderID, String folderName, Context ctx) {
			super();
			mFolderID = folderID;
			mFolderName = folderName;
			mContext = ctx;
		}

		public MyAsyncCookie(int folderID, Context ctx) {
			super();
			mFolderID = folderID;
			mContext = ctx;
		}

		public MyAsyncCookie(String folderName, Context ctx) {
			super();
			mFolderName = folderName;
			mContext = ctx;
		}
		
		public int getFolderID() {
			return mFolderID;
		}

		public String getFolderName() {
			return mFolderName;
		}

		public Context getContext() {
			return mContext;
		}
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folders);
		
		Intent intent = getIntent();
		mICDType = intent.getIntExtra(AppValue.INTENT_EXTRA_ICDTYPE, AppValue.ICDTYPE_9);
		
		if (mICDType == AppValue.ICDTYPE_10) {
			setTitle("ICD10 Folders");
			mAdapter = new SimpleCursorAdapter(getBaseContext(),
					R.layout.folder_item, null,
					new String[] { ICD10FOLDER.NAME },
					new int[] { R.id.folder_name }, 0);
		} else {
			setTitle("ICD9 Folders");
			mAdapter = new SimpleCursorAdapter(getBaseContext(),
					R.layout.folder_item, null,
					new String[] { ICD9FOLDER.NAME },
					new int[] { R.id.folder_name }, 0);
		}
		
		mListView = (ListView) findViewById(R.id.folder_listview);
		mListView.setAdapter(mAdapter);
		
		registerForContextMenu(mListView);
		
		//ActionBar setup
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setDisplayShowTitleEnabled(false);

		mAsyncQueryListener = new MyAsyncQueryListener();
		mAsyncQueryHandler = new AsyncQueryHandlerEx(getContentResolver(), mAsyncQueryListener);
        getSupportLoaderManager().initLoader(mICDType, null, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.folder_ctx_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int itemId = item.getItemId();
		if (itemId == R.id.action_edit_folder) {
			onFolderEdit((int)info.id);
			return true;
		} else if (itemId == R.id.action_delete_folder) {
			onFolderDelete((int)info.id);
			return true;
		}
		return super.onContextItemSelected(item);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.folder_menu, menu);	    
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_add_folder) {
			onFolderAdd();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader;
		if (arg0 == AppValue.ICDTYPE_10) {
			cursorLoader = new CursorLoader(this,
					ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS,
					new String[] { ICD10FOLDER._ID, ICD10FOLDER.NAME }, null,
					null, null);
		} else {
			cursorLoader = new CursorLoader(this,
					ICD9X10ContentProvider.CONTENT_URI_ICD9FOLDERS,
					new String[] { ICD9FOLDER._ID, ICD10FOLDER.NAME }, null,
					null, null);
		}

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
	
	private void onFolderAdd(){
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.folder_input, null);
		
		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.folder_name);
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setView(promptsView)
			.setTitle("New Folder")
			.setMessage("Enter Folder Name")
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								doFolderAdd(userInput.getText().toString());
							}
						})
				.setNegativeButton("Cancel", null);

		dlg.show();
	}
	
	private void doFolderAdd(String folderName){
		ContentValues cv = new ContentValues();
		Uri uri;
		if (mICDType == AppValue.ICDTYPE_10) {
			uri = ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS;
			cv.put(ICD10FOLDER.NAME, folderName);
		} else {
			uri = ICD9X10ContentProvider.CONTENT_URI_ICD9FOLDERS;
			cv.put(ICD9FOLDER.NAME, folderName);
		}		

		mAsyncQueryHandler.startInsert(mICDType, new MyAsyncCookie(folderName, this), uri, cv);
	}
	
	private void onFolderDelete(final int idFolder){
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setMessage("Are you sure you want to delete this folder?")
		.setCancelable(false)
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						doFolderDelete(idFolder);
					}
				})
		.setNegativeButton("No", null);

		dlg.show();
	}
	
	private void doFolderDelete(int idFolder) {
		Uri uri;
		if (mICDType == AppValue.ICDTYPE_10) {
			uri = ContentUris.withAppendedId(ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS, idFolder);
		} else {
			uri = ContentUris.withAppendedId(ICD9X10ContentProvider.CONTENT_URI_ICD9FOLDERS, idFolder);
		}
		
		mAsyncQueryHandler.startDelete(mICDType, new MyAsyncCookie(idFolder, this), uri, null, null);
	}
	
	private void onFolderEdit(final int idFolder) {
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.folder_input, null);
		
		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.folder_name);
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		
		dlg.setView(promptsView)
			.setTitle("Rename Folder")
			.setMessage("Enter Folder Name")
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								doFolderEdit(idFolder, userInput.getText().toString());
							}
						})
				.setNegativeButton("Cancel", null);

		dlg.show();
	}
	
	private void doFolderEdit(int idFolder, String name) {
		ContentValues cv = new ContentValues();
		Uri uri;
		if (mICDType == AppValue.ICDTYPE_10) {
			uri = ContentUris.withAppendedId(ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS, idFolder);
			cv.put(ICD10FOLDER.NAME, name);
		} else {
			uri = ContentUris.withAppendedId(ICD9X10ContentProvider.CONTENT_URI_ICD9FOLDERS, idFolder);
			cv.put(ICD9FOLDER.NAME, name);
		}		

		mAsyncQueryHandler.startUpdate(mICDType, new MyAsyncCookie(idFolder, name, this), uri, cv, null, null);		
	}
}
