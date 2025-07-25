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

import scala.concurrent.Future

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.testuser.common.utils.AsyncHmrcSpec
import uk.gov.hmrc.testuser.helpers.GeneratorProvider
import uk.gov.hmrc.testuser.helpers.stubs.MtdSaApiStubStub
import uk.gov.hmrc.testuser.models.ServiceKey._
import uk.gov.hmrc.testuser.repository.TestUserRepository

class MtdSaApiStubConnectorSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with BeforeAndAfterAll {

  trait Setup extends GeneratorProvider {
    val repository = mock[TestUserRepository]
    when(repository.identifierIsUnique(*)(*)).thenReturn(Future(true))

    val testIndividual   = await(generator.generateTestIndividual(Seq(MTD_INCOME_TAX, SELF_ASSESSMENT, NATIONAL_INSURANCE), None, None))
    val testOrganisation = await(generator.generateTestOrganisation(Seq(MTD_INCOME_TAX, SELF_ASSESSMENT, NATIONAL_INSURANCE, CORPORATION_TAX), None, None, None, None, None))

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val underTest = new MtdSaApiStubConnector(
      fakeApplication().injector.instanceOf[HttpClientV2],
      fakeApplication().injector.instanceOf[Configuration],
      fakeApplication().injector.instanceOf[Environment],
      fakeApplication().injector.instanceOf[ServicesConfig]
    ) {
      override lazy val serviceUrl: String = MtdSaApiStubStub.url
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    MtdSaApiStubStub.server.start()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    MtdSaApiStubStub.server.resetMappings()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    MtdSaApiStubStub.server.stop()
  }

  "createIndividual" should {
    "create a test individual" in new Setup {
      MtdSaApiStubStub.willSuccessfullyCreateTestIndividual()

      val result = await(underTest.createIndividual(testIndividual))
      result shouldBe testIndividual
    }

    "fail when the MtdSaApiStub returns an error" in new Setup {
      MtdSaApiStubStub.willFailWhenCreatingTestIndividual()

      intercept[UpstreamErrorResponse] {
        await(underTest.createIndividual(testIndividual))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "createOrganisation" should {
    "create a test organisation" in new Setup {
      MtdSaApiStubStub.willSuccessfullyCreateTestOrganisation()

      val result = await(underTest.createOrganisation(testOrganisation))
      result shouldBe testOrganisation
    }

    "fail when the MtdSaApiStub returns an error" in new Setup {
      MtdSaApiStubStub.willFailWhenCreatingTestOrganisation()

      intercept[UpstreamErrorResponse] {
        await(underTest.createOrganisation(testOrganisation))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
