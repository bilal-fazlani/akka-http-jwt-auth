package tech.bilal.akka.http.oidc.client

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.{
  DefaultScalaModule,
  ScalaObjectMapper
}

import scala.reflect.{ClassTag, classTag}
import scala.util.Try

object JsonUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def fromJson[T: ClassTag](json: String): Try[T] =
    Try {
      mapper.readValue[T](json, classTag[T].runtimeClass.asInstanceOf[Class[T]])
    }

  def toJson(value: Any): String = mapper.writeValueAsString(value)
}
