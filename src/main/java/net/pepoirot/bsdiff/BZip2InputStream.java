package net.pepoirot.bsdiff;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

public class BZip2InputStream extends BZip2CompressorInputStream {
    public BZip2InputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public int read(byte[] dest, int offs, int len) throws IOException {
        return len != 0 ? super.read(dest, offs, len) : 0;
    }
}
