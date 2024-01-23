package app.avocado

import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseConfig {
    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            dotenv().get("SUPABASE_URL"),
            dotenv().get("SUPABASE_ANON_KEY")
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true //apply your custom config
            })
            install(Postgrest) {}
            install(Auth) {
                alwaysAutoRefresh = false
                autoLoadFromStorage = false
            }
            install(Storage) {}
        }
    }

    val supabaseAdmin: SupabaseClient by lazy {
        createSupabaseClient(
            dotenv().get("SUPABASE_URL"),
            dotenv().get("SUPABASE_SERVICE_ROLE_KEY")
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true //apply your custom config
            })
            install(Postgrest) {}
            install(Storage) {}
        }
    }
}