/*
  $Id: $
  @file GcrRegion.java
  @brief Contains the GcrRegion.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr;

import java.util.Arrays;

public enum GcrRegion
{
    DEFAULT("gcr.io"),
    US("us.gcr.io"),
    EU("eu.gcr.io"),
    ASIA("asia.gcr.io");

    private String _endpoint = null;

    GcrRegion(String endpoint)
    {
        _endpoint = endpoint;
    }

    public String getEndpoint()
    {
        return _endpoint;
    }

    public String getHttpsEndpoint()
    {
        return String.format("https://%s", _endpoint);
    }

    public static GcrRegion getRegion(String endpoint)
    {
        return Arrays.stream(values())
            .filter((region) -> region._endpoint.equalsIgnoreCase(endpoint))
            .findFirst()
            .orElse(null);
    }
}
