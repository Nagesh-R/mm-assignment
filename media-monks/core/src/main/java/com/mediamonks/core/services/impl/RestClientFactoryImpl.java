package com.mediamonks.core.services.impl;

import com.mediamonks.core.services.RestClientFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component(service = RestClientFactory.class, immediate = true)
@Designate(ocd = RestClientFactoryImpl.Config.class)
public class RestClientFactoryImpl implements RestClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientFactoryImpl.class);
    private MultiThreadedHttpConnectionManager conMgr;
    int connectionTimeOut;
    int readTimeOut;
    int maxTotalConnections;
    int defaultMaxConnectionsPerHost;

    @Activate
    public final void activate(final Config config) {
        connectionTimeOut = config.connectionTimeOut();
        readTimeOut = config.readTimeOut();
        maxTotalConnections = config.maxTotalConnections();
        defaultMaxConnectionsPerHost = config.defaultMaxConnectionsPerHost();
    }

    @Override
    public String sendGetRequest(String uri) {
        String response = "{}";
        if(StringUtils.isNotEmpty(uri)){
            MultiThreadedHttpConnectionManager connectionManager = getMultiThreadedConf();
            HttpClient httpClient = new HttpClient(connectionManager);
            GetMethod getMethod = new GetMethod(uri);
            try {
                getMethod.setRequestHeader("ContentType","application/json");
                HttpMethodParams params = new HttpMethodParams();
                params.setParameter("ContentType", "application/json");
                params.setParameter("charset", "UTF-8");
                getMethod.setParams(params);
                int httpStatus = httpClient.executeMethod(getMethod);
                if(httpStatus == 200) {
                    response = getMethod.getResponseBodyAsString();
                } else {
                    LOGGER.error("***** RestClientFactoryImpl :: sendGetRequest :: Failed to fetch data from URI *****");
                }

            } catch (UnsupportedEncodingException e) {
                LOGGER.error("***** RestClientFactoryImpl :: sendGetRequest :: Unsupported Encoding Exception has occurred *****", e);
            } catch (HttpException e) {
                LOGGER.error("***** RestClientFactoryImpl :: sendGetRequest :: Http Exception has occurred *****", e);
            } catch (IOException e) {
                LOGGER.error("***** RestClientFactoryImpl :: sendGetRequest :: IO Exception has occurred *****", e);
            } finally {
                getMethod.releaseConnection();
            }
        }else {
            LOGGER.error("***** RestClientFactoryImpl :: sendGetRequest :: The requested URI is empty *****");
        }
        return response;
    }

    @ObjectClassDefinition(name = "RestClientFactoryImpl")
    public @interface Config {

        @AttributeDefinition(name = "HttpConnectionTimeOut in milliseconds")
        int connectionTimeOut() default 10000;

        @AttributeDefinition(name = "ReadTimeOut in milliseconds")
        int readTimeOut() default 10000;

        @AttributeDefinition(name = "Max Total Connections")
        int maxTotalConnections() default 100;

        @AttributeDefinition(name = "Default Max Connection per Host")
        int defaultMaxConnectionsPerHost() default 100;
    }

    private MultiThreadedHttpConnectionManager getMultiThreadedConf() {
        if (conMgr == null) {
            conMgr = new MultiThreadedHttpConnectionManager();
            conMgr.getParams().setConnectionTimeout(connectionTimeOut);
            conMgr.getParams().setSoTimeout(readTimeOut);
            conMgr.getParams().setMaxTotalConnections(maxTotalConnections);
            conMgr.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnectionsPerHost);
        }
        return conMgr;
    }
}
