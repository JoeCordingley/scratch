package com.joecordingley

import cats.data.State
import cats.data.State._

/**
  * Created by joe on 18/07/17.
  */
object PropertyTesting extends App{

  trait RNG {
    def nextInt:(RNG,Int)
    def nextUnsignedInt:(RNG,Int)
  }
  case class rand(seed:Int) extends RNG {
    override def nextInt: (RNG, Int) = {
      val r = new scala.util.Random(seed)
      val i = r.nextInt()
      (rand(i),i)
    }
    override def nextUnsignedInt: (RNG,Int) = ???
  }

  case class Gen[A](sample:State[RNG,A]){
    def flatMap[B](f:A => Gen[B]):Gen[B] = Gen(sample.flatMap(f(_).sample))
    def map[B](f:A => B): Gen[B] = flatMap(a =>unit(f(a)))
    def listOfN(size:Gen[Int]):Gen[List[A]] = size.flatMap(s => sequence(List.fill(s)(this)))
    def map2[B,C](gen2: Gen[B])(f:(A,B)=>C):Gen[C] = Gen(sample.map2(gen2.sample)(f))
    def map22[B,C](gen2: Gen[B])(f:(A,B)=>C):Gen[C] = for{
      a <- this
      b <- gen2
    } yield f(a,b)
  }
  def map2[A,B,C](o1:Option[A],o2:Option[B])(f:(A,B)=>C):Option[C] = for {
    a <- o1
    b <- o2
  } yield f(a,b)

  def sequence[A](l:List[Option[A]]):Option[List[A]]= l.foldRight[Option[List[A]]](Some(Nil)){case(a,b)=>map2(a,b)(_::_)}
  def sequence2[A](l:List[Option[A]]):Option[List[A]]= l.foldRight[Option[List[A]]](Some(Nil))(map2(_,_)(_::_))
  def union[A](g1:Gen[A],g2: Gen[A]):Gen[A] = boolean.flatMap(if(_) g1 else g2)
  def boolean(weightForTrue:Double):Gen[Boolean] = {
    Gen(
      State[RNG,Boolean]{rng =>
        val (rng2,i) = rng.nextUnsignedInt
        val bool = i.toDouble/Int.MaxValue > weightForTrue
        (rng2,bool)
      }
    )

  }
  def weighted[A](g1:(Gen[A],Double),g2:(Gen[A],Double)):Gen[A] = (g1,g2) match {
    case((gen1,w1),(gen2,w2)) => boolean(w1/(w1+w2)).flatMap(if (_) gen1 else gen2)
  }

  def sequence[A](l:List[Gen[A]]):Gen[List[A]] = l.foldRight(unit[List[A]](Nil))(_.map2(_)(_::_))
  def choose(start:Int,stopExclusive:Int): Gen[Int] = Gen(State[RNG,Int](_.nextInt).map(start+_%(stopExclusive-start)))
  def unit[A](a: => A): Gen[A] = Gen(pure(a))
  def boolean: Gen[Boolean] = Gen(
    for {
      rng <- get[RNG]
      (rng2,i) = rng.nextInt
      b = i%2 == 0
      _ <- set(rng2)
    } yield b
  )
  def boolean2: Gen[Boolean] = Gen(choose(0,2).sample.map{case 0 => true; case 1 =>false})
  def boolean3: Gen[Boolean] = Gen(
    State[RNG,Boolean]{rng =>
      val (rng2,i) = rng.nextInt
      (rng2,i%2==0)
    }
  )

  def sequence[S,A](l:List[State[S,A]]):State[S,List[A]] = l.foldRight(pure[S,List[A]](Nil)){
    case (s,sl)=> for {
      l <- sl
      a <- s
    } yield a::l
  }
  def sequence2[S,A](l:List[State[S,A]]):State[S,List[A]] = l.foldRight(pure[S,List[A]](Nil))(_.map2(_)(_::_))
  def listOfN[A](n:Int,g:Gen[A]):Gen[List[A]] = Gen(sequence2(List.fill(n)(g.sample)))

  val ch = choose(0,10)
  val s1 = for {
    i1 <- ch.sample
    i2 <- ch.sample
    i3 <- ch.sample
  } yield (i1,i2,i3)
  println(s1.run(rand(0)).value)
  println(listOfN(3,choose(0,10)).sample.run(rand(0)).value)


}
