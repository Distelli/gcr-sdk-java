package com.distelli.gcr.models;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// See https://docs.docker.com/registry/spec/manifest-v2-1/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrManifestV2Schema1 implements GcrManifest
{
    private static ObjectMapper OM = new ObjectMapper();
    private static Pattern SIGNATURES_PATTERN = Pattern.compile(",\\s*\"signatures\"\\s*:");
    public static final String MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v1+json";
    public static final String SIGNED_MEDIA_TYPE = "application/vnd.docker.distribution.manifest.v1+prettyjws";

    public static GcrManifestV2Schema1 create(GcrManifest manifest) {
        String manifestStr = manifest.toString();
        GcrManifestV2Schema1 result;
        try {
            result = OM.readValue(manifestStr, GcrManifestV2Schema1.class);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
        if ( null != result.signatures && result.signatures.size() > 0 ) {
            Matcher matcher = SIGNATURES_PATTERN.matcher(manifestStr);
            int matches = 0;
            while ( matcher.find() ) {
                matches++;
            }
            if ( matches <= 0 ) {
                throw new IllegalStateException("Expected "+SIGNATURES_PATTERN+" to match");
            }
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while ( matcher.find() ) {
                // Last match:
                if ( --matches == 0 ) {
                    matcher.appendReplacement(sb, "");
                    break;
                } else {
                    matcher.appendReplacement(sb, matcher.group());
                }
            }
            sb.append(manifestStr.substring(manifestStr.indexOf(']', matcher.end())+1));
            result.setPayload(sb.toString());
        }
        return result;
    }

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
        return ( null != signatures && null != payload ) ? SIGNED_MEDIA_TYPE : MEDIA_TYPE;
    }

    /**
     * The JSON payload that is signed.
     */
    @JsonIgnore
    public String getPayload() {
        return payload;
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

    // Payload that is signed:
    protected String payload;
    protected List<JsonNode> signatures;

    public int getSchemaVersion() {
        return 1;
    }

    @Override
    public String toString() {
        try {
            if ( null != payload && null != signatures ) {
                String prefix = payload;
                if ( prefix.endsWith("}") ) {
                    prefix = prefix.substring(0, prefix.length()-1);
                }
                return prefix + ",\"signatures\":" + OM.writeValueAsString(signatures) + "}";
            }
            if ( null == payload && null == signatures ) {
                return OM.writeValueAsString(this);
            }
            if ( null == signatures ) {
                return payload;
            }
            throw new IllegalStateException("Signatures defined, but no payload is defined!");
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }
}
