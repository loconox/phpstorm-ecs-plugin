package dev.loconox.phpstorm.ecs

import org.junit.Assert.*
import org.junit.Test

class EcsOutputParserTest {

    // ── shortCheckerName ────────────────────────────────────────────

    @Test
    fun `shortCheckerName returns last segment after backslash`() {
        assertEquals(
            "ArraySyntaxFixer",
            EcsOutputParser.shortCheckerName("PhpCsFixer\\Fixer\\ArrayNotation\\ArraySyntaxFixer")
        )
    }

    @Test
    fun `shortCheckerName returns full string when no backslash`() {
        assertEquals("SimpleChecker", EcsOutputParser.shortCheckerName("SimpleChecker"))
    }

    @Test
    fun `shortCheckerName handles empty string`() {
        assertEquals("", EcsOutputParser.shortCheckerName(""))
    }

    @Test
    fun `shortCheckerName handles trailing backslash`() {
        assertEquals("", EcsOutputParser.shortCheckerName("Foo\\Bar\\"))
    }

    // ── extractLineNumbersFromDiff ──────────────────────────────────

    @Test
    fun `extractLineNumbersFromDiff extracts single hunk`() {
        val diff = """
            --- a/file.php
            +++ b/file.php
            @@ -10,7 +10,7 @@
             some context
        """.trimIndent()
        assertEquals(listOf(10), EcsOutputParser.extractLineNumbersFromDiff(diff))
    }

    @Test
    fun `extractLineNumbersFromDiff extracts multiple hunks`() {
        val diff = """
            @@ -5,3 +5,3 @@
             line
            @@ -20,6 +20,6 @@
             line
            @@ -100,2 +100,2 @@
             line
        """.trimIndent()
        assertEquals(listOf(5, 20, 100), EcsOutputParser.extractLineNumbersFromDiff(diff))
    }

    @Test
    fun `extractLineNumbersFromDiff returns empty for no hunks`() {
        assertEquals(emptyList<Int>(), EcsOutputParser.extractLineNumbersFromDiff("no diff here"))
    }

    @Test
    fun `extractLineNumbersFromDiff returns empty for empty string`() {
        assertEquals(emptyList<Int>(), EcsOutputParser.extractLineNumbersFromDiff(""))
    }

    // ── findJsonStart ───────────────────────────────────────────────

    @Test
    fun `findJsonStart returns 0 for clean JSON`() {
        assertEquals(0, EcsOutputParser.findJsonStart("""{"files":{}}"""))
    }

    @Test
    fun `findJsonStart skips PHP notices before JSON`() {
        val output = """PHP Notice: something wrong
{"files":{}}"""
        assertEquals(output.indexOf('{'), EcsOutputParser.findJsonStart(output))
    }

    @Test
    fun `findJsonStart returns -1 when no JSON`() {
        assertEquals(-1, EcsOutputParser.findJsonStart("just text, no braces"))
    }

    // ── parseErrors ─────────────────────────────────────────────────

    @Test
    fun `parseErrors parses error with source_class`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "errors": [
                {
                    "line": 42,
                    "message": "Array syntax is wrong",
                    "source_class": "PhpCsFixer\\Fixer\\ArrayNotation\\ArraySyntaxFixer"
                }
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseErrors(json)
        assertEquals(1, messages.size)
        assertEquals(42, messages[0].line)
        assertEquals("ArraySyntaxFixer: Array syntax is wrong", messages[0].message)
    }

    @Test
    fun `parseErrors parses error without source_class`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "errors": [
                {"line": 7, "message": "Something is off"}
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseErrors(json)
        assertEquals(1, messages.size)
        assertEquals(7, messages[0].line)
        assertEquals("Something is off", messages[0].message)
    }

