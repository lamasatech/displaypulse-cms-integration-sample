package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests reading the broadcast payload, which shares its shape with `GET /audience/current`. */
class AudienceUpdateParserTest {

    private val parser = AudienceUpdateParser(Gson())

    @Test
    fun `a measured payload maps every field`() {
        val snapshot = parser.parse(
            """
            {"schema_version":2,"measurement_status":"measured","observed_at":"2026-11-03T14:13:08.085Z",
             "current":{"people_now":11,"looking_now":1,
               "demographics_now":{"population":"people_now","gender":{"male":5,"female":0,"unknown":6},
                 "age":{"minor":0,"adult":6,"mature_adult":0,"senior":0,"unknown":5}}},
             "session":{"viewers_total":15,"avg_dwell_ms":10200,"reach_total":213,"ots_total":236,
               "frequency":1.11,"verified_reach_total":6,"staff_excluded_total":0},
             "line_flow":{"measurement_status":"unavailable"},
             "active_campaign_id":"camp-1"}
            """.trimIndent()
        )!!

        assertEquals("measured", snapshot.measurementStatus)
        assertEquals("2026-11-03T14:13:08.085Z", snapshot.observedAt)
        assertEquals(11, snapshot.peopleNow)
        assertEquals(1, snapshot.lookingNow)
        assertEquals(5, snapshot.gender["male"])
        assertEquals(6, snapshot.age["adult"])
        assertEquals(213, snapshot.reachTotal)
        assertEquals(236, snapshot.otsTotal)
        assertEquals(10_200L, snapshot.avgDwellMs)
        assertEquals("camp-1", snapshot.activeCampaignId)
    }

    @Test
    fun `an unavailable payload keeps absent figures null instead of zero`() {
        val snapshot = parser.parse("""{"schema_version":2,"measurement_status":"unavailable","observed_at":null,"current":null}""")!!

        assertEquals("unavailable", snapshot.measurementStatus)
        assertNull(snapshot.peopleNow)
        assertNull(snapshot.reachTotal)
        assertNull(snapshot.activeCampaignId)
    }

    @Test
    fun `a missing, blank or malformed payload is refused`() {
        for (payload in listOf(null, "", "   ", "not json", "{\"current\":", "[1,2]", "42", "null")) {
            assertNull(payload, parser.parse(payload))
        }
    }

    @Test
    fun `a field of the wrong type is refused rather than read as zero`() {
        assertNull(parser.parse("""{"measurement_status":"measured","current":{"people_now":"many"}}"""))
    }
}
