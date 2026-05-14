package com.suryashakti.solar.utils;

import java.util.Random;

public class WeatherSimulator {

    public static final String SUNNY = "Sunny";
    public static final String CLOUDY = "Cloudy";
    public static final String PARTLY_CLOUDY = "Partly Cloudy";

    /**
     * Simulates daily generation based on weather condition.
     * Uses random variation.
     */
    public static float simulateDailyGeneration(String weather, float panelCapacityKw) {
        float peakHours;
        switch (weather) {
            case SUNNY:
                peakHours = 5.5f + new Random().nextFloat(); // 5.5 - 6.5 hrs
                break;
            case PARTLY_CLOUDY:
                peakHours = 3.0f + new Random().nextFloat() * 2; // 3 - 5 hrs
                break;
            case CLOUDY:
            default:
                peakHours = 1.0f + new Random().nextFloat() * 1.5f; // 1 - 2.5 hrs
                break;
        }
        return panelCapacityKw * peakHours;
    }

    /**
     * Provides a stable estimate for UI display (average values).
     */
    public static float estimateDailyGeneration(String weather, float panelCapacityKw) {
        float avgPeakHours;
        switch (weather) {
            case SUNNY:
                avgPeakHours = 6.2f;
                break;
            case PARTLY_CLOUDY:
                avgPeakHours = 4.0f;
                break;
            case CLOUDY:
            default:
                avgPeakHours = 1.8f;
                break;
        }
        return panelCapacityKw * avgPeakHours;
    }

    /**
     * Returns emoji for weather condition
     */
    public static String getWeatherEmoji(String weather) {
        switch (weather) {
            case SUNNY: return "☀️";
            case PARTLY_CLOUDY: return "⛅";
            default: return "☁️";
        }
    }
}
