package com.brwsoftware.brwicd9x10;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


public final class ICDFavoriteManager {
	
	private static final String TAG = "ICDFavoriteManager";
	private static final ICDFavoriteManager INSTANCE = new ICDFavoriteManager();

	private int mICD9FavGroupID;
	private int mICD10FavGroupID;
	private MyAsyncQueryHandler mAsyncQueryHandler;
	private ContentResolver mContentResolver;
	
	private static final int TOKEN_INIT_ICD9 = 1;
	private static final int TOKEN_INIT_ICD10 = 2;
	private static final int TOKEN_ADD_FAVORITE = 3;
	private static final int TOKEN_DEL_FAVORITE = 4;
	
	private static final class MyAsyncQueryHandler extends AsyncQueryHandler {
		private final WeakReference<ICDFavoriteManager> mManager;
		public MyAsyncQueryHandler(ICDFavoriteManager manager, ContentResolver cr) {
			super(cr);
			mManager = new WeakReference<ICDFavoriteManager>(manager);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			ICDFavoriteManager theManager = mManager.get();
			switch (token) {
			case TOKEN_INIT_ICD9:
				if (cursor.moveToFirst()) {
					AppLog.d(TAG, "Init ICD9Group complete");
					theManager.mICD9FavGroupID = cursor.getInt(0);
				}
				break;
			case TOKEN_INIT_ICD10:
				if (cursor.moveToFirst()) {
					AppLog.d(TAG, "Init ICD10Group complete");
					theManager.mICD10FavGroupID = cursor.getInt(0);
				}
				break;
			case TOKEN_ADD_FAVORITE:
				break;
			case TOKEN_DEL_FAVORITE:
				break;			
			}

			cursor.close();
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			//ICDFavoriteManager theManager = mManager.get();
			if(token == TOKEN_ADD_FAVORITE) {
				AppLog.d(TAG, "Favorite added");
			}
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			//ICDFavoriteManager theManager = mManager.get();
			if(token == TOKEN_DEL_FAVORITE) {
				AppLog.d(TAG, "Favorite deleted");
			}
		}
	}

	private ICDFavoriteManager() {
	}

	public static ICDFavoriteManager getInstance() {
		return INSTANCE;
	}

	
	public static final class ICDFavorite {
		private Uri mUri;
		private int mParentID;
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
		public int getParentID() {
			return mParentID;
		}
		public void setParentID(int value) {
			mParentID = value;
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
		public boolean IsSingleItem() {return mParentID != 0;}
	}

	public int getICD9FavGroupID() {
		return mICD9FavGroupID;
	}

	public int getICD10FavGroupID() {
		return mICD10FavGroupID;
	}	
	
	public void initialize(ContentResolver cr) {
		AppLog.d(TAG, "Initializing");
		mContentResolver = cr;
		if(mContentResolver == null) {
			throw new IllegalArgumentException("ICDFavoriteManager content resolver can not be null");
		}		
		mAsyncQueryHandler = new MyAsyncQueryHandler(this, mContentResolver);
		mAsyncQueryHandler.startQuery(TOKEN_INIT_ICD9, null, ICD9X10ContentProvider.CONTENT_URI_ICD9FAV_GROUP, null, null, null, null);
		mAsyncQueryHandler.startQuery(TOKEN_INIT_ICD10, null, ICD9X10ContentProvider.CONTENT_URI_ICD10FAV_GROUP, null, null, null, null);
	}

	public void addFavorite(ICDFavorite icdFavorite) {
		AppLog.d(TAG, "Adding favorite");
		if(mContentResolver == null) {
			throw new IllegalStateException("ICDFavoriteManager has not been properly initialized");
		}
		mAsyncQueryHandler = new MyAsyncQueryHandler(this, mContentResolver);
		mAsyncQueryHandler.startInsert(TOKEN_ADD_FAVORITE, null, icdFavorite.getUri(), icdFavorite.getContentValues());	
	}

	public void delFavorite(ICDFavorite icdFavorite) {
		AppLog.d(TAG, "Deleting favorite");
		if(mContentResolver == null) {
			throw new IllegalStateException("ICDFavoriteManager has not been properly initialized");
		}
		mAsyncQueryHandler = new MyAsyncQueryHandler(this, mContentResolver);
		mAsyncQueryHandler.startDelete(TOKEN_DEL_FAVORITE, null, icdFavorite.getUri(), icdFavorite.getSelection(), icdFavorite.getSelectionArgs());	
	}
}