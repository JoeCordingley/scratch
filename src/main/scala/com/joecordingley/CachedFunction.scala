package com.joecordingley

import cats.data.State
import cats.data.State._

/**
  * Created by joe on 26/08/17.
  */
object CachedFunction extends App{

  //let's say we have a function that takes a long time ;)
  def longFunction(i:Int) = {
    println(s"calculating $i times two")
    i*2
  }

  //we can cache results in a map when calling a function
  def cachedFunction[A,B](f:A=>B,cachedResults:Map[A,B],a:A):(Map[A,B],B) =
    if (cachedResults contains a)
      (cachedResults,cachedResults(a))
    else {
      val result = f(a)
      val newCache = cachedResults + (a -> result)
      (newCache,result)
    }

  //lets capture it for our specific function
  case class CachedLongFunction(cache:Map[Int,Int]){
    def apply(i:Int):(CachedLongFunction,Int)= {
      val (newCache, result) = cachedFunction(longFunction,cache,i)
      (CachedLongFunction(newCache),result)
    }
  }

  //we can transform it into a State monad
  //the first type parameter is the state part and the second is the return
  def evaluateLongFunctionWithCache(i:Int):State[CachedLongFunction,Int] = State(_.apply(i))

  //first we chain any computations as a for comprehension`
  //we can compose state computations as other state computations
  //state monads flatmap implicitly passes the state part of it to the next bit
  val longFunctionComputations:State[CachedLongFunction,List[Int]] = for {
    a <- evaluateLongFunctionWithCache(3)
    b <- evaluateLongFunctionWithCache(2)
    c <- evaluateLongFunctionWithCache(3)
  } yield List(a,b,c)

  //obviously we need an initial state
  val initialState = CachedLongFunction(Map.empty)


  //so we can run our computations starting from our initial state and get our updated state and return.
  //Somewhat annoyingly, it returns an Eval so we have to call value to get our values out. I think it might be to do
  //... with stack safety
  val (cachedLongFunction,result) =longFunctionComputations.run(initialState).value
  //calculating 3 times two
  //calculating 2 times two
  println(result)
  //List(6, 4, 6)

  //simple if we just have a set number of computations of the long function, if its more variable, it's a bit more confusing

  //we are going to need some more tools

  //this is a standard monad function that just joins two monads together with a function
  def map2[S,A,B,C](s1:State[S,A],s2:State[S,B])(f:(A,B)=>C):State[S,C] = for {
    a <- s1
    b <- s2
  } yield f(a,b)

  //this one looks quite confusing but again it's a standard pattern for monads
  //it just turns a list of monad to a monad of list
  //pure is the unit constuctor for the state monad
  //I'm not sure why these functions weren't included as part of the cats library
  def sequence[S,A](l: List[State[S,A]]): State[S,List[A]] = l.foldRight(pure[S,List[A]](Nil))(map2(_,_)(_::_))

  //so now we can make state computations that evaluate lists

  def listLongFunctionComputations(l:List[Int]):State[CachedLongFunction,List[Int]] = {
    val individualStateComputations = l map evaluateLongFunctionWithCache
    sequence(individualStateComputations)
  }

  val functionValues = List(7,2,3)
  //we can use the cache from before so we don't have to calculate it again
  val (cache2,result2) = listLongFunctionComputations(functionValues).run(cachedLongFunction).value
  //calculating 7 times two
  println(result2)
  //List(14, 4, 6)

  //there are other tools we can use with State
  //get, set and modify are particularly useful for checking or changing the state in the middle of a for comprehension
  //and we can use recursion if we don't know how many function calls we will do

  //this is a bit of a nonsense one to show recursion and checks
  //it's just a function that computes successive longfunctions until the map is a certain size

  def doSomeComputationsUntilMapIsAsBigAs(size:Int,results:List[Int],nextComputation:Int):State[CachedLongFunction,List[Int]] = for {
    currentState <- get[CachedLongFunction]
    returnVal <- if (currentState.cache.size == size)
      pure[CachedLongFunction,List[Int]](results.reverse)
    else
      for {
        result <- evaluateLongFunctionWithCache(nextComputation)
        moreResults = result :: results
        innerReturnVal <- doSomeComputationsUntilMapIsAsBigAs(size,moreResults,nextComputation+1)
      } yield innerReturnVal
  } yield returnVal

  val (cache3,result3) = doSomeComputationsUntilMapIsAsBigAs(5,Nil,1).run(cache2).value
  //calculating 1 times two
  //calculating 4 times two
  println(result3)
  //List(2, 4, 6, 8)



}
