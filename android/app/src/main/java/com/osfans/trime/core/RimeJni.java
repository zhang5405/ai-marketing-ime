/*
 * Minimal JNI bridge for librime_jni.so.
 * Package and class name MUST be com.osfans.trime.core.RimeJni
 * to match the JNI native method name mangling in the .so file.
 *
 * The .so file was extracted from Trime APK (GPL-3.0) but this
 * is a minimal, independently written JNI declaration class.
 */

package com.osfans.trime.core;

@SuppressWarnings("unused")
public class RimeJni {

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

    /** Simulate a key sequence (e.g., pinyin input like "nihao") */
    public static native boolean simulateRimeKeySequence(String keySequence);

    /** Process a single key event */
    // public static native boolean processRimeKey(int keycode, int mask);

    /** Commit current composition text */
    public static native boolean commitRimeComposition();

    /** Clear current composition */
    public static native void clearRimeComposition();

    // ===== Output =====

    /** Get committed text and clear commit buffer */
    public static native RimeProto.CommitProto getRimeCommit();

    /** Get current context (composition + candidates menu) */
    public static native RimeProto.ContextProto getRimeContext();

    /** Get current engine status */
    // public static native RimeProto.StatusProto getRimeStatus();

    // ===== Candidates =====

    /** Select a candidate by index */
    public static native boolean selectRimeCandidate(int index, boolean global);

    // ===== Options =====

    /** Set a runtime option */
    public static native void setRimeOption(String option, boolean value);

    /** Get a runtime option */
    public static native boolean getRimeOption(String option);

    // ===== Utility =====

    /** Get raw input string */
    // public static native String getRimeRawInput();
}