    @Test
    fun `parseErrors defaults line to 1 when missing`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "errors": [{"message": "no line"}]
        }""").asJsonObject

        val messages = EcsOutputParser.parseErrors(json)
        assertEquals(1, messages[0].line)
    }

    @Test
    fun `parseErrors defaults message when missing`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "errors": [{"line": 1}]
        }""").asJsonObject

        val messages = EcsOutputParser.parseErrors(json)
        assertEquals("ECS error", messages[0].message)
    }

    @Test
    fun `parseErrors returns empty when no errors key`() {
        val json = com.google.gson.JsonParser.parseString("""{"diffs": []}""").asJsonObject
        assertTrue(EcsOutputParser.parseErrors(json).isEmpty())
    }

    @Test
    fun `parseErrors handles multiple errors`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "errors": [
                {"line": 1, "message": "a"},
                {"line": 2, "message": "b"},
                {"line": 3, "message": "c"}
            ]
        }""").asJsonObject

        assertEquals(3, EcsOutputParser.parseErrors(json).size)
    }

    // ── parseDiffs ──────────────────────────────────────────────────

    @Test
    fun `parseDiffs extracts checker names and line numbers`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "diffs": [
                {
                    "diff": "@@ -15,3 +15,3 @@\n-old\n+new",
                    "applied_checkers": [
                        "PhpCsFixer\\Fixer\\Import\\NoUnusedImportsFixer",
                        "PhpCsFixer\\Fixer\\Whitespace\\IndentationTypeFixer"
                    ]
                }
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseDiffs(json)
        assertEquals(1, messages.size)
        assertEquals(15, messages[0].line)
        assertEquals("NoUnusedImportsFixer, IndentationTypeFixer", messages[0].message)
    }

    @Test
    fun `parseDiffs defaults to ECS when no applied_checkers`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "diffs": [
                {"diff": "@@ -1,3 +1,3 @@\n-old\n+new"}
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseDiffs(json)
        assertEquals("ECS", messages[0].message)
    }

    @Test
    fun `parseDiffs creates message at line 1 when no hunk headers`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "diffs": [
                {
                    "diff": "some diff without hunk headers",
                    "applied_checkers": ["Checker"]
                }
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseDiffs(json)
        assertEquals(1, messages.size)
        assertEquals(1, messages[0].line)
    }

    @Test
    fun `parseDiffs creates message per hunk when multiple hunks`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "diffs": [
                {
                    "diff": "@@ -5,3 +5,3 @@\n-a\n+b\n@@ -30,3 +30,3 @@\n-c\n+d",
                    "applied_checkers": ["Checker"]
                }
            ]
        }""").asJsonObject

        val messages = EcsOutputParser.parseDiffs(json)
        assertEquals(2, messages.size)
        assertEquals(5, messages[0].line)
        assertEquals(30, messages[1].line)
    }

    @Test
    fun `parseDiffs skips entries without diff key`() {
        val json = com.google.gson.JsonParser.parseString("""{
            "diffs": [
                {"applied_checkers": ["Checker"]}
            ]
        }""").asJsonObject

        assertTrue(EcsOutputParser.parseDiffs(json).isEmpty())
    }

    @Test
    fun `parseDiffs returns empty when no diffs key`() {
        val json = com.google.gson.JsonParser.parseString("""{"errors": []}""").asJsonObject
        assertTrue(EcsOutputParser.parseDiffs(json).isEmpty())
    }

    // ── parseOutput (end-to-end) ────────────────────────────────────

    @Test
    fun `parseOutput handles realistic ECS output with errors`() {
        val output = """{
            "files": {
                "/app/src/Foo.php": {
                    "errors": [
                        {
                            "line": 10,
                            "message": "Missing return type",
                            "source_class": "Symplify\\CodingStandard\\Fixer\\ReturnTypeFixer"
                        }
                    ],
                    "diffs": []
                }
            }
        }"""

        val messages = EcsOutputParser.parseOutput(output)
        assertEquals(1, messages.size)
        assertEquals(10, messages[0].line)
        assertEquals("ReturnTypeFixer: Missing return type", messages[0].message)
    }

    @Test
    fun `parseOutput handles realistic ECS output with diffs`() {
        val output = """{
            "files": {
                "/app/src/Bar.php": {
                    "errors": [],
                    "diffs": [
                        {
                            "diff": "@@ -3,5 +3,5 @@\n-use Foo;\n+use Bar;",
                            "applied_checkers": [
                                "PhpCsFixer\\Fixer\\Import\\OrderedImportsFixer"
                            ]
                        }
                    ]
                }
            }
        }"""

        val messages = EcsOutputParser.parseOutput(output)
        assertEquals(1, messages.size)
        assertEquals(3, messages[0].line)
        assertEquals("OrderedImportsFixer", messages[0].message)
    }

    @Test
    fun `parseOutput handles multiple files`() {
        val output = """{
            "files": {
                "/app/A.php": {
                    "errors": [{"line": 1, "message": "err1"}],
                    "diffs": []
                },
                "/app/B.php": {
                    "errors": [{"line": 2, "message": "err2"}],
                    "diffs": []
                }
            }
        }"""

        val messages = EcsOutputParser.parseOutput(output)
        assertEquals(2, messages.size)
    }

    @Test
    fun `parseOutput skips PHP notices before JSON`() {
        val output = """PHP Notice: Undefined variable in /some/file.php on line 5
PHP Warning: blah
{"files": {"/app/Foo.php": {"errors": [{"line": 1, "message": "test"}], "diffs": []}}}"""

        val messages = EcsOutputParser.parseOutput(output)
        assertEquals(1, messages.size)
        assertEquals("test", messages[0].message)
    }

    @Test
    fun `parseOutput returns empty for empty string`() {
        assertTrue(EcsOutputParser.parseOutput("").isEmpty())
    }

    @Test
    fun `parseOutput returns empty for whitespace only`() {
        assertTrue(EcsOutputParser.parseOutput("   \n  ").isEmpty())
    }

    @Test
    fun `parseOutput returns empty for non-JSON output`() {
        assertTrue(EcsOutputParser.parseOutput("no json here at all").isEmpty())
    }

    @Test
    fun `parseOutput returns empty for invalid JSON`() {
        assertTrue(EcsOutputParser.parseOutput("{invalid json!!!").isEmpty())
    }

    @Test
    fun `parseOutput returns empty when files key is missing`() {
        assertTrue(EcsOutputParser.parseOutput("""{"totals": {"changed": 0}}""").isEmpty())
    }

    @Test
    fun `parseOutput returns empty when files is not an object`() {
        assertTrue(EcsOutputParser.parseOutput("""{"files": "not_an_object"}""").isEmpty())
    }

    @Test
    fun `parseOutput returns empty when no violations`() {
        val output = """{"files": {"/app/Clean.php": {"errors": [], "diffs": []}}}"""
        assertTrue(EcsOutputParser.parseOutput(output).isEmpty())
    }

    @Test
    fun `parseOutput combines errors and diffs from same file`() {
        val output = """{
            "files": {
                "/app/Mixed.php": {
                    "errors": [{"line": 5, "message": "err"}],
                    "diffs": [
                        {
                            "diff": "@@ -10,3 +10,3 @@\n-x\n+y",
                            "applied_checkers": ["Checker"]
                        }
                    ]
                }
            }
        }"""

        val messages = EcsOutputParser.parseOutput(output)
        assertEquals(2, messages.size)
        assertEquals(5, messages[0].line)
        assertEquals(10, messages[1].line)
    }
}
