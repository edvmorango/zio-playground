package snippets

import java.util.concurrent.TimeUnit

import scalaz.zio.duration.Duration
import scalaz.zio.stream.{Sink, Stream}
import scalaz.zio.{App, IO, ZIO}

object StreamEx extends App {

  def run(args: List[String]): ZIO[StreamEx.Environment, Nothing, Int] = {

    val stream = Stream.fromIterable(List(2, 2, 1, 1, 0, 1, 0, 0, 2))

    for {

      res <- stream
        .mapM[StreamEx.Environment, Nothing, Int](
          a =>
            IO.succeedLazy(a)
              .delay(Duration.apply(a, TimeUnit.SECONDS)))
        .run(Sink.collect[Int])
      _ <- IO.effectTotal(res.foreach(println(_)))

    } yield 1

  }

}
