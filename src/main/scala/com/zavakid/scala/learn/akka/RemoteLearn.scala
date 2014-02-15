package com.zavakid.scala.learn.akka

import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
 * author: luwu luwu@mogujie.com
 * 2014 2014-02-14 上午11:40
 */
object Commands {

  case object AreYouAlive

  case object ImHere

  case object StartWork

  case object FindTarget

}

class SourceActor extends Actor {

  private val identifyId = 1

  import Commands._
  override def receive: Actor.Receive = {
    case StartWork =>
      val target = context.system.actorSelection("akka.tcp://targetSystem@127.0.0.1:2553/user/targetActor")
      target ! Identify(identifyId)
    case ActorIdentity(`identifyId`, Some(ref)) =>
      println("find target!")
      context.watch(ref)
      context.become(active(ref))
      import context.dispatcher
      context.system.scheduler.schedule(1 second, 1 second, self, FindTarget)

    case ActorIdentity(`identifyId`, None) =>
      println("can't find target, try again")
      import context.dispatcher
      context.system.scheduler.scheduleOnce(1 seconds, self, StartWork)
  }

  def active(target: ActorRef): Actor.Receive = {
    case FindTarget => target ! AreYouAlive
    case ImHere => println("target is alive")
    case Terminated(`target`) =>
      println("target is terminated, find it again!")
      context.unbecome
      import context.dispatcher
      context.system.scheduler.scheduleOnce(1 seconds, self, StartWork)
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
  if (args(0) == "source") {
    println("start source ...")
    startSource
  }
  else {
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
    val system = ActorSystem("targetSystem", targetConfig)
    system.actorOf(Props[TargetActor], "targetActor")
  }
}
