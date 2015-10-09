package com.brwsoftware.brwicd9x10;

import java.util.List;

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
	public static final String DELETE_FROM_SELECT = "_use_delete_with_select_";
    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    
	
	private static final String AUTHORITY = "com.brwsoftware.brwicd9x10.contentprovider";
	public static final Uri CONTENT_URI_ICD9S = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9S);
	public static final Uri CONTENT_URI_ICD10S = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10S);
	public static final Uri CONTENT_URI_ICD9FOLDERS = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD9FOLDERS);
	public static final Uri CONTENT_URI_ICD10FOLDERS = Uri.parse("content://" + AUTHORITY + "/" + URI_PATH.ICD10FOLDERS);
	
	public static final class URI_PATH {
		static final String ICD9S = "icd9s";
		static final String ICD9FOLDERS = "icd9folders";
		static final String ICD10S = "icd10s";
		static final String ICD10FOLDERS = "icd10folders";
	}

	private static final class URI_CODE {
		static final int ICD9_LIST = 100;
		static final int ICD9X10_LIST = 110;
		
		static final int ICD10_LIST = 200;
		static final int ICD10X9_LIST = 210;
		
		static final int ICD9FOLDERS = 300;
		static final int ICD9FOLDERS_ID = 310;
		static final int ICD9FOLDERS_ID_ICD9S = 320;
		static final int ICD9FOLDERS_ID_ICD9S_ID = 330;
		
		static final int ICD10FOLDERS = 400;
		static final int ICD10FOLDERS_ID = 410;
		static final int ICD10FOLDERS_ID_ICD10S = 420;
		static final int ICD10FOLDERS_ID_ICD10S_ID = 430;
	}
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9S, URI_CODE.ICD9_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9S + "/#/" + URI_PATH.ICD10S, URI_CODE.ICD9X10_LIST);

		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10S, URI_CODE.ICD10_LIST);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10S + "/#/" + URI_PATH.ICD9S, URI_CODE.ICD10X9_LIST);
		
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FOLDERS, URI_CODE.ICD9FOLDERS);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FOLDERS + "/#", URI_CODE.ICD9FOLDERS_ID);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FOLDERS + "/#/" + URI_PATH.ICD9S, URI_CODE.ICD9FOLDERS_ID_ICD9S);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD9FOLDERS + "/#/" + URI_PATH.ICD9S + "/#", URI_CODE.ICD9FOLDERS_ID_ICD9S_ID);
		
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FOLDERS, URI_CODE.ICD10FOLDERS);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FOLDERS + "/#", URI_CODE.ICD10FOLDERS_ID);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FOLDERS + "/#/" + URI_PATH.ICD10S, URI_CODE.ICD10FOLDERS_ID_ICD10S);
		sURIMatcher.addURI(AUTHORITY, URI_PATH.ICD10FOLDERS + "/#/" + URI_PATH.ICD10S + "/#", URI_CODE.ICD10FOLDERS_ID_ICD10S_ID);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void insertFromSelect(Uri uri, ContentValues values) {
		String selStatment = values.getAsString(INSERT_FROM_SELECT);
		if(selStatment == null) {
			throw new IllegalArgumentException("Invalid use of insertFromSelect");
		}
		
		values.remove(INSERT_FROM_SELECT);	
		
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT OR IGNORE INTO ");
        
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FOLDERS_ID_ICD9S:
			sql.append(ICD9FOLDERITEM.TABLE_NAME)
				.append(" (")
				.append(ICD9FOLDERITEM.FOLDER_ID)
				.append(",")
				.append(ICD9FOLDERITEM.ICD9_ID)
				.append(")"); 
			sql.append("select ")
				.append(uri.getPathSegments().get(1))
				.append(",").append(ICD9._ID)
				.append(" from ").append(ICD9.TABLE_NAME)
				.append(" where ").append(selStatment);
			break;
		case URI_CODE.ICD10FOLDERS_ID_ICD10S:
			sql.append(ICD10FOLDERITEM.TABLE_NAME)
				.append(" (")
				.append(ICD10FOLDERITEM.FOLDER_ID)
				.append(",")
				.append(ICD10FOLDERITEM.ICD10_ID)
				.append(")"); 
			sql.append("select ")
				.append(uri.getPathSegments().get(1))
				.append(",").append(ICD10._ID)
				.append(" from ").append(ICD10.TABLE_NAME)
				.append(" where ").append(selStatment);
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
        
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		db.execSQL(sql.toString());
		
		//Notify anyone who is interested
		getContext().getContentResolver().notifyChange(uri, null);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int delCount = 0;
		String tableName;
		List<String> segs = uri.getPathSegments();

		int uriType = sURIMatcher.match(uri);

		String subQuery = null;
		boolean delSubQuery = (selection != null && selection.equals(DELETE_FROM_SELECT));
		if(delSubQuery) {
			if(selectionArgs != null && selectionArgs.length == 1) {
				StringBuilder sql = new StringBuilder();
				switch (uriType) {
				case URI_CODE.ICD9FOLDERS_ID_ICD9S:
					sql.append("select ")
					.append(ICD9FOLDERVIEW.ICD9_ID)
					.append(" from ").append(ICD9FOLDERVIEW.TABLE_NAME)
					.append(" where ")
					.append(ICD9FOLDERVIEW.FOLDER_ID).append(" = ").append(segs.get(1))
					.append(" and ").append(selectionArgs[0]);
					subQuery = sql.toString();
					break;
				case URI_CODE.ICD10FOLDERS_ID_ICD10S:
					sql.append("select ")
					.append(ICD10FOLDERVIEW.ICD10_ID)
					.append(" from ").append(ICD10FOLDERVIEW.TABLE_NAME)
					.append(" where ")
					.append(ICD10FOLDERVIEW.FOLDER_ID).append(" = ").append(segs.get(1))
					.append(" and ").append(selectionArgs[0]);
					subQuery = sql.toString();
					break;
				default:
				}
			}
			if(TextUtils.isEmpty(subQuery)) {
				throw new IllegalArgumentException("Invalid use of deleteFromSelect");
			}
		}
		
		switch (uriType) {
		case URI_CODE.ICD9FOLDERS_ID:
			tableName = ICD9FOLDER.TABLE_NAME;
			selection = ICD9FOLDER._ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};			
			break;
		case URI_CODE.ICD10FOLDERS_ID:
			tableName = ICD10FOLDER.TABLE_NAME;
			selection = ICD10FOLDER._ID + " = ?";
			selectionArgs = new String[] {uri.getLastPathSegment()};
			break;			
		case URI_CODE.ICD9FOLDERS_ID_ICD9S_ID:
			tableName = ICD9FOLDERITEM.TABLE_NAME;
			selection = ICD9FOLDERITEM.FOLDER_ID + " = ? and " + ICD9FOLDERITEM.ICD9_ID + " = ?";			
			selectionArgs = new String[] {segs.get(1), segs.get(3)};			
			break;
		case URI_CODE.ICD10FOLDERS_ID_ICD10S_ID:
			tableName = ICD10FOLDERITEM.TABLE_NAME;
			selection = ICD10FOLDERITEM.FOLDER_ID + " = ? and " + ICD10FOLDERITEM.ICD10_ID + " = ?";			
			selectionArgs = new String[] {segs.get(1), segs.get(3)};			
			break;
		case URI_CODE.ICD9FOLDERS_ID_ICD9S:
			tableName = ICD9FOLDERITEM.TABLE_NAME;
			if(delSubQuery) {
				selection = ICD9FOLDERITEM.FOLDER_ID + " = ? and " + ICD9FOLDERITEM.ICD9_ID + " in (" + subQuery + ")";
			} else {
				selection = ICD9FOLDERITEM.FOLDER_ID + " = ?";
			}
			selectionArgs = new String[] {segs.get(1)};
			break;
		case URI_CODE.ICD10FOLDERS_ID_ICD10S:
			tableName = ICD10FOLDERITEM.TABLE_NAME;
			if(delSubQuery) {
				selection = ICD10FOLDERITEM.FOLDER_ID + " = ? and " + ICD10FOLDERITEM.ICD10_ID + " in (" + subQuery + ")";
			} else {
				selection = ICD10FOLDERITEM.FOLDER_ID + " = ?";
			}
			selectionArgs = new String[] {segs.get(1)};
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
		
		//Do the delete
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		delCount = db.delete(tableName, selection, selectionArgs);
		
		//Notify anyone who is interested
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
		if(values == null) values = new ContentValues();
		
		if(values.getAsString(INSERT_FROM_SELECT) != null) {
			insertFromSelect(uri, values);
			return uri;
		}
		
		long newID = 0;
		String tableName;
		List<String> segs = uri.getPathSegments();
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FOLDERS:
			tableName = ICD9FOLDER.TABLE_NAME;
			break;
		case URI_CODE.ICD10FOLDERS:
			tableName = ICD10FOLDER.TABLE_NAME;
			break;
		case URI_CODE.ICD9FOLDERS_ID_ICD9S_ID:
			tableName = ICD9FOLDERITEM.TABLE_NAME;
			values.put(ICD9FOLDERITEM.FOLDER_ID, segs.get(1));
			values.put(ICD9FOLDERITEM.ICD9_ID, segs.get(3));
			break;
		case URI_CODE.ICD10FOLDERS_ID_ICD10S_ID:
			tableName = ICD10FOLDERITEM.TABLE_NAME;
			values.put(ICD10FOLDERITEM.FOLDER_ID, segs.get(1));
			values.put(ICD10FOLDERITEM.ICD10_ID, segs.get(3));
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: " + uri);
		}
		
		//Do the insert
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
			//TODO: this will fail on conflict
			newID = db.insert(tableName, null, values);
		}
		else {
			newID = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}
		Uri newUri = Uri.withAppendedPath(uri, String.valueOf(newID));

		//Notify anyone interested
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
        List<String> segs = uri.getPathSegments();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9_LIST:
			queryBuilder.setTables(ICD9.TABLE_NAME);
			break;
		case URI_CODE.ICD9X10_LIST:
			queryBuilder.setTables(ICD9X10VIEW.TABLE_NAME);
			selection = ICD9X10VIEW.ICD9_ID + " = ?";
			selectionArgs = new String[] {segs.get(1)};
			break;
		case URI_CODE.ICD10_LIST:
			queryBuilder.setTables(ICD10.TABLE_NAME);
			break;
		case URI_CODE.ICD10X9_LIST:
			queryBuilder.setTables(ICD10X9VIEW.TABLE_NAME);
			selection = ICD10X9VIEW.ICD10_ID + " = ?";
			selectionArgs = new String[] {segs.get(1)};			
			break;			
		case URI_CODE.ICD9FOLDERS:
			queryBuilder.setTables(ICD9FOLDER.TABLE_NAME);
			if(TextUtils.isEmpty(sortOrder)) {
				sortOrder = ICD9FOLDER.DEFAULT_SORT_ORDER;
			}
			break;
		case URI_CODE.ICD10FOLDERS:
			queryBuilder.setTables(ICD10FOLDER.TABLE_NAME);
			if(TextUtils.isEmpty(sortOrder)) {
				sortOrder = ICD10FOLDER.DEFAULT_SORT_ORDER;
			}
			break;
		case URI_CODE.ICD9FOLDERS_ID_ICD9S:
			queryBuilder.setTables(ICD9FOLDERVIEW.TABLE_NAME);
			if(TextUtils.isEmpty(selection)) {
				selection = ICD9FOLDERVIEW.FOLDER_ID + " = ?";				
			} else {
				selection = ICD9FOLDERVIEW.FOLDER_ID + " = ? and " + selection;								
			}
			if(selectionArgs == null || selectionArgs.length == 0) {
				selectionArgs = new String[] {segs.get(1)};
			} else {
				String[] newSelectionArgs = new String[selectionArgs.length + 1];				
 				newSelectionArgs[0] = segs.get(1);
 				System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
				selectionArgs = newSelectionArgs;
			}
			if(TextUtils.isEmpty(sortOrder)) {
				sortOrder = ICD9FOLDERVIEW.DEFAULT_SORT_ORDER;
			}
			break;	
		case URI_CODE.ICD10FOLDERS_ID_ICD10S:
			queryBuilder.setTables(ICD10FOLDERVIEW.TABLE_NAME);
			if(TextUtils.isEmpty(selection)) {
				selection = ICD10FOLDERVIEW.FOLDER_ID + " = ?";
			} else {
				selection = ICD9FOLDERVIEW.FOLDER_ID + " = ? and " + selection;
			}
			if(selectionArgs == null || selectionArgs.length == 0) {
				selectionArgs = new String[] {segs.get(1)};
			} else {
				String[] newSelectionArgs = new String[selectionArgs.length + 1];				
 				newSelectionArgs[0] = segs.get(1);
 				System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
				selectionArgs = newSelectionArgs;
			}
			if(TextUtils.isEmpty(sortOrder)) {
				sortOrder = ICD10FOLDERVIEW.DEFAULT_SORT_ORDER;
			}
			break;			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		Cursor cursor;
		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getReadableDatabase();

		if(TextUtils.isEmpty(limit)) {
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);				
		} else {
			if(TextUtils.isEmpty(offset)) {
				offset = "0";
			}
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder, offset + "," + limit);			
		}
		
		//Notify anyone interested
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int updCount = 0;
		String tableName;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case URI_CODE.ICD9FOLDERS_ID:
			tableName = ICD9FOLDER.TABLE_NAME;
			selection = ICD9FOLDER._ID + " = ?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			break;
		case URI_CODE.ICD10FOLDERS_ID:
			tableName = ICD10FOLDER.TABLE_NAME;
			selection = ICD10FOLDER._ID + " = ?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			break;
		default:
			throw new IllegalArgumentException("Unknown or Unsupported URI: "
					+ uri);
		}

		SQLiteDatabase db = ICD9X10Database.OpenHelper.getInstance(getContext()).getWritableDatabase();
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
			//TODO: this will fail on conflict
			updCount = db.update(tableName, values, selection, selectionArgs);
		} else {
			updCount = db.updateWithOnConflict(tableName, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_IGNORE);
		}
		
		//Notify anyone interested
		getContext().getContentResolver().notifyChange(uri, null);
		
		return updCount;
	}
}
