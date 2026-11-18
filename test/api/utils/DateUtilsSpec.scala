/*
 * Copyright 2026 HM Revenue & Customs
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

package api.utils

import java.time.{Clock, Instant, LocalDateTime, ZoneOffset}

class DateUtilsSpec extends UnitSpec {

  "longDateTimestampGmt" should {
    "return Date/time in format [EEE, dd MMM yyyy HH:mm:ss z]" when {
      "given a LocalDateTime" in {
        val result = DateUtils.longDateTimestampGmt(
          LocalDateTime.of(2023, 1, 17, 12, 0)
        )
        result shouldBe "Tue, 17 Jan 2023 12:00:00 GMT"
      }
    }
  }

  "nowAsUtc" should {
    "return the current timestamp in UTC format" in {
      implicit val clock: Clock =
        Clock.fixed(
          Instant.parse("2026-11-18T10:30:45.123Z"),
          ZoneOffset.UTC
        )

      DateUtils.nowAsUtc shouldBe "2026-11-18T10:30:45.123Z"
    }
  }

}
