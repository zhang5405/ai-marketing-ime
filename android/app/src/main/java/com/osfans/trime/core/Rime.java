/*
 * JNI bridge for librime_jni.so.
 * Class name MUST be "Rime" — the .so's JNI_OnLoad does
 * FindClass("com/osfans/trime/core/Rime") and RegisterNatives.
 */

package com.osfans.trime.core;

@SuppressWarnings("unused")
public class Rime {

    private static volatile boolean loaded = false;

    static {
        try {
            System.loadLibrary("rime_jni");
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load librime_jni.so: " + e.getMessage());
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    // ===== Lifecycle =====

    /** Initialize RIME engine */
    public static native void startupRime(
            String sharedDir,
            String userDir,
            String versionName,
            boolean fullCheck
    );

    /** Shutdown RIME engine */
    public static native void exitRime();

    // ===== Input =====

    /** Simulate key sequence (e.g., "nihao") */
    public static native boolean simulateRimeKeySequence(String keySequence);

    /** Commit current composition */
    public static native boolean commitRimeComposition();

    /** Clear current composition */
    public static native void clearRimeComposition();

    // ===== Output =====

    /** Get committed text */
    public static native RimeProto.CommitProto getRimeCommit();

    /** Get context (composition + candidates) */
    public static native RimeProto.ContextProto getRimeContext();

    // ===== Candidates =====

    /** Select candidate by index */
    public static native boolean selectRimeCandidate(int index, boolean global);

    // ===== Options =====

    /** Set runtime option */
    public static native void setRimeOption(String option, boolean value);

    /** Get runtime option */
    public static native boolean getRimeOption(String option);
}
