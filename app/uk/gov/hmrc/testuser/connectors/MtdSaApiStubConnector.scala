/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.testuser.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.testuser.models.JsonFormatters._
import uk.gov.hmrc.testuser.models._
import uk.gov.hmrc.testuser.services.ApplicationLogger

@Singleton
class MtdSaApiStubConnector @Inject() (
    httpClient: HttpClientV2,
    runModeConfiguration: Configuration,
    environment: Environment,
    config: ServicesConfig
  )(implicit ec: ExecutionContext
  ) extends ApplicationLogger {

  import config.baseUrl

  lazy val serviceUrl: String = baseUrl("mtd-sa-api-stub")

  def createIndividual(individual: TestIndividual)(implicit hc: HeaderCarrier): Future[TestIndividual] = {
    logger.info(s"Calling mtd-sa-api-stub ($serviceUrl) to create individual $individual")
    httpClient.post(url"$serviceUrl/test-users/individuals")
      .withBody(Json.toJson(MtdSaApiStubTestIndividual.from(individual)))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Right(_)  => individual
        case Left(err) => throw err
      }
  }

  def createOrganisation(organisation: TestOrganisation)(implicit hc: HeaderCarrier): Future[TestOrganisation] = {
    logger.info(s"Calling mtd-sa-api-stub ($serviceUrl) to create organisation $organisation")
    httpClient.post(url"$serviceUrl/test-users/organisations")
      .withBody(Json.toJson(MtdSaApiStubTestOrganisation.from(organisation)))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Right(_)  => organisation
        case Left(err) => throw err
      }
  }
}
