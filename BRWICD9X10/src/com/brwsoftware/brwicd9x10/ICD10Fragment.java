package com.brwsoftware.brwicd9x10;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10GROUPITEM;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10X9VIEW;

public class ICD10Fragment extends ICDFragment {

	@Override
	String getChildLabel() {
		return "ICD9";
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
		browseParam.setBrowseFavorites(icdSearch.isShowFavorites());
		browseParam.setRawSearchValue(icdSearch.getSearchValue());
		
		Uri uri = icdSearch.isShowFavorites() ?  ICD9X10ContentProvider.CONTENT_URI_ICD10FAV : ICD9X10ContentProvider.CONTENT_URI_ICD10;
        
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
		ICD9X10QueryBuilder.QueryParts qryParts = ICD9X10QueryBuilder.drillICD10X9(String.valueOf(icdSearch.getParentID()));

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				ICD9X10ContentProvider.CONTENT_URI_ICD10X9,
				qryParts.getProjection(), qryParts.getSelection(),
				qryParts.getSelectionArgs(), null);

		ICDCursorLoader icdCursorLoader = new ICDCursorLoader();
		icdCursorLoader.setCursorLoader(cursorLoader);

		return icdCursorLoader;
	}
	
	@Override
	void loadFavoriteAdd(ICDFavoriteManager.ICDFavorite icdFavorite) {
		int groupId = ICDFavoriteManager.getInstance().getICD10FavGroupID();
		if(icdFavorite.IsSingleItem()) {
			ContentValues cv = new ContentValues();
			cv.put(ICD10GROUPITEM.GROUP_ID, groupId);
			cv.put(ICD10GROUPITEM.ICD10_ID, icdFavorite.getParentID());
			
			icdFavorite.setUri(ICD9X10ContentProvider.CONTENT_URI_ICD10FAV);
			icdFavorite.setContentValues(cv);
		} else {
			ICD9X10QueryBuilder.FavoriteParam favParam = new ICD9X10QueryBuilder.FavoriteParam();
			favParam.setGroupID(groupId);
			favParam.setRawSearchValue(icdFavorite.getSearchValue());
			
			ContentValues cv = new ContentValues();
			cv.put(ICD10GROUPITEM.GROUP_ID, 0);
			cv.put(ICD10GROUPITEM.ICD10_ID, 0);
			cv.put(ICD9X10ContentProvider.INSERT_FROM_SELECT, ICD9X10QueryBuilder.getICD10FavoriteSubQuery(favParam));
			
			icdFavorite.setUri(ICD9X10ContentProvider.CONTENT_URI_ICD10FAV);
			icdFavorite.setContentValues(cv);
		}		
	}

	@Override
	void loadFavoriteDel(ICDFavoriteManager.ICDFavorite icdFavorite) {
		int groupId = ICDFavoriteManager.getInstance().getICD10FavGroupID();
		if(icdFavorite.IsSingleItem()) {
			String[] selectionArgs = 
				{
					String.valueOf(groupId),
					String.valueOf(icdFavorite.getParentID())
				};
			
			icdFavorite.setUri(ICD9X10ContentProvider.CONTENT_URI_ICD10FAV);
			icdFavorite.setSelection(ICD10GROUPITEM.GROUP_ID + " = ? and " + ICD10GROUPITEM.ICD10_ID + " = ?");
			icdFavorite.setSelectionArgs(selectionArgs);
		} else {
			String[] selectionArgs = 
				{
					String.valueOf(groupId)
				};
			icdFavorite.setUri(ICD9X10ContentProvider.CONTENT_URI_ICD10FAV);
			icdFavorite.setSelectionArgs(selectionArgs);
			
			if (TextUtils.isEmpty(icdFavorite.getSearchValue())) {
				icdFavorite.setSelection(ICD10GROUPITEM.GROUP_ID + " = ?");
			} else {
				ICD9X10QueryBuilder.FavoriteParam favParam = new ICD9X10QueryBuilder.FavoriteParam();
				favParam.setRawSearchValue(icdFavorite.getSearchValue());
				icdFavorite.setSelection(ICD10GROUPITEM.GROUP_ID + " = ? and " + ICD10GROUPITEM.ICD10_ID + " in (" + ICD9X10QueryBuilder.getICD10FavoriteSubQuery(favParam) + ")");
			}
		}
	}	
}
