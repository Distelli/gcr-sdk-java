package com.distelli.gcr.models;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

// NOTE: gcr appears to reject this manifest format with INVALID_MANIFEST, perhaps
// it will be supported in the future?
//
// See https://docs.docker.com/registry/spec/manifest-v2-2/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrManifestV2Schema2 implements GcrManifest
{
    private static ObjectMapper OM = new ObjectMapper();
    public static final String MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v2+json";

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        public static class ConfigBuilder {
            protected String mediaType = "application/vnd.docker.container.image.v1+json";
        }
        protected String mediaType = "application/vnd.docker.container.image.v1+json";
        protected int size;
        protected String digest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayerItem {
        public static class LayerItemBuilder {
            protected String mediaType = "application/vnd.docker.image.rootfs.diff.tar.gzip";
        }
        protected String mediaType = "application/vnd.docker.image.rootfs.diff.tar.gzip";
        protected int size;
        protected String digest;
        protected List<String> urls;
    }

    public static class GcrManifestV2Schema2Builder {
        protected String mediaType = MEDIA_TYPE;
        protected List<LayerItem> layers = Collections.emptyList();
    }

    public int getSchemaVersion() {
        return 2;
    }

    protected String mediaType = MEDIA_TYPE;
    protected Config config;
    protected List<LayerItem> layers = Collections.emptyList();

    @Override
    public List<String> getReferencedDigests() {
        return layers.stream()
            .map((layer) -> layer.getDigest())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        try {
            return OM.writeValueAsString(this);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }
}
