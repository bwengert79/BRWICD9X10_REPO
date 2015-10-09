package com.brwsoftware.brwicd9x10;

public final class AppValue {
	public static final int ICDTYPE_9 = 9;
	public static final int ICDTYPE_10 = 10;
	public static final int ICDORDINAL_9 = 0;
	public static final int ICDORDINAL_10 = 1;
	
	public final static String INTENT_EXTRA_ICDTYPE = "com.brwsoftware.brwicd9x10.ICDTYPE";
	public final static String INTENT_EXTRA_ICDORDINAL = "com.brwsoftware.brwicd9x10.ICDORDINAL";

	public final static String INTENT_ACTION_FOLDER_DELETE = "com.brwsoftware.brwicd9x10.FOLDER_DELETE";
	public final static String INTENT_ACTION_FOLDER_UPDATE = "com.brwsoftware.brwicd9x10.FOLDER_UPDATE";
	public final static String INTENT_EXTRA_FOLDER_ID = "com.brwsoftware.brwicd9x10.FOLDER_ID";
	public final static String INTENT_EXTRA_FOLDER_NAME = "com.brwsoftware.brwicd9x10.FOLDER_NAME";

	public final static String PREFKEY_CURRENT_ICD9_FOLDER_ID = "current_icd9_folder_id";
	public final static String PREFKEY_CURRENT_ICD9_FOLDER_NAME = "current_icd9_folder_name";
	public final static String PREFKEY_CURRENT_ICD10_FOLDER_ID = "current_icd10_folder_id";
	public final static String PREFKEY_CURRENT_ICD10_FOLDER_NAME = "current_icd10_folder_name";

	public static final int MY_FAVORITES_FOLDER_ID = 1;
	public static final String MY_FAVORITES_FOLDER_NAME = "My Favorites";
}
