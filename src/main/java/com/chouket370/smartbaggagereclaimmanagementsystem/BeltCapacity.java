package com.chouket370.smartbaggagereclaimmanagementsystem;

public enum BeltCapacity {
    SMALL(50),
    MEDIUM(150),
    LARGE(300);

    private final int maxPassengers;

    BeltCapacity(int maxPassengers) {
        this.maxPassengers = maxPassengers;
    }

    public int getMaxPassengers() {
        return maxPassengers;
    }

    public static BeltCapacity forPassengerCount(int count) {
        if (count <= 50) return SMALL;
        if (count <= 150) return MEDIUM;
        return LARGE;
    }
}
