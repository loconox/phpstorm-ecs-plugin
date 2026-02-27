package dev.loconox.phpstorm.ecs

import org.junit.Assert.*
import org.junit.Test

class EcsConfigurationTest {

    @Test
    fun `default values are correct`() {
        val config = EcsConfiguration()
        assertEquals("", config.toolPath)
        assertEquals(30000, config.timeout)
        assertEquals("", config.getEcsConfigPath())
        assertEquals(100, config.maxMessagesPerFile)
    }

    @Test
    fun `setToolPath stores value`() {
        val config = EcsConfiguration()
        config.toolPath = "/usr/local/bin/ecs"
        assertEquals("/usr/local/bin/ecs", config.toolPath)
    }

    @Test
    fun `setToolPath with null stores empty string`() {
        val config = EcsConfiguration()
        config.toolPath = "/some/path"
        config.setToolPath(null)
        assertEquals("", config.toolPath)
    }

    @Test
    fun `setTimeout stores value`() {
        val config = EcsConfiguration()
        config.timeout = 60000
        assertEquals(60000, config.timeout)
    }

    @Test
    fun `setEcsConfigPath stores value`() {
        val config = EcsConfiguration()
        config.setEcsConfigPath("/project/ecs.php")
        assertEquals("/project/ecs.php", config.getEcsConfigPath())
    }

    @Test
    fun `clone creates independent copy`() {
        val original = EcsConfiguration()
        original.toolPath = "/path/to/ecs"
        original.timeout = 5000
        original.setEcsConfigPath("/my/ecs.php")

        val cloned = original.clone()

        assertEquals(original.toolPath, cloned.toolPath)
        assertEquals(original.timeout, cloned.timeout)
        assertEquals(original.getEcsConfigPath(), cloned.getEcsConfigPath())

        // Modify clone, original should be unaffected
        cloned.toolPath = "/other/path"
        cloned.timeout = 9999
        assertEquals("/path/to/ecs", original.toolPath)
        assertEquals(5000, original.timeout)
    }

    @Test
    fun `compareTo compares by toolPath`() {
        val a = EcsConfiguration()
        a.toolPath = "aaa"

        val b = EcsConfiguration()
        b.toolPath = "bbb"

        assertTrue(a.compareTo(b) < 0)
        assertTrue(b.compareTo(a) > 0)
        assertEquals(0, a.compareTo(a))
    }

    @Test
    fun `default constants are correct`() {
        assertEquals("vendor/bin/ecs", EcsConfiguration.DEFAULT_TOOL_PATH)
        assertEquals("ecs.php", EcsConfiguration.DEFAULT_CONFIG_PATH)
    }
}
