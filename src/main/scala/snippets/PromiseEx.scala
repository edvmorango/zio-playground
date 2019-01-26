package snippets

import java.util.concurrent.TimeUnit

import scalaz.zio.duration.Duration
import scalaz.zio.{App, IO, Promise}

object PromiseEx extends App {

  def run(args: List[String]): IO[Nothing, PromiseEx.ExitStatus] = {

    val delayedMessage = (pr: Promise[Nothing, String]) =>
      IO.sleep(Duration.apply(3, TimeUnit.SECONDS)) *> IO
        .succeedLazy("Message")
        .flatMap(pr.succeed)

    // Enables neat concurrency
    for {
      pr <- Promise.make[Nothing, String]
      _ <- delayedMessage(pr).fork
      _ <- IO.succeed(println("Awaiting"))
      _ <- pr.await.flatMap(m => IO.succeed(println(m)))
    } yield ExitStatus.ExitNow(1)

  }

}
