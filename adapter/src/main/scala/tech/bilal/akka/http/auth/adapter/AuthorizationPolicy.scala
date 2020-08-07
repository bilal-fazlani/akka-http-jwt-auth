package tech.bilal.akka.http.auth.adapter

import tech.bilal.akka.http.auth.adapter.AuthorizationPolicy.PolicyExpression
import tech.bilal.akka.http.auth.adapter.AuthorizationPolicy.PolicyExpression.{
  And,
  ExpressionOperator,
  Or
}

import scala.concurrent.Future

/**
  * An authorization policy is a way to filter incoming HTTP requests based on rules
  */
trait AuthorizationPolicy[T] {

  /**
    * Applies a new authorization policy in combination with previous policy.
    * Passing of both policies is requried for authorization to succeed.
    *
    * @param authorizationPolicy new Authorization policy
    * @return combined authorization policy
    */
  def &(authorizationPolicy: AuthorizationPolicy[T]): AuthorizationPolicy[T] = {
    PolicyExpression(this, And, authorizationPolicy)
  }

  /**
    * Applies a new authorization policy if the previous policy fails.
    * Authorization will succeed if any of the provided policy passes.
    *
    * @param authorizationPolicy new Authorization policy
    * @return combined authorization policy
    */
  def |(authorizationPolicy: AuthorizationPolicy[T]): AuthorizationPolicy[T] = {
    PolicyExpression(this, Or, authorizationPolicy)
  }
}

/**
  * An authorization policy is a way to provide filter incoming HTTP requests based on standard rules.
  */
object AuthorizationPolicy {

  final case class PolicyExpression[T](
      left: AuthorizationPolicy[T],
      operator: ExpressionOperator,
      right: AuthorizationPolicy[T]
  ) extends AuthorizationPolicy[T]

  object PolicyExpression {
    trait ExpressionOperator
    case object Or extends ExpressionOperator {
      override def toString: String = "|"
    }
    case object And extends ExpressionOperator {
      override def toString: String = "&"
    }
  }

  /**
    * Allows custom request filtering based on access token properties.
    *
    * @param predicate Filter
    */
  final case class CustomPolicy[A](predicate: A => Boolean)
      extends AuthorizationPolicy[A]

  /**
    * Allows custom request filtering based on access token properties.
    *
    * @param predicate Async filter
    */
  final case class CustomPolicyAsync[A](predicate: A => Future[Boolean])
      extends AuthorizationPolicy[A]
}
