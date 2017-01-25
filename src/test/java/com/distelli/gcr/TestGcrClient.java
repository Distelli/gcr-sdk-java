package com.distelli.gcr;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;
import org.junit.Before;
import com.distelli.gcr.models.GcrRepository;
import com.distelli.gcr.models.GcrManifest;
import com.distelli.gcr.models.GcrImageTag;
import com.distelli.gcr.models.GcrBlobMeta;
import com.distelli.gcr.models.GcrBlobUpload;
import com.distelli.gcr.models.GcrManifestMeta;
import com.distelli.gcr.models.GcrManifestV2Schema2;
import com.distelli.gcr.models.GcrManifestV2Schema1;
import com.distelli.gcr.auth.GcrCredentials;
import com.distelli.gcr.auth.GcrServiceAccountCredentials;
import java.util.List;
import java.util.Date;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class TestGcrClient {
    private static ObjectMapper OM = new ObjectMapper();
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
        System.err.println(client.getManifest("distelli-alpha/bmaher", "latest").toString());
        if ( true ) return;
        // Test a V2.1 manifest with signature (gcr appears to reject v2.1 unsigned manifests):
        final String manifestStr = "{\"schemaVersion\":1,\"name\":\"unused\",\"tag\":\"unused\",\"architecture\":\"amd64\",\"fsLayers\":[{\"blobSum\":\"sha256:0a8490d0dfd399b3a50e9aaa81dba0d425c3868762d46526b41be00886bcc28b\"}],\"history\":[{\"v1Compatibility\":\"{\\\"architecture\\\":\\\"amd64\\\",\\\"config\\\":{\\\"ArgsEscaped\\\":false,\\\"AttachStderr\\\":false,\\\"AttachStdin\\\":false,\\\"AttachStdout\\\":false,\\\"Cmd\\\":[],\\\"Domainname\\\":\\\"\\\",\\\"Entrypoint\\\":[],\\\"Env\\\":[\\\"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\\\"],\\\"ExposedPorts\\\":{},\\\"Hostname\\\":\\\"11fbdc1f630f\\\",\\\"Image\\\":\\\"\\\",\\\"Labels\\\":{},\\\"MacAddress\\\":\\\"\\\",\\\"NetworkDisabled\\\":false,\\\"OnBuild\\\":[],"+
            "\\\"OpenStdin\\\":false,\\\"Shell\\\":[],\\\"StdinOnce\\\":false,\\\"StopSignal\\\":\\\"\\\",\\\"Tty\\\":false,\\\"User\\\":\\\"\\\",\\\"Volumes\\\":{},\\\"WorkingDir\\\":\\\"\\\"},\\\"container\\\":\\\"11fbdc1f630f302229e97546bcc3e511b58fdd663937c034a61139d7a1c0d83f\\\",\\\"container_config\\\":{\\\"ArgsEscaped\\\":false,\\\"AttachStderr\\\":false,\\\"AttachStdin\\\":false,\\\"AttachStdout\\\":false,\\\"Cmd\\\":[\\\"/bin/sh\\\",\\\"-c\\\",\\\"#(nop) ADD file:92ab746eb22dd3ed2b87469c719adf3c1bed7302653bbd76baafd7cfd95e911e in / \\\"],\\\"Domainname\\\":\\\"\\\",\\\"Entrypoint\\\":[],"+
            "\\\"Env\\\":[\\\"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\\\"],\\\"ExposedPorts\\\":{},\\\"Hostname\\\":\\\"11fbdc1f630f\\\",\\\"Image\\\":\\\"\\\",\\\"Labels\\\":{},\\\"MacAddress\\\":\\\"\\\",\\\"NetworkDisabled\\\":false,\\\"OnBuild\\\":[],\\\"OpenStdin\\\":false,\\\"Shell\\\":[],\\\"StdinOnce\\\":false,\\\"StopSignal\\\":\\\"\\\",\\\"Tty\\\":false,\\\"User\\\":\\\"\\\",\\\"Volumes\\\":{},\\\"WorkingDir\\\":\\\"\\\"},\\\"created\\\":\\\"2016-12-27T18:17:25.702182968Z\\\",\\\"docker_version\\\":\\\"1.12.3\\\",\\\"id\\\":\\\"6f8c9172659eb112704e024716f466c6ce950e4719b8c7a9629e4742c1332ea9\\\","+
            "\\\"os\\\":\\\"linux\\\",\\\"parent\\\":\\\"\\\",\\\"throwaway\\\":false}\"}],\"signatures\":[],\"signatures\":[{\"header\":{\"alg\":\"ES256\",\"jwk\":{\"crv\":\"P-256\",\"kid\":\"LMBI:4SKM:GJJK:GE3K:O4JC:EXVI:LZOX:7XQO:B65B:LLZX:AQ7K:5X7R\",\"kty\":\"EC\",\"x\":\"wWKPR8QPcyJKtMC0wrmwzrzmLvDCM50TpQib9UvUGSo\",\"y\":\"ixDhOxbVX45Xoki5YK5lbhiG3AHqpiYQHSxFBYA_tpA\"}},\"protected\":\"eyJmb3JtYXRMZW5ndGgiOjE2NzQsImZvcm1hdFRhaWwiOiJmUSIsInRpbWUiOiIyMDE3LTAxLTI1VDAwOjIxOjI2WiJ9\",\"signature\":\"5jN4xtRigjBPYTe1pl5wvEjq0swoNVzFM1UdcwIKWWFSD2FTtuI_TmyjLQlgFGnTQm0zslsBU2RehScKsSxr_A\"}]}";
        final String layerFileName =
            "/0a8490d0dfd399b3a50e9aaa81dba0d425c3868762d46526b41be00886bcc28b";
        final String tag = "GCR";
        final String layerDigest = "sha256:0a8490d0dfd399b3a50e9aaa81dba0d425c3868762d46526b41be00886bcc28b";
        final File layerFile = new File(getClass().getResource(layerFileName).getFile());
        final long layerLength = layerFile.length();
        if ( ! layerFile.exists() ) throw new IllegalStateException("Could not find '"+layerFile+"' on the classpath");

        GcrBlobUpload upload = client.createBlobUpload(repo.getFullName());
        GcrBlobMeta blobMeta  = client.blobUploadChunk(upload, new FileInputStream(layerFile), null, layerDigest);
        assertEquals(blobMeta.getDigest(), layerDigest);
        assertEquals(blobMeta.getLength().longValue(), layerLength);

        blobMeta = client.getBlobMeta(repo.getFullName(), layerDigest);
        assertEquals(blobMeta.getDigest(), layerDigest);
        assertEquals(blobMeta.getLength().longValue(), layerLength);

        client.getBlob(repo.getFullName(), layerDigest, (is, meta) -> {
                assertEquals(meta.getLength().longValue(), layerLength);
                assertEquals(meta.getDigest(), layerDigest);
                isEqual(is, new FileInputStream(layerFile));
                return null;
            });

        GcrManifestV2Schema1 manifestProto = GcrManifestV2Schema1.create(
            new GcrManifest() {
                @Override
                public String getMediaType() {
                    return GcrManifestV2Schema1.SIGNED_MEDIA_TYPE;
                }
                @Override
                public String toString() {
                    return manifestStr;
                }
            });
        assertEquals(manifestProto.getArchitecture(), "amd64");
        assertEquals(manifestProto.toString(), manifestStr);
        assertEquals(manifestProto.getMediaType(), GcrManifestV2Schema1.SIGNED_MEDIA_TYPE);
        GcrManifestMeta manifestMeta = client.putManifest(repo.getFullName(), tag, manifestProto);

        assertNotNull(manifestMeta.getLocation());
        assertNotNull(manifestMeta.getDigest());

        GcrManifest manifest = client.getManifest(repo.getFullName(), tag);
        assertNotNull(manifest);
        assertEquals(OM.readTree(manifest.toString()), OM.readTree(manifestStr));
        assertEquals(manifest.getMediaType(), GcrManifestV2Schema1.SIGNED_MEDIA_TYPE);



        // Can't delete the blob since it is now referenced:
        //assertTrue(client.deleteBlob(repo.getFullName(), digest));

        
        // THESE ARE UNSUPPORTED in GCR!
        // upload = client.blobUploadChunk(upload, new ByteArrayInputStream(chunk1), (long)chunk1.length);
        // GcrBlobUpload progress = client.getBlobUploadProgress(upload);
        // client.cancelUploadChunk(upload);
    }

    private boolean isEqual(InputStream i1, InputStream i2) throws IOException {
        try {
            // do the compare
            while (true) {
                int fr = i1.read();
                int tr = i2.read();

                if (fr != tr)
                    return false;

                if (fr == -1)
                    return true;
            }
        } finally {
            if (i1 != null)
                i1.close();
            if (i2 != null)
                i2.close();
        }
    }
}
