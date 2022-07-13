package de.crightgames.blxckoxymoron.paintball.gun

data class Magazine (
    val size: Int,
    val reloadSpeed: Long, // millis
    var content: Int
)
