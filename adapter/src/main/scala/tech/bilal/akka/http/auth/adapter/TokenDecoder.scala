package tech.bilal.akka.http.auth.adapter

trait TokenDecoder[A] {
  def decode(string: String): Option[A]
}
