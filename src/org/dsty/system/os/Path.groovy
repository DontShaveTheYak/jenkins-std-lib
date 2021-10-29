/* groovylint-disable CatchException, DuplicateStringLiteral, FactoryMethodName, UnnecessaryGetter, MethodCount */
package org.dsty.system.os

import java.net.URL
import hudson.FilePath
import java.util.Date
import hudson.util.io.ArchiverFactory
import com.cloudbees.groovy.cps.NonCPS
import hudson.model.TaskListener

import org.dsty.jenkins.Build

/**
 * A wrapper around {@link hudson.FilePath} that allows easy file operations
 * with out resorting to a shell like <code>sh()</code> or <code>bat()</code>.
 * <p>
 * A nice starting point is to get the {@link Path} to your jobs workspace using
 * <a href="Path.html#workspace()">Path.workspace()</a>. You can also get
 * the {@link Path} to the current users home directory using
 * <a href="Path.html#userHome()">Path.userHome()</a> or
 * <a href="Path.html#jenkinsHome()">Path.jenkinsHome()</a>. You can obtain a
 * {@link Path} to the current working directory with
 * <a href="Path.html#cwd()">Path.cwd()</a>.
 */
class Path implements Serializable {

    /**
     * Internal {@link FilePath} object.
     */
    private final FilePath fp

    /**
     * Creates a Path from a String reprensentation.
     *
     * @param path An absolute path on the system. The path doesn't have to exist yet.
     */
    Path(String path) {
        this.fp = new FilePath(null, path)
    }

    /**
     * Creates a Path from a FilePath object.
     *
     * @param path A path on the system. The path doesn't have to exist yet.
     */
    private Path(FilePath path) {
        this.fp = path
    }

    /**
     * Returns an absolute Path from the current Path instance.
     *
     * If the current Path is already absolute, then it's returned as a new Path.
     *
     * If the current Path is empty then the Path to the current user directory,
     * which is determined by the system property **user.dir**, is returned.
     *
     * Otherwise the current Path is resolved in a system-dependent way. On UNIX
     * systems, a relative Path is made absolute by resolving it against the current
     * user directory. On Microsoft Windows systems, a relative Path is made absolute
     * by resolving it against the current directory of the drive named by the
     * Path, if any; if not, it is resolved against the current user directory.
     *
     * @return  The absolute Path denoting the same file or
     * directory as the current Path.
     */
    Path absolutize() {
        FilePath abs = fp.absolutize()
        return new Path(abs)
    }

    /**
     * Get a Path to a file/directory using a relative or absolute path
     * from the current Path.
     *
     * @param path A relative or absolute path
     * @return A Path to the child file/directory.
     */
    Path child(String path) {
        FilePath child = fp.child(path)
        return new Path(child)
    }

    /**
     * Sets the file permission.
     *
     * On Windows, no-op.
     *
     * @param mask File permission mask. To simplify the permission copying,
     *             if the parameter is -1, this method becomes no-op.
     *
     *             please note mask is expected to be an octal if you use chmod command line values,
     *             so preceded by a '0' in java notation, ie chmod(0644)
     *
     *             Only supports setting read, write, or execute permissions for the
     *             owner, group, or others, so the largest permissible value is 0777.
     *             Attempting to set larger values (i.e. the setgid, setuid, or sticky
     *             bits) will cause an IOException to be thrown.
     */
    void chmod(int mask) {
        fp.chmod(mask)
    }

    /**
     * Copies the src Path to the current Path.
     *
     * @param src The file/directory that is being copied to the
     * current Path.
     */
    void copyFrom(Path src) {
        fp.copyFrom(src.fp)
    }

    /**
     * Copies current Path to the specified target.
     *
     * @param target The destination path.
     */
    void copyTo(Path target) {
        fp.copyTo(target.fp)
    }

    /**
     * Copies the contents of this directory recursively into the specified target directory.
     *
     * @param target The directory where items will be copied to.
     * @return The number of files copied.
     */
    int copyRecursiveTo(Path target) {
        return fp.copyRecursiveTo(target.fp)
    }

