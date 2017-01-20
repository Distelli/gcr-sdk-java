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

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrIterator implements Iterable<GcrIterator>, Iterator<GcrIterator>
{
    private int pageSize = 100;
    private String marker = null;
    private boolean isFirstPage = true;

    public GcrIterator pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

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
            setMarker(null);
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
                setMarker(value);
                return;
            }
        } catch(UnsupportedEncodingException usee) {
            throw(new RuntimeException(usee));
        } catch(URISyntaxException use) {
            throw(new RuntimeException("Invalid URI Ref in Link: "+linkHeader, use));
        }
    }

    public void setMarker(String marker) {
        this.isFirstPage = false;
        this.marker = marker;
    }

    @Override
    public boolean hasNext() {
        return this.marker != null || isFirstPage;
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
