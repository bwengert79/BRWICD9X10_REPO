package com.brwsoftware.brwicd9x10;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;

import com.brwsoftware.brwicd9x10.ICD9X10ContentProvider.URI_PATH;
import com.brwsoftware.brwicd9x10.ICD9X10Database.*;

public final class ICD9X10QueryBuilder {
	private ICD9X10QueryBuilder(){		
	}
	
	private static final Pattern SELARG_PATTERN = Pattern.compile("&|#|\\|");
	
	public static class QueryParts{
		private String mTitle;
		private String mSelection;
		private String[] mSelectionArgs;
		private String[] mProjection;
		private Uri mUri;
		
		public String getSelection() {
			return mSelection;
		}
		public String[] getSelectionArgs() {
			return mSelectionArgs;
		}
		public String getTitle() {
			return mTitle;
		}
		public String[] getProjection() {
			return mProjection;
		}
		public Uri getUri() {
			return mUri;
		}
	}
	
	public static final class BrowseParam{
		private boolean mBrowseByFolder;
		private String mRawSearchValue;
		private String mFolderName;
		
		public boolean isBrowseByFolder() {
			return mBrowseByFolder;
		}
		public void setBrowseByFolder(boolean browseByFolder, String folderName) {
			mBrowseByFolder = browseByFolder;
			mFolderName = browseByFolder ? folderName : null;
		}
		public String getRawSearchValue() {
			return mRawSearchValue;
		}
		public void setRawSearchValue(String value) {
			mRawSearchValue = value;
		}
		public String getFolderName() {
			return mFolderName;
		}
	}
	
	public static final class SubQueryParam{
		private String mRawSearchValue;

		public String getRawSearchValue() {
			return mRawSearchValue;
		}
		public void setRawSearchValue(String value) {
			mRawSearchValue = value;
		}
	}
	private enum LogicalOperator
	{
		loNone,
		loAND,
		loOR
	}
	
	private static class QueryParam {
		public String mTitle;
		public String mQueryColumn;
		public String mRawSearchValue;
		public String[] mProjection;
	}

	private static String[] getICD9Projection() {
		String[] projection = { ICD9._ID, ICD9.ICD9_CODE, ICD9.LONG_DESC };
		return projection;
	}
	private static String[] getICD9FolderProjection() {
		String[] projection = { "icd9_id as _id", ICD9FOLDERVIEW.ICD9_CODE, ICD9FOLDERVIEW.LONG_DESC };
		return projection;
	}
	private static String[] getICD9X10Projection() {
		String[] projection = { "icd10_id as _id", ICD9X10VIEW.ICD10_CODE, ICD9X10VIEW.ICD10_LONG_DESC };
		return projection;
	}
	private static String[] getICD10Projection() {
		String[] projection = { ICD10._ID, ICD10.ICD10_CODE, ICD10.LONG_DESC };
		return projection;
	}	
	private static String[] getICD10FolderProjection() {
		String[] projection = { "icd10_id as _id", ICD10FOLDERVIEW.ICD10_CODE, ICD10FOLDERVIEW.LONG_DESC };
		return projection;
	}
	private static String[] getICD10X9Projection() {
		String[] projection = { "icd9_id as _id", ICD10X9VIEW.ICD9_CODE, ICD10X9VIEW.ICD9_LONG_DESC };
		return projection;
	}
	
	private static LogicalOperator getLogicalOperator(String rawSearchValue){
		LogicalOperator theLogicalOperator = LogicalOperator.loNone;
		
		if(rawSearchValue.indexOf('&') != -1){
			theLogicalOperator = LogicalOperator.loAND;
		}
		else if(rawSearchValue.indexOf('|') != -1){
			theLogicalOperator = LogicalOperator.loOR;
		}

		return theLogicalOperator;
	}
	
	private static String[] getSelectionArgs(String rawSearchValue) {
		//Parse ignoring empty results
		ArrayList<String> selectionArgs = new ArrayList<String>();
		Matcher m = SELARG_PATTERN.matcher(rawSearchValue);
		int delimCount = 0;
		int s1 = 0;
		int s2 = 0;
		while(m.find()){
			delimCount++;
			s2 = m.start();
			if(s2 - s1 > 0)
			{
				selectionArgs.add(rawSearchValue.substring(s1, s2));
			}
			s1 = m.end();
		}
		if (s1 > 0 && (rawSearchValue.length() - s1 > 0)) {
			selectionArgs.add(rawSearchValue.substring(s1));
		}
		String[] retValue = null;
		if(delimCount == 0) {
			retValue = new String[] { rawSearchValue };
		} else {
			retValue = new String[selectionArgs.size()];
			selectionArgs.toArray(retValue);
		}
		
		return retValue;
	}
	
