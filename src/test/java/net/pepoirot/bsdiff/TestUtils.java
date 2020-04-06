package net.pepoirot.bsdiff;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class TestUtils {
    static File getFile(String filename) {
        final URL resource = TestUtils.class.getResource("/" + filename);
        if (resource == null) {
            throw new IllegalStateException("Check " + filename + " is in the test build directory");
        }
        return new File(resource.getFile());
    }

    static void assertContentEquals(File expected, File actual) throws IOException {
        final byte[] expectedContent = readFileToByteArray(expected);
        final byte[] actualContent = readFileToByteArray(actual);

        assertArrayEquals(expectedContent, actualContent);
    }
}
