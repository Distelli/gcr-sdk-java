/*
  $Id: $
  @file GcrClient.java
  @brief Contains the GcrClient.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.distelli.gcr.models.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.http.*;
import com.distelli.gcr.serializers.*;
import com.distelli.gcr.exceptions.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcrClient
{
    private static final Logger log = LoggerFactory.getLogger(GcrClient.class);

    private GcrHttpClient _httpClient = null;

    public GcrClient(GcrCredentials gcrCredentials, GcrRegion gcrRegion)
    {
        _httpClient = new GcrHttpClient(gcrCredentials);
        if(gcrRegion == null)
            gcrRegion = GcrRegion.DEFAULT;
        _httpClient.setEndpoint(gcrRegion.getHttpsEndpoint());
    }

    public List<GcrRepository> listRepositories(GcrIterator iterator)
        throws IOException, GcrException
    {
        GcrHttpClient.RequestBuilder requestBuilder = _httpClient.GET()
        .withPath("v2/_catalog");

        if(iterator != null)
        {
            requestBuilder.withQueryParam("n", ""+iterator.getPageSize());
            requestBuilder.withQueryParam("last", iterator.getMarker());
        }

        GcrHttpResponse httpResponse = requestBuilder.execute();
        if(iterator != null)
        {
            String linkHeader = httpResponse.getResponseHeader("Link");
            iterator.updateMarker(linkHeader);
        }
        int httpStatusCode = httpResponse.getHttpStatusCode();
        JsonNode responseJson = httpResponse.getResponseAsJsonNode();
        if(httpStatusCode / 100 != 2)
        {
            List<GcrError> errors = GcrErrorSerializer.deserialize(responseJson);
            throw(new GcrException(errors));
        }

        return GcrRepositorySerializer.deserializeList(responseJson);
    }

    public List<GcrImageTag> listImageTags(GcrRepository repository,
                                           GcrIterator iterator)
        throws IOException, GcrException
    {
        return listImageTags(String.format("%s/%s",
                                           repository.getProjectName(),
                                           repository.getRepositoryName()),
                             iterator);
    }

    public List<GcrImageTag> listImageTags(String repository, GcrIterator iterator)
        throws IOException, GcrException
    {
        GcrHttpClient.RequestBuilder requestBuilder = _httpClient.GET()
        .withPath("/v2/"+repository+"/tags/list");

        if(iterator != null)
        {
            requestBuilder.withQueryParam("n", ""+iterator.getPageSize());
            requestBuilder.withQueryParam("last", iterator.getMarker());
        }

        GcrHttpResponse httpResponse = requestBuilder.execute();
        if(iterator != null)
        {
            String linkHeader = httpResponse.getResponseHeader("Link");
            iterator.updateMarker(linkHeader);
        }
        int httpStatusCode = httpResponse.getHttpStatusCode();
        JsonNode responseJson = httpResponse.getResponseAsJsonNode();
        if(httpStatusCode / 100 != 2)
        {
            List<GcrError> errors = GcrErrorSerializer.deserialize(responseJson);
            throw(new GcrException(errors));
        }

        return GcrImageTagSerializer.deserializeList(responseJson);
    }
}
