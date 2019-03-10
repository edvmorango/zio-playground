package snippets

import java.util.concurrent.TimeUnit

import scalaz.zio.clock._
import scalaz.zio.duration.Duration
import scalaz.zio.{App, IO, ZIO, _}

object PromiseEx extends App {

  def run(args: List[String]): ZIO[Clock, Nothing, Int] = {

    val delayedMessage = (pr: Promise[Nothing, String]) =>
      sleep(Duration.apply(3, TimeUnit.SECONDS)) *> IO.succeedLazy("Message") flatMap pr.succeed

    // Enables neat concurrency
    for {
      pr <- Promise.make[Nothing, String]
      _ <- delayedMessage(pr).fork
      _ <- IO.succeed(println("Awaiting"))
      _ <- pr.await.flatMap(m => IO.succeed(println(m)))
    } yield 1

  }

}
