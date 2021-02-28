package com.github.jordenk.ingest

import io.circe.Decoder
import cats.implicits._

/**
  * Representation of a function block nested in the raw input json.
  */
case class FunctionBlock(
  label: String,
  tail: String,
  member: String,
  link: String,
  kind: FunctionBlockKind
)

object FunctionBlock {
  implicit val functionJsonDecoder: Decoder[FunctionBlock] =
    Decoder.forProduct5("label", "tail", "member", "link", "kind")(FunctionBlock.apply)
}

sealed trait FunctionBlockKind

case object AbstractDefFbk extends FunctionBlockKind
case object AbstractValFbk extends FunctionBlockKind
case object ClassFbk extends FunctionBlockKind
case object DefFbk extends FunctionBlockKind
case object FinalClassFbk extends FunctionBlockKind
case object FinalCaseClassFbk extends FunctionBlockKind
case object FinalDefFbk extends FunctionBlockKind
case object ImplicitAbstractDefFbk extends FunctionBlockKind
case object ImplicitDefFbk extends FunctionBlockKind
case object ImplicitFinalDefFbk extends FunctionBlockKind
case object ImplicitFinalValFbk extends FunctionBlockKind
case object ImplicitValFbk extends FunctionBlockKind
case object LazyValFbk extends FunctionBlockKind
case object MacroDefFbk extends FunctionBlockKind
case object ObjectFbk extends FunctionBlockKind
case object SealedAbstractClassFbk extends FunctionBlockKind
case object TraitFbk extends FunctionBlockKind
case object TypeFbk extends FunctionBlockKind
case object ValFbk extends FunctionBlockKind

object FunctionBlockKind {
  implicit val functionBlockKindDecoder: Decoder[FunctionBlockKind] = Decoder.decodeString.emap {
    case "abstract def"          => AbstractDefFbk.asRight
    case "abstract val"          => AbstractValFbk.asRight
    case "class"                 => ClassFbk.asRight
    case "def"                   => DefFbk.asRight
    case "final class"           => FinalClassFbk.asRight
    case "final case class"      => FinalCaseClassFbk.asRight
    case "final def"             => FinalDefFbk.asRight
    case "implicit abstract def" => ImplicitAbstractDefFbk.asRight
    case "implicit def"          => ImplicitDefFbk.asRight
    case "implicit final def"    => ImplicitFinalDefFbk.asRight
    case "implicit final val"    => ImplicitFinalValFbk.asRight
    case "implicit val"          => ImplicitValFbk.asRight
    case "lazy val"              => LazyValFbk.asRight
    case "macro def"             => MacroDefFbk.asRight
    case "object"                => ObjectFbk.asRight
    case "sealed abstract class" => SealedAbstractClassFbk.asRight
    case "trait"                 => TraitFbk.asRight
    case "type"                  => TypeFbk.asRight
    case "val"                   => ValFbk.asRight
  }
}
