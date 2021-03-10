import androidx.compose.desktop.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.drew.imaging.ImageMetadataReader
import java.io.File
import java.io.FileFilter
import javax.swing.JFileChooser
import javax.swing.JPanel
import kotlin.math.roundToInt

data class Picture(
    var name: String = "",
    var height: Int = 0,
    var width: Int = 0,
    var xResolution: Int = 0,
    var yResolution: Int = 0,
    var compression: String = "-",
    var depth: String = "-",
    var depthDescr: String = "-"
)

fun main() {
    val pictures = mutableStateListOf<Picture>()
    Window(title = "ImageInfo", size = IntSize(1200, 600)) {
        var dirChosen by remember { mutableStateOf("") }
        Column {
            Row {
                Button(onClick = {
                    val fileDir = chooseDirectory() ?: return@Button
                    if (fileDir.isDirectory) {
                        dirChosen = fileDir.absolutePath
                        File(fileDir, "ImageInfo.txt").bufferedWriter().use { out ->
                            fileDir.listFiles(object : FileFilter {
                                override fun accept(pathname: File?): Boolean {
                                    if (pathname != null) {
                                        return formats.contains(pathname.extension.toLowerCase())
                                    }
                                    return false
                                }
                            })?.forEach { f ->
                                val builder = StringBuilder()

                                val picture = Picture()

                                val metadata = ImageMetadataReader.readMetadata(f)
                                for (directory in metadata.directories) {
                                    for (tag in directory.tags) {
                                        builder.append(
                                            String.format(
                                                "[%s] - %s = %s\n",
                                                directory.name, tag.tagName, tag.description
                                            )
                                        )
                                        if (tag.tagName == "Image Width" ||
                                            tag.tagName == "X Max"
                                        ) {
                                            picture.width = tag.description.split(" ")[0].toInt()
                                        }
                                        if (tag.tagName == "Image Height" ||
                                            tag.tagName == "Y Max"
                                        ) {
                                            picture.height = tag.description.split(" ")[0].toInt()
                                        }
                                        if (tag.tagName == "File Name") {
                                            picture.name = tag.description
                                        }
                                        if (tag.tagName == "X Resolution" ||
                                            tag.tagName == "Horizontal DPI"
                                        ) {
                                            picture.xResolution = tag.description.split(" ")[0].toInt()
                                        }
                                        if (tag.tagName == "Y Resolution" ||
                                            tag.tagName == "Vertical DPI"
                                        ) {
                                            picture.yResolution = tag.description.split(" ")[0].toInt()
                                        }
                                        if (tag.tagName == "Compression Type" ||
                                            tag.tagName == "Compression"
                                        ) {
                                            picture.compression = tag.description
                                        }
                                        if (tag.tagName == "Pixels Per Unit X" ||
                                            tag.tagName == "X Pixels per Meter"
                                        ) {
                                            picture.xResolution =
                                                (tag.description.toDouble() / 100f * 2.54f).roundToInt()
                                        }
                                        if (tag.tagName == "Pixels Per Unit Y" ||
                                            tag.tagName == "Y Pixels per Meter"
                                        ) {
                                            picture.yResolution =
                                                (tag.description.toDouble() / 100f * 2.54f).roundToInt()
                                        }
                                        if (tag.tagName == "Bits per Pixel" || tag.tagName == "Bits Per Pixel" ||
                                            tag.tagName == "Data Precision" ||
                                            tag.tagName == "Bits Per Sample" ||
                                            tag.tagName == "Color Planes"

                                        ) {
                                            picture.depth = tag.description
                                            picture.depthDescr = tag.tagName
                                        }
                                    }
                                    if (directory.hasErrors()) {
                                        for (error in directory.errors) {
                                            builder.append(String.format("ERROR: %s'n", error))
                                        }
                                    }
                                }
                                builder.append("*********************")
                                out.write(builder.toString())
                                pictures.add(picture)
                            }
                        }
                    }
                }) {
                    Text("Choose directory")
                }
                Text(text = dirChosen)
            }

            Box(
                modifier = Modifier
                    .background(color = Color(254, 254, 254))
                    .padding(start = 10.dp, top = 10.dp)
            ) {
                Row {
                    TextBox(text = "Name", 200.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Width, pixels")
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Height, pixels")
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "X resolution, dpi", 125.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Y resolution, dpi", 125.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Compression", 125.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Depth", 180.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    TextBox(text = "Depth description", 130.dp)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            LazyScrollable(pictures)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyScrollable(items: SnapshotStateList<Picture>) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color(254, 254, 254))
            .padding(10.dp)
    ) {
        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state) {
            items(items = items,
                itemContent = { item ->
                    Row {
                        TextBox(text = item.name, 200.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.width.toString())
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.height.toString())
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.xResolution.toString(), 125.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.yResolution.toString(), 125.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.compression, 125.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.depth, 180.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        TextBox(text = item.depthDescr, 130.dp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                })
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state,
                itemCount = items.size,
                averageItemSize = 50.dp
            )
        )
    }
}

@Composable
fun TextBox(text: String = "Item", width: Dp = 100.dp) {
    Box(
        modifier = Modifier.width(width)
            .background(color = Color(0, 0, 0, 10))
            .padding(start = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text)
    }
}

fun chooseDirectory(): File? {
    val chooser = JFileChooser()
    chooser.currentDirectory = File(".")
    chooser.dialogTitle = "Choose directory"
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

    chooser.isAcceptAllFileFilterUsed = false

    return if (chooser.showOpenDialog(JPanel()) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else {
        null
    }
}

val formats = arrayListOf("jpg", "jpeg", "gif", "tif", "tiff", "bmp", "png", "pcx")