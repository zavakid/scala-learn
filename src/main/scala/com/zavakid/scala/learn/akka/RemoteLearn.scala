package com.zavakid.scala.learn.akka

import akka.actor.{ActorSelection, Props, ActorSystem, Actor}
import com.typesafe.config.ConfigFactory

/**
 * author: luwu luwu@mogujie.com
 * 2014 2014-02-14 上午11:40
 */
object Commands {

  object AreYouAlive

  object ImHere

  object FindTarget

}

class SourceActor extends Actor {

  private var target: ActorSelection = _

  import Commands._

  override def receive: Actor.Receive = {
    case FindTarget =>
      target = context.system.actorSelection("akka://targetSystem@127.0.0.1/user/targetSystem")
      context.become(askTarget)
  }

  def askTarget: Actor.Receive = {
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
}

object RemoteLearn extends App {
  if (args(0) == "source")
    startSource
  else startTarget

  def startSource = {
    val system = ActorSystem("sourceSystem", ConfigFactory.load.atPath("source"))
    val source = system.actorOf(Props[SourceActor], "sourceActor")
    source ! Commands.FindTarget
  }

  def startTarget = {
    val system = ActorSystem("targetSystem", ConfigFactory.load.atPath("target"))
    system.actorOf(Props[TargetActor], "targetActor")
  }
}
