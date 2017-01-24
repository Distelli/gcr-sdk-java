package com.distelli.gcr.models;

// This is a marker interface used to denote any manifest
// that may be uploaded to a Docker Registry. See
// https://docs.docker.com/registry/spec/manifest-v2-1/
// https://docs.docker.com/registry/spec/manifest-v2-2/
public interface GcrManifest {
    public String getMediaType();
}
