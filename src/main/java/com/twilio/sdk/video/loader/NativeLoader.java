package com.twilio.sdk.video.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Random;

public abstract class NativeLoader
{
    private static final String[] NATIVE_LIBRARIES = {
        "twilio-video-1.0.0",
    };

    private static final Object lock = new Object();
    private static volatile boolean loaded = false;

    public static void loadNativeLibraries()
    {
        if (loaded)
            return;

        synchronized (lock)
        {
            if (loaded)
                return;

            // this will throw if it fails, so loaded=true won't get done
            for (final String lib : NATIVE_LIBRARIES)
                loadNativeLibrary(lib);

            loaded = true;
        }
    }

    /**
     * Attempt to load <code>libraryName</code> first from inside our JAR file, then using normal means.
     *
     * This assumes you've packed the native library into your JAR file
     * at path <code>/libs/${os.arch}-${os.name}/</code>.  The arch and
     * name variables will have all spaces converted into underscores.
     *
     * The library will be extracted from the JAR and written to a temporary
     * directory, and then loaded.
     *
     * A shutdown hook will be installed that deletes the library and its
     * temporary directory.
     *
     * If this fails for any reason, it will throw an exception.
     *
     * @param libraryName The library name you'd usually pass to {@link System#LoadLibrary(String)}
     */
    static void loadNativeLibrary(final String libraryName)
    {
        if (libraryName == null)
            throw new IllegalArgumentException("libraryName cannot be null");

        InputStream is = null;
        FileOutputStream os = null;
        File libfile = null;

        try {
            final String osArch = System.getProperty("os.arch").replace(" ", "_").toLowerCase();
            final String osName = System.getProperty("os.name").replace(" ", "_").toLowerCase();
            final String libraryFilename = System.mapLibraryName(libraryName);
            final String resourceName = "/libs/" + osArch + "-" + osName + "/" + libraryFilename;

            is = NativeLoader.class.getResourceAsStream(resourceName);
            if (is == null)
                throw new RuntimeException("Unable to load native library file '" + resourceName + "'");

            final String tmpDir = System.getProperty("java.io.tmpdir");
            if (tmpDir == null)
                throw new RuntimeException("Unable to find system temporary directory");

            File tmpDirFile = null;
            for (int retries = 0; retries < 100; ++retries) {
                tmpDirFile = new File(tmpDir, new BigInteger(128, new Random()).toString(16));
                if (!tmpDirFile.exists() && tmpDirFile.mkdirs())
                    break;
                tmpDirFile = null;
            }

            if (tmpDirFile == null)
                throw new RuntimeException("Can't create temporary directory '" + tmpDir + "'");

            if (!tmpDirFile.setWritable(true, false) ||
                !tmpDirFile.setReadable(true, false) ||
                !tmpDirFile.setExecutable(true, false))
            {
                throw new RuntimeException("Unable to set permissions on temporary directory");
            }

            // make sure nobody tried to do something funny
            // in our new directory before we set perms
            if (!deleteContents(tmpDirFile))
                throw new RuntimeException("Failed to clean out temporary directory. Someone is likely messing with you!");

            libfile = new File(tmpDirFile, libraryFilename);
            os = new FileOutputStream(libfile);
            final byte[] buf = new byte[64*1024];
            int bin;
            while ((bin = is.read(buf)) > 0)
                os.write(buf, 0, bin);

            os.close();
            os = null;

            final File finalLibfile = libfile;
            final File finalTmpDirFile = tmpDirFile;
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    try {
                        finalLibfile.delete();
                        finalTmpDirFile.delete();
                    } catch (final Exception e) { }
                }
            });

            System.load(libfile.getAbsolutePath());
        } catch (final Exception e) {
            try {
                // if that fails, try to load the "normal" way
                System.loadLibrary(libraryName);
            } catch (final UnsatisfiedLinkError e1) {
                throw new RuntimeException(e);  // propagate the original error
            }
        } finally {
            if (os != null) {
                // something failed.  close and delete.
                try {  os.close(); } catch (final Exception e) { }
                if (libfile != null)
                    try { libfile.delete(); } catch (final Exception e)  { }
            }

            if (is != null)
                try { is.close(); } catch (final Exception e) { }
        }
    }

    private static boolean deleteContents(final File directory)
    {
        if (directory == null || !directory.isDirectory())
            throw new IllegalArgumentException("'directory' must refer to a directory");

        final File[] files = directory.listFiles();
        for (final File f : files) {
            if (f.isDirectory()) {
                if (!deleteContents(f))
                    return false;
            }

            if (!f.delete())
                return false;
        }

        return true;
    }
}
