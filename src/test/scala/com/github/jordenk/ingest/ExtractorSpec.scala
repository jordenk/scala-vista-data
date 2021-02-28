package com.github.jordenk.ingest
import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Sync}
import com.github.jordenk.ingest.Extractor._
import fs2.Stream
import fs2.io.file.readAll
import io.circe.DecodingFailure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{convertToAnyShouldWrapper, matchPattern}

import java.nio.file.Path

class ExtractorSpec extends AnyFlatSpec with IOApp {
  def run(args: List[String]): IO[ExitCode] = IO(ExitCode.Success)

  private def readFile[F[_]: Sync: ContextShift](path: Path): Stream[F, Byte] =
    Stream
      .resource(Blocker[F])
      .flatMap(blocker =>
        readAll[F](
          path,
          blocker,
          4096
        )
      )

  private val inputByteStream: Stream[IO, Byte] =
    readFile[IO](Path.of(getClass.getResource("/test_input.jsonl").getPath))

  "decodeJsonLine" should "decode properly formatted json." in {
    val validInputJsonString =
      """{"package_name":"cats.instances","file_name":"cats.instances.all","short_description":"","kind":"object","case_class_link":null,"class_link":null,"object_link":"cats/instances/package$$all$.html","trait_link":null,"function_block":{"label":"catsStdNonEmptyParallelForSeqZipSeq","tail":"(): Aux[Seq, ZipSeq]","member":"cats.instances.SeqInstances.catsStdNonEmptyParallelForSeqZipSeq","link":"cats/instances/package$$all$.html#catsStdNonEmptyParallelForSeqZipSeq:cats.NonEmptyParallel.Aux[Seq,cats.data.ZipSeq]","kind":"implicit def"}}"""
    val actualInputBlock = decodeJsonLine(validInputJsonString)
    val expectedInputBlock = InputBlock(
      packageName = "cats.instances",
      fileName = "cats.instances.all",
      shortDescription = "",
      kind = ObjectKind,
      caseClassLink = None,
      classLink = None,
      objectLink = Some("cats/instances/package$$all$.html"),
      traitLink = None,
      functionBlock = FunctionBlock(
        label = "catsStdNonEmptyParallelForSeqZipSeq",
        tail = "(): Aux[Seq, ZipSeq]",
        member = "cats.instances.SeqInstances.catsStdNonEmptyParallelForSeqZipSeq",
        link =
          "cats/instances/package$$all$.html#catsStdNonEmptyParallelForSeqZipSeq:cats.NonEmptyParallel.Aux[Seq,cats.data.ZipSeq]",
        kind = ImplicitDefFbk
      )
    )
    actualInputBlock shouldBe Right(expectedInputBlock)
  }

  it should "return an error when decoding fails." in {
    val invalidInputJsonString = """{"package_name":"cats.instances"}"""
    val actualInputBlock = decodeJsonLine(invalidInputJsonString)
    actualInputBlock should matchPattern {
      case Left(_: DecodingFailure) =>
    }
  }

  "bytesToInputBlocks" should "decode and keep InputJson with function block kinds in FunctionBlockKindsToKeep." in {
    val inputBlocks = inputByteStream
      .through(bytesToInputBlocks())
      .compile
      .toList
      .unsafeRunSync()

    val functionBlockKinds = inputBlocks.map(_.functionBlock.kind)

    functionBlockKinds.toSet shouldBe FunctionBlockKindsToKeep.toSet
  }

//  it should "println." in {
//    val kinds = inputByteStream
//      .map(decodeJsonLine)
//      .collect({
//        case Right(value) => value
//      })
//      .filter(_.functionBlock.kind == DefFbk)
//      .filter(_.functionBlock.label == "collectFirst")
//      .compile
//      .toList
//      .unsafeRunSync()
//
//    for {
//      i <- (1 to 20)
//      k = kinds(i)
//      _ = println()
//
//      _ = println(k.functionBlock.label)
//      _ = println(k.functionBlock.member)
//      _ = println(k.functionBlock.tail)
//
//    } yield ()
//
//    1 shouldBe 1
//  }

}
