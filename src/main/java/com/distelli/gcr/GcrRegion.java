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

    /**
     * Get the GcrRegion corresponding to the provided endpoint.
     *
     * @param endpoint the endpoint for the region, such as {@code "us.gcr.io"} or {@code "eu.gcr.io"}
     * @return the GcrRegion, or null if no such region exists
     */
    public static GcrRegion getRegionByEndpoint(String endpoint)
    {
        return Arrays.stream(values())
            .filter((region) -> region._endpoint.equalsIgnoreCase(endpoint))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get the GcrRegion corresponding to the associated short name.
     *
     * @param region the short name for the region, such as {@code "us"} or {@code "eu"}
     * @return the GcrRegion, or null if no such region exists
     */
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
