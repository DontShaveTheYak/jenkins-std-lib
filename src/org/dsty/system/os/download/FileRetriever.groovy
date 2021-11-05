package org.dsty.system.os.download

import org.dsty.system.os.Path

/**
 * A {@link FileRetriever} is capable of retrieving a file from some remote source and saving it to the current
 * Jenkins agent.
 */
interface FileRetriever {

    /**
     * Retrieves a file/files from the given <code>url</code> and saves it to the <code>destination</code>
     * file/directory.
     * <p>
     * It's up to the <code>Class</code> that implements {@link #retrieve(String, Path) retrieve} to decide
     * how to handle a <code>destination</code> that might or might not already exist.
     *
     * @param url          The path to the remote file/files to be retrieved.
     * @param destination  The {@link Path} on the machine the file/files will be saved.
     */
    void retrieve(String url, Path destination)

}
