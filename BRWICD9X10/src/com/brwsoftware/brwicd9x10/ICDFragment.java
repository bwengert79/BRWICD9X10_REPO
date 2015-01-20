package com.brwsoftware.brwicd9x10;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public abstract class ICDFragment extends Fragment implements
		LoaderCallbacks<Cursor>, OnScrollListener
{

	private ICDAdapter mAdapter;
	private ExpandableListView mExpandableListView;
	private boolean mShowFavorites;
	private String mSearchValue;
	private String mBrowseTitle;

	private static final int LOADER_ID_PARENT = -1;
	private static final String TAG = "ICDFragment";
	private static final String BUNDLE_PARENT_ID = "parent_id";
	private int mChunkCount;
	private static final int CHUNK_SIZE = 50;
	private static final int VISIBILITY_THRESHOLD = 10;	
	private boolean mLoading;
	private int mPreviousGroupCount;

	abstract String getChildLabel();
	abstract ICDAdapter getNewAdapter();
	abstract ICDCursorLoader getNewParentCursorLoader(ICDSearch icdSearch);
	abstract ICDCursorLoader getNewChildCursorLoader(ICDSearch icdSearch);
	abstract void loadFavoriteAdd(ICDFavoriteManager.ICDFavorite icdFavorite);
	abstract void loadFavoriteDel(ICDFavoriteManager.ICDFavorite icdFavorite);

	public ICDFragment() {
		super();
	}
	
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
			label.setText(getChildLabel());
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
	
		private boolean mShowFavorites;
		private String mSearchValue;
		private int mParentID;
		private int mOffset;
		private int mLimit;
		
		public boolean isShowFavorites() {
			return mShowFavorites;
		}
		public void setShowFavorites(boolean value) {
			mShowFavorites = value;
		}
		public String getSearchValue() {
			return mSearchValue;
		}
		public void setSearchValue(String value) {
			mSearchValue = value;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		//This prevented an illegal state exception from within 
		//ICDAdapter::getChildrenCursor::getLoaderManager 
		//when the orientation changed
		setRetainInstance(true);
		
		if (mAdapter == null) {
			//Retrieve default settings
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			mShowFavorites = sharedPref.getBoolean(getResources().getString(R.string.pref_startup_fav_key), false);
			
			// Initialize the adapter
			mAdapter = getNewAdapter();

			// Initialize the loader
			beginDataLoader(false);
		} else {
			beginDataLoader(true);
		}
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
					
					ICDFavoriteManager.ICDFavorite icdFavorite = new ICDFavoriteManager.ICDFavorite();
					icdFavorite.setParentID(ExpandableListView.getPackedPositionGroup(id));
					
					if(mShowFavorites) {
						icdFavorite.setConfirmMessage("Are you sure you want to remove this code (" + strCode + ") from your favorites?");
						confirmFavoriteDel(icdFavorite);
					} else {
						icdFavorite.setConfirmMessage("Are you sure you want to add this code (" + strCode + ") to your favorites?");
						confirmFavoriteAdd(icdFavorite);
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
			icdSearch.setShowFavorites(mShowFavorites);
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
				mAdapter.setChildrenCursor(loader.getId(), null);
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
	
	void onShowFavorites(boolean showFavorites) {
		mShowFavorites = showFavorites;
		doSearch();
	}
	
	void confirmFavoriteAdd(final ICDFavoriteManager.ICDFavorite icdFavorite) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
		
		dlg.setMessage(icdFavorite.getConfirmMessage())
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								addFavorite(icdFavorite);
							}
						})
				.setNegativeButton("No", null);

		dlg.show();
	}
	
	void confirmFavoriteDel(final ICDFavoriteManager.ICDFavorite icdFavorite) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());

		dlg.setMessage(icdFavorite.getConfirmMessage())
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								delFavorite(icdFavorite);
							}
						})
				.setNegativeButton("No", null);

		dlg.create().show();
	}
	
	void addFavorite(ICDFavoriteManager.ICDFavorite icdFavorite) {
		loadFavoriteAdd(icdFavorite);
		ICDFavoriteManager.getInstance().addFavorite(icdFavorite);
	}
	
	void delFavorite(ICDFavoriteManager.ICDFavorite icdFavorite) {
		loadFavoriteDel(icdFavorite);
		ICDFavoriteManager.getInstance().delFavorite(icdFavorite);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem miAdd = menu.findItem(R.id.action_add_favorite);
		if(miAdd != null) {
			miAdd.setVisible(!mShowFavorites && !TextUtils.isEmpty(mSearchValue));
		}
		
		MenuItem miDel = menu.findItem(R.id.action_del_favorite);
		if(miDel != null) {
			miDel.setVisible(mShowFavorites);
		}

		MenuItem miFav = menu.findItem(R.id.action_favorites);
		if (miFav != null) {
			miFav.setIcon(mShowFavorites ? R.drawable.ic_menu_star_on : R.drawable.ic_menu_star_off);
			if (miFav.isCheckable()) {
				miFav.setChecked(mShowFavorites);
			}
		}

		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_add_favorite) {
			ICDFavoriteManager.ICDFavorite icdFavorite = new ICDFavoriteManager.ICDFavorite();
			icdFavorite.setConfirmMessage("This will add all codes for the current search to your favorites.\n\nDo you want to continue?");
			icdFavorite.setSearchValue(mSearchValue);
			confirmFavoriteAdd(icdFavorite);
			return true;
		} else if (itemId == R.id.action_del_favorite) {
			ICDFavoriteManager.ICDFavorite icdFavorite = new ICDFavoriteManager.ICDFavorite();
			if (TextUtils.isEmpty(mSearchValue)) {
				icdFavorite.setConfirmMessage("This will remove ALL codes from your favorites.\n\nDo you want to continue?");
			} else {
				icdFavorite.setConfirmMessage("This will remove all codes for the current search from your favorites.\n\nDo you want to continue?");
			}

			icdFavorite.setSearchValue(mSearchValue);
			confirmFavoriteDel(icdFavorite);
			return true;
		} else if (itemId == R.id.action_favorites) {
			mShowFavorites = !mShowFavorites;
			item.setIcon(mShowFavorites ? R.drawable.ic_menu_star_on : R.drawable.ic_menu_star_off);
			if (item.isCheckable()) {
				item.setChecked(mShowFavorites);
			}
			onShowFavorites(mShowFavorites);
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
}
