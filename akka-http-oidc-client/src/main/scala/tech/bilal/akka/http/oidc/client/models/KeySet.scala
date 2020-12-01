package tech.bilal.akka.http.oidc.client.models

//import io.bullet.borer.Decoder

case class KeySet(keys: List[Key])

object KeySet {
  val empty: KeySet = KeySet(Nil)
//  given keySetDec as Decoder[KeySet] =
//    Decoder.from(KeySet.apply _)
}
