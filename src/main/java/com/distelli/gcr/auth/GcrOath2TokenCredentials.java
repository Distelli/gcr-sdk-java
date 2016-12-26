/*
  $Id: $
  @file GcrOath2TokenCredentials.java
  @brief Contains the GcrOath2TokenCredentials.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.auth;

import lombok.*;
import java.util.Base64;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrOath2TokenCredentials implements GcrCredentials
{
    private String oauth2AccessToken;

    @Override
    public String getHttpBasicAuthHeader()
    {
        String encoded = Base64.getEncoder().encodeToString(String.format("_token:%s", this.oauth2AccessToken).getBytes());
        return String.format("Basic %s", encoded);
    }
}
