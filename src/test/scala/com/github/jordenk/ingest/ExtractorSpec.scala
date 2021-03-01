package com.github.jordenk.ingest
import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Sync}
import com.github.jordenk.ingest.Extractor._
import fs2.Stream
import fs2.io.file.readAll
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
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
    val decodingResult = decodeJsonLine(invalidInputJsonString)
    decodingResult should matchPattern {
      case Left(_: ExtractorError) =>
    }
  }

  "bytesToInputBlocks" should "decode and keep InputJson with function block kinds in FunctionBlockKindsToKeep." in {
    implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

    val inputBlocks = inputByteStream
      .through(bytesToInputBlocks[IO]())
      .compile
      .toList
      .unsafeRunSync()

    val functionBlockKinds = inputBlocks.map(_.functionBlock.kind)

    functionBlockKinds.toSet shouldBe FunctionBlockKindsToKeep.toSet
  }
}
