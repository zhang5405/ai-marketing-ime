/** JNI: preedit state */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class CompositionProto {
    public int length, cursorPos, selStart, selEnd;
    public String preedit, commitTextPreview;
    public CompositionProto(int length, int cursorPos, int selStart, int selEnd,
                            String preedit, String commitTextPreview) {
        this.length = length; this.cursorPos = cursorPos;
        this.selStart = selStart; this.selEnd = selEnd;
        this.preedit = preedit; this.commitTextPreview = commitTextPreview;
    }
}
