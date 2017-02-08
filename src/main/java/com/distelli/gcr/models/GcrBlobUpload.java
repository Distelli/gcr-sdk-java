package com.distelli.gcr.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
public class GcrBlobUpload
{
    protected boolean complete;
    protected String blobLocation;
    protected String digest;
    protected String uploadLocation;
    protected String uploadId;
    protected long rangeBegin;
    protected String mediaType;
}
