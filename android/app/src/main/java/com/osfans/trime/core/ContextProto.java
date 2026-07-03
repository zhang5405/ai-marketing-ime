/** JNI: input context */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class ContextProto {
    public CompositionProto composition;
    public MenuProto menu;
    public String input;
    public int caretPos;
    public ContextProto(CompositionProto composition, MenuProto menu,
                        String input, int caretPos) {
        this.composition = composition; this.menu = menu;
        this.input = input; this.caretPos = caretPos;
    }
}
