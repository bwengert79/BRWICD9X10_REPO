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
    public static final int DATABASE_VERSION = 1;
    
    public static class OpenHelper extends SQLiteOpenHelper {

    	public interface Listener {
    		void OnProgress(String msg);
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

					String[] sqlcmds = sb.toString().split(";");
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
				ProcessZipFile(db, "data_group.zip", false);

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

	    		PublishProgress("Creating ICD9x10 Views");
				ProcessZipFile(db, "schema_views.zip", true);

	    		PublishProgress("Creating ICD9x10 Indexes");
				ProcessZipFile(db, "schema_indexes.zip", false);

				db.setTransactionSuccessful();
	    		
				PublishProgress("ICD9x10 Database is ready");
	    		
			} catch (Exception e) {
				AppLog.e(TAG, "Failure creating icd9x10 database", e);
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
			// TODO Auto-generated method stub
    		AppLog.i(TAG, "onUpgrade");
		}
    }
    
    public static final class ICD9 implements BaseColumns {

        private ICD9() {}
        
        public static final String TABLE_NAME = "icd9";
        
        public static final String ICD9_CODE = "icd9_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd9_code ASC";
    }
    
/*    public static final class ICD9GEM implements BaseColumns {

        private ICD9GEM() {}
        
        public static final String TABLE_NAME = "icd9_gem";
        
        public static final String ICD9_CODE = "icd9_code";
        public static final String ICD10_CODE = "icd10_code";
        public static final String APPROX_FLAG = "approx_flag";
        public static final String NOMAP_FLAG = "nomap_flag";
        public static final String COMBO_FLAG = "combo_flag";
        public static final String SCENARIO_FLAG = "scenario_flag";
        public static final String CHOICE_FLAG = "choice_flag";

        public static final String DEFAULT_SORT_ORDER = "icd9_code ASC";
    }*/
    
    public static final class ICD10 implements BaseColumns {

        private ICD10() {}
        
        public static final String TABLE_NAME = "icd10";
        
        public static final String ICD10_CODE = "icd10_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "icd10_code ASC";
    }

/*    public static final class ICD10GEM implements BaseColumns {

        private ICD10GEM() {}
        
        public static final String TABLE_NAME = "icd10_gem";
        
        public static final String ICD9_CODE = "icd9_code";
        public static final String ICD10_CODE = "icd10_code";
        public static final String APPROX_FLAG = "approx_flag";
        public static final String NOMAP_FLAG = "nomap_flag";
        public static final String COMBO_FLAG = "combo_flag";
        public static final String SCENARIO_FLAG = "scenario_flag";
        public static final String CHOICE_FLAG = "choice_flag";

        public static final String DEFAULT_SORT_ORDER = "icd10_code ASC";
    }*/
    
    public static final class ICD9GROUP implements BaseColumns {

        private ICD9GROUP() {}
        
        public static final String TABLE_NAME = "icd9_group";
        
        public static final String TYPE = "type";
        public static final String NAME = "name";

        public static final String DEFAULT_SORT_ORDER = "type ASC, name ASC";
    }
        
    public static final class ICD10GROUP implements BaseColumns {

        private ICD10GROUP() {}
        
        public static final String TABLE_NAME = "icd10_group";
        
        public static final String TYPE = "type";	//1-system, 2-user
        public static final String NAME = "name";

        public static final String DEFAULT_SORT_ORDER = "type ASC, name ASC";
    }

    public static final class ICD9GROUPITEM implements BaseColumns {

        private ICD9GROUPITEM() {}
        
        public static final String TABLE_NAME = "icd9_groupitem";
        
        public static final String GROUP_ID = "group_id";
        public static final String ICD9_ID = "icd9_id";

        public static final String DEFAULT_SORT_ORDER = "group_id ASC, icd9_id ASC";
    }

    public static final class ICD10GROUPITEM implements BaseColumns {

        private ICD10GROUPITEM() {}
        
        public static final String TABLE_NAME = "icd10_groupitem";
        
        public static final String GROUP_ID = "group_id";
        public static final String ICD10_ID = "icd10_id";

        public static final String DEFAULT_SORT_ORDER = "group_id ASC, icd10_id ASC";
    }

    public static final class ICD9FAV01VIEW {

        private ICD9FAV01VIEW() {}
        
        public static final String TABLE_NAME = "icd9_fav01_view";
        
        public static final String ICD9_ID = "icd9_id";
        public static final String ICD9_CODE = "icd9_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "type ASC, name ASC";
    }

    public static final class ICD10FAV01VIEW {

        private ICD10FAV01VIEW() {}
        
        public static final String TABLE_NAME = "icd10_fav01_view";
        
        public static final String ICD10_ID = "icd10_id";
        public static final String ICD10_CODE = "icd10_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "type ASC, name ASC";
    }

    public static final class ICD9X10VIEW {

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

    public static final class ICD10X9VIEW {

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

    public static final class ICD9GROUPUSERVIEW {

        private ICD9GROUPUSERVIEW() {}
        
        public static final String TABLE_NAME = "icd9_group_user_view";
        
        public static final String GROUP_ID = "group_id";
        public static final String GROUP_NAME = "group_name";
        public static final String ICD9_ID = "icd9_id";
        public static final String ICD9_CODE = "icd9_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "group_name ASC, icd9_code ASC";
    }

    public static final class ICD10GROUPUSERVIEW {

        private ICD10GROUPUSERVIEW() {}
        
        public static final String TABLE_NAME = "icd10_group_user_view";
        
        public static final String GROUP_ID = "group_id";
        public static final String GROUP_NAME = "group_name";
        public static final String ICD10_ID = "icd10_id";
        public static final String ICD10_CODE = "icd10_code";
        public static final String LONG_DESC = "long_desc";

        public static final String DEFAULT_SORT_ORDER = "group_name ASC, icd10_code ASC";
    }
}
