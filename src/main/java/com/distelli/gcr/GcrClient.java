/*
  $Id: $
  @file GcrClient.java
  @brief Contains the GcrClient.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import com.distelli.gcr.models.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.serializers.*;
import com.distelli.gcr.exceptions.*;
import java.net.URI;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.ByteArrayInputStream;

public class GcrClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(GcrClient.class);
    private static Pattern RANGE_PATTERN = Pattern.compile("bytes=0-([0-9]+)");

    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private OkHttpClient _httpClient;
    private URI _endpoint;

    public static class Builder {
        private OkHttpClient.Builder _httpClientBuilder = new OkHttpClient.Builder();
        private URI _endpoint;

        // If you have other OkHttpClient's that you want to
        // share a connection pool with:
        public Builder connectionPool(ConnectionPool pool) {
            _httpClientBuilder.connectionPool(pool);
            return this;
        }

        public Builder gcrCredentials(GcrCredentials creds) {
            if ( null == creds ) return this;
            _httpClientBuilder.addInterceptor((chain) -> {
                    String authHeader = creds.getHttpBasicAuthHeader();
                    Request req = chain.request();
                    if ( null != authHeader ) {
                        req = req.newBuilder()
                            .header("Authorization", creds.getHttpBasicAuthHeader())
                            .build();
                    }
                    return chain.proceed(req);
                });
            return this;
        }

        public Builder endpoint(URI endpoint) {
            _endpoint = endpoint;
            return this;
        }

        public Builder gcrRegion(GcrRegion gcrRegion) {
            _endpoint = URI.create(((null == gcrRegion)?GcrRegion.DEFAULT:gcrRegion).getHttpsEndpoint());
            return this;
        }

        public GcrClient build() {
            return new GcrClient(_httpClientBuilder.build(), _endpoint);
        }
    }

    private GcrClient(OkHttpClient client, URI endpoint)
    {
        _httpClient = client;
        _endpoint = endpoint;
    }

    public GcrClient(GcrCredentials gcrCredentials, GcrRegion gcrRegion)
    {
        this(new Builder()
             .gcrCredentials(gcrCredentials)
             ._httpClientBuilder.build(),
             URI.create(((null == gcrRegion)?GcrRegion.DEFAULT:gcrRegion).getHttpsEndpoint()));
    }

    public ConnectionPool getConnectionPool() {
        return _httpClient.connectionPool();
    }

    public List<GcrRepository> listRepositories(GcrIterator iterator)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .get()
            .url(addIterator(HttpUrl(), iterator)
                 .addPathSegment("_catalog")
                 .build())
            .build();

        try ( Response httpResponse = _httpClient.newCall(request).execute() ) {
            if(iterator != null)
            {
                String linkHeader = httpResponse.header("Link");
                iterator.updateMarker(linkHeader);
            }
            int httpStatusCode = httpResponse.code();
            JsonNode responseJson = readTree(httpResponse.body(), httpResponse.code());
            if(httpStatusCode / 100 != 2)
            {
                List<GcrError> errors = GcrErrorSerializer.deserialize(responseJson);
                throw(new GcrException(errors));
            }

            return GcrRepositorySerializer.deserializeList(responseJson);
        }
    }

    public List<GcrImageTag> listImageTags(GcrRepository repository,
                                           GcrIterator iterator)
        throws IOException, GcrException
    {
        return listImageTags(repository.getFullName(),
                             iterator);
    }

    public List<GcrImageTag> listImageTags(String repository, GcrIterator iterator)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .get()
            .url(addIterator(HttpUrl(), iterator)
                 .addPathSegments(repository)
                 .addPathSegment("tags")
                 .addPathSegment("list")
                 .build())
            .build();

        try ( Response httpResponse = _httpClient.newCall(request).execute() ) {
            if(iterator != null)
            {
                String linkHeader = httpResponse.header("Link");
                iterator.updateMarker(linkHeader);
            }
            int httpStatusCode = httpResponse.code();
            JsonNode responseJson = readTree(httpResponse.body(), httpResponse.code());
            if(httpStatusCode / 100 != 2)
            {
                List<GcrError> errors = GcrErrorSerializer.deserialize(responseJson);
                throw(new GcrException(errors));
            }

            return GcrImageTagSerializer.deserializeList(responseJson);
        }
    }

    public GcrManifest getManifest(String repository, String reference)
        throws IOException, GcrException
    {
        return getManifest(repository, reference, "*/*");
    }

    // GET /v2/<name>/manifests/<reference>
    public GcrManifest getManifest(String repository, String reference, String acceptHeader)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .get()
            .header("Accept", acceptHeader)
            .url(HttpUrl()
                 .addPathSegments(repository)
                 .addPathSegment("manifests")
                 .addPathSegment(reference)
                 .build())
            .build();

        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( 404 == response.code() ) {
                return null;
            }
            if ( response.code() / 100 == 2 ) {
                return GcrManifest.create(response.body().string(),
                                          response.header("Content-Type"));
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    // PUT /v2/<name>/manifests/<reference>
    // reference may be a tag or GcrManifestMeta.digest
    public GcrManifestMeta putManifest(String repository, String reference, GcrManifest manifest)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .put(RequestBody.create(MediaType.parse(manifest.getMediaType()),
                                    manifest.toString()))
            .url(HttpUrl()
                 .addPathSegments(repository)
                 .addPathSegment("manifests")
                 .addPathSegment(reference)
                 .build())
            .build();
        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( response.code() / 100 == 2 ) {
                return GcrManifestMeta.builder()
                    .digest(response.header("Docker-Content-Digest"))
                    .location(response.header("Location"))
                    .mediaType(manifest.getMediaType())
                    .build();
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    // GET /v2/<repository>/blobs/<digest>
    public <T> T getBlob(String repository, String digest, GcrBlobReader<T> reader)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .get()
            .url(HttpUrl()
                 .addPathSegments(repository)
                 .addPathSegment("blobs")
                 .addPathSegment(digest)
                 .build())
            .build();
        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( 404 == response.code() ) {
                return reader.read(new ByteArrayInputStream(new byte[0]), null);
            }
            if ( response.code() / 100 != 2 ) {
                throw new GcrException(
                    GcrErrorSerializer.deserialize(
                        readTree(response.body(), response.code())));
            }
            return reader.read(response.body().byteStream(),
                               GcrBlobMeta.builder()
                               .digest(digest)
                               .length(response.body().contentLength())
                               .build());
        }        
    }

    // HEAD /v2/<repository>/blobs/<digest>
    public GcrBlobMeta getBlobMeta(String repository, String digest)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .head()
            .url(HttpUrl()
                 .addPathSegments(repository)
                 .addPathSegment("blobs")
                 .addPathSegment(digest)
                 .build())
            .build();
        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( 404 == response.code() ) return null;
            if(response.code() / 100 != 2) {
                throw new GcrException(
                    GcrErrorSerializer.deserialize(
                        readTree(response.body(), response.code())));
            }
            return GcrBlobMeta.builder()
                .digest(digest)
                .length(response.body().contentLength())
                .build();
        }
    }

    // POST /v2/<repository>/blobs/uploads/
    public GcrBlobUpload createBlobUpload(String repository)
        throws IOException, GcrException
    {
        return createBlobUpload(repository, null, null);
    }

    // cross repository blob mounting:
    // POST /v2/<name>/blobs/uploads/?mount=<digest>&from=<repository name>
    public GcrBlobUpload createBlobUpload(String repository, String digest, String fromRepository)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .post(RequestBody.create(null, ""))
            .url(addCrossMount(HttpUrl(), digest, fromRepository)
                 .addPathSegments(repository)
                 .addPathSegment("blobs")
                 .addPathSegments("uploads/")
                 .build())
            .build();
        try ( Response httpResponse = _httpClient.newCall(request).execute() ) {
            switch ( httpResponse.code() ) {
            case 404: return null;
            case 201: return GcrBlobUpload.builder()
                    .complete(true)
                    .blobLocation(httpResponse.header("Location"))
                    .digest(httpResponse.header("Docker-Content-Digest"))
                    .build();
            case 202: return GcrBlobUpload.builder()
                    .uploadLocation(httpResponse.header("Location"))
                    .uploadId(httpResponse.header("Docker-Upload-UUID"))
                    .rangeBegin(0)
                    .complete(false)
                    .build();
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(httpResponse.body(), httpResponse.code())));
        }
    }

    // GET /v2/<repository>/blobs/uploads/<uuid>
    public GcrBlobUpload getBlobUploadProgress(GcrBlobUpload blobUpload)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .get()
            .url(getUploadLocation(blobUpload))
            .build();
        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( 204 == response.code() ) {
                String range = response.header("Range");
                Matcher matcher = ( null == range ) ? null : RANGE_PATTERN.matcher(range);
                if ( null == matcher || ! matcher.find() ) {
                    throw new IllegalStateException("Missing Range header got headers="+response.headers());
                }
                return blobUpload.toBuilder()
                    .rangeBegin(parseLong(matcher.group(1)))
                    .build();
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    // PATCH /v2/<name>/blobs/uploads/<uuid>
    // Returns a new GcrBlobUpload with startRange upadated.
    public GcrBlobUpload blobUploadChunk(GcrBlobUpload blobUpload, InputStream chunk, Long chunkLength)
        throws IOException, GcrException
    {
        AtomicLong actualChunkLength = new AtomicLong();
        Request.Builder request = new Request.Builder()
            .patch(toRequestBody(chunk, chunkLength, actualChunkLength))
            .header("Expect", "100-continue")
            .header("Range", getRangeHeader(blobUpload.getRangeBegin(), chunkLength))
            .url(getUploadLocation(blobUpload, null));

        try ( Response response = _httpClient.newCall(request.build()).execute() ) {
            if ( 202 == response.code() || 204 == response.code() ) {
                String range = response.header("Range");
                Matcher matcher = ( null == range ) ? null : RANGE_PATTERN.matcher(range);
                if ( null == matcher || ! matcher.find() ) {
                    return blobUpload.toBuilder()
                        .rangeBegin(blobUpload.getRangeBegin() + actualChunkLength.get())
                        .build();
                }
                return blobUpload.toBuilder()
                    .rangeBegin(parseLong(matcher.group(1)))
                    .build();
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    // PUT /v2/<name>/blob/uploads/<uuid>?digest=<digest>
    public GcrBlobMeta blobUploadChunk(GcrBlobUpload blobUpload, InputStream chunk, Long chunkLength, String digest)
        throws IOException, GcrException
    {
        AtomicLong actualChunkLength = new AtomicLong();
        Request.Builder request = new Request.Builder()
            .put(toRequestBody(chunk, chunkLength, actualChunkLength))
            .header("Expect", "100-continue")
            .header("Range", getRangeHeader(blobUpload.getRangeBegin(), chunkLength))
            .url(getUploadLocation(blobUpload, digest));

        try ( Response response = _httpClient.newCall(request.build()).execute() ) {
            if ( 201 == response.code() ) {
                return GcrBlobMeta.builder()
                    .digest(response.header("Docker-Content-Digest"))
                    .length(blobUpload.getRangeBegin() + actualChunkLength.get())
                    .build();
            }
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    // DELETE /v2/<name>/blobs/uploads/<uuid>
    public void cancelUploadChunk(GcrBlobUpload blobUpload)
        throws IOException, GcrException
    {
        Request request = new Request.Builder()
            .delete(RequestBody.create(null, ""))
            .url(getUploadLocation(blobUpload))
            .build();
        try ( Response httpResponse = _httpClient.newCall(request).execute() ) {
            if ( httpResponse.code() / 100 == 2 ) return;
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(httpResponse.body(), httpResponse.code())));
        }
    }

    // DELETE /v2/<name>/blobs/<digest>
    public boolean deleteBlob(String repository, String digest)
        throws IOException, GcrException
    {

        Request request = new Request.Builder()
            .delete(RequestBody.create(null, ""))
            .url(HttpUrl()
                 .addPathSegments(repository)
                 .addPathSegment("blobs")
                 .addPathSegment(digest)
                 .build())
            .build();
        try ( Response response = _httpClient.newCall(request).execute() ) {
            if ( response.code() / 100 == 2 ) return true;
            if ( 404 == response.code() ) return false;
            throw new GcrException(
                GcrErrorSerializer.deserialize(
                    readTree(response.body(), response.code())));
        }
    }

    private static Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch ( NumberFormatException ex ) {
            return null;
        }
    }

    private String getRangeHeader(long rangeBegin, Long chunkLength) {
        if ( null == chunkLength ) {
            return rangeBegin + "-";
        }
        return rangeBegin + "-" + (rangeBegin+chunkLength-1L);
    }

    private RequestBody toRequestBody(InputStream in, Long length, AtomicLong actualLength) {
        return new RequestBody() {
            @Override
            public long contentLength() {
                return ( null == length ) ? -1 : length;
            }
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }
            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                okio.Source source = okio.Okio.source(in);
                if ( null != length ) {
                    actualLength.addAndGet(length);
                    sink.write(source, length);
                } else {
                    actualLength.addAndGet(sink.writeAll(source));
                }
            }
        };
    }

    private HttpUrl getUploadLocation(GcrBlobUpload blobUpload) {
        return getUploadLocation(blobUpload, null);
    }

    private HttpUrl getUploadLocation(GcrBlobUpload blobUpload, String digest) {
        HttpUrl url = HttpUrl.parse(blobUpload.getUploadLocation());
        if ( null == url ) {
            throw new IllegalArgumentException(
                "GcrBlobUpload.uploadLocation="+blobUpload.getUploadLocation()+" is invalid");
        }
        if ( null != digest ) {
            url = url.newBuilder()
                .addQueryParameter("digest", digest)
                .build();
        }
        return url;
    }

    private HttpUrl.Builder addCrossMount(HttpUrl.Builder urlBuilder, String digest, String fromRepository) {
        if ( null == digest ) return urlBuilder;
        urlBuilder.addQueryParameter("mount", digest);
        if ( null == fromRepository ) return urlBuilder;
        return urlBuilder.addQueryParameter("from", fromRepository);
    }

    private HttpUrl.Builder HttpUrl() {
        return HttpUrl.get(_endpoint).newBuilder()
            .addPathSegment("v2");
    }

    private boolean isJson(MediaType mediaType) {
        return null != mediaType &&
            "application".equals(mediaType.type()) &&
            "json".equals(mediaType.subtype());
    }

    private JsonNode readTree(ResponseBody body, int code) throws IOException {
        if ( ! isJson(body.contentType()) ) {
            String bodyStr = new String(body.bytes(), ISO_8859_1);
            JsonNodeFactory jnf = OBJECT_MAPPER.getNodeFactory();
            return jnf.objectNode()
                .set("errors",
                     jnf.arrayNode()
                     .add(jnf.objectNode()
                          .put("code", ""+code)
                          .put("message", "<empty body>")));
        }
        JsonParser parser = OBJECT_MAPPER.getFactory().createJsonParser(body.byteStream());
        if ( null == parser.nextToken() ) {
            JsonNodeFactory jnf = OBJECT_MAPPER.getNodeFactory();
            return jnf.objectNode()
                .set("errors",
                     jnf.arrayNode()
                     .add(jnf.objectNode()
                          .put("code", ""+code)
                          .put("message", "<empty body>")));
        }
        return OBJECT_MAPPER.readValue(parser, JsonNode.class);
    }

    private HttpUrl.Builder addIterator(HttpUrl.Builder builder, GcrIterator iterator) {
        if ( null == iterator ) return builder;
        if ( null != iterator.getMarker() ) {
            builder.addQueryParameter("last", iterator.getMarker());
        }
        return builder.addQueryParameter("n", ""+iterator.getPageSize());
    }
}