    /**
     * Creates a temporary directory.
     *
     * The directory is named temp.dir-$RAND_NUMBERS and will be
     * deleted when the Closure exits, regardless if any Exceptions are throws.
     *
     * The tempDir Path will be passed into the closure.
     *
     * @param userCode The code to be executed while the temp directory exists.
     */
    void withTempDir(Closure userCode) {
        FilePath tempFP = fp.createTempDir('temp', 'dir-')
        Path tmpDir = new Path(tempFP)
        try {
            userCode(tmpDir)
            tmpDir.deleteRecursive()
        } catch (Exception ex) {
            tmpDir.deleteRecursive()
        }

    }

    /**
     * Creates a temporary directory.
     *
     * You can provide your own Path which allows you full control
     * over the name of the directory. The Path is deleted when the
     * Closure exits.
     *
     * The tempDir Path will be passed into the closure.
     *
     * @param tempDir A Path to a directory that may or may not exist.
     * @param userCode The code to be executed while the temp directory exists.
     */
    void withTempDir(Path tempDir, Closure userCode) {
        tempDir.mkdirs()
        try {
            userCode(tempDir)
            tempDir.deleteRecursive()
        } catch (Exception ex) {
            tempDir.deleteRecursive()
        }

    }

    /**
     * Creates a temporary file.
     *
     * The file is named temp.file-$RAND_NUMBERS and will be
     * deleted when the Closure exits, regardless if any Exceptions are throws.
     *
     * The tempDir Path will be passed into the closure.
     *
     * @param userCode The code to be executed while the temp file exists.
     */
    void withTempFile(Closure userCode) {
        FilePath tempFP = fp.createTempDir('temp', 'file-')
        Path tmpFile = new Path(tempFP)

        try {
            userCode(tmpFile)
            tmpFile.delete()
        } catch (Exception ex) {
            tmpFile.delete()
        }

    }

    /**
     * Creates a temporary file.
     *
     * You can provide your own Path which allows you full control
     * over the name of the file. The Path is deleted when the
     * Closure exits.
     *
     * The tempDir Path will be passed into the closure.
     *
     * @param tempFile A Path to a file that may or may not exist.
     * @param userCode The code to be executed while the temp file exists.
     */
    void withTempFile(Path tempFile, Closure userCode) {
        tempFile.touch()
        try {
            userCode(tempFile)
            tempFile.delete()
        } catch (Exception ex) {
            tempFile.delete()
        }

    }

    /**
     * Deletes the file or directory denoted by the current Path. If
     * the Path is a directory, then the directory must be empty in
     * order to be deleted.
     *
     * Note that the java.nio.file.Files class defines the delete
     * method to throw an IOException when a file cannot be deleted.
     * This is useful for error reporting and to diagnose why a file
     * cannot be deleted.
     *
     * @return true if and only if the file or directory is
     *          successfully deleted; false otherwise
     */
    boolean delete() {
        return fp.delete()
    }

    /**
     * Deletes all the contents of this directory, but not the directory itself
     */
    void deleteContents() {
        fp.deleteContents()
    }

    /**
     * Deletes this directory, including all its contents recursively.
     */
    void deleteRecursive() {
        fp.deleteRecursive()
    }

    /**
     * Computes the MD5 digest of the file in hex string.
     *
     * @return The MD5 digest.
     */
    String digest() {
        return fp.digest()
    }

    @Override
    @NonCPS
    boolean equals(Object otherPath) {
        return otherPath.toString() == this.toString()
    }

    /**
     * Checks if the file exists.
     *
     * @return true if the file is present, false otherwise.
     */
    Boolean exists() {
        return fp.exists()
    }

    /**
     * Gets the file name portion except the extension.
     *
     * For example, "foo" for "foo.txt" and "foo.tar" for "foo.tar.gz".
     *
     * @return The name of the file or directory for the current Path.
     */
    String getBaseName() {
        return fp.getBaseName()
    }

    /**
     * Returns the number of unallocated bytes in the partition named
     * by the current Path.
     *
     * The returned number of unallocated bytes is a hint, but not
     * a guarantee, that it is possible to use most or any of these
     * bytes. The number of unallocated bytes is most likely to be
     * accurate immediately after this call. It is likely to be made
     * inaccurate by any external I/O operations including those made
     * on the system outside of this virtual machine. This method
     * makes no guarantee that write operations to this file system
     * will succeed.
     *
     * @return  The number of unallocated bytes on the partition or <tt>0L</tt>
     *          if the abstract pathname does not name a partition. This
     *          value will be less than or equal to the total file system size
     *          returned by getTotalDiskSpace.
     */
    long getFreeDiskSpace() {
        return fp.getFreeDiskSpace()
    }

