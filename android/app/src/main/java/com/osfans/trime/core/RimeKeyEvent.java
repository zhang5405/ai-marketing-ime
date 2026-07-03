/** JNI: key event data */
package com.osfans.trime.core;

@SuppressWarnings("unused")
public class RimeKeyEvent {
    public int keycode, mask;
    public String keyevent;
    public RimeKeyEvent(int keycode, int mask, String keyevent) {
        this.keycode = keycode; this.mask = mask; this.keyevent = keyevent;
    }
}
