package snippets

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import scalaz.zio.interop.future.IOObjOps
import scalaz.zio.stream.{Sink, Stream}
import scalaz.zio.{App, IO}

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

case class Avenger(url: String,
                   name: String,
                   appearances: Int,
                   active: Boolean,
                   gender: String)

object StreamSinkEx extends App {

  implicit val sttpBackend = AsyncHttpClientFutureBackend()

  val ec = ExecutionContext.global

  def run(args: List[String]): IO[Nothing, StreamSinkEx.ExitStatus] = {

    def readFile(name: String) =
      IO.succeed(Source.fromFile(s"src/main/resources/$name", "UTF-8"))

    def closeFile(source: BufferedSource) = IO.suspend(IO.succeed(source.close))

    def consumeUrl(url: String) =
      IO.fromFuture(ec)(() => sttp.get(uri"$url").send())

    val sinkInitial = IO.succeed(List[String]())

    def customSink = Sink.foldM(sinkInitial) { (s, a: String) =>
      consumeUrl(a).map(res => Sink.Step.more(s :+ res.statusText))
    }

    readFile("avengers-dataset.csv")
      .bracket(closeFile) { src =>
        val stream = Stream.fromIterable(src.getLines().toList)

        for {
          result <- stream.run(customSink).run
          els = result.toEither.right.get
          _ <- IO.succeed(els.foreach(r => println(r)))
        } yield ExitStatus.ExitNow(0)

      }

  }
}
