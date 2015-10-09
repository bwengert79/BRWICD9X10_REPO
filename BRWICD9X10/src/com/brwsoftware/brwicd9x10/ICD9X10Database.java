package com.brwsoftware.brwicd9x10;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.provider.BaseColumns;

public class ICD9X10Database {

    private ICD9X10Database() {}
    
    public static final String DATABASE_NAME = "icd9x10.db";
    public static final int DATABASE_VERSION = 2;
    public static final int DATABASE_ACTION_NONE = 0;
    public static final int DATABASE_ACTION_CREATE = 1;
    public static final int DATABASE_ACTION_UPGRADE = 2;
    
    public static class OpenHelper extends SQLiteOpenHelper {

    	public interface Listener {
    		void OnProgress(String msg);
    		void OnError(String msg, Exception e);
    		void OnAction(int action);
    	}
    	
    	private static final String TAG = "ICD9X10OpenHelper";
    	private static OpenHelper mInstance;	//singleton pattern
    	private Context mContext;
    	private Listener mListener;
    	
    	private OpenHelper(){
    		// Exists only to defeat instantiation.
    		super(null, ICD9X10Database.DATABASE_NAME, null, ICD9X10Database.DATABASE_VERSION);
    	}
    	private OpenHelper(Context context) {
            super(context, ICD9X10Database.DATABASE_NAME, null, ICD9X10Database.DATABASE_VERSION);
            mContext = context.getApplicationContext();
    	}

    	public static synchronized OpenHelper getInstance(Context context) {
    		if(mInstance == null) {
    			mInstance = new OpenHelper(context);
    		}
			return mInstance;    		
    	}
    	
		public void setListener(Listener listener) {
			this.mListener = listener;
		}    	
    	
		protected void PublishProgress(String msg){
			if(mListener != null) mListener.OnProgress(msg);
		}
		
		protected void PublishError(String msg, Exception e){
			if(mListener != null) mListener.OnError(msg, e);
		}
		
		protected void PublishAction(int action){
			if(mListener != null) mListener.OnAction(action);
		}
		
     	private void ProcessZipFile(SQLiteDatabase db, String ZipFileName, boolean parseCommand) throws Exception{    		
    		AppLog.i(TAG, "ProcessZipFile - %s", ZipFileName);
			String UnzipFileName = null;
			try {
				// Unzip
				InputStream is = mContext.getAssets().open(ZipFileName);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
				ZipEntry ze = zis.getNextEntry();
				byte[] buffer = new byte[1024];
				int count = 0;
				if (ze != null) {
					UnzipFileName = ze.getName();
					FileOutputStream fos = mContext.openFileOutput(UnzipFileName, Context.MODE_PRIVATE);
					while ((count = zis.read(buffer)) != -1) {
						fos.write(buffer, 0, count);
					}
					fos.close();
					zis.closeEntry();
				}
				zis.close();
				
				// Read and process file
				String line = null;
				FileInputStream fis = mContext.openFileInput(UnzipFileName);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			
				if (parseCommand) {
					StringBuilder sb = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						if(line.length() > 0) {							
							sb.append(line.replace('\t', ' '));
							if(sb.charAt(sb.length() - 1) != ' ') {
								sb.append(' ');
							}
						}
					}

					String[] sqlcmds = sb.toString().split("\\$");
					for (String sql : sqlcmds) {
						String sqlTrimmed = sql.trim();
						if(sqlTrimmed.length() > 0) {
							db.execSQL(sqlTrimmed);
						}
					}

				} else {

					while ((line = reader.readLine()) != null) {
						if(line.length() > 0) {
							db.execSQL(line);	
						}						
					}
				}
				reader.close();
				fis.close();
			} finally {
				if(UnzipFileName != null) {
					mContext.deleteFile(UnzipFileName);
				}
			}
    	}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
    		AppLog.i(TAG, "onCreate: creating icd9x10 database");
    		PublishProgress("Creating ICD9x10 Database");
    		
    		long startTime = SystemClock.elapsedRealtime();
			try {
				db.beginTransaction();
				
	    		PublishProgress("Creating ICD9x10 Tables");
				ProcessZipFile(db, "schema_tables.zip", false);
				
	    		PublishProgress("Importing Data: Groups");
				ProcessZipFile(db, "data_folder.zip", false);

	    		PublishProgress("Importing Data: ICD9");
	    		ProcessZipFile(db, "data_icd9.zip", false);

	    		PublishProgress("Importing Data: ICD9 GEM");
				ProcessZipFile(db, "data_icd9_gem.zip", false);

	    		PublishProgress("Importing Data: ICD10");
				ProcessZipFile(db, "data_icd10.zip", false);

	    		PublishProgress("Importing Data: ICD10 GEM");
				ProcessZipFile(db, "data_icd10_gem.zip", false);

	    		PublishProgress("Importing Data: Sequence");
				ProcessZipFile(db, "data_sequence.zip", false);

	    		PublishProgress("Creating ICD9x10 Tiggers");
				ProcessZipFile(db, "schema_triggers.zip", true);

	    		PublishProgress("Creating ICD9x10 Views");
				ProcessZipFile(db, "schema_views.zip", true);

	    		PublishProgress("Creating ICD9x10 Indexes");
				ProcessZipFile(db, "schema_indexes.zip", false);

				db.setTransactionSuccessful();
	    		
				PublishProgress("ICD9x10 Database is ready");
				PublishAction(DATABASE_ACTION_CREATE);
	    		
			} catch (Exception e) {
				AppLog.e(TAG, "Failure creating icd9x10 database", e);
				PublishError("ICD9x10 Database create failed", e);
			} finally {
				if (db.inTransaction()) {
					db.endTransaction();
				}
			}
			long endTime = SystemClock.elapsedRealtime();
			double elapsedSeconds = (endTime - startTime) / 1000.0;
    		AppLog.i(TAG, "onCreate exit: duration - %f seconds", elapsedSeconds);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    		AppLog.i(TAG, "onUpgrade: upgrading icd9x10 database");
    		PublishProgress("Upgrading ICD9x10 Database");

