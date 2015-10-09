package com.brwsoftware.brwicd9x10;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.brwsoftware.brwicd9x10.AsyncQueryHandlerEx;

public abstract class ICDFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnScrollListener, FoldersDialog.FoldersDialogListener
{
	private ICDAttributes mAttributes;
	private ICDAdapter mAdapter;
	private ExpandableListView mExpandableListView;
	private String mSearchValue;
	private String mBrowseTitle;
	private boolean mFolderView;
	private int mFolderID;
	private String mFolderName;
	private AsyncQueryHandlerEx mAsyncQueryHandler;
	private MyAsyncQueryListener mAsyncQueryListener;
	private MyBroadcastReceiver mBroadcastReceiver;
	private Menu mMenu;

	private static final int LOADER_ID_PARENT = -1;
	private static final String TAG = "ICDFragment";
	private static final String BUNDLE_PARENT_ID = "parent_id";
	private int mChunkCount;
	private static final int CHUNK_SIZE = 50;
	private static final int VISIBILITY_THRESHOLD = 10;	
	private boolean mLoading;
	private int mPreviousGroupCount;
	private static final int TOKEN_ADD_FOLDER_ITEM = 1;
	private static final int TOKEN_DEL_FOLDER_ITEM = 2;
	private static final int TOKEN_ADD_FOLDER_ITEMS = 3;
	private static final int TOKEN_DEL_FOLDER_ITEMS = 4;
	private enum OnResumeAction {
		NONE,
		CURRENT_FOLDER_DELETED_NOTIFY,
		CURRENT_FOLDER_DELETED_RELOAD,
		CURRENT_FOLDER_UPDATED_RELOAD
	}
	OnResumeAction mOnResumeAction = OnResumeAction.NONE;
	
	abstract ICDAdapter getNewAdapter();
	abstract ICDCursorLoader getNewParentCursorLoader(ICDSearch icdSearch);
	abstract ICDCursorLoader getNewChildCursorLoader(ICDSearch icdSearch);
	abstract void loadFolderAdd(ICDFolder icdFolder);
	abstract void loadFolderDel(ICDFolder icdFolder);
	abstract ICDAttributes getICDAttributes();
	
	protected class ICDAdapter extends SimpleCursorTreeAdapter {

		public ICDAdapter(Context context, Cursor cursor, 
				int groupLayout, String[] groupFrom, int[] groupTo, 
				int childLayout, String[] childFrom, int[] childTo) {

			super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			int parentID = groupCursor.getInt(0);
			int pos = groupCursor.getPosition();
			AppLog.d(TAG, "getChildrenCursor: groupCursor.getInt=%d groupCursor.getPosition=%d", parentID, pos);
			Bundle bundle = new Bundle();
			bundle.putInt(BUNDLE_PARENT_ID, parentID);
			getLoaderManager().restartLoader(pos, bundle, ICDFragment.this);
			return null;
		}

