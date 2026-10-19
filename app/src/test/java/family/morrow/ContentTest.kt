package family.morrow
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.LocalDate
class ContentTest {
    private fun source() = File("../build/content-assets/content.yaml").readText()
    @Test fun loadsCompletePack() {
        val pack = Content.parse(source())
        assertEquals(28, pack.weeks.flatMap { it.days }.size)
        assertEquals(LocalDate.parse("2026-10-19"), pack.today(LocalDate.parse("2026-10-18")).date)
        assertEquals(LocalDate.parse("2026-10-28"), pack.today(LocalDate.parse("2026-10-28")).date)
        assertEquals(LocalDate.parse("2026-11-15"), pack.today(LocalDate.parse("2027-01-01")).date)
        assertTrue(pack.translations["ICB"].isNullOrEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsUnknownSchema() { Content.parse(source().replace("schema: 1", "schema: 2")) }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidDates() { Content.parse(source().replace("'2026-10-20'", "'2026-10-19'")) }
}
