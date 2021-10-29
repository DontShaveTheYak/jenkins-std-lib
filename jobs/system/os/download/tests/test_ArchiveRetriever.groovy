@Library('jenkins-std-lib')
import org.dsty.system.os.Path
import org.dsty.system.os.download.ArchiveRetriever

node() {

    final String url = 'https://releases.hashicorp.com/terraform/1.0.7/terraform_1.0.7_linux_amd64.zip'

    final Path ws = Path.workspace()

    ws.deleteContents()

    Path myDir = ws.child('myDir')

    ArchiveRetriever retriever = new ArchiveRetriever()

    retriever.retrieve(url, myDir)

    if (myDir.list().size() != 2) {

        error("The contents of the archive was not exactred to ${myDir}.")

    }

}
