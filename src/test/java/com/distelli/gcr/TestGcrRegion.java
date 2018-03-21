package com.distelli.gcr;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestGcrRegion {
    @Test
    public void testGetRegion() {
        GcrRegion defaultRegion = GcrRegion.getRegion("default");
        assertThat(defaultRegion, equalTo(GcrRegion.DEFAULT));

        GcrRegion usRegion = GcrRegion.getRegion("us");
        assertThat(usRegion, equalTo(GcrRegion.US));

        GcrRegion euRegion = GcrRegion.getRegion("eu");
        assertThat(euRegion, equalTo(GcrRegion.EU));

        GcrRegion asiaRegion = GcrRegion.getRegion("asia");
        assertThat(asiaRegion, equalTo(GcrRegion.ASIA));

        GcrRegion fakeRegion = GcrRegion.getRegion("fake");
        assertThat(fakeRegion, nullValue());
    }

    @Test
    public void testGetRegionByEndpoint() {
        GcrRegion defaultRegion = GcrRegion.getRegionByEndpoint("gcr.io");
        assertThat(defaultRegion, equalTo(GcrRegion.DEFAULT));

        GcrRegion usRegion = GcrRegion.getRegionByEndpoint("us.gcr.io");
        assertThat(usRegion, equalTo(GcrRegion.US));

        GcrRegion euRegion = GcrRegion.getRegionByEndpoint("eu.gcr.io");
        assertThat(euRegion, equalTo(GcrRegion.EU));

        GcrRegion asiaRegion = GcrRegion.getRegionByEndpoint("asia.gcr.io");
        assertThat(asiaRegion, equalTo(GcrRegion.ASIA));

        GcrRegion fakeRegion = GcrRegion.getRegionByEndpoint("fake.example.com");
        assertThat(fakeRegion, nullValue());
    }
}
