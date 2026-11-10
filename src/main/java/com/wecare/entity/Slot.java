package com.wecare.entity;

import java.time.LocalTime;

/** One-hour appointment slots: 9 AM - 1 PM and 2 PM - 6 PM. */
public enum Slot {
    S09(9), S10(10), S11(11), S12(12), S14(14), S15(15), S16(16), S17(17);

    private final int hour;
    Slot(int hour) { this.hour = hour; }
    public LocalTime start() { return LocalTime.of(hour, 0); }
    public String label() { return fmt(hour) + " - " + fmt(hour + 1); }
    private static String fmt(int h) { int x = h % 12 == 0 ? 12 : h % 12; return String.format("%02d:00 %s", x, h < 12 ? "AM" : "PM"); }
}
