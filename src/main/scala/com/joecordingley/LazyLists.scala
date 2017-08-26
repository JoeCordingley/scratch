//package com.joecordingley
//
///**
//  * Created by joe on 23/06/17.
//  */
//object LazyLists {
//
//  sealed trait Stream[+A] {
//    import Stream._
//    def headOption:Option[A] = this match {
//      case Empty => None
//      case Cons(h,_) => Some(h())
//    }
//    def take(n:Int):Stream[A] = this match {
//      case _ if n <= 0 => Empty
//      case Empty => Empty
//      case Cons(h,t) => cons(h(),t().take(n-1))
//    }
//    def takeWhile(p: A => Boolean):Stream[A] = this match {
//      case Empty => empty
//      case Cons(h,t) if p(h()) => cons(h(),t().takeWhile(p))
//      case _ => empty
//    }
//    def foldRight[B](z: => B)(f: (A, => B) =>B): B = this match {
//      case Cons(h,t) => f(h,t().foldRight(z)(f))
//      case _ => z
//    }
//    def exists(p:A => Boolean): Boolean = foldRight(false)((a,b)=> p(a)|| b)
//    def forAll(p:A => Boolean): Boolean = foldRight(true)((a,b) => p(a) && b)
//    def takeWhile2(p: A => Boolean):Stream[A] = foldRight(empty[A])((a,b)=>if(p(a)) cons(a,b) else empty)
//    def headOption2:Option[A] = foldRight(None[A])((a,_)=>Some(a))
//    def map[B](f:A => B):Stream[B] = foldRight(empty[B])((a,b) => cons(f(a),b))
//    def append[B>:A](bs: =>Stream[B]):Stream[B] = foldRight(bs)((a, b) => cons(a,b))
//    def flatMap[B](f:A=>Stream[B]):Stream[B] = foldRight(empty[B])((a,b) => f(a).append(b))
//    def map[B](f:A=>B):Stream[B] = unfold(this){
//      case Empty => None
//      case Cons(h, tl) => Some(f(h),tl())
//    }
//    def take2(i:Int):Stream[A] = unfold((this,i)){
//      case (_,0)|(Empty,_) => None
//      case (Cons(h,t),i) => Some((h(),(t(),i-1)))
//    }
//    def takeWhile3(p:A=>Boolean):Stream[A] = unfold(this){
//      case Empty => None
//      case Cons(h,_) if !p(h()) => None
//      case Cons(h,t) => Some((h(),t()))
//    }
//    def zipWith[B](s:Stream[B]):Stream[(A,B)] = unfold((this,s)){
//      case (Empty,_)|(_,Empty) => None
//      case (Cons(h1,t1),Cons(h2,t2)) => Some(((h1(),h2()),(t1(),t2())))
//    }
//    def zipAll[B](s:Stream[B]):Stream[(Option[A],Option[B])] = ???
//    def startsWith[B>:A](s:Stream[B]):Boolean =
//      zipAll(s)
//        .takeWhile {
//          case (_,None) => false
//          case _ => true
//        }
//        .forAll {
//          case (Some(a1),Some(a2)) => a1 == a2
//          case _ => false
//        }
//    def tails:Stream[Stream[A]] = unfold(this){
//      case Empty => None
//      case s@ Cons(_,t) => Some((s,t()))
//    } append Stream(empty)
//
//  }
//  case object Empty extends Stream[Nothing]
//  case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]
//
//  object Stream {
//    def cons[A](hd: => A, tl: => Stream[A]):Stream[A] = {
//      lazy val head = hd
//      lazy val tail = tl
//      Cons(() => head, () => tail)
//    }
//    def empty[A]: Stream[A] = Empty
//    def apply[A](as: A*):Stream[A] = if (as.isEmpty) empty else cons(as.head,apply(as.tail:_*))
//    def toList[A](s:Stream[A]):List[A] = s match {
//      case Empty => Nil
//      case Cons(h,t) => h() :: toList(t())
//    }
//    def constant[A](a:A):Stream[A] = cons(a,constant(a))
//    def from(a:Int):Stream[Int] = cons(a,from(a+1))
//    def unfold[A,S](z: S)(f: S => Option[(A,S)]):Stream[A] = f(z) match {
//      case None => empty
//      case Some((a,s)) => cons(a,unfold(s)(f))
//    }
//    def fibs: Stream[Int] = unfold((0,1)){
//      case (f1,f2) => {
//        val f3 = f1+f2
//        Some((f1,(f2,f3)))
//      }
//    }
//    def from2(a:Int):Stream[Int] = unfold(a)(s=>Some((s,s+1)))
//    def constant[A](a:A):Stream[A] = unfold(a)(s => Some((s,s)))
//
//  }
//
//}
