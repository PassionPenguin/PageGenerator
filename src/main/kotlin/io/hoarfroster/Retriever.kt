package io.hoarfroster

import org.jsoup.Jsoup
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class RetrieveResult(val tags: MutableList<Tag>)

fun retrieveResult(repoUrl: String): RetrieveResult {
    val tags: MutableList<Tag> = mutableListOf()
    val connection = URL(repoUrl.replace("blob", "commits")).openConnection() as HttpsURLConnection
    val document = Jsoup.parse(connection.inputStream.bufferedReader().readText())
    document.select("[data-hovercard-type=\"pull_request\"][data-url].issue-link.js-issue-link")
        .filter { e ->
            Regex("#([0-9]+?)$").matches(e.html())
        }.map { e ->
            Regex("#([0-9]+?)$").find(e.html())?.groupValues?.get(1)
        }.forEach { it ->
            Thread.sleep(1000)
            val conn = URL("https://github.com/xitu/gold-miner/pull/$it").openConnection() as HttpsURLConnection
            val doc = Jsoup.parse(conn.inputStream.bufferedReader().readText())
            if (doc.select(".js-issue-labels > *").size > 0
                && doc.selectFirst(".js-issue-labels").text().contains("翻译完成")
            ) {
                doc.select(".js-issue-labels > *").forEach {
                    if(!it.text().contains("翻译完成"))
                        tags.add(Tag(it.text()))
                }
            }
        }
    return RetrieveResult(tags = tags)
}