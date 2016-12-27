/*
  $Id: $
  @file GcrServiceAccountCredentials.java
  @brief Contains the GcrServiceAccountCredentials.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.auth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.distelli.gcr.exceptions.*;

public class GcrServiceAccountCredentials implements GcrCredentials
{
    private String jsonKey;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public GcrServiceAccountCredentials(File jsonKeyFile)
        throws GcrAuthException
    {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonKeyFile);
            this.jsonKey = OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch(JsonProcessingException jpe) {
            throw(new GcrAuthException(jpe));
        } catch(IOException ioe) {
            throw(new RuntimeException(ioe));
        }
    }

    public GcrServiceAccountCredentials(String jsonKey)
    {
        this.jsonKey = jsonKey;
    }

    public GcrServiceAccountCredentials(InputStream jsonKey)
        throws GcrAuthException
    {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonKey);
            this.jsonKey = OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch(JsonProcessingException jpe) {
            throw(new GcrAuthException(jpe));
        } catch(IOException ioe) {
            throw(new RuntimeException(ioe));
        }
    }

    @Override
    public String getHttpBasicAuthHeader()
    {
        String encoded = Base64.getEncoder().encodeToString(String.format("_json_key:%s", this.jsonKey).getBytes());
        return String.format("Basic %s", encoded);
    }
}
