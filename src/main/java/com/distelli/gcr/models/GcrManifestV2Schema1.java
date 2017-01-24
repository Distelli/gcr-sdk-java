package com.distelli.gcr.models;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

// See https://docs.docker.com/registry/spec/manifest-v2-1/
// This is an UNSIGNED manifest

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrManifestV2Schema1 implements GcrManifest
{
    public static final String MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v1+json";

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        protected String v1Compatibility;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FSLayerItem {
        protected String blobSum;
    }

    @Override @JsonIgnore
    public String getMediaType() {
        return MEDIA_TYPE;
    }

    public static class GcrManifestV2Schema1Builder {
        protected int schemaVersion = 1;
    }

    protected String name;
    protected String tag;
    protected String architecture;
    protected List<FSLayerItem> fsLayers;
    protected List<HistoryItem> history;

    public int getSchemaVersion() {
        return 1;
    }
}
