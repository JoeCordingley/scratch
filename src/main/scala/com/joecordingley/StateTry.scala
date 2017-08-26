package com.joecordingley

import cats.data.State

/**
  * Created by joe on 20/05/17.
  */
object StateTry extends App{
  type Stack = List[Int]
  val pop = State[Stack,Int] {
    case x :: xs => (xs, x)
    case Nil => sys.error("stack is empty")
  }
  def push(a: Int) = State[Stack,Unit] {
    case xs => (a :: xs,())
  }
  def stackManip:State[Stack,Int] = for {
    _ <- push(3)
    a <- pop
    b <- pop
  } yield b
  def runner2:State[Stack,Int] = State[Stack,Int] {
    case x :: xs if x == 2 => (xs,2)
    case x :: xs => runner2.run(xs).value
    case Nil => sys.error("hey")
  }



  println(runner2.run(List(5,8,2,1)).value)

}
