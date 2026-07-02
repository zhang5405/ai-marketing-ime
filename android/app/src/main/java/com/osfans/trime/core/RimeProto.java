/*
 * Minimal JNI data classes matching librime_jni.so native interface.
 * Package and field names must match exactly what the native code expects.
 */

package com.osfans.trime.core;

/** Committed text from RIME engine */
public class RimeProto {
    public static class CommitProto {
        public String text;
    }

    public static class CandidateProto {
        public String text;
        public String comment;
        public String label;
    }

    public static class CompositionProto {
        public int length;
        public int cursorPos;
        public int selStart;
        public int selEnd;
        public String preedit;
        public String commitTextPreview;
    }

    public static class MenuProto {
        public int pageSize;
        public int pageNumber;
        public boolean isLastPage;
        public int highlightedCandidateIndex;
        public CandidateProto[] candidates;
        public String selectKeys;
        public String[] selectLabels;
    }

    public static class ContextProto {
        public CompositionProto composition;
        public MenuProto menu;
        public String input;
        public int caretPos;
    }

    public static class StatusProto {
        public String schemaId;
        public String schemaName;
        public boolean isDisabled;
        public boolean isComposing;
        public boolean isAsciiMode;
        public boolean isFullShape;
        public boolean isSimplified;
        public boolean isTraditional;
        public boolean isAsciiPunct;
    }
}
