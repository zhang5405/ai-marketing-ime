/** JNI: candidate with text + comment + label */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class CandidateProto {
    public String text;
    public String comment;
    public String label;
    public CandidateProto(String text, String comment, String label) {
        this.text = text; this.comment = comment; this.label = label;
    }
}
