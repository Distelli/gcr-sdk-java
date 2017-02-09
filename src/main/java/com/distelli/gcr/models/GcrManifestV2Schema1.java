package com.distelli.gcr.models;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.AccessLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

// See https://docs.docker.com/registry/spec/manifest-v2-1/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrManifestV2Schema1 implements GcrManifest
{
    private static Pattern SIGNATURES_PATTERN = Pattern.compile(",\\s*\"signatures\"\\s*:");
    public static final String MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v1+json";
    public static final String SIGNED_MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v1+prettyjws";

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
        if ( null == toString ) return MEDIA_TYPE;
        // Kind of hacky here...
        return SIGNATURES_PATTERN.matcher(toString).find() ?
            SIGNED_MEDIA_TYPE : MEDIA_TYPE;
    }

    @Override @JsonIgnore
    public List<String> getReferencedDigests() {
        return fsLayers.stream()
            .map((layer) -> layer.getBlobSum())
            .collect(Collectors.toList());
    }

    public static class GcrManifestV2Schema1Builder {
        protected int schemaVersion = 1;
        protected String mediaType = SIGNED_MEDIA_TYPE;
    }

    protected String name;
    protected String tag;
    protected String architecture;
    protected List<FSLayerItem> fsLayers;
    protected List<HistoryItem> history;

    @Getter(AccessLevel.NONE)
    protected String toString;

    public int getSchemaVersion() {
        return 1;
    }

    public void setSchemaVersion(int ignored) {}
    public void setSignatures(Object ignored) {}

    @Override
    public String toString() {
        if ( null != toString ) {
            return toString;
        }
        try {
            return GcrManifest.toString(this);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }
}
