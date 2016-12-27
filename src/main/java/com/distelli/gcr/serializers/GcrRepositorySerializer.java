/*
  $Id: $
  @file GcrRepositorySerializer.java
  @brief Contains the GcrRepositorySerializer.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.serializers;

import java.util.ArrayList;
import java.util.List;

import com.distelli.gcr.models.*;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcrRepositorySerializer extends GcrSerializer
{
    private static final Logger log = LoggerFactory.getLogger(GcrRepositorySerializer.class);

    public GcrRepositorySerializer()
    {

    }

    public static List<GcrRepository> deserializeList(JsonNode jsonNode)
    {
        List<GcrRepository> repos = new ArrayList<GcrRepository>();
        if(jsonNode == null)
            return repos;
        JsonNode reposNode = jsonNode.at("/repositories");
        if(reposNode.isMissingNode())
            return repos;
        if(!reposNode.isArray())
            return repos;
        for(JsonNode objNode : reposNode) {
            GcrRepository repo = deserialize(objNode);
            if(repo != null)
                repos.add(repo);
        }
        return repos;
    }

    public static GcrRepository deserialize(JsonNode jsonNode)
    {
        String repoName = jsonNode.asText();
        if(repoName == null || repoName.trim().isEmpty())
            return null;
        GcrRepository repo = new GcrRepository();
        String[] parts = repoName.split("/");
        if(parts.length == 2)
        {
            repo.setProjectName(parts[0]);
            repo.setRepositoryName(parts[1]);
        }
        else
            repo.setRepositoryName(repoName);
        return repo;
    }
}
