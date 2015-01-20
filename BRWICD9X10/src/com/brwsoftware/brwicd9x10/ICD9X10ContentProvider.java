package com.brwsoftware.brwicd9x10;

import java.util.Map.Entry;

import com.brwsoftware.brwicd9x10.ICD9X10Database.*;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

public final class ICD9X10ContentProvider extends ContentProvider {

	public static final String INSERT_FROM_SELECT = "_use_insert_with_select_";
    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    
	private static final class URI_PATH {

		static final String ICD9 = "icd9";
		static final String ICD9FAV = "icd9fav";
		static final String ICD9X10 = "icd9x10";
		static final String ICD10 = "icd10";
		static final String ICD10FAV = "icd10fav";
		static final String ICD10X9 = "icd10x9";
		static final String GROUP = "group";
	}

	private static final class URI_CODE {
		static final int ICD9_LIST = 100;
		static final int ICD9X10_LIST = 110;
		static final int ICD9FAV_LIST = 120;
		static final int ICD9FAV_ID = 130;
		static final int ICD9FAV_GROUP_ID = 140;
		static final int ICD10_LIST = 200;
		static final int ICD10X9_LIST = 210;
		static final int ICD10FAV_LIST = 220;
		static final int ICD10FAV_ID = 230;
		static final int ICD10FAV_GROUP_ID = 240;
	}
	
	private static final String AUTHORITY = "com.brwsoftware.brwicd9x10.contentprovider";

