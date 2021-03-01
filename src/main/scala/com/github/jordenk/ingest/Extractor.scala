package com.github.jordenk.ingest

import cats.effect.Sync
import fs2.{Pipe, text}
import io.chrisdavenport.log4cats.Logger
import io.circe.parser.decode
import cats.implicits._

object Extractor {

  def decodeJsonLine(jsonString: String): Either[ExtractorError, InputBlock] =
    decode[InputBlock](jsonString) match {
      case Left(circeError)  => ExtractorError(s"""
           |Could not decode input json string. Decoding error: ${circeError.getMessage}
           |Raw input string: $jsonString
           |""".stripMargin).asLeft
      case Right(inputBlock) => inputBlock.asRight

    }

  def bytesToInputBlocks[F[_]: Sync: Logger](): Pipe[F, Byte, InputBlock] =
    bytesStream => {
      bytesStream
        .through(text.utf8Decode)
        .through(text.lines)
        .map(decodeJsonLine)
        .evalTap({
          case Right(_)    => Sync[F].unit
          case Left(error) => Logger[F].warn(error.message)
        })
        .collect({ case Right(inputBlock) => inputBlock })
        .filter(block => FunctionBlockKindsToKeep.contains(block.functionBlock.kind))
    }

  // Does not compile as a Set. Use List for now.
  val FunctionBlockKindsToKeep = List(
    AbstractDefFbk,
    AbstractValFbk,
    DefFbk,
    FinalDefFbk,
    ImplicitAbstractDefFbk,
    ImplicitDefFbk,
    ImplicitFinalDefFbk,
    ImplicitFinalValFbk,
    ImplicitValFbk,
    LazyValFbk
  )

}

case class ExtractorError(message: String)
