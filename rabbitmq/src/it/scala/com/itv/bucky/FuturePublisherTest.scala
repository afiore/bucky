package com.itv.bucky

import com.itv.bucky.lifecycle.{AmqpClientLifecycle, DeclarationLifecycle}
import com.itv.lifecycle.Lifecycle
import org.scalatest.Assertion

import scala.concurrent.Future
import BuckyUtils._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._
import SameThreadExecutionContext.implicitly


trait FuturePublisherTest extends PublisherBaseTest[Future] with ScalaFutures {

  override def withPublisher(testQueueName: QueueName, requeueStrategy: RequeueStrategy[Future], shouldDeclare: Boolean)
                            (f: (TestFixture[Future]) => Unit): Unit = {
    val key = RoutingKey(testQueueName.value)
    val exchangeName = ExchangeName("")
    val clientLifecycle = for {
      client <- AmqpClientLifecycle(IntegrationUtils.config)
      _ <- DeclarationLifecycle(IntegrationUtils.defaultDeclaration(testQueueName), client)
    }  yield client

    Lifecycle.using(clientLifecycle) { amqpClient =>
      Lifecycle.using(amqpClient.publisher()) { publisher =>
        f(TestFixture[Future](publisher, key, exchangeName, testQueueName, amqpClient, None))

      }
    }
  }

  override def verifySuccess(f: Future[Unit]): Assertion = f.asTry.futureValue shouldBe 'success

}
