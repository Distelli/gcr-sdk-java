package com.distelli.gcr;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;
import org.junit.Before;
import com.distelli.gcr.models.GcrRepository;
import com.distelli.gcr.models.GcrImageTag;
import com.distelli.gcr.auth.GcrCredentials;
import com.distelli.gcr.auth.GcrServiceAccountCredentials;
import java.util.List;
import java.util.Date;
import java.io.IOException;
import java.io.File;

public class TestGcrClient {
    private GcrClient client;

    private static GcrCredentials getGcrCredentials(String credName) {
        if ( null == credName ) return null;
        File credFile = new File(credName);
        if ( credFile.exists() ) {
            return new GcrServiceAccountCredentials(credFile);
        }
        System.err.println("file="+credName+" does not exist!");
        return null;
    }

    @Before
    public void setup() throws IOException {
        GcrCredentials gcrCredential = getGcrCredentials(System.getenv("GCR_CREDENTIAL_FILENAME"));

        if ( null == gcrCredential ) {
            System.err.println("Missing GCR_CREDENTIAL_FILENAME environment variable. Set this to the file that contains the GcrServiceAccountCredentials");
        }

        GcrRegion gcrRegion = null;
        try {
            gcrRegion = GcrRegion.valueOf(System.getenv("GCR_REGION"));
        } catch ( Exception ex ) {}

        assumeTrue(null != gcrCredential);

        client = new GcrClient(gcrCredential, gcrRegion);
    }

    @Test
    public void testListRepositories() throws IOException {
        System.out.println("REPOSITORIES:");
        int iterations = 0;
        for ( GcrIterator iterator : new GcrIterator().pageSize(2) ) {
            for ( GcrRepository repo : client.listRepositories(iterator) ) {
                System.out.println("\t- "+repo.getProjectName() + "/" + repo.getRepositoryName());
            }
            if ( iterations++ > 3 ) break;
        }
    }

    private GcrRepository getGcrRepository() throws IOException {
        List<GcrRepository> repos = client.listRepositories(new GcrIterator().pageSize(1));
        if ( repos.isEmpty() ) {
            return null;
        }
        return repos.get(0);
    }

    @Test
    public void testListImageTags() throws IOException {
        GcrRepository repo = getGcrRepository();
        if ( null == repo ) {
            System.err.println("NO REPOSITORIES FOUND");
            return;
        }
        System.out.println("TAGS FOR '"+repo.getProjectName() + "/" + repo.getRepositoryName()+"':");
        int iterations = 0;
        for ( GcrIterator iterator : new GcrIterator().pageSize(2) ) {
            for ( GcrImageTag imageTag : client.listImageTags(repo, iterator) ) {
                System.out.println(
                    "\t- "+imageTag.getTag()+
                    " "+imageTag.getSha()+
                    " "+imageTag.getLayerId()+
                    " "+new Date(imageTag.getCreated()));
            }
            if ( iterations++ > 3 ) break;
        }
    }
}
