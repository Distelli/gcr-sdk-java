package com.distelli.gcr.models;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

class GcrManifestHelper {
    private static ObjectMapper OM = new ObjectMapper();

    private static final Map<String, Class<? extends GcrManifest>> TYPES = new HashMap<>();
    static {
        TYPES.put(GcrManifestV2Schema1.MEDIA_TYPE, GcrManifestV2Schema1.class);
        TYPES.put(GcrManifestV2Schema1.SIGNED_MEDIA_TYPE, GcrManifestV2Schema1.class);
        TYPES.put(GcrManifestV2Schema2.MEDIA_TYPE, GcrManifestV2Schema2.class);
        TYPES.put(GcrManifestV2Schema2List.MEDIA_TYPE, GcrManifestV2Schema2List.class);
    }

    protected static GcrManifest create(String manifestStr, String mediaType) throws IOException {
        Class<? extends GcrManifest> type = TYPES.get(mediaType);
        if ( null != type ) {
            GcrManifest manifest = OM.readValue(manifestStr, type);
            manifest.setToString(manifestStr);
            return manifest;
        }

        // Not sure what else to do here...
        return new GcrManifest() {
            @Override
            public String toString() {
                return manifestStr;
            }
            @Override
            public String getMediaType() {
                return mediaType;
            }
            @Override
            public List<String> getReferencedDigests() {
                throw new UnsupportedOperationException(
                    "Unknown media type: "+mediaType+" not supported");
            }
        };
    }

    protected static String toString(GcrManifest manifest) throws IOException {
        return OM.writeValueAsString(manifest);
    }
}