	private static QueryParts browseCode(QueryParam param) {
		QueryParts theQueryParts = new QueryParts();
		StringBuilder sbTitle = new StringBuilder();

		sbTitle.append(param.mTitle);
		theQueryParts.mProjection = param.mProjection;
		String rawSearchValues[] = getSelectionArgs(param.mRawSearchValue);
		LogicalOperator theLogicalOperator = getLogicalOperator(param.mRawSearchValue);
		
		if (rawSearchValues.length > 0 && theLogicalOperator == LogicalOperator.loOR) {
			// OR is the only operator that makes sense
			int i = 0;
			theQueryParts.mSelectionArgs = new String[rawSearchValues.length];
			StringBuilder sbSelection = new StringBuilder();
			for (String rawSearchItem : rawSearchValues) {
				if (sbSelection.length() > 0) {
					sbSelection.append(" or ");
					sbTitle.append(" or ");
				}

				sbSelection.append(param.mQueryColumn + " like ?");
				theQueryParts.mSelectionArgs[i++] = rawSearchItem + "%";
				sbTitle.append(rawSearchItem);
			}
			theQueryParts.mSelection = sbSelection.toString();
		} else if (rawSearchValues.length > 0) {
			sbTitle.append(rawSearchValues[0]);
			theQueryParts.mSelection = param.mQueryColumn + " like ?";

			String FormattedSeachValue = rawSearchValues[0] + "%";
			theQueryParts.mSelectionArgs = new String[] { FormattedSeachValue };
		} else {
			//Just so we dont blow up
			theQueryParts.mSelection = param.mQueryColumn + " like ?";
			theQueryParts.mSelectionArgs = new String[] { "%" };
		}

		theQueryParts.mTitle = sbTitle.toString();
		return theQueryParts;
	}
	
	private static QueryParts browseDesc(QueryParam param) {
		QueryParts theQueryParts = new QueryParts();
		StringBuilder sbTitle = new StringBuilder();

		sbTitle.append(param.mTitle);
		theQueryParts.mProjection = param.mProjection;
		String rawSearchValues[] = getSelectionArgs(param.mRawSearchValue);
		LogicalOperator theLogicalOperator = getLogicalOperator(param.mRawSearchValue);

		if (rawSearchValues.length > 0 && theLogicalOperator != LogicalOperator.loNone) {
			int i = 0;
			theQueryParts.mSelectionArgs = new String[rawSearchValues.length];
			StringBuilder sbSelection = new StringBuilder();
			for (String rawSearchItem : rawSearchValues) {
				if (sbSelection.length() > 0) {
					sbSelection.append(theLogicalOperator == LogicalOperator.loAND ? " and " : " or ");
					sbTitle.append(theLogicalOperator == LogicalOperator.loAND ? " and " : " or ");
				}

				sbSelection.append(param.mQueryColumn + " like ?");
				theQueryParts.mSelectionArgs[i++] = "%" + rawSearchItem + "%";
				sbTitle.append(rawSearchItem);
			}
			theQueryParts.mSelection = sbSelection.toString();
		} else if(rawSearchValues.length > 0) {
			sbTitle.append(rawSearchValues[0]);
			theQueryParts.mSelection = param.mQueryColumn + " like ?";

			String FormattedSeachValue = "%" + rawSearchValues[0] + "%";
			theQueryParts.mSelectionArgs = new String[] { FormattedSeachValue };
		} else {
			//Just so we dont blow up
			theQueryParts.mSelection = param.mQueryColumn + " like ?";
			theQueryParts.mSelectionArgs = new String[] { "%" };
		}

		theQueryParts.mTitle = sbTitle.toString();

		return theQueryParts;
	}
	
	public static QueryParts browseICD9All(BrowseParam theParam) {
		QueryParts theQueryParts = new QueryParts();

		if (theParam.isBrowseByFolder()) {
			theQueryParts.mTitle = "Browsing all ICD9 in folder " + theParam.getFolderName();
			theQueryParts.mProjection = getICD9FolderProjection();
		} else {
			theQueryParts.mTitle = "Browsing all ICD9";
			theQueryParts.mProjection = getICD9Projection();
		}

		return theQueryParts;
	}

	public static QueryParts browseICD9Code(BrowseParam theParam) {
		QueryParam param = new QueryParam();
		param.mProjection = theParam.isBrowseByFolder() ? getICD9FolderProjection() : getICD9Projection();
		param.mQueryColumn = ICD9.ICD9_CODE;
		param.mTitle = theParam.isBrowseByFolder() ? theParam.getFolderName() + " codes starting with " : "Codes starting with ";
		param.mRawSearchValue = theParam.getRawSearchValue();
		
		return browseCode(param);
	}
	