    /**
     * Gets just the file name portion without directories.
     *
     * For example, "foo.txt" for "../abc/foo.txt"
     *
     * @return The name of the file/directory.
     */
    String getName() {
        return fp.getName()
    }

    /**
     * Gets the parent Path.
     *
     * @return The parent Path or null if there is no parent.
     */
    Path getParent() {
        FilePath parent = fp.getParent()
        return new Path(parent)
    }

    /**
     * Gets the full path of the file on the remote machine.
     *
     * @return The full path of the remote Path.
     */
    String getRemote() {
        return fp.getRemote()
    }

    /**
     * Returns the size of the partition named by this Path.
     *
     * @return The size, in bytes, of the partition or <tt>0L</tt> if the
     *         Path does not name a partition.
     */
    long getTotalDiskSpace() {
        return fp.getTotalDiskSpace()
    }

    /**
     * Returns the number of bytes available to this virtual machine on the
     * partition named by this Path. When possible, this method checks
     * for write permissions and other operating system restrictions and will
     * therefore usually provide a more accurate estimate of how much new data
     * can actually be written than getFreeDiskSpace.
     *
     * The returned number of available bytes is a hint, but not a guarantee,
     * that it is possible to use most or any of these bytes. The number of
     * unallocated bytes is most likely to be accurate immediately after this call.
     * It is likely to be made inaccurate by any external I/O operations including
     * those made on the system outside of this virtual machine. This method makes
     * no guarantee that write operations to this file system will succeed.
     *
     * @return  The number of available bytes on the partition or <tt>0L</tt>
     *          if the Path does not name a partition. On systems
     *          where this information is not available, this method
     *          will be equivalent to a call to getFreeDiskSpace.
     */
    long getUsableDiskSpace() {
        return fp.getUsableDiskSpace()
    }

    @Override
    @NonCPS
    int hashCode() {
        return fp.hashCode()
    }

    /**
     * Downloads a tgz/zip file from a URL and extracts it to the current {@link Path}, if necessary.
     * <p>
     * This method is a convenience method designed for installing a binary package to a location
     * that supports upgrade and downgrade. Specifically,
     * <ul>
     * <li>If the target directory doesn't exist {@linkplain #mkdirs() it will be created}.
     * <li>The timestamp of the archive is left in the installation directory upon extraction.
     * <li>If the timestamp left in the directory does not match the timestamp of the current archive file,
     *     the directory contents will be discarded and the archive file will be re-extracted.
     * <li>If the connection is refused but the target directory already exists, it is left alone.
     * </ul>
     *
     * @param downloadURL  The URL to download the tgz/zip file from. This URL must support the
     *                     {@code Last-Modified} header.
     * @return  <code>true</code> if the archive was extracted. false if the extraction was skipped
     *          because the target directory was considered up to date.
     */
    Boolean installIfNecessaryFrom(String downloadURL) {
        return installIfNecessaryFrom(downloadURL, "Extracting to ${fp.getRemote()}")
    }

    /**
     * Downloads a tgz/zip archive file from a URL and extracts it to the current {@link Path}, if necessary.
     * <p>
     * This method is a convenience method designed for installing a binary package to a location
     * that supports upgrade and downgrade. Specifically,
     * <ul>
     * <li>If the target directory doesn't exist {@linkplain #mkdirs() it will be created}.
     * <li>The timestamp of the archive is left in the installation directory upon extraction.
     * <li>If the timestamp left in the directory does not match the timestamp of the current archive file,
     *     the directory contents will be discarded and the archive file will be re-extracted.
     * <li>If the connection is refused but the target directory already exists, it is left alone.
     * </ul>
     *
     * @param downloadURL  The URL to download the tgz/zip file from. This URL must support the
     *                     {@code Last-Modified} header.
     * @param extractMsg   A msg that is displayed in the build console if the arhcive
     * @return  <code>true</code> if the archive was extracted. false if the extraction was skipped
     *          because the target directory was considered up to date.
     */
    Boolean installIfNecessaryFrom(String downloadURL, String extractMsg) {
        final URL url = new URL(downloadURL)

        final Build build = new Build()
        final TaskListener task = build.getCurrentContext(hudson.model.TaskListener)

        return fp.installIfNecessaryFrom(url, task, extractMsg)
    }

