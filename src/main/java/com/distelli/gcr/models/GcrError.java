/*
  $Id: $
  @file GcrError.java
  @brief Contains the GcrError.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrError
{
    protected String code;
    protected String message;
}
