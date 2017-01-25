package com.distelli.gcr.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrManifestMeta
{
    // This is the manifest digest, NOT the image digest!
    protected String digest;
    protected String location;
    protected String mediaType;
}
