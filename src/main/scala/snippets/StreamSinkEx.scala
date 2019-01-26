package snippets

import scalaz.zio.stream.Stream
import scalaz.zio.{App, IO}

import scala.io.{BufferedSource, Source}

case class Avenger(url: String,
                   name: String,
                   appearances: Int,
                   active: Boolean,
                   gender: String)

object StreamEx extends App {

  def run(args: List[String]): IO[Nothing, StreamEx.ExitStatus] = {

    def readFile(name: String) = IO.succeed(Source.fromResource(name))

    def closeFile(source: BufferedSource) = IO.suspend(IO.succeed(source.close))

    readFile("avengers-dataset.csv")
      .bracket(closeFile) { src =>
        val stream = Stream.fromIterable(src.getLines().toIterable)
        for {
          _ <- stream.foreach(v => IO.succeed(println(v)))
        } yield ExitStatus.ExitNow(0)

      }

  }
}
