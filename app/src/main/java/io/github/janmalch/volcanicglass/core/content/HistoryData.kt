package io.github.janmalch.volcanicglass.core.content

import androidx.core.net.toUri
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream


@Serializable
internal data class HistoryData(
    val recentUris: List<String>,
) {

    // FIXME: why is serialization acting up with List<Uri> and contextual
    val recents get() = recentUris.map { it.toUri() }

    companion object {
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal object HistoryDataStoreSerializer : Serializer<HistoryData> {
    override val defaultValue: HistoryData = HistoryData(emptyList())

    override suspend fun readFrom(input: InputStream): HistoryData =
        input.use { Json.decodeFromStream(it) }

    override suspend fun writeTo(
        t: HistoryData,
        output: OutputStream
    ) {
        output.use { Json.encodeToStream(t, it) }
    }

}
