package com.laioffer

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Playlist (
    val id: Long,
    val songs: List<Song>
)

@Serializable
data class Song (
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)


// Project entrance, starts server at 8080
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() { // extension
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }

    // RESTful api: get, post, put, delete
    routing {   // endpoints
        get("/") {
            call.respondText("Hello World!")
        }

        get("/feed") {
            val jsonString: String? = this::class.java.classLoader.getResource("feed.json")?.readText()
//            call.respondNullable(jsonString)
            call.respondText(jsonString ?: "null")


        }

        get("/playlists") {
            val jsonString: String? = this::class.java.classLoader.getResource("playlists.json")?.readText()
            call.respondText(jsonString ?: "null")
        }

        // json string -> java/kotlin class object: decode / deserialize
        // java/kotlin class object -> json string: serialize
        get("/playlists/{id}") {
            // .let is when it is not null, like if(jsonString != null)
            this::class.java.classLoader.getResource("playlists.json")?.readText()?.let{
                // jsonString -> List<Playlist> -> loop and filter
                val playlists: List<Playlist> = Json.decodeFromString(ListSerializer(Playlist.serializer()), it)

                // call does roundtrip, starts from browser, takes the URL info and brings to service to match an endpoint route
                // after service execution store in call and brings it back to browser to display

                // Find and return first item match otherwise return null
                val id = call.parameters["id"]
                val playlist: Playlist? = playlists.firstOrNull{item -> item.id.toString() == id}
                call.respondNullable(playlist)

            }
                // Fallback
                ?: call.respondText("null")

        }

        static("/"){
            staticBasePackage = "static"
            static("songs"){
                resources("songs")
            }
        }
    }
}