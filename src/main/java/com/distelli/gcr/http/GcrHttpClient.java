/*
  $Id: $
  @file GcrHttpClient.java
  @brief Contains the GcrHttpClient.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.http;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.distelli.gcr.auth.GcrCredentials;

import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.client.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcrHttpClient implements Closeable
{
    private static final Logger log = LoggerFactory.getLogger(GcrHttpClient.class);

    protected CloseableHttpClient _httpClient;
    private GcrCredentials _gcrCredentials;
    protected String _endpoint;

    public GcrHttpClient(GcrCredentials credentials)
    {
        try {
            HttpClientBuilder clientBuilder = HttpClients.custom();
            //TODO: Add SSL
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(100); //TODO: Configurable
            _httpClient = clientBuilder.build();
        } catch(Throwable t) {
            throw(new RuntimeException(t));
        }

        _gcrCredentials = credentials;
    }

    @Override
    public void close() throws IOException {
        _httpClient.close();
    }

    public void setEndpoint(String endpoint) {
        _endpoint = endpoint;
    }

    public String getEndpoint() {
        return _endpoint;
    }

    public RequestBuilder DELETE() {
        return new RequestBuilder(this, new HttpDelete());
    }
    public RequestBuilder GET() {
        return new RequestBuilder(this, new HttpGet());
    }
    public RequestBuilder PATCH() {
        return new RequestBuilder(this, new HttpPatch());
    }
    public RequestBuilder POST() {
        return new RequestBuilder(this, new HttpPost());
    }
    public RequestBuilder PUT() {
        return new RequestBuilder(this, new HttpPut());
    }

    private GcrCredentials getGcrCredentials()
    {
        return _gcrCredentials;
    }

    private HttpClient getHttpClient()
    {
        return _httpClient;
    }

    public static class RequestBuilder {
        private GcrHttpClient _client;
        private HttpRequestBase _request;
        private Map<String, String> _headers;
        private Map<String, String> _queryParams;
        private String _endpoint;
        private String _path;
        private RequestBuilder(GcrHttpClient client, HttpRequestBase request) {
            _client = client;
            _request = request;
            _headers = new HashMap<String, String>();
            _queryParams = new HashMap<String, String>();
            _endpoint = _client.getEndpoint();
        }

        public RequestBuilder withPath(String path) {
            _path = path;
            return this;
        }

        public RequestBuilder withQueryParams(Map<String, String> queryParams) {
            if(queryParams == null)
                return this;
            _queryParams.putAll(queryParams);
            return this;
        }

        public RequestBuilder withQueryParam(String key, String value) {
            _queryParams.put(key, value);
            return this;
        }

        public RequestBuilder withRequestHeader(String key, String value) {
            _headers.put(key, value);
            return this;
        }

        public RequestBuilder withRequestHeaders(Map<String, String> requestHeaders) {
            if(requestHeaders == null)
                return this;
            _headers.putAll(requestHeaders);
            return this;
        }

        public GcrHttpResponse execute()
            throws IOException
        {
            //Set the HttpRequestHeaders on the request
            for(Map.Entry<String, String> entry : _headers.entrySet())
                _request.addHeader(entry.getKey(), entry.getValue());

            //Add the authorization header
            GcrCredentials gcrCredentials = _client.getGcrCredentials();
            _request.addHeader("Authorization", gcrCredentials.getHttpBasicAuthHeader());

            //Now set the URI: endpoint + path + query params
            StringBuilder queryParamBuilder = new StringBuilder();
            List<String> queryParamPairs = new ArrayList<String>();
            for(Map.Entry<String, String> queryParam : _queryParams.entrySet())
            {
                try {
                    String paramValue = queryParam.getValue();
                    if(paramValue != null)
                    {
                        paramValue = URLEncoder.encode(paramValue, "UTF-8");
                        queryParamPairs.add(queryParam.getKey()+"="+paramValue);
                    }
                } catch(UnsupportedEncodingException usee) {
                    //cannot happen
                    throw(new RuntimeException(usee));
                }
            }

            String queryString = "";
            if(queryParamPairs.size() > 0)
                queryString = "?"+String.join("&", queryParamPairs);

            URI uri = URI.create(_endpoint+"/"+_path+queryString);
            _request.setURI(uri);
            HttpResponse httpResponse = _client.getHttpClient().execute(_request);

            return new GcrHttpResponse(_request, httpResponse);
        }
    }
}