    /**
     * Check if the relative child is really a descendant after symlink resolution if any.
     *
     * @return true if the file is a descendant, false otherwise.
     */
    boolean isDescendant(String childPath) {
        return fp.isDescendant(childPath)
    }

    /**
     * Tests whether the file denoted by this Path is a
     * directory.
     *
     * @return  true if and only if the file denoted by this
     *          Path exists <em>and</em> is a directory,
     *          false otherwise.
     */
    boolean isDirectory() {
        return fp.isDirectory()
    }

    /**
     * Checks if this Path is on Jenkins or an Agent.
     *
     * @return true if the path is on an agent, false otherwise.
     */
    boolean isRemote() {
        return fp.isRemote()
    }

    /**
     * Gets the last modified time stamp of this file/directory, by using the clock
     * of the machine where this file actually resides.
     *
     * @return  A long value representing the time the file was
     *          last modified, measured in milliseconds since the epoch
     *          (00:00:00 GMT, January 1, 1970), or 0L if the
     *          file does not exist or if an I/O error occurs
     */
    long lastModified() {
        return fp.lastModified()
    }

    /**
     * Returns the length of the file denoted by this Path.
     * The return value is unspecified if this pathname denotes a directory.
     *
     * @return  The length, in bytes, of the file denoted by this
     *          Path, or 0L if the file does not exist.  Some
     *          operating systems may return 0L for pathnames
     *          denoting system-dependent entities such as devices or pipes.
     */
    long length() {
        return fp.length()
    }

    List<Path> list() {
        List<FilePath> dirContents = fp.list()

        List<Path> results = dirContents.collect { FilePath path -> new Path(path) }

        return results
    }

    /**
     * Returns an array of Paths naming the files and directories in the
     * directory denoted by the current Path.
     *
     * If this Path does not denote a directory, then this
     * method returns null. Otherwise an array of strings is
     * returned, one for each file or directory in the directory. Names
     * denoting the directory itself and the directory's parent directory are
     * not included in the result.
     *
     * There is no guarantee that the name strings in the resulting array
     * will appear in any specific order; they are not, in particular,
     * guaranteed to appear in alphabetical order.
     *
     * @return  An array of strings naming the files and directories in the
     *          directory denoted by the current Path. The array will be
     *          empty if the directory is empty. Returns null if
     *          this abstract pathname does not denote a directory, or if an
     *          I/O error occurs.
     */
    List<Path> list(String includes, String excludes) {
        List<FilePath> filteredFiles = fp.list(includes, excludes)

        List<Path> results = filteredFiles.collect { FilePath path -> new Path(path) }

        return results
    }

    /**
     * List up files and directories in this directory.
     *
     * This method returns direct children of the directory denoted by the 'this' object.
     *
     * return A list of Paths found in the current directory.
     */
    List<Path> listDirectories() {
        List<FilePath> dirs = fp.listDirectories()

        List<Path> results = dirs.collect { FilePath path -> new Path(path) }

        return results
    }

    /**
     * If path doesn't exist, create it as a directory.
     */
    void mkdirs() {
        fp.mkdirs()
    }

    /**
     * Gets the file permission bit mask.
     *
     * @return -1 on Windows, since such a concept doesn't make sense.
     * @see #chmod(int)
     */
    int mode() {
        return fp.mode()
    }

    /**
     * Moves all the contents of this directory into the specified directory, then delete this directory itself.
     *
     * @param target The destination where the files/directories will be moved to.
     */
    void moveAllChildrenTo(Path target) {
        fp.moveAllChildrenTo(target.fp)
    }

    /**
     * Reads this file into a string, by using the current system encoding on the remote machine.
     * @return The contents of the file.
     */
    String read() {
        return fp.readToString()
    }

    /**
     * Rename this file/directory to the target filepath.
     * @param target The new path to use.
     */
    void renameTo(Path target) {
        fp.renameTo(target.fp)
    }

    /**
     * Get another file/folder from the same directory.
     *
     * @param relPath A relative path that starts at the current paths parent.
     * @return A path to the sibling.
     */
    Path sibling(String relPath) {
        FilePath sib = fp.sibling(relPath)
        return new Path(sib)
    }

