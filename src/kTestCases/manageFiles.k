import java.io.File

var homeDir : String = System.getProperty("user.home")
var dlDir : File = File(homeDir + File.separator + "Downloads")
var dlFiles : Seq[File] = dlDir.listFiles()
var dlSortedFiles : Seq[SortedFile] = Seq{SortedFile(f) | f : File :- dlFiles.contains(f)}
var musicDir : File = File(homeDir + File.separator + "Music")
var picturesDir : File = File(homeDir + File.separator + "Pictures")

fun isMusic(f : File) : Bool {
    f.extension = "mp3" || f.extension = "wav"
}

fun isPicture(f : File) : Bool {
    f.extension = "jpg" || f.extension = "png"
}

fun move(f : File, targetDir: File) : Bool {
    f.parentDir() = targetDir || File.move(f, targetDir)
}

class SortedFile extends File {
    var f : File
    req isMusic(f) => move(f,musicDir)
    req isPicture(f) => move(f,pictureDir)
}