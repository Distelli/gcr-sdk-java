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

import org.apache.log4j.Logger;
import com.distelli.gcr.models.*;
import com.fasterxml.jackson.databind.JsonNode;

public class GcrErrorSerializer extends GcrSerializer
{
    private static final Logger log = Logger.getLogger(GcrErrorSerializer.class);

    public GcrErrorSerializer()
    {

    }

    public static List<GcrError> deserialize(JsonNode jsonNode)
    {
        List<GcrError> errors = new ArrayList<GcrError>();
        if(jsonNode == null)
            return errors;
        JsonNode errorsNode = jsonNode.at("/errors");
        if(errorsNode.isMissingNode())
            return errors;
        if(!errorsNode.isArray())
            return errors;

        for(JsonNode objNode : errorsNode) {
            GcrError error = convertValue(objNode, GcrError.class);
            if(error != null)
                errors.add(error);
        }

        return errors;
    }
}
