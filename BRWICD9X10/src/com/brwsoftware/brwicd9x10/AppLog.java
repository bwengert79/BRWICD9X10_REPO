package com.brwsoftware.brwicd9x10;

import android.util.Log;

//The order in terms of verbosity, from least to most is:
//ERROR (6)
//WARN (5)
//INFO (4)
//DEBUG (3)
//VERBOSE (2)

//The default log level set by the Android OS is 4 and above
//Its the reverse for the levels of detail in that verbose is the most detailed log level you can use
//At bare minimum to get debug and verbose to show you have to set it, which you can do via:
//   adb shell setprops log.tag._tag_name_here_ DEBUG
//	 ***NOTE: tag name is case sensitive


//Note: This is loosely based on Google IO example application LogUtils class
public class AppLog {

	private static final String EMPTY = "";
	
    public static void v(final String tag, String message) {
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message);
        }
    }

    public static void v(final String tag, String message, Throwable cause) {
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message, cause);
        }
    }

    public static void v(final String tag, String format, Object... args) {
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
        	Log.v(tag, format(format, args));
        }
    }

	public static void v(String tag, String format, Throwable cause, Object... args) {
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
        	Log.v(tag, format(format, args), cause);
        }
	}
    
    @SuppressWarnings("unused")
	public static void d(final String tag, String message) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    @SuppressWarnings("unused")
	public static void d(final String tag, String message, Throwable cause) {
        if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);
        }
    }

    @SuppressWarnings("unused")
	public static void d(final String tag, String format, Object... args) {
    	if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
    		Log.d(tag, format(format, args));
    	}
    }

	@SuppressWarnings("unused")
	public static void d(String tag, String format, Throwable cause, Object... args) {
    	if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, format(format, args), cause);
    	}
	}

    public static void i(final String tag, String message) {
        Log.i(tag, message);
    }
    
    public static void i(final String tag, String message, Throwable cause) {
        Log.i(tag, message, cause);
    }

    public static void i(final String tag, String format, Object... args) {
        Log.i(tag, format(format, args));
    }

	public static void i(String tag, String format, Throwable cause, Object... args) {
		Log.i(tag, format(format, args), cause);
	}

	public static void w(final String tag, String message) {
        Log.w(tag, message);
    }

    public static void w(final String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void w(final String tag, String format, Object... args) {
        Log.w(tag, format(format, args));
    }

	public static void w(String tag, String format, Throwable cause, Object... args) {
		Log.w(tag, format(format, args), cause);
	}

    public static void e(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void e(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }


    public static void e(final String tag, String format, Object... args) {
        Log.e(tag, format(format, args));
    }

	public static void e(String tag, String format, Throwable cause, Object... args) {
		Log.e(tag, format(format, args), cause);
	}
    
	private static String format(String format, Object... args) {
		try {
			return String.format(format == null ? EMPTY : format, args);
		} catch (RuntimeException e) {
			AppLog.w("AppLog", "format error. reason=%s, format=%s", e.getMessage(), format);
			return String.format(EMPTY, format);
		}

	}
   
}
