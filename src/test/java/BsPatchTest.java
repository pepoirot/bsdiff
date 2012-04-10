import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public final class BsPatchTest extends TestBase {

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
