package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.models.SearchParams
import app.avocado.models.SearchResults
import app.avocado.utils.baseUrl
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.campaignRouting() {
    route("${baseUrl}/search") {
        get {
            // Extracting 'limit' and 'offset' from the query string
            // Providing default values if they are not specified in the request
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 10 // Default limit
            val offset = call.parameters["offset"]?.toIntOrNull() ?: 0 // Default offset
            // Extracting and validating 'query'
            val query = call.parameters["query"]
            if (query.isNullOrEmpty() || query.length <= 1) {
                call.respondText(
                    "Search query must be longer than 1 character",
                    status = HttpStatusCode.BadRequest
                )
                return@get  // Important to return here to stop further execution
            }

            val results = supabase.postgrest.rpc("search_songs_artists", SearchParams(query, 2, 10, 0))
                .decodeList<SearchResults>()
            println(results)
            // call.respond(...)
        }
    }
}