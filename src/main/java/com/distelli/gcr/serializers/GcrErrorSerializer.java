/*
  $Id: $
  @file GcrErrorSerializer.java
  @brief Contains the GcrErrorSerializer.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.serializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.distelli.gcr.models.*;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcrErrorSerializer extends GcrSerializer
{
    private static final Logger log = LoggerFactory.getLogger(GcrErrorSerializer.class);

    public GcrErrorSerializer()
    {

    }

    public static List<GcrError> deserialize(JsonNode jsonNode)
    {
        if(jsonNode == null)
            return Collections.emptyList();
        List<GcrError> errors = new ArrayList<GcrError>();
        JsonNode errorsNode = jsonNode.at("/errors");
        if(!errorsNode.isArray())
            return Collections.singletonList(
                GcrError.builder()
                .code("UNEXPECTED JsonNode")
                .message("GOT="+jsonNode)
                .build());

        for(JsonNode objNode : errorsNode) {
            GcrError error = convertValue(objNode, GcrError.class);
            if(error != null)
                errors.add(error);
        }

        return errors;
    }
}
