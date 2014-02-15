package com.zavakid.scala.learn.akka

import akka.actor.{ActorSelection, Props, ActorSystem, Actor}
import com.typesafe.config.ConfigFactory

/**
 * author: luwu luwu@mogujie.com
 * 2014 2014-02-14 上午11:40
 */
object Commands {

  case object AreYouAlive
  case object ImHere
  case object FindTarget
  case object StartWork
}

class SourceActor extends Actor {

  private var target: ActorSelection = _

  import Commands._

  override def receive: Actor.Receive = {
    case StartWork =>
      target = context.system.actorSelection("akka.tcp://targetSystem@127.0.0.1:2553/user/targetActor")
      context.become(askTarget)

      import scala.concurrent.duration._
      import context.dispatcher
      val myself = context.self
      context.system.scheduler.schedule(1 second, 1 second, myself, FindTarget)
  }

  def askTarget(): Actor.Receive = {
    case FindTarget => target ! AreYouAlive
    case ImHere => println("target is alive")
    case other => println(s"other msg: $other")
  }

}

class TargetActor extends Actor {

  import Commands._

  override def receive: Actor.Receive = {
    case AreYouAlive =>
      println("receive source ask me")
      sender ! ImHere
    case other => println(s"other msg: $other")
  }

  override def preStart(): Unit = {
    println(s"target alived, the path ${self.path}")
  }
}

object RemoteLearn extends App {
  if (args(0) == "source"){
    println("start source ...")
    startSource
  }
  else{
    println("start target ...")
    startTarget
  }


  def startSource() {
    val root = ConfigFactory.load
    val system = ActorSystem("sourceSystem", root.getConfig("source").withFallback(root))
    val source = system.actorOf(Props[SourceActor], "sourceActor")
    source ! Commands.StartWork
  }

  def startTarget() {
    val root = ConfigFactory.load
    val targetConfig = root.getConfig("target").withFallback(root)
    val system = ActorSystem("targetSystem",targetConfig )
    system.actorOf(Props[TargetActor], "targetActor")
  }
}
