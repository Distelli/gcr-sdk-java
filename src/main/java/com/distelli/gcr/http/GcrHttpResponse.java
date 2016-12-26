/*
  $Id: $
  @file GcrHttpResponse.java
  @brief Contains the GcrHttpResponse.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.log4j.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GcrHttpResponse
{
    private static final Logger log = Logger.getLogger(GcrHttpResponse.class);
    private HttpResponse _httpResponse = null;
    private HttpRequestBase _httpRequest = null;
    private Map<String, String> _headers = null;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public GcrHttpResponse(HttpRequestBase httpRequest, HttpResponse httpResponse)
    {
        _httpResponse = httpResponse;
        _httpRequest = httpRequest;
        _headers = new HashMap<String, String>();
        Header[] responseHeaders = _httpResponse.getAllHeaders();
        for(Header header : responseHeaders)
            _headers.put(header.getName(), header.getValue());
    }

    public int getHttpStatusCode() {
        return _httpResponse.getStatusLine().getStatusCode();
    }

    public Map<String, String> getResponseHeaders() {
        return _headers;
    }

    public String getResponseHeader(String headerName) {
        return _headers.get(headerName);
    }

    public String getResponseAsJsonString()
        throws IOException
    {
        JsonNode jsonNode = getResponseAsJsonNode();
        if(jsonNode == null)
            return null;
        return OBJECT_MAPPER.writeValueAsString(jsonNode);
    }

    public JsonNode getResponseAsJsonNode()
        throws IOException
    {
        InputStream in = getInputStream();
        if(in == null)
            return null;
        return OBJECT_MAPPER.readTree(in);
    }

    public InputStream getInputStream()
        throws IOException
    {
        HttpEntity entity = _httpResponse.getEntity();
        if(entity == null)
            return null;

        InputStream result = entity.getContent();
        if(result != null)
            return result;

        return null;
    }
}
