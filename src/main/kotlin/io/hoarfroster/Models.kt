package io.hoarfroster

class Article(
    private val title: String,
    private val description: String,
    private val translator: String,
    private val repoUrl: String,
    private val tags: MutableList<Tag>,
    private val time: String,
    private val url: String
) {
    override fun toString(): String {
        return "{\"title\":\"$title\",\"description\":\"$description\",\"translator\":\"$translator\",\"repoUrl\":\"$repoUrl\",\"tags\":$tags,\"time\":\"$time\",\"url\":\"$url\"}"
    }
}

class Tag(
    private val name: String
) {
    override fun toString(): String {
        return "{\"name\":\"$name\"}"
    }
}