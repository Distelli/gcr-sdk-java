/*
  $Id: $
  @file GcrImageTagSerializer.java
  @brief Contains the GcrImageTagSerializer.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.serializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.distelli.gcr.models.*;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcrImageTagSerializer extends GcrSerializer
{
    private static final Logger log = LoggerFactory.getLogger(GcrImageTagSerializer.class);

    public GcrImageTagSerializer()
    {

    }

    public static List<GcrImageTag> deserializeList(JsonNode jsonNode)
    {
        JsonNode manifestNode = jsonNode.at("/manifest");
        List<GcrImageTag> tagList = new ArrayList<GcrImageTag>();
        Iterator<Map.Entry<String, JsonNode>> iter = manifestNode.fields();
        while(iter.hasNext())
        {
            Map.Entry<String, JsonNode> field = iter.next();
            String sha = field.getKey();
            JsonNode value = field.getValue();
            String layerId = value.at("/layerId").asText();
            Long created = value.at("/timeCreatedMs").asLong();
            Iterator<JsonNode> tagIter = value.at("/tag").elements();
            while(tagIter.hasNext())
            {
                String tag = tagIter.next().asText();
                GcrImageTag imgTag = new GcrImageTag()
                .builder()
                .sha(sha)
                .tag(tag)
                .layerId(layerId)
                .created(created)
                .build();

                tagList.add(imgTag);
            }
        }

        //now sort by created time
        Collections.sort(tagList, new Comparator<GcrImageTag>() {
                public int compare(GcrImageTag imgTag1, GcrImageTag imgTag2) {
                    long i1CreateTime = getCreateTime(imgTag1);
                    long i2CreateTime = getCreateTime(imgTag2);
                    if(i1CreateTime > i2CreateTime)
                        return 1;
                    if(i1CreateTime < i2CreateTime)
                        return -1;
                    return 0;
                }

                private long getCreateTime(GcrImageTag imgTag) {
                    Long createTime = imgTag.getCreated();
                    if(createTime == null)
                        return 0;
                    return createTime.longValue();
                }
            });
        return tagList;
    }
}
