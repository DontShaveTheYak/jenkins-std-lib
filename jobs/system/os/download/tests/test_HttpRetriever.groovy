@Library('jenkins-std-lib')
import org.dsty.system.os.Path
import org.dsty.system.os.download.HttpRetriever

node() {

    final String url = 'https://raw.githubusercontent.com/nektos/act/master/install.sh'

    final Path ws = Path.workspace()

    ws.deleteContents()

    Path myFile = ws.child('myFile')

    HttpRetriever retriever = new HttpRetriever()

    retriever.retrieve(url, myFile)

    if (!myFile.exists()) {

        error("The file ${myFile} was not downloaded!")

    }

    if (myFile.length() == 0) {

        error("The file ${myFile} was created but is empty!")
    }

}
