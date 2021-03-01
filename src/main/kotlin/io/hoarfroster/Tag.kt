package io.hoarfroster

class Tag internal constructor(var name: String) {
    override fun toString(): String {
        return "{name: \"$name\"}"
    }
}