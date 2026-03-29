package io.github.janmalch.volcanicglass.core.content

import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.Serializer
import io.github.janmalch.volcanicglass.core.UriKSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream


@Serializable
internal data class VaultData(
    @Serializable(with = UriKSerializer::class)
    val directory: Uri?,
) {

    companion object {
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal object VaultDataStoreSerializer : Serializer<VaultData> {
    override val defaultValue: VaultData = VaultData(null)

    override suspend fun readFrom(input: InputStream): VaultData =
        input.use { Json.decodeFromStream(it) }

    override suspend fun writeTo(
        t: VaultData,
        output: OutputStream
    ) {
        output.use { Json.encodeToStream(t, it) }
    }

}
