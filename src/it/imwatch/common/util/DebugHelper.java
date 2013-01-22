package it.imwatch.common.util;

import android.os.Environment;
import android.util.Log;


import it.imwatch.toolkit.BuildConfig;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Debugging helper class, contains static methods to determine the debug
 * status.
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
     * This is determined by the value of <code>BuildConfig.DEBUG</code> (which is set
     * at build time to true iff the app is exported as a release build) or by the
     * presence of a file on the root of the external storage whose filename is
     * <code>imWatch-DEBUG</code>.
     * <p/>
     * Please note that if {@link #isVerboseDebug()} returns <code>true</code>, it is
     * assumed that regular debug is enabled as well, so in that case this method
     * returns <code>true</code> even if there's no <code>imWatch-DEBUG</code> file in
     * <code>/imdata</code>.
     * <p/>
     * If the external storage is currently unmounted, the file is assumed not present.
     *
     * @return Returns <code>true</code> if debug mode is enabled, else <code>false</code>.
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
     * external storage which filename is <code>imWatch-DEBUG-VERBOSE</code>.
     * <p/>
     * If the external storage is currently unmounted, the file is assumed not present.
     *
     * @return Returns <code>true</code> if debug mode is enabled, else <code>false</code>.
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
        