    /**
     * Creates a tar file from this directory/file and writes it to the given Path.
     *
     * @param dst The path to where the tar file is to be written.
     * @param gzip Use gzip compression, defaults to true.
     * @return The number of files/directories archived. This is only really useful
     *         to check if archiving was succesful.
     */
    int tar(Path dst, Boolean gzip = true) {
        ArchiverFactory compression = gzip ? ArchiverFactory.TARGZ : ArchiverFactory.TAR
        OutputStream os = dst.fp.write()
        return fp.archive(compression, os, (FileFilter)null)
    }

    /**
     * Creates a tar file from this directory and includes only the files that match
     * the supplied pattern.
     *
     * @param dst The path to where the tar file is to be written.
     * @param glob Ant file pattern mask, like "**&#x2F;*.java".
     * @param gzip Use gzip compression, defaults to true.
     * @return The number of files/directories archived. This is only really useful
     *         to check if archiving was succesful.
     */
    int tar(Path dst, String glob, Boolean gzip = true) {
        ArchiverFactory compression = gzip ? ArchiverFactory.TARGZ : ArchiverFactory.TAR
        OutputStream os = dst.fp.write()
        return fp.archive(compression, os, glob)
    }

    @Override
    @NonCPS
    String toString() {
        return fp.getRemote()
    }

    /**
     * Creates an empty file if it doesn't exist
     */
    void touch() {
        Date date = new Date()
        fp.touch(date.getTime())
    }

    /**
     * When the current path represents a tar file, extract the contents.
     *
     * @param target Target directory to expand files to. All the necessary directories will be created.
     * @param gzip Use gzip compression, defaults to true.
     */
    void untar(Path target, Boolean gzip = true) {
        FilePath.TarCompression compression = gzip ? FilePath.TarCompression.GZIP : FilePath.TarCompression.NONE
        fp.untar(target.fp, compression)
    }

    /**
     * When the current path represents a zip file, extract the contents.
     *
     * @param target Target directory to expand files to. All the necessary directories will be created.
     */
    void unzip(Path target) {
        fp.unzip(target.fp)
    }

    /**
     * Returns a {@link Path} by adding the given suffix to the current path.
     *
     * @param suffix The string to append to the current path.
     * @return The new path.
     */
    Path withSuffix(String suffix) {
        FilePath sufPath = fp.withSuffix(suffix)
        return new Path(sufPath)
    }

    /**
     * Creates a zip file from this directory or a file and writes it to the given Path.
     *
     * @param dst The path to where the zip file is to be written.
     */
    void zip(Path dst) {
        fp.zip(dst.fp)
    }

    /**
     * Overwrites the current file by placing the given String as the content.
     *
     * @param content The content to write to the file
     * @param encoding The charset like UTF-8, the default is null which will use the
     * default charset for the platform where the writing is happening.
     */
    void write(String content, String encoding = null) {
        fp.write(content, encoding)
    }

    /**
     * Get the path to the current builds workspace.
     *
     * @return A {@link Path} to the current builds workspace.
     */
    static Path workspace() {

        final Build build = new Build()
        Map<String, String> envVars = build.environmentVars()

        return new Path(envVars.WORKSPACE)
    }

    /**
     * Get the path to the current working directory. This is the same
     * as the <code>pwd()</code> step.
     * <p>
     * This method is useful for when you might be inside of
     * a <code>dir('someDir') {}</code> block and need to get
     * the path to it.
     *
     * @return A {@link Path} to the current working directory.
     */
    static Path cwd() {
        // Making build a static field caused random errors
        final Build build = new Build()

        FilePath cwd = build.getCurrentContext(FilePath.class)

        return new Path(cwd)
    }

    /**
     * Get the path to the current users home. This is the same
     * as <code>~<code> on unix systems.
     *
     * @return A {@link Path} to the current users home.
     */
    static Path userHome() {
        // Making build a static field caused random errors
        final Build build = new Build()

        Map<String, String> envVars = build.environmentVars()

        return new Path(envVars.HOME)
    }

    /**
     * Get the path to the Jenkins home directory.
     *
     * @return A {@link Path} to the Jenkins home directory.
     */
    static Path jenkinsHome() {
        // Making build a static field caused random errors
        final Build build = new Build()

        Map<String, String> envVars = build.environmentVars()

        return new Path(envVars.JENKINS_HOME)
    }

}
