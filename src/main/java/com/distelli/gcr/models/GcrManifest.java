package com.distelli.gcr.models;

import java.util.Map;
import java.util.HashMap;

// This is a marker interface used to denote any manifest
// that may be uploaded to a Docker Registry. See
// https://docs.docker.com/registry/spec/manifest-v2-1/
// https://docs.docker.com/registry/spec/manifest-v2-2/
//
public interface GcrManifest {
    /**
     * This is the appropriate media-type of this manifest.
     */
    public String getMediaType();

    /**
     * This MUST return the JSON representation of this manifest. Note that
     * signed manifests can not change their JSON representation without
     * causing signature verification to fail.
     *
     * @return the JSON representation of this manifest.
     */
    public String toString();

    // private static final Map<String, Class<? extends GcrManifest>> MANIFEST_IMPL = new HashMap<String, Class<? extends GcrManifest>>(){{
    //         put(GcrManifestV2Schema1.MEDIA_TYPE, GcrManifestV2Schema1.class);
    //         put(GcrManifestV2Schema1.SIGNED_MEDIA_TYPE, GcrManifestV2Schema1.class);
    //         put(GcrManifestV2Schema2.MEDIA_TYPE, GcrManifestV2Schema2.class);
    //         put(GcrManifestV2Schema2List.MEDIA_TYPE, GcrManifestV2Schema2List.class);
    //     }};
}
