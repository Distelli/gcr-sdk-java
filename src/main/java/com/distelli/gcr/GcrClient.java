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

import com.distelli.gcr.models.*;
import com.distelli.gcr.auth.*;
import com.distelli.gcr.serializers.*;
import com.distelli.gcr.exceptions.*;
import java.net.URI;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.HttpUrl;
import okhttp3.Response;

public class GcrClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(GcrClient.class);

    private OkHttpClient _httpClient;
    private URI _endpoint;

    public Builder builder() {
        return new Builder();
    }

    protected static class Builder {
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
            JsonNode responseJson = readTree(httpResponse.body().byteStream());
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
        return listImageTags(String.format("%s/%s",
                                           repository.getProjectName(),
                                           repository.getRepositoryName()),
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
            JsonNode responseJson = readTree(httpResponse.body().byteStream());
            if(httpStatusCode / 100 != 2)
            {
                List<GcrError> errors = GcrErrorSerializer.deserialize(responseJson);
                throw(new GcrException(errors));
            }

            return GcrImageTagSerializer.deserializeList(responseJson);
        }
    }

    // HEAD /v2/<repository>/blobs/<digest>
    public GcrBlobMeta getBlobMeta(String repository, String digest) {
        throw new UnsupportedOperationException();
    }

    // POST /v2/<repository>/blobs/uploads/
    public GcrBlobUpload createBlobUpload(String repository) {
        return createBlobUpload(repository, null, null);
    }

    // cross repository blob mounting:
    // POST /v2/<name>/blobs/uploads/?mount=<digest>&from=<repository name>
    public GcrBlobUpload createBlobUpload(String repository, String digest, String fromRepository) {
        throw new UnsupportedOperationException();
    }

    // GET /v2/<repository>/blobs/uploads/<uuid>
    public GcrBlobUpload getBlobUploadProgress(GcrBlobUpload blobUpload) {
        // Returns a new BlobUplaod with startRange updated.
        throw new UnsupportedOperationException();
    }

    // PATCH /v2/<name>/blobs/uploads/<uuid>
    public GcrBlobUpload blobUploadChunk(GcrBlobUpload blobUpload, InputStream chunk, long chunkLength) {
        return blobUploadChunk(blobUpload, chunk, chunkLength, null);
    }

    // PUT /v2/<name>/blob/uploads/<uuid>?digest=<digest>
    public GcrBlobUpload blobUploadChunk(GcrBlobUpload blobUpload, InputStream chunk, long chunkLength, String digest) {
        // Send Expect: 100-continue
        // Returns a new GcrBlobUpload with startRange upadated.
        throw new UnsupportedOperationException();
    }

    // DELETE /v2/<name>/blobs/uploads/<uuid>
    public void cancelUploadChunk(GcrBlobUpload blobUpload) {
        throw new UnsupportedOperationException();
    }

    // DELETE /v2/<name>/blobs/<digest>
    public boolean deleteBlob(String repository, String digest) {
        throw new UnsupportedOperationException();
    }

    private HttpUrl.Builder HttpUrl() {
        return HttpUrl.get(_endpoint).newBuilder()
            .addPathSegment("v2");
    }

    private JsonNode readTree(InputStream in) {
        try {
            return OBJECT_MAPPER.readTree(in);
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    private HttpUrl.Builder addIterator(HttpUrl.Builder builder, GcrIterator iterator) {
        if ( null == iterator ) return builder;
        if ( null != iterator.getMarker() ) {
            builder.addQueryParameter("last", iterator.getMarker());
        }
        return builder.addQueryParameter("n", ""+iterator.getPageSize());
    }
}
