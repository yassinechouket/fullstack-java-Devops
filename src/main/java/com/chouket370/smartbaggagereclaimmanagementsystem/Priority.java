package com.chouket370.smartbaggagereclaimmanagementsystem;

public enum Priority {
    EMERGENCY(4), HIGH(3), MEDIUM(2), LOW(1);

    private final int weight;

    Priority(int weight) {
        this.weight = weight;
    }

    public static Priority fromDelayMinutes(long delayMinutes) {
        if (delayMinutes > 60) return HIGH;
        if (delayMinutes > 30) return MEDIUM;
        return LOW;
    }
}
