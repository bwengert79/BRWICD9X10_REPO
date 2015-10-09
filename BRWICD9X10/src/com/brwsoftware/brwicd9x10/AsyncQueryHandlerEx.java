package com.brwsoftware.brwicd9x10;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class AsyncQueryHandlerEx extends AsyncQueryHandler {
	
    private WeakReference<AsyncQueryListener> mListener;

    public interface AsyncQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
        void onInsertComplete(int token, Object cookie, Uri uri);
        void onUpdateComplete(int token, Object cookie, int result);
        void onDeleteComplete(int token, Object cookie, int result);
    }
	public AsyncQueryHandlerEx(ContentResolver cr) {
		super(cr);
	}
	
	public AsyncQueryHandlerEx(ContentResolver cr, AsyncQueryListener listener) {
		super(cr);	
		mListener = new WeakReference<AsyncQueryHandlerEx.AsyncQueryListener>(listener);
	}
	
	public void setListener(AsyncQueryListener listener) {
		if(listener == null) {
			mListener = null;
		} else {
			mListener = new WeakReference<AsyncQueryHandlerEx.AsyncQueryListener>(listener);
		}
	}
	
	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		final AsyncQueryListener listener = mListener.get();
		if(listener != null) {
			listener.onQueryComplete(token, cookie, cursor);
		}
	}

	@Override
	protected void onInsertComplete(int token, Object cookie, Uri uri) {
		final AsyncQueryListener listener = mListener.get();
		if(listener != null) {
			listener.onInsertComplete(token, cookie, uri);
		}
	}

	@Override
	protected void onUpdateComplete(int token, Object cookie, int result) {
		final AsyncQueryListener listener = mListener.get();
		if(listener != null) {
			listener.onUpdateComplete(token, cookie, result);
		}
	}

	@Override
	protected void onDeleteComplete(int token, Object cookie, int result) {
		final AsyncQueryListener listener = mListener.get();
		if(listener != null) {
			listener.onDeleteComplete(token, cookie, result);
		}
	}
}
