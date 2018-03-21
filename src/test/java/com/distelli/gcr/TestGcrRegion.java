package com.distelli.gcr;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class TestGcrRegion {
    @Test
    public void testGetRegion() {
        GcrRegion defaultRegion = GcrRegion.getRegion("gcr.io");
        assertThat(defaultRegion, equalTo(GcrRegion.DEFAULT));

        GcrRegion usRegion = GcrRegion.getRegion("us.gcr.io");
        assertThat(usRegion, equalTo(GcrRegion.US));

        GcrRegion euRegion = GcrRegion.getRegion("eu.gcr.io");
        assertThat(euRegion, equalTo(GcrRegion.EU));

        GcrRegion asiaRegion = GcrRegion.getRegion("asia.gcr.io");
        assertThat(asiaRegion, equalTo(GcrRegion.ASIA));

        GcrRegion fakeRegion = GcrRegion.getRegion("fake.example.com");
        assertThat(fakeRegion, nullValue());
    }
}
