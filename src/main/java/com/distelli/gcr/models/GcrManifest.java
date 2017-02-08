package com.distelli.gcr.models;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

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

    /**
     * A list of sha256:... strings which are the name of the blobs referenced
     * by this manifest.
     */
    public List<String> getReferencedDigests();

    /**
     * The V2Schmea1 signed manifest MUST be preserved byte-for-byte, so support this:
     */
    public default void setToString(String toString) {}

    public static GcrManifest create(String manifestStr, String mediaType) throws IOException {
        return GcrManifestHelper.create(manifestStr, mediaType);
    }

    public static String toString(GcrManifest manifest) throws IOException {
        return GcrManifestHelper.toString(manifest);
    }
}
