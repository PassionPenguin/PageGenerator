package io.hoarfroster

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import java.io.File

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
        Thread.sleep(if (index % 10 == 0) 1500 else 1000)
        println("Processing ${it.path}")

        val sourceMarkdown = it.readText()
        val source: String = HtmlRenderer.builder().build().render(Parser.builder().build().parse(sourceMarkdown))
        val document = Jsoup.parse(source)
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
        Article(
            document.selectFirst("h1").text(), /* TITLE */
            description, /* DESCRIPTION */
            translator, /* TRANSLATOR */
            url, /* Repo URL */
            retrieveResult.tags, /* TAGS */
            it.lastModified(), /* TIME */
            it.path
        )
    }

    val configJson = File("${inputDir}/config/article.min.json")
    configJson.writeText(filesInformation.toString())
}