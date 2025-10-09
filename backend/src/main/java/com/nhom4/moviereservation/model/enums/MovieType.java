package com.nhom4.moviereservation.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MovieType {
    ComingSoon(0),
    NowShowing(1),
    Removed(2);

    private final int value;

    MovieType(int value) {
        this.value = value;
    }

    @JsonValue // Khi serialize (Java -> JSON), trả về String name
    public String toValue() {
        return this.name();
    }

    public int getValue() {
        return value;
    }

    // Khi deserialize (JSON -> Java), chuyển từ String sang Enum
    @JsonCreator
    public static MovieType fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return MovieType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Nếu không match, thử parse theo số
            try {
                int intValue = Integer.parseInt(value);
                return fromInt(intValue);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid MovieType value: " + value);
            }
        }
    }

    // Helper method để convert từ int
    public static MovieType fromInt(int value) {
        for (MovieType type : MovieType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MovieType value: " + value);
    }
}