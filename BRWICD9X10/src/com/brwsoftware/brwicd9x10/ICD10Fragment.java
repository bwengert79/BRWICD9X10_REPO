package com.brwsoftware.brwicd9x10;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.brwsoftware.brwicd9x10.ICD9X10ContentProvider.URI_PATH;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10X9VIEW;

public class ICD10Fragment extends ICDFragment {
	
	@Override
	ICDAttributes getICDAttributes() {
		ICDAttributes attr = new ICDAttributes();
		
		attr.setICDType(10);
		attr.setChildLabel("ICD9");
		attr.setPrefKeyFolderID(AppValue.PREFKEY_CURRENT_ICD10_FOLDER_ID);
		attr.setPrefKeyFolderName(AppValue.PREFKEY_CURRENT_ICD10_FOLDER_NAME);
		
		return attr;
	}

	@Override
	ICDAdapter getNewAdapter() {
		ICDAdapter adapter = new ICDAdapter(
				getActivity(),
				null,
				R.layout.group_parent, 
				new String[] { ICD10.ICD10_CODE, ICD10.LONG_DESC }, 
				new int[] { R.id.icd_code, R.id.icd_desc }, 
				R.layout.group_child, 
				new String[] { ICD10X9VIEW.ICD9_CODE, ICD10X9VIEW.ICD9_LONG_DESC }, 
				new int[] {R.id.icd_code, R.id.icd_desc });
		
		return adapter;
	}

	@Override
	ICDCursorLoader getNewParentCursorLoader(ICDSearch icdSearch) {
		CursorLoader cursorLoader;
		ICD9X10QueryBuilder.QueryParts qryParts;
		ICDCursorLoader icdCursorLoader = new ICDCursorLoader();
		String searchValue = icdSearch.getSearchValue();
		
		ICD9X10QueryBuilder.BrowseParam browseParam = new ICD9X10QueryBuilder.BrowseParam();
		browseParam.setBrowseByFolder(icdSearch.isFolderView(), icdSearch.getFolderName());
		browseParam.setRawSearchValue(icdSearch.getSearchValue());
		
		Uri uri = icdSearch.isFolderView() ? ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS
				.buildUpon()
				.appendPath(String.valueOf(icdSearch.getFolderID()))
				.appendPath(URI_PATH.ICD10S)
				.build()
				: ICD9X10ContentProvider.CONTENT_URI_ICD10S;
				
		//For now limit the results to the number of rows specified by QUERY_PARAMETER_LIMIT. 
		//Eventually I would like to implement some sort of virtual scrolling using OFFSET and LIMIT.
		uri = uri.buildUpon()
				.appendQueryParameter(ICD9X10ContentProvider.QUERY_PARAMETER_LIMIT, String.valueOf(icdSearch.getLimit()))
                .build();
        
		if (TextUtils.isEmpty(searchValue)) {
			qryParts = ICD9X10QueryBuilder.browseICD10All(browseParam);
			cursorLoader = new CursorLoader(getActivity(),
					uri,
					qryParts.getProjection(), null, null, null);
		} else if (searchValue.startsWith("#")) {
			qryParts = ICD9X10QueryBuilder.browseICD10Code(browseParam);
			cursorLoader = new CursorLoader(getActivity(),
					uri,
					qryParts.getProjection(), qryParts.getSelection(),
					qryParts.getSelectionArgs(), null);
		} else {
			qryParts = ICD9X10QueryBuilder.browseICD10Desc(browseParam);
			cursorLoader = new CursorLoader(getActivity(),
					uri,
					qryParts.getProjection(), qryParts.getSelection(),
					qryParts.getSelectionArgs(), null);
		}
		
		icdCursorLoader.setCursorLoader(cursorLoader);
		icdCursorLoader.setQueryDesc(qryParts.getTitle());
		
		return icdCursorLoader;
	}

	@Override
	ICDCursorLoader getNewChildCursorLoader(ICDSearch icdSearch) {
		ICD9X10QueryBuilder.QueryParts qryParts = ICD9X10QueryBuilder.drillICD10X9(icdSearch.getParentID());

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				qryParts.getUri(), qryParts.getProjection(), null, null, null);

		ICDCursorLoader icdCursorLoader = new ICDCursorLoader();
		icdCursorLoader.setCursorLoader(cursorLoader);

		return icdCursorLoader;
	}
	
	@Override
	void loadFolderAdd(ICDFolder icdFolder) {
		if(icdFolder.IsSingleItem()) {
			Uri uri = ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS
					.buildUpon()
					.appendPath(String.valueOf(icdFolder.getFolderID()))
					.appendPath(URI_PATH.ICD10S)
					.appendPath(String.valueOf(icdFolder.getItemID()))
					.build();
			
			icdFolder.setUri(uri);
		} else {
			Uri uri = ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS
					.buildUpon()
					.appendPath(String.valueOf(icdFolder.getFolderID()))
					.appendPath(URI_PATH.ICD10S)
					.build();
			
			ICD9X10QueryBuilder.SubQueryParam favParam = new ICD9X10QueryBuilder.SubQueryParam();
			favParam.setRawSearchValue(icdFolder.getSearchValue());
			
			ContentValues cv = new ContentValues();
			cv.put(ICD9X10ContentProvider.INSERT_FROM_SELECT, ICD9X10QueryBuilder.getICD10FolderSubQuery(favParam));
			
			icdFolder.setUri(uri);
			icdFolder.setContentValues(cv);
		}		
	}

	@Override
	void loadFolderDel(ICDFolder icdFolder) {
		if(icdFolder.IsSingleItem()) {
			Uri uri = ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS
					.buildUpon()
					.appendPath(String.valueOf(icdFolder.getFolderID()))
					.appendPath(URI_PATH.ICD10S)
					.appendPath(String.valueOf(icdFolder.getItemID()))
					.build();
			icdFolder.setUri(uri);
		} else {
			Uri uri = ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS
					.buildUpon()
					.appendPath(String.valueOf(icdFolder.getFolderID()))
					.appendPath(URI_PATH.ICD10S)
					.build();
			icdFolder.setUri(uri);
			
			if (!TextUtils.isEmpty(icdFolder.getSearchValue())) {
				ICD9X10QueryBuilder.SubQueryParam favParam = new ICD9X10QueryBuilder.SubQueryParam();
				favParam.setRawSearchValue(icdFolder.getSearchValue());
				
				icdFolder.setSelection(ICD9X10ContentProvider.DELETE_FROM_SELECT);
				icdFolder.setSelectionArgs(new String[] {ICD9X10QueryBuilder.getICD10FolderSubQuery(favParam)});
			}
		}
	}	
}