	public static final Uri CONTENT_URI_ICD9 = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9);
	public static final Uri CONTENT_URI_ICD9FAV = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9FAV);
	public static final Uri CONTENT_URI_ICD9X10 = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9X10);
	public static final Uri CONTENT_URI_ICD10 = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10);
	public static final Uri CONTENT_URI_ICD10FAV = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10FAV);
	public static final Uri CONTENT_URI_ICD10X9 = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10X9);
	public static final Uri CONTENT_URI_ICD9FAV_GROUP = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9FAV + "/" + URI_PATH.GROUP);
	public static final Uri CONTENT_URI_ICD10FAV_GROUP = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10FAV + "/" + URI_PATH.GROUP);
	
	/*
	 * public static final String CONTENT_ICD9 =
	 * ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ICD9.TABLE_NAME; public
	 * static final String CONTENT_ICD10 = ContentResolver.CURSOR_DIR_BASE_TYPE
	 * + "/" + ICD10.TABLE_NAME; public static final String CONTENT_ITEM_ICD9 =
	 * ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ICD9.TABLE_NAME; public
	 * static final String CONTENT_ITEM_ICD10 =
	 * ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ICD10.TABLE_NAME;
	 * 
	 * public static final String CONTENT_ICD9X10 =
	 * ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ICD9GEM.TABLE_NAME; public
	 * static final String CONTENT_ICD10X9 =
	 * ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + ICD10GEM.TABLE_NAME; public
	 * static final String CONTENT_ITEM_ICD9X10 =
	 * ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ICD9GEM.TABLE_NAME; public
	 * static final String CONTENT_ITEM_ICD10X9 =
	 * ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + ICD10GEM.TABLE_NAME;
	 */
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9, URI_CODE.ICD9_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FAV, URI_CODE.ICD9FAV_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9X10, URI_CODE.ICD9X10_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10, URI_CODE.ICD10_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FAV, URI_CODE.ICD10FAV_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10X9, URI_CODE.ICD10X9_LIST);

		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FAV + "/#", URI_CODE.ICD9FAV_ID);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FAV + "/#", URI_CODE.ICD10FAV_ID);

		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FAV + "/" + URI_PATH.GROUP, URI_CODE.ICD9FAV_GROUP_ID);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FAV + "/" + URI_PATH.GROUP, URI_CODE.ICD10FAV_GROUP_ID);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void insertFromSelect(Uri uri, ContentValues values) {
		String selStatment = values.getAsString(INSERT_FROM_SELECT);
		if(selStatment == null) {
			throw new IllegalArgumentException("Invalid use of insertFromSelect");
		}
		
		values.remove(INSERT_FROM_SELECT);
		
		String tableName;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FAV_LIST:
			tableName = ICD9GROUPITEM.TABLE_NAME;
			break;
		case URI_CODE.ICD10FAV_LIST:
			tableName = ICD10GROUPITEM.TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
		
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT OR IGNORE INTO ");
        sql.append(tableName);
        sql.append('(');

        Object[] bindArgs = null;
        int size = (values != null && values.size() > 0) ? values.size() : 0;
        if (size > 0) {
            bindArgs = new Object[size];
            int i = 0;
            
			// solution for API < 11
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				for (Entry<String, Object> item : values.valueSet()) {
					String colName = item.getKey();
					sql.append((i > 0) ? "," : "");
					sql.append(colName);
					bindArgs[i++] = values.get(colName);
				}
			} else {
				for (String colName : values.keySet()) {
					sql.append((i > 0) ? "," : "");
					sql.append(colName);
					bindArgs[i++] = values.get(colName);
				}
			}      
            
            sql.append(')');            
        } else {
        	throw new IllegalArgumentException("Must supply column names");
        }

        sql.append(selStatment);
        
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		db.execSQL(sql.toString());
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int delCount = 0;
		String tableName;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FAV_LIST:
			tableName = ICD9GROUPITEM.TABLE_NAME;
			break;
		case URI_CODE.ICD9FAV_ID:
			tableName = ICD9GROUPITEM.TABLE_NAME;
			selection = ICD9GROUPITEM._ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};			
			break;
		case URI_CODE.ICD10FAV_LIST:
			tableName = ICD10GROUPITEM.TABLE_NAME;
			break;
		case URI_CODE.ICD10FAV_ID:
			tableName = ICD10GROUPITEM.TABLE_NAME;
			selection = ICD10GROUPITEM._ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		delCount = db.delete(tableName, selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		return delCount;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		if(values.getAsString(INSERT_FROM_SELECT) != null) {
			insertFromSelect(uri, values);
			return uri;
		}
		
		long newID = 0;
		String tableName;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FAV_LIST:
			tableName = ICD9GROUPITEM.TABLE_NAME;
			break;
		case URI_CODE.ICD10FAV_LIST:
			tableName = ICD10GROUPITEM.TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
			//TODO: this will fail on conflict
			newID = db.insert(tableName, null, values);
		}
		else {
			newID = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}
		Uri newUri = Uri.withAppendedPath(uri, String.valueOf(newID));

		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		String limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT);
        String offset = uri.getQueryParameter(QUERY_PARAMETER_OFFSET);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9_LIST:
			queryBuilder.setTables(ICD9.TABLE_NAME);
			break;
		case URI_CODE.ICD9FAV_LIST:
			queryBuilder.setTables(ICD9FAV01VIEW.TABLE_NAME);
			break;
		case URI_CODE.ICD9FAV_ID:
			queryBuilder.setTables(ICD9FAV01VIEW.TABLE_NAME);
			selection = ICD9FAV01VIEW.ICD9_ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};
			break;
		case URI_CODE.ICD9X10_LIST:
			queryBuilder.setTables(ICD9X10VIEW.TABLE_NAME);
			break;
		case URI_CODE.ICD10_LIST:
			queryBuilder.setTables(ICD10.TABLE_NAME);
			break;
		case URI_CODE.ICD10FAV_LIST:
			queryBuilder.setTables(ICD10FAV01VIEW.TABLE_NAME);
			break;
		case URI_CODE.ICD10FAV_ID:
			queryBuilder.setTables(ICD10FAV01VIEW.TABLE_NAME);
			selection = ICD10FAV01VIEW.ICD10_ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};
			break;
		case URI_CODE.ICD10X9_LIST:
			queryBuilder.setTables(ICD10X9VIEW.TABLE_NAME);
			break;
		case URI_CODE.ICD9FAV_GROUP_ID:
			queryBuilder.setTables(ICD9GROUP.TABLE_NAME);
			projection = new String[] {ICD9GROUP._ID};
			selection = ICD9GROUP.TYPE + " = ? and " + ICD9GROUP.NAME + " = ?";
			selectionArgs = new String[] {"1", "FAV01"};
			break;
		case URI_CODE.ICD10FAV_GROUP_ID:
			queryBuilder.setTables(ICD10GROUP.TABLE_NAME);
			projection = new String[] {ICD10GROUP._ID};
			selection = ICD10GROUP.TYPE + " = ? and " + ICD10GROUP.NAME + " = ?";
			selectionArgs = new String[] {"1", "FAV01"};
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		Cursor cursor;
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getReadableDatabase();

		switch (uriType) {
		case URI_CODE.ICD9FAV_ID:
		case URI_CODE.ICD10FAV_ID:
		case URI_CODE.ICD9FAV_GROUP_ID:
		case URI_CODE.ICD10FAV_GROUP_ID:
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, null);
			break;
		default:
			if(TextUtils.isEmpty(limit)) {
				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);				
			} else {
				if(TextUtils.isEmpty(offset)) {
					offset = "0";
				}
				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder, offset + "," + limit);			
			}
		}		

		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