		@Override
		public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
			AppLog.d(TAG, "newChildView");
			View viewChild = super.newChildView(context, cursor, isLastChild, parent);
			TextView label = (TextView) viewChild.findViewById(R.id.icd_label);
			label.setText(mAttributes.getChildLabel());
			return viewChild;
		}
	}
	
	protected class ICDCursorLoader {
		private String mQueryDesc;
		private CursorLoader mCursorLoader;
		public String getQueryDesc() {
			return mQueryDesc;
		}
		public CursorLoader getCursorLoader() {
			return mCursorLoader;
		}
		public void setQueryDesc(String value) {
			mQueryDesc = value;
		}
		public void setCursorLoader(CursorLoader value) {
			mCursorLoader = value;
		}
	}
	
	protected class ICDSearch {
	
		private String mSearchValue;
		private String mFolderName;
		private int mFolderID;
		private int mParentID;
		private int mOffset;
		private int mLimit;
		
		public boolean isFolderView() {
			return (mFolderID != 0);
		}
		public String getSearchValue() {
			return mSearchValue;
		}
		public void setSearchValue(String value) {
			mSearchValue = value;
		}
		public String getFolderName() {
			return mFolderName;
		}
		public void setFolderName(String value) {
			mFolderName = value;
		}
		public int getFolderID() {
			return mFolderID;
		}
		public void setFolderID(int value) {
			mFolderID = value;
		}
		public int getParentID() {
			return mParentID;
		}
		public void setParentID(int value) {
			mParentID = value;
		}
		public int getOffset() {
			return mOffset;
		}
		public void setOffset(int offset) {
			mOffset = offset;
		}
		public int getLimit() {
			return mLimit;
		}
		public void setLimit(int limit) {
			mLimit = limit;
		}
	}
	
	protected class ICDFolder {
		private Uri mUri;
		private int mItemID;
		private int mFolderID;
		private String mSearchValue;
		private String mSelection;
		private String[] mSelectionArgs;
		private ContentValues mContentValues;
		private String mConfirmMessage;
		
		public Uri getUri() {
			return mUri;
		}
		public void setUri(Uri value) {
			mUri = value;
		}
		public int getItemID() {
			return mItemID;
		}
		public void setItemID(int value) {
			mItemID = value;
		}
		public int getFolderID() {
			return mFolderID;
		}
		public void setFolderID(int value) {
			mFolderID = value;
		}
		public String getSearchValue() {
			return mSearchValue;
		}
		public void setSearchValue(String value) {
			mSearchValue = value;
		}
		public String getSelection() {
			return mSelection;
		}
		public void setSelection(String value) {
			mSelection = value;
		}
		public String[] getSelectionArgs() {
			return mSelectionArgs;
		}
		public void setSelectionArgs(String[] value) {
			mSelectionArgs = value;
		}
		public ContentValues getContentValues() {
			return mContentValues;
		}
		public void setContentValues(ContentValues value) {
			mContentValues = value;
		}
		public String getConfirmMessage() {
			return mConfirmMessage;
		}
		public void setConfirmMessage(String value) {
			mConfirmMessage = value;
		}
		public boolean IsSingleItem() {return mItemID != 0;}
	}
	
	protected class ICDAttributes {
		private int mICDType;
		private String mChildLabel;
		private String mPrefKeyFolderID;
		private String mPrefKeyFolderName;
		
		public int getICDType() {
			return mICDType;
		}
		public void setICDType(int value) {
			mICDType = value;
		}
		public String getChildLabel() {
			return mChildLabel;
		}
		public void setChildLabel(String value) {
			mChildLabel = value;
		}
		public String getPrefKeyFolderID() {
			return mPrefKeyFolderID;
		}
		public void setPrefKeyFolderID(String value) {
			mPrefKeyFolderID = value;
		}
		public String getPrefKeyFolderName() {
			return mPrefKeyFolderName;
		}
		public void setPrefKeyFolderName(String value) {
			mPrefKeyFolderName = value;
		}
	}
	
	protected class MyAsyncQueryListener implements AsyncQueryHandlerEx.AsyncQueryListener {

		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {	
		}

		@Override
		public void onInsertComplete(int token, Object cookie, Uri uri) {
			switch(token) {
			case TOKEN_ADD_FOLDER_ITEM:
				AppLog.d(TAG, "Folder item added");
				break;
			case TOKEN_ADD_FOLDER_ITEMS:
				AppLog.d(TAG, "Folder items added");
				break;
			default:
				break;
			}
		}

		@Override
		public void onUpdateComplete(int token, Object cookie, int result) {
		}

		@Override
		public void onDeleteComplete(int token, Object cookie, int result) {
			switch(token) {
			case TOKEN_DEL_FOLDER_ITEM:
				AppLog.d(TAG, "Folder item deleted");
				break;
			case TOKEN_DEL_FOLDER_ITEMS:
				AppLog.d(TAG, "Folder items deleted");
				if(cookie != null && cookie.getClass() == ICDFolder.class) {
					ICDFolder icdFolder = (ICDFolder)cookie;
					if(!icdFolder.IsSingleItem()) {
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
						if(sharedPref.getBoolean(getResources().getString(R.string.pref_reset_after_remove_key), false)) {
							resetSearch();
						}
					}
				}
				break;
			default:
				break;
			}			
		}		
	}

	private final class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AppValue.INTENT_ACTION_FOLDER_DELETE)
					&& ICDFragment.this.mAttributes.getICDType() == intent.getIntExtra(AppValue.INTENT_EXTRA_ICDTYPE, 0)
					&& ICDFragment.this.getCurrentFolderID() == intent.getIntExtra(AppValue.INTENT_EXTRA_FOLDER_ID, 0)) {
				ICDFragment.this.OnCurrentFolderDeleted();
			} else if (intent.getAction().equals(AppValue.INTENT_ACTION_FOLDER_UPDATE)
					&& ICDFragment.this.mAttributes.getICDType() == intent.getIntExtra(AppValue.INTENT_EXTRA_ICDTYPE, 0)
					&& ICDFragment.this.getCurrentFolderID() == intent.getIntExtra(AppValue.INTENT_EXTRA_FOLDER_ID, 0)) {
				String newFolderName = intent.getStringExtra(AppValue.INTENT_EXTRA_FOLDER_NAME);
				if(newFolderName != null){
					ICDFragment.this.OnCurrentFolderUpdated(newFolderName);
				}
			}
		}
	}
    
	public ICDFragment() {
		super();
	}
	
	protected boolean isFolderView() {
		return mFolderView;
	}
	protected int getActiveFolderID() {
		return mFolderView ? mFolderID : 0;
	}
	protected String getActiveFolderName() {
		return mFolderView ? mFolderName : null;
	}
	protected int getCurrentFolderID() {
		return mFolderID;
	}
	protected String getCurrentFolderName() {
		return mFolderName;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		
		//This prevented an illegal state exception from within 
		//ICDAdapter::getChildrenCursor::getLoaderManager 
		//when the orientation changed
		setRetainInstance(true);
		
		if(mAttributes == null) {
			mAttributes = getICDAttributes();
		}
		
		if(mAsyncQueryHandler == null) {
			mAsyncQueryListener = new MyAsyncQueryListener();
			mAsyncQueryHandler = new AsyncQueryHandlerEx(getActivity().getContentResolver(), mAsyncQueryListener);
		}
		
		if(mBroadcastReceiver == null) {
			mBroadcastReceiver = new MyBroadcastReceiver();
		}
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(AppValue.INTENT_ACTION_FOLDER_DELETE);
		intentFilter.addAction(AppValue.INTENT_ACTION_FOLDER_UPDATE);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
		
		if (mAdapter == null) {
			//Retrieve default settings
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			mFolderView = sharedPref.getBoolean(getResources().getString(R.string.pref_startup_fav_key), false);			
			mFolderID = sharedPref.getInt(mAttributes.mPrefKeyFolderID, 0);
			mFolderName = sharedPref.getString(mAttributes.mPrefKeyFolderName, null);
			
			// Initialize the adapter
			mAdapter = getNewAdapter();

			// Initialize the loader
			beginDataLoader(false);
		} else {
			beginDataLoader(true);
		}
	}
	
	@Override
	public void onDestroy() {
		if(mBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
		}
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
		switch (mOnResumeAction) {
		case CURRENT_FOLDER_UPDATED_RELOAD:
			beginDataLoader(true);
			break;
		case CURRENT_FOLDER_DELETED_RELOAD: {
			beginDataLoader(true);
			AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
			dlg.setMessage(
					"Folder View has been turned off because the current folder was deleted.")
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, null)
					.show();
			break;
		}
		case CURRENT_FOLDER_DELETED_NOTIFY: {
			AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
			dlg.setMessage("The current folder has been deleted.\n\nYou will need to select a new current folder before using the Folder View option.")
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, null)
					.show();
			break;
		}
		default:
			break;
		}

		mOnResumeAction = OnResumeAction.NONE;
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_browse, container, false);
		
		mExpandableListView = (ExpandableListView) rootView.findViewById(R.id.exp_listview);
		mExpandableListView.setAdapter(mAdapter);
		mExpandableListView.setOnScrollListener(this);
		
		mExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					TextView code = (TextView)view.findViewById(R.id.icd_code);
					
					String strCode;
					if (code != null) {
						strCode = (String) code.getText();
					} else {
						strCode = "current selection";
					}					
					
					ICDFolder icdFolder = new ICDFolder();
					icdFolder.setFolderID(getCurrentFolderID());
					icdFolder.setItemID(ExpandableListView.getPackedPositionGroup(id));
					
					if(isFolderView()) {
						icdFolder.setConfirmMessage("Are you sure you want to remove this code (" + strCode + ") from the current folder (" + getCurrentFolderName() + ")?");
						confirmDeleteFromFolder(icdFolder);
					} else {
						icdFolder.setConfirmMessage("Are you sure you want to add this code (" + strCode + ") to the current folder (" + getCurrentFolderName() + ")?");
						confirmAddToFolder(icdFolder);
					}
				}
				return true;
			}
		});
		
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final TextView tvTitle = (TextView) getActivity().findViewById(R.id.browse_title);
		if(tvTitle != null) tvTitle.setText(mBrowseTitle);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int idLoader, Bundle bundle) {
		AppLog.d(TAG, "onCreateLoader - idLoader=%d", idLoader);
		ICDCursorLoader icdCursorLoader = new ICDCursorLoader();
		ICDSearch icdSearch = new ICDSearch();

		if (idLoader == LOADER_ID_PARENT) {
			icdSearch.setSearchValue(mSearchValue);
			icdSearch.setFolderID(getActiveFolderID());
			icdSearch.setFolderName(getActiveFolderName());
			icdSearch.setLimit(CHUNK_SIZE * (mChunkCount + 1));
			icdCursorLoader = getNewParentCursorLoader(icdSearch);
			mBrowseTitle = icdCursorLoader.getQueryDesc();
			final TextView tvTitle = (TextView) getActivity().findViewById(R.id.browse_title);
			if(tvTitle != null) tvTitle.setText(mBrowseTitle);
		} else if (idLoader > LOADER_ID_PARENT) {
			icdSearch.setParentID(bundle.getInt(BUNDLE_PARENT_ID));
			icdCursorLoader = getNewChildCursorLoader(icdSearch); 
		} else {
			throw new IllegalArgumentException("Unknown loader id: " + idLoader);
		}
		return icdCursorLoader.getCursorLoader();
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		AppLog.d(TAG, "onLoadFinished: cursor id=%d", loader.getId());
		if (loader.getId() == LOADER_ID_PARENT) {
			mAdapter.setGroupCursor(data);
			if(mChunkCount == 0) {
				mExpandableListView.setSelection(0);
			}
			mLoading = false;
		} else {
			mAdapter.setChildrenCursor(loader.getId(), data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		AppLog.d(TAG, "onLoaderReset: cursor id=%d", loader.getId());
		if (loader.getId() == LOADER_ID_PARENT) {
			mAdapter.setGroupCursor(null);
		} else {
			if (mAdapter.getGroupCount() > 0) {
				try {
					//Crash reports indicated NullPointerException being thrown from within setChildrenCursor.
					//Seems to be happening on first-run scenarios.
					mAdapter.setChildrenCursor(loader.getId(), null);
				} catch (NullPointerException e) {
					AppLog.w(TAG, "setChildrenCursor - NullPointerException: " + e.getMessage());
				}
			}
		}
	}
	
	void doSearch() {
		//changeCursor(null) will clear the group cursor as well as any existing child cursors.
		//This was necessary as child cursors will be left over and would appear in the
		//listview under certain circumstances.
		mAdapter.changeCursor(null);
		
		//Search the new search
		mChunkCount = 0;
		mPreviousGroupCount = 0;
		beginDataLoader(true);
	}
	
	void loadMoreData() {
		mChunkCount += 1;
		beginDataLoader(true);
	}
	
	void resetSearch() {
		mSearchValue = null;
		doSearch();
	}
	
	void beginDataLoader(boolean restart) {
		mLoading = true;
		if(restart) {
			getLoaderManager().restartLoader(LOADER_ID_PARENT, null, this);
		} else {
			getLoaderManager().initLoader(LOADER_ID_PARENT, null, this);
		}
	}
	
	void onNewSearch(String searchValue) {
		if (TextUtils.equals("*", searchValue)) {
			mSearchValue = null;
		} else if (TextUtils.equals("\\*", searchValue)) {
			mSearchValue = "*";
		} else {
			if(searchValue.length() != TextUtils.getTrimmedLength(searchValue)) {
				mSearchValue = searchValue.trim();
			} else {
				mSearchValue = searchValue;
			}
		}
		doSearch();
	}
	
	void onFolderViewSelected(MenuItem item) {
		if(!isFolderView() && !hasCurrentFolder()) {
			return;
		}
		
		//Toggle FolderView
		mFolderView = !isFolderView();
		setFolderViewMenuItem(item);

		doSearch();
	}
	
	void onSetCurrentFolder() {
		FoldersDialog dialog = new FoldersDialog();
        dialog.initialize(mAttributes.getICDType(), this);
        dialog.show(getFragmentManager(), "FoldersDialogFragment");	
	}
	
	void OnCurrentFolderDeleted() {
		mOnResumeAction = isFolderView() ? OnResumeAction.CURRENT_FOLDER_DELETED_RELOAD : OnResumeAction.CURRENT_FOLDER_DELETED_NOTIFY;
		mFolderID = 0;
		mFolderName = null;
		mFolderView = false;		
		storeCurrentFolder();
		setFolderViewMenuItem();
	}
	
	void OnCurrentFolderUpdated(String newFolderName) {
		if(isFolderView()) {
			mOnResumeAction = OnResumeAction.CURRENT_FOLDER_UPDATED_RELOAD;
		}
		mFolderName = newFolderName;
		storeCurrentFolder();		
	}
	
	@Override
	public void onFolderSelected(int folderID, String folderName) {
		mFolderID = folderID;
		mFolderName = folderName;
		storeCurrentFolder();

		if (isFolderView()) {
			doSearch();
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			if (sharedPref.getBoolean(getResources().getString(R.string.pref_folder_view_after_current_key),false)) {
				mFolderView = true;
				setFolderViewMenuItem();
				doSearch();
			} else {
				Toast.makeText(getActivity(),
						"The current folder has been set to " + folderName,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	void storeCurrentFolder()
	{
		SharedPreferences.Editor editPref = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		
		if(mFolderID == 0) {
			editPref.remove(mAttributes.getPrefKeyFolderID());
			editPref.remove(mAttributes.mPrefKeyFolderName);
		
		} else {
			editPref.putInt(mAttributes.getPrefKeyFolderID(), mFolderID);
			editPref.putString(mAttributes.mPrefKeyFolderName, mFolderName);			
		}
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
			editPref.commit();
		} else {
			editPref.apply();
		}		
	}
	
	boolean hasCurrentFolder() {
		if (getCurrentFolderID() != 0) return true;
		
		AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
		dlg.setMessage("Before this function can be performed you need to set your current folder."
				+ "\n\nThis can be done from the menu."
				+ "\n\nNote: This only needs to be done once. Your selection will be remembered for later.")
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, null);

		dlg.show();		
		return false;
	}
	
	void confirmAddToFolder(final ICDFolder icdFolder) {
		if (hasCurrentFolder()) {
			AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
			dlg.setMessage(icdFolder.getConfirmMessage())
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									doAddToFolder(icdFolder);
								}
							})
					.setNegativeButton("No", null);

			dlg.show();
		}
	}
	
	void confirmDeleteFromFolder(final ICDFolder icdFolder) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());

		dlg.setMessage(icdFolder.getConfirmMessage())
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								doDeleteFromFolder(icdFolder);
							}
						})
				.setNegativeButton("No", null);

		dlg.create().show();
	}
	
	void doAddToFolder(ICDFolder icdFolder) {
		loadFolderAdd(icdFolder);
		AppLog.d(TAG, "Adding to folder");
		mAsyncQueryHandler.startInsert(icdFolder.IsSingleItem() ? TOKEN_ADD_FOLDER_ITEM : TOKEN_ADD_FOLDER_ITEMS, icdFolder, icdFolder.getUri(), icdFolder.getContentValues());
	}
	
	void doDeleteFromFolder(ICDFolder icdFolder) {
		loadFolderDel(icdFolder);
		AppLog.d(TAG, "Deleting from folder");
		mAsyncQueryHandler.startDelete(icdFolder.IsSingleItem() ? TOKEN_DEL_FOLDER_ITEM : TOKEN_DEL_FOLDER_ITEMS, icdFolder, icdFolder.getUri(), icdFolder.getSelection(), icdFolder.getSelectionArgs());
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem miAdd = menu.findItem(R.id.action_add_to_folder);
		if(miAdd != null) {
			miAdd.setVisible(!isFolderView() && !TextUtils.isEmpty(mSearchValue));
		}
		
		MenuItem miDel = menu.findItem(R.id.action_del_from_folder);
		if(miDel != null) {
			miDel.setVisible(isFolderView());
		}

		MenuItem miFav = menu.findItem(R.id.action_folder_view);
		if (miFav != null) {
			setFolderViewMenuItem(miFav);
		}

		mMenu = menu;
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_add_to_folder) {
			ICDFolder icdFolder = new ICDFolder();
			icdFolder.setConfirmMessage("This will add all codes for the current search to the current folder (" + getCurrentFolderName() + ").\n\nDo you want to continue?");
			icdFolder.setSearchValue(mSearchValue);
			icdFolder.setFolderID(getCurrentFolderID());
			confirmAddToFolder(icdFolder);
			return true;
		} else if (itemId == R.id.action_del_from_folder) {
			ICDFolder icdFolder = new ICDFolder();
			if (TextUtils.isEmpty(mSearchValue)) {
				icdFolder.setConfirmMessage("This will remove ALL codes from the current folder (" + getCurrentFolderName() + ").\n\nDo you want to continue?");
			} else {
				icdFolder.setConfirmMessage("This will remove all codes for the current search from the current folder (" + getCurrentFolderName() + ").\n\nDo you want to continue?");
			}

			icdFolder.setSearchValue(mSearchValue);
			icdFolder.setFolderID(getCurrentFolderID());
			confirmDeleteFromFolder(icdFolder);
			return true;
		} else if (itemId == R.id.action_folder_view) {
			onFolderViewSelected(item);
			return true;
		} else if (itemId == R.id.action_set_current_folder) {
			onSetCurrentFolder();
			return true;
		} else if (itemId == R.id.action_reset_search) {
			resetSearch();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int groupCount = -1;
		if(mAdapter != null) {
			groupCount = mAdapter.getGroupCount();
		}
 		
		//Use the readily available values as a quick check
        if (!mLoading && 
        		groupCount > 0 && 
        		groupCount >= CHUNK_SIZE && 
        		groupCount > mPreviousGroupCount &&
        		(totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBILITY_THRESHOLD)) {
        	
        	//If in the ball park, check actual group related values
        	int visibleGroupCount = getVisibleGroupCount();
        	int firstVisibleGroup = getFirstVisibleGroup();
        	if((groupCount - visibleGroupCount) <= (firstVisibleGroup + VISIBILITY_THRESHOLD)) {
	          	AppLog.d(TAG, "onScroll - loading more data - firstVisibleItem=%d visibleItemCount=%d totalItemCount=%d groupCount=%d visibleGroupCount=%d firstVisibleGroup=%d",
	          			firstVisibleItem, visibleItemCount, totalItemCount, 
	          			groupCount, visibleGroupCount, firstVisibleGroup);
	          	mPreviousGroupCount = groupCount;
	        	loadMoreData();
        	}
        }		
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Nothing to do 		
	}
	
	int getVisibleGroupCount() {
		int count = 0;
		int posCur = mExpandableListView.getFirstVisiblePosition();
	    int posLast = mExpandableListView.getLastVisiblePosition();

		while (posCur < posLast) {
			long packedPosition = mExpandableListView.getExpandableListPosition(posCur);
			if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				count++;
			}
			posCur++;
		}
 
        return count;	    
	}
	
	int getFirstVisibleGroup() {
		int posFirst = -1;
		int posCur = mExpandableListView.getFirstVisiblePosition();
	    int posLast = mExpandableListView.getLastVisiblePosition();

		while (posCur < posLast) {
			long packedPosition = mExpandableListView.getExpandableListPosition(posCur);
			if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				posFirst = posCur;
				break;
			}
			posCur++;
		}

        return posFirst;	    
	}
	
	void setFolderViewMenuItem() {
		if(mMenu != null)
		{
			MenuItem miFav = mMenu.findItem(R.id.action_folder_view);
			if (miFav != null) {
				setFolderViewMenuItem(miFav);
			}
		}	
	}
	
	void setFolderViewMenuItem(MenuItem item) {
		item.setIcon(isFolderView() ? R.drawable.ic_menu_star_on : R.drawable.ic_menu_star_off);
		if (item.isCheckable()) {
			item.setChecked(isFolderView());
		}
	}
}
