package org.dsty.system.os.download

import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.NoSuchFileException
import java.nio.channels.ReadableByteChannel

import com.cloudbees.groovy.cps.NonCPS

import org.dsty.system.os.Path


/**
 * Downloads a file from an HTTP url.
 */
class HttpRetriever implements FileRetriever, Serializable {

    /**
     * Downloads the file from the <code>url</code> to the <code>filePath</code>.
     * <p>
     * The <code>filePath</code> must exist.
     *
     * @throws          NoSuchFileException
     * @param url       The {@link URL} where the file will be downloaded from.
     * @param filePath  The path on the system where the file will be written.
     */
    @NonCPS
    private void download(URL url, String filePath) throws NoSuchFileException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())

        FileOutputStream fileOutputStream = new FileOutputStream(filePath) // groovylint-disable-line
        FileChannel fileChannel = fileOutputStream.getChannel()

        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)

        fileChannel.close()
    }

    /**
     * Retrieves the file from the <code>url</code> to the <code>destination</code>.
     * <p>
     * If <code>destination</code> doesn't exist it will be created.
     *
     * @param url       The url where the file will be downloaded from.
     * @param filePath  The {@link org.dsty.system.os.Path} on the system where the file will be written.
     */
    void retrieve(String url, Path destination) {

        if (!destination.exists()) {
            destination.touch()
        }

        final URL httpURL = new URL(url)

        download(httpURL, destination.toString())

    }

}
