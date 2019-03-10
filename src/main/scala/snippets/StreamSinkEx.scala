package snippets

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import scalaz.zio.stream.{Sink, Stream}
import scalaz.zio.{DefaultRuntime, IO, ZIO}

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

case class Avenger(url: String,
                   name: String,
                   appearances: Int,
                   active: Boolean,
                   gender: String)

object StreamSinkEx {

  implicit val sttpBackend = AsyncHttpClientFutureBackend()

  val ec = ExecutionContext.global

  def app: ZIO[Any, Throwable, StatusCode] = {

    def readFile(name: String) =
      IO.succeed(Source.fromFile(s"src/main/resources/$name", "UTF-8"))

    def closeFile(source: BufferedSource) = IO.suspend(IO.succeed(source.close))

    def consumeUrl(url: String) =
      IO.fromFuture(_ => sttp.get(uri"$url").send())

    val sinkInitial = IO.succeed(List[String]())

    def customSink = Sink.foldM(sinkInitial) { (s, a: String) =>
      consumeUrl(a).map(res => Sink.Step.more(s :+ res.statusText))
    }

    readFile("avengers-dataset.csv")
      .bracket(closeFile) { src =>
        val stream = Stream.fromIterable(src.getLines().toList)

        for {
          result <- stream.run(customSink).run
          _ <- IO.effect {
            result.toEither match {
              case Left(e)    => println(e.getMessage)
              case Right(els) => els.foreach(r => println(r))
            }
          }
        } yield 1

      }

  }
}

object StreamSinkExApp extends App {

  val runtime = new DefaultRuntime {}

  runtime.unsafeRun(StreamSinkEx.app)

}
