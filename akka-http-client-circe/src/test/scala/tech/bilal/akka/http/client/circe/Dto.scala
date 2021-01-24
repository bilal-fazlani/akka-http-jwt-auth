package tech.bilal.akka.http.client.circe

import io.circe.Decoder

case class Dto(userId: Int, completed: Boolean, id: Int, title: String)
object Dto {
  given Decoder[Dto] = Decoder.forProduct4("userId", "completed", "id", "title")(Dto.apply)
}
