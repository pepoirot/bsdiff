package net.pepoirot.bsdiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BsPatch implements Runnable {

    static int offtin(byte buf[]) {
        int y;

        y = buf[7] & 0x7F;
        y = y * 256;
        y += buf[6];
        y = y * 256;
        y += buf[5];
        y = y * 256;
        y += buf[4];
        y = y * 256;
        y += buf[3];
        y = y * 256;
        y += buf[2];
        y = y * 256;
        y += buf[1];
        y = y * 256;
        y += buf[0];

        if ((buf[7] & 0x80) > 0) y = -y;

        return y;
    }


    private final File oldFile;
    private final File newFile;
    private final File patchFile;

    public BsPatch(File oldFile, File newFile, File patchFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.patchFile = patchFile;
    }

    public BsPatch(String oldFile, String newFile, String patchFile) {
        this(new File(oldFile), new File(newFile), new File(patchFile));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.printf("usage: bspatch oldfile newfile patchfile");
            System.exit(1);
        }

        new BsPatch(args[0], args[1], args[2]).run();
    }

    public void run() {
        try {
            applyPatch();
        } catch (IOException e) {
            throw new IllegalStateException("Could not patch " + oldFile + " with " + patchFile, e);
        }
    }

    public void applyPatch() throws IOException {
        InputStream f = null, cpf = null, dpf = null, epf = null;
        InputStream cpfbz2 = null, dpfbz2 = null, epfbz2 = null;
        InputStream fd = null;
        int oldsize = 0, newsize;
        long bzctrllen, bzdatalen;
        byte[] header = new byte[32];
        byte[] buf = new byte[8];
        byte[] old;
        byte[] newS;
        int oldpos, newpos;
        int[] ctrl = new int[3];
        long lenread;
        int i;

        /* Open patch file */
        try {
            f = new FileInputStream(patchFile);
        } catch (FileNotFoundException e) {
            throw new IOException("Can not open " + patchFile);
        }

        /*
      File format:
          0	8	"BSDIFF40"
          8	8	X
          16	8	Y
          24	8	sizeof(newfile)
          32	X	bzip2(control block)
          32+X	Y	bzip2(diff block)
          32+X+Y	???	bzip2(extra block)
      with control block a set of triples (x,y,z) meaning "add x bytes
      from oldfile to x bytes from the diff block; copy y bytes from the
      extra block; seek forwards in oldfile by z bytes".
      */

        /* Read header */
        if (f.read(header, 0, 32) < 32) {
            if (f.read() == -1) {
                throw new IOException("Corrupt patch\n");
            }
            throw new IOException("Invalid header " + patchFile);
        }

        /* Check for appropriate magic */
        if (!new String(header).startsWith("BSDIFF40"))
            throw new IOException("Corrupt patch\n");

        /* Read lengths from header */
        bzctrllen = offtin(Arrays.copyOfRange(header, 8, header.length));
        bzdatalen = offtin(Arrays.copyOfRange(header, 16, header.length));
        newsize = offtin(Arrays.copyOfRange(header, 24, header.length));
        if ((bzctrllen < 0) || (bzdatalen < 0) || (newsize < 0))
            throw new IOException("Corrupt patch\n");

        /* Close patch file and re-open it via bzip2 at the right places */
        try {
            f.close();
        } catch (IOException e) {
            throw new IOException("Can't close " + patchFile);
        }

        try {
            cpf = new FileInputStream(patchFile);
        } catch (FileNotFoundException e) {
            err("Can't open(%s)", patchFile);
        }

        if (cpf.skip(32) != 32) {
            err("Can't seek(%s, %lld)", patchFile, 32);
        }

        try {
            cpfbz2 = new BZip2InputStream(cpf);
        } catch (IOException e) {
            err(e.getMessage());
        }

        try {
            dpf = new FileInputStream(patchFile);
        } catch (FileNotFoundException e) {
            err("fopen(%s)", patchFile);
        }

        if (dpf.skip(32 + bzctrllen) != (32 + bzctrllen)) {
            err("fseeko(%s, %lld)", patchFile,
                    (32 + bzctrllen));
        }

        try {
            dpfbz2 = new BZip2InputStream(dpf);
        } catch (IOException e) {
            err(e.getMessage());
        }

        try {
            epf = new FileInputStream(patchFile);
        } catch (FileNotFoundException e) {
            err("fopen(%s)", patchFile);
        }

        if (epf.skip(32 + bzctrllen + bzdatalen) != (32 + bzctrllen + bzdatalen)) {
            err("fseeko(%s, %lld)", patchFile,
                    (32 + bzctrllen + bzdatalen));
        }

        try {
            epfbz2 = new BZip2InputStream(epf);
        } catch (IOException e) {
            err(e.getMessage());
        }

        try {
            fd = new FileInputStream(oldFile);
        } catch (FileNotFoundException e) {
            err("Can't open %s", oldFile);
        }

        oldsize = (int) oldFile.length();
        old = new byte[oldsize];

        if (fd.read(old, 0, oldsize) != oldsize) {
            err("%s", oldFile);
        }

        try {
            fd.close();
        } catch (IOException e) {
            err("%s", oldFile);
        }

        newS = new byte[newsize];

        oldpos = 0;
        newpos = 0;
        while (newpos < newsize) {
            /* Read control data */
            for (i = 0; i <= 2; i++) {
                lenread = cpfbz2.read(buf, 0, 8);
                if ((lenread < 8))
                    err("Corrupt patch\n");
                ctrl[i] = offtin(buf);
            }

            /* Sanity-check */
            if (newpos + ctrl[0] > newsize)
                err("Corrupt patch\n");

            /* Read diff string */
            lenread = dpfbz2.read(newS, newpos, ctrl[0]);
            if ((lenread < ctrl[0]))
                err("Corrupt patch\n");

            /* Add old data to diff string */
            for (i = 0; i < ctrl[0]; i++)
                if ((oldpos + i >= 0) && (oldpos + i < oldsize))
                    newS[newpos + i] += old[oldpos + i];

            /* Adjust pointers */
            newpos += ctrl[0];
            oldpos += ctrl[0];

            /* Sanity-check */
            if (newpos + ctrl[1] > newsize)
                err("Corrupt patch\n");

            /* Read extra string */
            lenread = epfbz2.read(newS, newpos, ctrl[1]);
            if ((lenread < ctrl[1]))
                err("Corrupt patch\n");

            /* Adjust pointers */
            newpos += ctrl[1];
            oldpos += ctrl[2];
        }

        /* Clean up the bzip2 reads */
        cpfbz2.close();
        dpfbz2.close();
        epfbz2.close();

        try {
            cpf.close();
            dpf.close();
            epf.close();
        } catch (IOException e) {
            err("fclose(%s)", patchFile);
        }

        /* Write the new file */
        final FileOutputStream fw = new FileOutputStream(newFile);
        fw.write(newS);
        fw.close();
    }

    public static void err(String message, Object... args) throws IOException {
        throw new IOException(String.format(message, args));
    }

}