	public static QueryParts browseICD9Desc(BrowseParam theParam) {
		QueryParam param = new QueryParam();
		param.mProjection = theParam.isBrowseByFolder() ? getICD9FolderProjection() : getICD9Projection();
		param.mQueryColumn = ICD9.LONG_DESC;
		param.mTitle = theParam.isBrowseByFolder() ? theParam.getFolderName() + " containing " : "Descriptions containing ";
		param.mRawSearchValue = theParam.getRawSearchValue();
		
		return browseDesc(param);
	}
	
	public static QueryParts browseICD10All(BrowseParam theParam){
		QueryParts theQueryParts = new QueryParts();

		if (theParam.isBrowseByFolder()) {
			theQueryParts.mTitle = "Browsing all ICD10 in folder " + theParam.getFolderName();
			theQueryParts.mProjection = getICD10FolderProjection();
		} else {
			theQueryParts.mTitle = "Browsing all ICD10";
			theQueryParts.mProjection = getICD10Projection();
		}
		return theQueryParts;
	}

	public static QueryParts browseICD10Code(BrowseParam theParam) {
		QueryParam param = new QueryParam();
		param.mProjection = theParam.isBrowseByFolder() ? getICD10FolderProjection() : getICD10Projection();
		param.mQueryColumn = ICD10.ICD10_CODE;
		param.mTitle = theParam.isBrowseByFolder() ? theParam.getFolderName() + " codes starting with " : "Codes starting with ";
		param.mRawSearchValue = theParam.getRawSearchValue();
		
		return browseCode(param);
	}
	
	public static QueryParts browseICD10Desc(BrowseParam theParam) {
		QueryParam param = new QueryParam();
		param.mProjection = theParam.isBrowseByFolder() ? getICD10FolderProjection() : getICD10Projection();
		param.mQueryColumn = ICD10.LONG_DESC;
		param.mTitle = theParam.isBrowseByFolder() ? theParam.getFolderName() + " containing " : "Descriptions containing ";
		param.mRawSearchValue = theParam.getRawSearchValue();
		
		return browseDesc(param);
	}

	public static QueryParts drillICD9X10(int ICD9ID){
		QueryParts theQueryParts = new QueryParts();
		theQueryParts.mProjection = getICD9X10Projection();
		theQueryParts.mUri = ICD9X10ContentProvider.CONTENT_URI_ICD9S
				.buildUpon()
				.appendPath(String.valueOf(ICD9ID))
				.appendPath(URI_PATH.ICD10S)
				.build();				
		return theQueryParts;
	}

	public static QueryParts drillICD10X9(int ICD10ID){
		QueryParts theQueryParts = new QueryParts();
		theQueryParts.mProjection = getICD10X9Projection();
		theQueryParts.mUri = ICD9X10ContentProvider.CONTENT_URI_ICD10S
				.buildUpon()
				.appendPath(String.valueOf(ICD10ID))
				.appendPath(URI_PATH.ICD9S)
				.build();			
		return theQueryParts;
	}
	
	public static String getICD9FolderSubQuery(SubQueryParam theParam) {
		QueryParts queryParts;
		QueryParam queryParam = new QueryParam();
		queryParam.mRawSearchValue = theParam.getRawSearchValue();
		
		if (theParam.getRawSearchValue().startsWith("#")) {
			queryParam.mQueryColumn = ICD9.ICD9_CODE;
			queryParts = browseCode(queryParam);
		} else {
			queryParam.mQueryColumn = ICD9.LONG_DESC;
			queryParts = browseDesc(queryParam);
		}

		String strSelection = queryParts.getSelection();
		for(String arg : queryParts.getSelectionArgs())
		{
			strSelection = strSelection.replaceFirst("\\?", "'" + arg + "'");
		}

		return strSelection;		
	}
	
	public static String getICD10FolderSubQuery(SubQueryParam theParam) {
		QueryParts queryParts;
		QueryParam queryParam = new QueryParam();
		queryParam.mRawSearchValue = theParam.getRawSearchValue();
		
		if (theParam.getRawSearchValue().startsWith("#")) {
			queryParam.mQueryColumn = ICD10.ICD10_CODE;
			queryParts = browseCode(queryParam);
		} else {
			queryParam.mQueryColumn = ICD10.LONG_DESC;
			queryParts = browseDesc(queryParam);
		}

		String strSelection = queryParts.getSelection();
		for(String arg : queryParts.getSelectionArgs())
		{
			strSelection = strSelection.replaceFirst("\\?", "'" + arg + "'");
		}

		return strSelection;		
	}
}
