/*
  $Id: $
  @file GcrRepository.java
  @brief Contains the GcrRepository.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrRepository
{
    protected String projectName;
    protected String repositoryName;
}
