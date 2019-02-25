package services

import config.RequestParams

import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

trait QueryHandler {

  def query(dasId: String, params: RequestParams)(implicit ec: ExecutionContext): Future[List[DateTime]]

}
