/** JNI: engine status flags */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class StatusProto {
    public String schemaId, schemaName;
    public boolean isDisabled, isComposing, isAsciiMode, isFullShape, isSimplified, isTraditional, isAsciiPunct;
    public StatusProto(String schemaId, String schemaName, boolean isDisabled, boolean isComposing,
                       boolean isAsciiMode, boolean isFullShape, boolean isSimplified,
                       boolean isTraditional, boolean isAsciiPunct) {
        this.schemaId = schemaId; this.schemaName = schemaName;
        this.isDisabled = isDisabled; this.isComposing = isComposing;
        this.isAsciiMode = isAsciiMode; this.isFullShape = isFullShape;
        this.isSimplified = isSimplified; this.isTraditional = isTraditional;
        this.isAsciiPunct = isAsciiPunct;
    }
}
