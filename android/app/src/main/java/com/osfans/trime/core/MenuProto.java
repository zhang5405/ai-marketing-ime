/** JNI: candidates menu */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class MenuProto {
    public int pageSize, pageNumber, highlightedCandidateIndex;
    public boolean isLastPage;
    public CandidateProto[] candidates;
    public String selectKeys;
    public String[] selectLabels;
    public MenuProto(int pageSize, int pageNumber, boolean isLastPage,
                     int highlightedCandidateIndex, CandidateProto[] candidates,
                     String selectKeys, String[] selectLabels) {
        this.pageSize = pageSize; this.pageNumber = pageNumber;
        this.isLastPage = isLastPage;
        this.highlightedCandidateIndex = highlightedCandidateIndex;
        this.candidates = candidates;
        this.selectKeys = selectKeys; this.selectLabels = selectLabels;
    }
}
