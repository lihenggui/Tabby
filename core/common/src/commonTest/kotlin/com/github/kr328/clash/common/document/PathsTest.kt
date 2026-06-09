package com.github.kr328.clash.common.document

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

class PathsTest {
  private val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

  @Test
  fun resolvesRootPath() {
    assertEquals(Path(uuid = null, scope = null, relative = null), Paths.resolve("/"))
  }

  @Test
  fun resolvesProfilePath() {
    assertEquals(Path(uuid = uuid, scope = null, relative = null), Paths.resolve("/$uuid"))
  }

  @Test
  fun resolvesConfigurationScopePath() {
    assertEquals(
      Path(uuid = uuid, scope = Path.Scope.Configuration, relative = null),
      Paths.resolve("/$uuid/${Paths.CONFIGURATION_ID}"),
    )
  }

  @Test
  fun resolvesProviderRelativePath() {
    assertEquals(
      Path(uuid = uuid, scope = Path.Scope.Providers, relative = listOf("proxy.yaml")),
      Paths.resolve("/$uuid/${Paths.PROVIDERS_ID}/proxy.yaml"),
    )
  }

  @Test
  fun filtersBlankCurrentAndParentSegments() {
    assertEquals(
      Path(uuid = uuid, scope = Path.Scope.Providers, relative = listOf("proxy.yaml")),
      Paths.resolve("//./../$uuid/${Paths.PROVIDERS_ID}/./proxy.yaml"),
    )
  }

  @Test
  fun formatsPathAsDocumentId() {
    assertEquals(
      "/$uuid/${Paths.PROVIDERS_ID}/proxy.yaml",
      Path(uuid = uuid, scope = Path.Scope.Providers, relative = listOf("proxy.yaml")).toString(),
    )
  }

  @Test
  fun rejectsUnknownScope() {
    assertFailsWith<IllegalArgumentException> { Paths.resolve("/$uuid/unknown") }
  }
}
