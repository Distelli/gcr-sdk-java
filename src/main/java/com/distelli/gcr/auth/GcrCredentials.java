/*
  $Id: $
  @file GcrCredentials.java
  @brief Contains the GcrCredentials.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.auth;

public interface GcrCredentials
{
    public String getHttpBasicAuthHeader();
}
