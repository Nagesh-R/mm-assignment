package com.mediamonks.core.services;

public interface Weather {
    String constructApiURI(String city);
    String fetchWeatherData(String uri);
    String getWeatherDataFromCache(String apiURL);
    boolean flushWeatherCache();
}
