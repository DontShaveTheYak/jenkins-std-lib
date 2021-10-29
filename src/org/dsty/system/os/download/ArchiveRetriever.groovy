package org.dsty.system.os.download

import org.dsty.system.os.Path

/**
 * Downloads a <code>tar</code> or <code>zip</code> archive file from an HTTP url and unpacks it.
 */
class ArchiveRetriever implements FileRetriever, Serializable {

    /**
     * Retrieves the archive tar/zip file from the <code>url</code> and extracts it into the <code>destination</code>.
     * <p>
     * If <code>destination</code> doesn't exist it will be created.
     *
     * @param url       The url where the archive file will be downloaded from.
     * @param filePath  The {@link org.dsty.system.os.Path} on the system where the archive will be extracted to.
     */
    void retrieve(String url, Path destination) {

        destination.installIfNecessaryFrom(url)

    }

}