    		long startTime = SystemClock.elapsedRealtime();
			try {
				db.beginTransaction();

				switch (oldVersion) {
				case 1:
					PublishProgress("Applying Upgrade v02");
					ProcessZipFile(db, "upgrade_v02.zip", true);
					// Note: remove the break for incremental upgrade (1 -> 2,
					// 1->2->3, etc)
					break;
				default:
					throw new IllegalStateException(
							"onUpgrade() with unknown oldVersion " + oldVersion);
				}
				
				db.setTransactionSuccessful();

				PublishProgress("ICD9x10 Database is ready");
				PublishAction(DATABASE_ACTION_UPGRADE);
	    		
			} catch (Exception e) {
				AppLog.e(TAG, "Failure upgrading icd9x10 database", e);
				PublishError("ICD9x10 Database upgrade failed", e);
			} finally {
				if (db.inTransaction()) {
					db.endTransaction();
				}
			}
			
			long endTime = SystemClock.elapsedRealtime();
			double elapsedSeconds = (endTime - startTime) / 1000.0;
    		AppLog.i(TAG, "onUpgrade exit: duration - %f seconds", elapsedSeconds);
		}
    }
    
    public static final class ICD9 implements BaseColumns {

        private ICD9() {}
        
        public static final String TABLE_NAME = "icd9";
        
        public static final String ICD9_CODE = "icd9_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd9_code ASC";
    }
    
    public static final class ICD10 implements BaseColumns {

        private ICD10() {}
        
        public static final String TABLE_NAME = "icd10";
        
        public static final String ICD10_CODE = "icd10_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd10_code ASC";
    }
    
    public static final class ICD9FOLDER implements BaseColumns {

        private ICD9FOLDER() {}
        
        public static final String TABLE_NAME = "icd9_folder";
        
         public static final String NAME = "name";

        public static final String DEFAULT_SORT_ORDER = "name COLLATE NOCASE ASC";
    }
        
    public static final class ICD10FOLDER implements BaseColumns {

        private ICD10FOLDER() {}
        
        public static final String TABLE_NAME = "icd10_folder";
        
        public static final String NAME = "name";

        public static final String DEFAULT_SORT_ORDER = "name COLLATE NOCASE ASC";
    }

    public static final class ICD9FOLDERITEM implements BaseColumns {

        private ICD9FOLDERITEM() {}
        
        public static final String TABLE_NAME = "icd9_folderitem";
        
        public static final String FOLDER_ID = "folder_id";
        public static final String ICD9_ID = "icd9_id";

        public static final String DEFAULT_SORT_ORDER = "folder_id ASC, icd9_id ASC";
    }

    public static final class ICD10FOLDERITEM implements BaseColumns {

        private ICD10FOLDERITEM() {}
        
        public static final String TABLE_NAME = "icd10_folderitem";
        
        public static final String FOLDER_ID = "folder_id";
        public static final String ICD10_ID = "icd10_id";

        public static final String DEFAULT_SORT_ORDER = "folder_id ASC, icd10_id ASC";
    }

    public static final class ICD9X10VIEW implements BaseColumns {

        private ICD9X10VIEW() {}
        
        public static final String TABLE_NAME = "icd9x10_view";
        
        public static final String ICD9_ID = "icd9_id";
        public static final String ICD9_CODE = "icd9_code";
        public static final String ICD9_LONG_DESC = "icd9_long_desc";
        public static final String ICD10_ID = "icd10_id";
        public static final String ICD10_CODE = "icd10_code";
        public static final String ICD10_LONG_DESC = "icd10_long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd10_code ASC";
    }

    public static final class ICD10X9VIEW implements BaseColumns {

        private ICD10X9VIEW() {}
        
        public static final String TABLE_NAME = "icd10x9_view";
        
        public static final String ICD9_ID = "icd9_id";
        public static final String ICD9_CODE = "icd9_code";
        public static final String ICD9_LONG_DESC = "icd9_long_desc";
        public static final String ICD10_ID = "icd10_id";
        public static final String ICD10_CODE = "icd10_code";
        public static final String ICD10_LONG_DESC = "icd10_long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd10_code ASC";
    }

    public static final class ICD9FOLDERVIEW implements BaseColumns {

        private ICD9FOLDERVIEW() {}
        
        public static final String TABLE_NAME = "icd9_folder_view";
        
        public static final String FOLDER_ID = "folder_id";
        public static final String FOLDER_NAME = "folder_name";
        public static final String ICD9_ID = "icd9_id";
        public static final String ICD9_CODE = "icd9_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "folder_name COLLATE NOCASE ASC, icd9_code ASC";
    }

    public static final class ICD10FOLDERVIEW implements BaseColumns {

        private ICD10FOLDERVIEW() {}
        
        public static final String TABLE_NAME = "icd10_folder_view";
        
        public static final String FOLDER_ID = "folder_id";
        public static final String FOLDER_NAME = "folder_name";
        public static final String ICD10_ID = "icd10_id";
        public static final String ICD10_CODE = "icd10_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "folder_name COLLATE NOCASE ASC, icd10_code ASC";
    }
}
