package snippets

import java.util.concurrent.TimeUnit

import scalaz.zio.duration.Duration
import scalaz.zio.{App, IO}
import scalaz.zio.stream.{Sink, Stream}

object StreamEx extends App {

  def run(args: List[String]): IO[Nothing, ExitStatus] = {

    val stream = Stream.fromIterable(List(2, 2, 1, 1, 0, 1, 0, 0, 2))

    for {

      res <- stream
        .mapM[Nothing, Int](
          a =>
            IO.succeedLazy(a)
              .delay(Duration.apply(a, TimeUnit.SECONDS)))
        .run(Sink.collect[Int])
      _ <- IO.succeedLazy {

        res.foreach(println(_))

      }

    } yield ExitStatus.ExitNow(0)

  }
}
