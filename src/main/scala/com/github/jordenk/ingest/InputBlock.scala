package com.github.jordenk.ingest

import cats.implicits._
import io.circe.{Decoder, HCursor}

/**
  * Representation of the raw json ingested.
  */
case class InputBlock(
  packageName: String,
  fileName: String,
  shortDescription: String,
  kind: InputBlockKind,
  caseClassLink: Option[String],
  classLink: Option[String],
  objectLink: Option[String],
  traitLink: Option[String],
  functionBlock: FunctionBlock
)

object InputBlock {
  implicit val decodeCaseClassTypeJson: Decoder[InputBlock] = (c: HCursor) =>
    for {
      packageName <- c.downField("package_name").as[String]
      fileName <- c.downField("file_name").as[String]
      shortDescription <- c.downField("short_description").as[String]
      kind <- c.downField("kind").as[InputBlockKind]
      caseClassLink <- c.downField("case_class_link").as[Option[String]]
      classLink <- c.downField("class_link").as[Option[String]]
      objectLink <- c.downField("object_link").as[Option[String]]
      traitLink <- c.downField("trait_link").as[Option[String]]
      functionBlock <- c.downField("function_block").as[FunctionBlock]
    } yield InputBlock(
      packageName = packageName,
      fileName = fileName,
      shortDescription = shortDescription,
      kind = kind,
      caseClassLink = caseClassLink,
      classLink = classLink,
      objectLink = objectLink,
      traitLink = traitLink,
      functionBlock = functionBlock
    )
}

sealed trait InputBlockKind

case object CaseClassKind extends InputBlockKind
case object ClassKind extends InputBlockKind
case object ObjectKind extends InputBlockKind
case object TraitKind extends InputBlockKind

object InputBlockKind {
  implicit val kindDecoder: Decoder[InputBlockKind] = Decoder.decodeString.emap {
    case "case class" => CaseClassKind.asRight
    case "class"      => ClassKind.asRight
    case "object"     => ObjectKind.asRight
    case "trait"      => TraitKind.asRight
    case e            => s"Unexpected value found with key kind. kind: $e".asLeft
  }
}
