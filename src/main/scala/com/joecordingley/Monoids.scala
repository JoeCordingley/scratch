package com.joecordingley

/**
  * Created by joe on 07/07/17.
  */
object Monoids {

  trait Monoid[A] {
    def op(a1: A, a2:A):A
    def zero: A
  }

  val intAddition = new Monoid[Int] {
    override def op(a1: Int, a2: Int): Int = a1+a2
    override def zero: Int = 0
  }

  val intMultiplication = new Monoid[Int] {
    override def op(a1: Int, a2: Int): Int = a1*a2

    override def zero: Int = 1
  }

  val booleanOr = new Monoid[Boolean] {
    override def op(a1: Boolean, a2: Boolean): Boolean = a1 || a2

    override def zero: Boolean = false
  }

  val booleanAnd = new Monoid[Boolean] {
    override def op(a1: Boolean, a2: Boolean): Boolean = a1 && a2

    override def zero: Boolean = true
  }

  def optionMonoid[A] = new Monoid[Option[A]]{
    override def op(a1: Option[A], a2: Option[A]): Option[A] = if (a1.isDefined) a1 else a2

    override def zero: Option[A] = None
  }

  def endoMonoid[A] = new Monoid[A=>A] {
    override def op(a1: A => A, a2: A => A): A => A = a1 compose a2

    override def zero: A => A = identity
  }

  def foldMap[A,B](as:List[A], m:Monoid[B])(f:A=>B):B = 
    as.map(f)
      .foldLeft(m.zero)(m.op)

  def foldRight[A,B](as:List[A])(z:B)(f:(A,B)=>B):B = {
    foldMap(as, endoMonoid[B])(f.curried)(z)
  }


}
