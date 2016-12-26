/*
  $Id: $
  @file GcrIterator.java
  @brief Contains the GcrIterator.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Iterator;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrIterator implements Iterable<GcrIterator>, Iterator<GcrIterator>
{
    private int pageSize = 100;
    private String marker = null;

    @Override
    public Iterator<GcrIterator> iterator()
    {
        return this;
    }

    public void updateMarker(String linkHeader) {
        //parse the linkHeader and get the value of the "last" query
        //param and set that as the marker
        if(linkHeader == null)
        {
            marker = null;
            return;
        }
        String[] parts = linkHeader.split(";");
        String uriRef = parts[0];
        if(uriRef.startsWith("<"))
            uriRef = uriRef.substring(1);
        if(uriRef.endsWith(">"))
            uriRef = uriRef.substring(0, uriRef.length() - 1);
        try {
            URI uri = new URI(uriRef);
            String queryString = uri.getQuery();
            parts = queryString.split("&");
            for(String part : parts)
            {
                int index = part.indexOf("=");
                String key = URLDecoder.decode(part.substring(0, index), "UTF-8");
                if(!key.equalsIgnoreCase("last"))
                    continue;
                String value = URLDecoder.decode(part.substring(index+1), "UTF-8");
                this.marker = value;
                return;
            }
        } catch(UnsupportedEncodingException usee) {
            throw(new RuntimeException(usee));
        } catch(URISyntaxException use) {
            throw(new RuntimeException("Invalid URI Ref in Link: "+linkHeader, use));
        }
    }

    @Override
    public boolean hasNext() {
        return this.marker != null; //TODO: Probably need to check isFirstPage
    }

    @Override
    public GcrIterator next() {
        return this;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
