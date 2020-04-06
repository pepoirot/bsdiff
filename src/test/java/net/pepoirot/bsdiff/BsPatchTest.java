package net.pepoirot.bsdiff;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static net.pepoirot.bsdiff.TestUtils.assertContentEquals;
import static net.pepoirot.bsdiff.TestUtils.getFile;

public final class BsPatchTest {

    private static final String SOURCE = "original.txt";
    private static final String PATCH = "patch.txt";
    private static final String PATCHED = "patched.txt";

    @Test
    public void testRun() throws IOException {
        final File outFile = File.createTempFile(this.getClass().getSimpleName(), null);
        final File sourceFile = getFile(SOURCE);
        final File patchFile = getFile(PATCH);

        try {
            new BsPatch(sourceFile, outFile, patchFile).run();
            assertContentEquals(getFile(PATCHED), outFile);
        } finally {
            FileUtils.deleteQuietly(outFile);
        }
    }

}
