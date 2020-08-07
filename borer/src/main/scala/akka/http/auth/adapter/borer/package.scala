package akka.http.auth.adapter

import io.bullet.borer.{Codec, Json}
import io.bullet.borer.derivation.MapBasedCodecs
import tech.bilal.akka.http.auth.adapter.TokenDecoder

package object borer {
  def decoderFor[T]: TokenDecoder[T] = {
    implicit val codec: Codec[T] = MapBasedCodecs.deriveCodec[T]
    (string: String) => Json.decode(string.getBytes).to[T].valueEither.toOption
  }
}
