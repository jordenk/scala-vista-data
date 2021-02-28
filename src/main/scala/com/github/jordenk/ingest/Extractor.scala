package com.github.jordenk.ingest

import fs2.{Pipe, text}
import io.circe._
import io.circe.parser.decode

object Extractor {

  def decodeJsonLine(jsonString: String): Either[Error, InputBlock] = decode[InputBlock](jsonString)

  def bytesToInputBlocks[F[_]](): Pipe[F, Byte, InputBlock] =
    bytesStream => {
      bytesStream
        .through(text.utf8Decode)
        .through(text.lines)
        .map(decodeJsonLine)
        // TODO log errors
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
