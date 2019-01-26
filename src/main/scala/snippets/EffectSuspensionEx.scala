package snippets

import scalaz.zio.{App, IO}

object EffectSuspensionEx extends App {

  def run(args: List[String]): IO[Nothing, ExitStatus] = {

    val lazyEvaluation = IO.succeedLazy {
      println("I'm a effect occurring in every call")
      10
    }

    val strictEvalution = IO.succeed {
      println("I'm a effect occurring once")
      10
    }

    for {
      _ <- strictEvalution
      _ <- strictEvalution
      _ <- strictEvalution
      _ <- lazyEvaluation
      _ <- lazyEvaluation
      _ <- lazyEvaluation
    } yield ExitStatus.ExitNow(1)

  }

}
