package io.hoarfroster

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

fun String.getLastSegment(): String {
    // return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
    return this.replaceFirst(".*/([^/?]+).*".toRegex(), "$1")
}

fun main(args: Array<String>) {

    var inputDir: String? = null

    for (name in args)
        if (name.contains(Regex("--input=(.+?)")))
            inputDir = name.substring(8)
    if (inputDir == null)
        return

    val dir = File("${inputDir}/documents/")
    val files = dir.listFiles { _, name -> name.endsWith(".md") }
    val filesInformation = files?.mapIndexed { index, it ->
        /* Pause thread to prevent HTTP 419 */
        Thread.sleep(if (index % 10 == 0) 1500 else 1000)
        println("Processing ${it.path}")

        var sourceMarkdown = it.readText()
        val document =
            Jsoup.parse(HtmlRenderer.builder().build().render(Parser.builder().build().parse(sourceMarkdown)))

        val originalUrl = Regex("原文地址：\\[.+?]\\((.+?)\\)").find(sourceMarkdown)?.groupValues?.get(1) ?: ""
        val url = Regex("本文永久链接：\\[.+?]\\((.+?)\\)").find(sourceMarkdown)?.groupValues?.get(1) ?: ""
        val translator = Regex("译者：\\[(.+?)]").find(sourceMarkdown)?.groupValues?.get(1) ?: ""
        val retrieveResult = retrieveResult(url)
        var description = ""
        for (e in document.select("p")) {
            if (e.text().isNotBlank()) {
                description = e.text()
                break
            }
        }

        println(" - Processing image")
        document.select("img").forEach { img ->
            /* Download external resources */
            val alt = img.attr("alt")
            val urlString = img.attr("src")

            with(
                File(
                    "${inputDir}/images/${
                        it.path.replace(
                            "${inputDir}/documents/",
                            ""
                        )
                    }-${urlString.getLastSegment()}"
                )
            ) {
                /* Only download the image if the file is not existed */
                if ((!this.isFile || !this.exists()) && !urlString.startsWith("../images/")) {
                    println("   - Processing image $urlString")
                    if (!this.parentFile.isDirectory || this.parentFile.exists())
                        this.parentFile.mkdirs()
                    this.createNewFile()
                    val imageUrlConn = URL(urlString).openConnection()
                    imageUrlConn.setRequestProperty("referer", originalUrl)
                    imageUrlConn.setRequestProperty(
                        "user-agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36"
                    )
                    imageUrlConn.setRequestProperty("origin", "https://www.medium.com/")

                    val `in`: InputStream = BufferedInputStream(imageUrlConn.getInputStream())

                    val out = ByteArrayOutputStream()
                    val buf = ByteArray(1024)
                    var n: Int
                    while (-1 != `in`.read(buf).also { n = it }) {
                        out.write(buf, 0, n)
                    }
                    out.close()
                    `in`.close()
                    val response = out.toByteArray()
                    this.outputStream().write(response)
                    sourceMarkdown = sourceMarkdown.replace(
                        """![$alt]($urlString)""",
                        """![$alt](../images/${
                            it.path.replace(
                                "${inputDir}/documents/",
                                ""
                            )
                        }-${urlString.getLastSegment()})"""
                    )
                }
            }
        }

        Article(
            document.selectFirst("h1").text(), /* TITLE */
            description, /* DESCRIPTION */
            translator, /* TRANSLATOR */
            url, /* Repo URL */
            retrieveResult.tags, /* TAGS */
            Date(it.lastModified()).toString(), /* TIME */
            it.path.replace("${inputDir}/documents/", "")
        )
        it.writeText(sourceMarkdown)
    }

    val configJson = File("${inputDir}/config/article.min.json")
    configJson.writeText(filesInformation.toString())
}