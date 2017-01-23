package com.distelli.gcr.models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GcrBlobMeta
{
    protected String digest;
    protected Long length;
}
