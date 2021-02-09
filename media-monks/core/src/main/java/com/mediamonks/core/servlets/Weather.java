package com.mediamonks.core.servlets;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "media-monks/components/weather",
        methods = HttpConstants.METHOD_GET,
        selectors = "api",
        extensions = "json")
@ServiceDescription("Weather API")
public class Weather extends SlingSafeMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Weather.class);

    @Reference
    com.mediamonks.core.services.Weather weatherService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String city = String.valueOf(request.getResource().getValueMap().getOrDefault("city", "Amsterdam"));
        String apiURL;
        String weatherData = "{}";

        response.setContentType("application/json");
        apiURL = weatherService.constructApiURI(city);
        if(StringUtils.isNotEmpty(apiURL)) {
            weatherData = weatherService.getWeatherDataFromCache(apiURL);
            response.getWriter().write(weatherData);
        } else {
            LOGGER.error("***** Weather Servlet :: doGet :: apiURL was empty *****");
            response.getWriter().write(weatherData);
        }
    }
}
