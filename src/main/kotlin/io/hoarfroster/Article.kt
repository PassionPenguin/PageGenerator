package io.hoarfroster

class Article internal constructor(/*
    val title: String,
    val description: String,
    val translator: String,
    val repoUrl: String,
    val tags: MutableList<Tag>,
    val time: String,
    val url: String
     */
    var title: String,
    var description: String,
    var translator: String,
    var repoUrl: String,
    var tags: List<Tag>,
    var time: String,
    var url: String
) {
    override fun toString(): String {
        return "{\"title\":\"$title\",\"description\":\"$description\",\"translator\":\"$translator\",\"repoUrl\":\"$repoUrl\",\"tags\":$tags,\"time\":\"$time\",\"url\":\"$url\"}"
    }
}