package com.brwsoftware.brwicd9x10;

import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD10FOLDER;
import com.brwsoftware.brwicd9x10.ICD9X10Database.ICD9FOLDER;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

public class FoldersDialog extends DialogFragment implements LoaderCallbacks<Cursor> {
    
	public static final int ICDTYPE_9 = 9;
	public static final int ICDTYPE_10 = 10;
    SimpleCursorAdapter mAdapter;
    ListView mListView;
    int mICDType;
    FoldersDialogListener mListener;
    
    public interface FoldersDialogListener {
        public void onFolderSelected(int folderID, String folderName);
    }
    
	public void initialize(int icdType, FoldersDialogListener listener) {
		mICDType = icdType;
		mListener = listener;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if ((mICDType != ICDTYPE_9 && mICDType != ICDTYPE_10)
				||
				mListener == null) {
			throw new RuntimeException("FolderDialog not initialized properly");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title;
		if (mICDType == ICDTYPE_10) {
			title = "ICD10 Folders";
			mAdapter = new SimpleCursorAdapter(getActivity(),
					R.layout.folder_item, null,
					new String[] { ICD10FOLDER.NAME },
					new int[] { R.id.folder_name }, 0);
		} else {
			title = "ICD9 Folders";
			mAdapter = new SimpleCursorAdapter(getActivity(),
					R.layout.folder_item, null,
					new String[] { ICD9FOLDER.NAME },
					new int[] { R.id.folder_name }, 0);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title).setAdapter(mAdapter,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Cursor cursor = mAdapter.getCursor();
						cursor.moveToPosition(which);
						mListener.onFolderSelected(cursor.getInt(0), cursor.getString(1));
					}
				});
		getActivity().getSupportLoaderManager().initLoader(mICDType, null, this);
		return builder.show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader cursorLoader;
		if (arg0 == ICDTYPE_10) {
			cursorLoader = new CursorLoader(getActivity(),
					ICD9X10ContentProvider.CONTENT_URI_ICD10FOLDERS,
					new String[] { ICD10FOLDER._ID, ICD10FOLDER.NAME }, null,
					null, null);
		} else {
			cursorLoader = new CursorLoader(getActivity(),
					ICD9X10ContentProvider.CONTENT_URI_ICD9FOLDERS,
					new String[] { ICD9FOLDER._ID, ICD10FOLDER.NAME }, null,
					null, null);
		}

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		mAdapter.swapCursor(arg1);	
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

}
