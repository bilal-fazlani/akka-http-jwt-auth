package tech.bilal.akka.http.client.circe

import io.circe.Decoder
import io.circe.Codec.AsObject

case class Dto(userId: Int, completed: Boolean, id: Int, title: String) derives AsObject