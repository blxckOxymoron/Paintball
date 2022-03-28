package de.crightgames.blxckoxymoron.paintball.util

import org.bukkit.util.Vector
import kotlin.math.hypot
import kotlin.math.round

object VectorUtils {

    private val cache = mutableMapOf<Int, List<Vector>>()

    fun vectorsInRadius(radius: Int): List<Vector> {

        cache[radius]?.let {
            return it
        }

        val vectors = mutableListOf<Vector>()

        var x = 0
        var y = 0
        var z = 0

        while (x < radius) {
            while (round(hypot(x.toDouble(), y.toDouble())) < radius) {
                while (round(hypot(hypot(x.toDouble(), y.toDouble()), z.toDouble())) < radius) {
                    vectors.add(Vector(x,y,z))
                    z++
                }
                z = 0
                y++
            }
            y = 0
            x++
        }

        // we have all for one corner. we need to
        // mirror it on x, y and z
        vectors.addAll(vectors.filter { it.blockX != 0 }.map { v -> v.clone().multiply(Vector(-1,  1,  1)) })
        vectors.addAll(vectors.filter { it.blockY != 0 }.map { v -> v.clone().multiply(Vector( 1, -1,  1)) })
        vectors.addAll(vectors.filter { it.blockZ != 0 }.map { v -> v.clone().multiply(Vector( 1,  1, -1)) })

        // check for duplicates, because I don't think the set handles it all by itself
        /*
        vectors.removeAll { vector ->
            val result = vectors.find {
                it.blockX == vector.blockX && it.blockY == vector.blockY && it.blockZ == vector.blockZ
            }

            Bukkit.broadcastMessage(vector.toString() + ", " + result.toString())

            return@removeAll result != vector
        }
        */

        vectors.sortByDescending { it.blockY }

        cache[radius] = vectors
        return vectors.toList()
    }
}