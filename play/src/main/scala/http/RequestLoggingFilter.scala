package http

import akka.stream.Materializer
import com.google.inject.*
import play.api.Logging
import play.api.mvc.*

import scala.concurrent.*

class RequestLoggingFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext)
    extends Filter,
      Logging:

  override def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader)
      : Future[Result] =
    val startTime = System.currentTimeMillis
    nextFilter(requestHeader).map { r =>
      val endTime     = System.currentTimeMillis
      val requestTime = endTime - startTime
      // info logging is disabled by default
      logger.warn(
        s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${r.header.status}"
      )
      r.withHeaders("Request-Time" -> requestTime.toString)
    }
