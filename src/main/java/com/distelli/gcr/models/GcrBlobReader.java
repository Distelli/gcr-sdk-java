package com.distelli.gcr.models;

import java.io.InputStream;
import java.io.IOException;

@FunctionalInterface
public interface GcrBlobReader<T> {
    // Note that meta will be null if the blob is not found,
    // but input stream will exist, but contain zero content.
    public T read(InputStream input, GcrBlobMeta meta) throws IOException;
}
