/** JNI: lombok-style data class for RimeCandidate -> Java */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class CandidateItem {
    public String text;
    public String comment;
    public CandidateItem(String text, String comment) { this.text = text; this.comment = comment; }
}
