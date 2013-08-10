package net.pepoirot.bsdiff;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.junit.Assert.assertArrayEquals;

abstract class TestBase {
    protected static final String SOURCE = "original.txt";
    protected static final String PATCH = "patch.txt";
    protected static final String PATCHED = "patched.txt";

    protected File getFile(String filename) {
        final URL resource = this.getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IllegalStateException("Check " + filename + " is in the test build directory");
        }
        return new File(resource.getFile());
    }

    protected void assertContentEquals(File expected, File actual) throws IOException {
        final byte[] expectedContent = readFileToByteArray(expected);
        final byte[] actualContent = readFileToByteArray(actual);

        assertArrayEquals(expectedContent, actualContent);
    }
}
