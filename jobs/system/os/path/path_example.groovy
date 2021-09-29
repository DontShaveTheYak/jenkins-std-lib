@Library('jenkins-std-lib')

import org.dsty.system.os.Path

node() {

    // Get a path to the current workspace.
    Path workspace = Path.workspace()

    println("Workspace Path is ${workspace}")

    // Make sure the workspace is empty
    workspace.deleteContents()

    stage('Working with directories') {

        // Create a directory to hold our tests
        Path dirA = new Path("${workspace}/dirA")

        // dirA does not exist yet, we need to create it
        dirA.mkdirs()

        // We can create a sub directory using the full path or using these methods
        Path dirB = dirA.withSuffix('/dirB')
        dirB.mkdirs()

        Path dirB2 = dirA.child('dirB2')
        dirB2.mkdirs()

        // We can also create a temporary directory
        dirB.withTempDir { Path tmpDir ->

            // get tmpDir's parent
            Path parent = tmpDir.getParent()

            // Verify that dirB is the parent
            if (parent != dirB) {
                error("${dirB} is not the parent of ${tmpDir}.")
            }

        }

        // Make sure the tmpDir was deleted
        List<Path> contentsDirB = dirB.list()
        if (contentsDirB) {
            error("${dirB} should be empty but contains: ${contentsDirB}")
        }

        // delete() will fail because dirA is not empty
        dirA.deleteRecursive() // deletes the contents and then deletes dirA

        // Lets verify all of our directories were deleted
        if (dirA.exists() || dirB.exists() || dirB2.exists()) {
            error('Should have deleted all directories.')
        }

        String someDir = "${workspace}/someDir"

        dir(someDir) {

            // Get a Path to the current working directory
            Path cwd = Path.cwd()

            // Verify cwd is the directory we are in.
            if (cwd != someDir) {
                error("Should be in ${someDir} but acutally in ${cwd}")
            }
        }

    }

    stage('Working with files') {

        // Create a Path for a future file inside of the workspace
        Path testFile = workspace.child('hello_world.txt')

        // We can create the file with .touch or just write to it
        testFile.write(env.BUILD_ID)

        // Read the contents back into a string
        String contents = testFile.read()

        //verify the contents
        if (contents != env.BUILD_ID) {
            error("${contents} doest no equal ${env.BUILD_ID}.")
        }

        // Create a new Path in the same directory as testFile
        Path newFile = testFile.sibling('test.txt')

        // Rename testFile to newFile
        testFile.renameTo(newFile)

        // Verify testFile is gone
        if (testFile.exists()) {
            error("${testFile} should not exist.")
        }

        // Verify newFile does exist
        if (!newFile.exists()) {
            error("${newFile} should exist.")
        }

        // Set the permissions on a file
        int filePerms = 0755
        newFile.chmod(filePerms)

        // verify file permissions
        if (newFile.mode() != filePerms) {
            error("${newFile} should have ${filePerms} not ${newFile.mode()} permissions.")
        }

        // Get the size in bytes
        println(newFile.length())

    }



    // All zip/unzip methods have a tar/untar equivalent
    stage('Working with zip/tar') {

        // We will use this later
        Path archive

        // We can choose the name of a tmpDir by passing a path
        Path zipTest = workspace.child('zipTest')

        // Create a directory to hold our tests
        workspace.withTempDir(zipTest) { Path tmpDir ->

            // Create a Path to our new file
            Path testFile = tmpDir.child('test.txt')

            testFile.touch()

            // Create a Path to a zip file
            archive = workspace.child('testFile.zip')

            // zip up our tmpDir
            tmpDir.zip(archive)
        }

        // verify our tmpDir is gone
        if (zipTest.exists()) {
            error("The path ${zipTest} should have been deleted.")
        }

        // verify our archive exists
        if (!archive.exists()) {
            error("The path ${archive} should have been created.")
        }

        // Unzip the archive into the workspace
        archive.unzip(workspace)

        // verify zipTest was archived correctly

        if (!zipTest.exists()) {
            error("${zipTest} should exist again.")
        }

        // get the test.txt file
        Path txtFile = zipTest.list().find { Path childPath ->
            childPath.getName() == 'test.txt'
        }

        // verify file was found
        if (!txtFile || !txtFile.exists()) {
            error("${txtFile} should exist.")
        }
    }

}
