package it.imwatch.common.util;

import android.os.Environment;
import android.util.Log;


import it.imwatch.toolkit.BuildConfig;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Debugging helper class, contains static methods to determine the debug
 * status.
 * <p/>
 * Created on: 10/9/12 Time: 10:19 AM
 * File version: 1.0
 * <p/>
 * Changelog:
 * Version 1.0
 * * Initial revision
 */
public class DebugHelper {

    /** Debugging tag */
    private static final String TAG = DebugHelper.class.getSimpleName();

    /** The filename of the verbose debug file. */
    public static final String VERBOSE_DEBUG_FILE = "imWatch-DEBUG-VERBOSE";

    /** The filename of the debug file. */
    public static final String DEBUG_FILE = "imWatch-DEBUG";

    /** The directory where the debug and verbose debug files are supposed to be. */
    public static final File FILES_PATH = Environment.getExternalStorageDirectory();

    /**
     * Gets a value indicating if the current environment is a debugging environment.
     * This is determined by the value of <pre>BuildConfig.DEBUG</pre> (which is set
     * at build time to true iff the app is exported as a release build) or by the
     * presence of a file on the root of the external storage whose filename is
     * <pre>imWatch-DEBUG</pre>.
     * <p/>
     * Please note that if {@link #isVerboseDebug()] returns <pre>true</pre>, it is
     * assumed that regular debug is enabled as well, so in that case this method
     * returns <pre>true</pre> even if there's no imWatch-DEBUG file in
     * <pre>/imdata</pre>.
     * <p/>
     * If the external storage is currently unmounted, the file is assumed not present.
     *
     * @return Returns <pre>true</pre> if debug mode is enabled, else <pre>false</pre>.
     */
    public static boolean isDebug() {
        String sdcardState = Environment.getExternalStorageState();
        final File[] files;

        if (Environment.MEDIA_MOUNTED.equals(sdcardState) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdcardState)) {

            files = FILES_PATH.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    return dir.getAbsolutePath().equals(FILES_PATH.getAbsolutePath()) &&
                           filename.equals(DEBUG_FILE);
                }
            });
        }
        else {
            files = new File[0];
        }

        // Disable IDEA's inspections that would want to simplify the value
        // (from their perspective, BuildConfig.DEBUG has a constant value)
        //noinspection ConstantConditions,PointlessBooleanExpression
        final boolean debug = !BuildConfig.DEBUG || files.length > 0 ||  isVerboseDebug();

        if (debug) Log.i(TAG, "Debug mode is active.");

        return debug;
    }

    /**
     * Gets a value indicating if the current environment is a verbose debugging environment.
     * This is determined by the value of by the presence of a file on the root of the
     * external storage which filename is <pre>imWatch-DEBUG-VERBOSE</pre>.
     * <p/>
     * If the external storage is currently unmounted, the file is assumed not present.
     *
     * @return Returns <pre>true</pre> if debug mode is enabled, else <pre>false</pre>.
     */
    public static boolean isVerboseDebug() {
        String sdcardState = Environment.getExternalStorageState();
        final File[] files;

        if (Environment.MEDIA_MOUNTED.equals(sdcardState) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdcardState)) {

            files = FILES_PATH.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    return dir.getAbsolutePath().equals(FILES_PATH.getAbsolutePath()) &&
                           filename.equals(VERBOSE_DEBUG_FILE);
                }
            });
        }
        else {
            files = new File[0];
        }

        final boolean verbose = files.length > 0;

        if (verbose) Log.i(TAG, "Verbose debug mode is active.");

        return verbose;
    }
}
        
