package com.distelli.gcr;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;
import org.junit.Before;
import com.distelli.gcr.models.GcrRepository;
import com.distelli.gcr.models.GcrImageTag;
import com.distelli.gcr.models.GcrBlobMeta;
import com.distelli.gcr.models.GcrBlobUpload;
import com.distelli.gcr.auth.GcrCredentials;
import com.distelli.gcr.auth.GcrServiceAccountCredentials;
import java.util.List;
import java.util.Date;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

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
            if ( iterations++ >= 3 ) break;
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
            if ( iterations++ >= 3 ) break;
        }
    }

    @Test
    public void testListImageTagRepoNotFound() throws IOException {
        List<GcrImageTag> tags = client.listImageTags("does-not-exist", null);
        assertTrue(""+tags, tags.isEmpty());
    }

    @Test
    public void testGetBlobMetaNotFound() throws IOException {
        assertNull(client.getBlobMeta("does-not-exist", "sha256:4f28f41a5bf874a0b8aa6540372e488f21cb3bd72e17ce1d54173cb38a25fe6a"));
    }

    @Test
    public void testUpload() throws Exception {
        GcrRepository repo = getGcrRepository();
        if ( null == repo ) {
            System.err.println("NO REPOSITORIES FOUND");
            return;
        }
        GcrBlobUpload upload = client.createBlobUpload(repo.getFullName());
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] chunk1 = "first chunk".getBytes(UTF_8);
        md.update(chunk1);
        String digest = "sha256:" + printHexBinary(md.digest()).toLowerCase();
        GcrBlobMeta blobMeta  = client.blobUploadChunk(upload, new ByteArrayInputStream(chunk1), null, digest);
        assertEquals(blobMeta.getDigest(), digest);
        assertEquals(blobMeta.getLength().intValue(), chunk1.length);

        blobMeta = client.getBlobMeta(repo.getFullName(), digest);
        assertEquals(blobMeta.getDigest(), digest);
        assertEquals(blobMeta.getLength().intValue(), chunk1.length);

        assertTrue(client.deleteBlob(repo.getFullName(), digest));
        
        // THESE ARE UNSUPPORTED in GCR!
        // upload = client.blobUploadChunk(upload, new ByteArrayInputStream(chunk1), (long)chunk1.length);
        // GcrBlobUpload progress = client.getBlobUploadProgress(upload);
        // client.cancelUploadChunk(upload);
    }
}
