/* groovylint-disable UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.system.os.Path
import org.dsty.system.os.download.FileRetriever
import org.dsty.system.System

/**
 * A {@link ToolInstaller} that uses a {@link org.dsty.system.os.download.FileRetriever FileRetriever}
 * to install a {@link CliTool} to the system.
 */
abstract class UrlInstaller extends AbstractToolInstaller {

    /**
     * If the file returned by the {@link org.dsty.system.os.download.FileRetriever FileRetriever}
     * will be a zip or tar file then set this to <code>true</code>.
     *
     * @see     #downloadPath()
     * @return  <code>true</code> if the file is a zip/tar file, otherwise <code>false</code>.
     */
    abstract Boolean isArchive()

    /**
     * This specifies the type of {@link org.dsty.system.os.download.FileRetriever FileRetriever} you want to use.
     *
     * @return A class that implements the {@link org.dsty.system.os.download.FileRetriever FileRetriever} interface.
     */
    abstract Class<FileRetriever> getRetriever()

    /**
     * This returns the download url for your {@link Executable}.
     * <p>
     * This url should point to a file that can be downloaded to install your {@link CliTool}. The type of file
     * will depend on your {@link org.dsty.system.os.download.FileRetriever FileRetriever}
     * specified by {@link #getRetriever()}.
     * <p>
     * Often the download URL will depend on the type of system and it's architecture. The
     * {@link org.dsty.system.System} will be passed in to provide that information.
     *
     * @param system  A {@link org.dsty.system.System} which provides the type of system and other
     *                information like the architecture.
     * @return        The url to download the file/files needed to install the {@link CliTool}
     */
    abstract String downloadUrl(System system)

    /**
     * Installs the {@link CliTool} to the system.
     * <p>
     * When called this will first check if the tool is installed using {@link #isInstalled()}
     * and if not then it will call {@link #download()} to get the {@link Executable} from the
     * {@link #downloadUrl()} using the {@link org.dsty.system.os.download.FileRetriever FileRetriever}
     * specified by {@link #getRetriever()}.
     * <p>
     * The {@link CliTool} is then created using {@link #getTool()}.
     *
     * @return A {@link CliTool} that is ready to run commands on the system.
     */
    CliTool install() {

        if (!isInstalled()) {
            download()
        }

        return getTool()
    }

    /**
     * Create the {@link org.dsty.system.System} object and uses it to call
     * {@link #downloadUrl()}.
     *
     * @return The url to download the file/files needed to install the {@link CliTool}.
     */
    protected String getDownloadUrl() {

        final System system = getSystem()
        return downloadUrl(system)
    }

    /**
     * The {@link org.dsty.system.os.Path Path} that is passed to
     * {@link #download()} when {@link #install()} is called.
     * <p>
     * If the {@link org.dsty.system.os.download.FileRetriever FileRetriever} {@link #isArchive()} then
     * {@link installDir()} is returned so the contents of the archive can be unpacked, otherwise
     * {@link installPath()} is returned.
     *
     * @return  If {@link #isArchive()} then {@link installDir()}, otherwise {@link installPath()}.
     */
    protected Path downloadPath() {

        Path destination = isArchive() ? installDir() : installPath()

        return destination
    }

    /**
     * Downloads the file/files from {@link #getDownloadUrl()}.
     * <p>
     * The file/files is downloaded to {@link #downloadPath()} using the
     * {@link org.dsty.system.os.download.FileRetriever FileRetriever} from {@link #getRetriever()}.
     *
     * @return The {@link org.dsty.system.os.Path Path} from {@link downloadPath()}.
     */
    protected Path download() {

        final Path destination = downloadPath()

        return retrieve(destination)
    }

    /**
     * Downloads the file/files from {@link #getDownloadUrl()}.
     * <p>
     * The file/files is downloaded to a path inside of {@link #installDir()}. This method
     * is useful for installing additional files for the {@link CliTool} like config files.
     *
     * @param childPath  A path that will be combined with {@link #installDir()} to create
     *                   the download location.
     * @return           The {@link org.dsty.system.os.Path} to the downloaded file/files.
     */
    protected Path download(String childPath) {

        final Path destination = installDir().child(childPath)

        return retrieve(destination)
    }

    /**
     * Downloads the file/files from {@link #getDownloadUrl()} using the
     * {@link org.dsty.system.os.download.FileRetriever FileRetriever} from {@link #getRetriever()}.
     *
     * @param destination  The {@link org.dsty.system.os.Path} to where the file/files will be downloaded.
     * @return             The parameter <code>destination</code> is returned.
     */
    protected Path retrieve(final Path destination) {

        final String url = getDownloadUrl()

        final FileRetriever source = getFileRetriever()

        source.retrieve(url, destination)

        return destination

    }

    /**
     * Creates an instance of {@link org.dsty.system.os.download.FileRetriever}
     * specifed by {@link #getRetriever()}.
     *
     * @return  A instance of {@link org.dsty.system.os.download.FileRetriever}.
     */
    protected FileRetriever getFileRetriever() {

        Class<FileRetriever> retriever = getRetriever()

        return retriever.newInstance()

    }

}
