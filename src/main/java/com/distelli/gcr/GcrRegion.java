/*
  $Id: $
  @file GcrRegion.java
  @brief Contains the GcrRegion.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr;

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

    public static GcrRegion getRegion(String region)
    {
        try {
            GcrRegion gcrRegion = GcrRegion.valueOf(region.toUpperCase());
            return gcrRegion;
        } catch(IllegalArgumentException iae) {
            return null;
        }
    }
}
