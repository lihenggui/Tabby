package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class EditableTextRouteTest {
  @Test
  fun editableTextMapRouteCarriesTitleAndInitialValues() {
    val values = linkedMapOf("example.com" to "127.0.0.1")

    val route = EditableTextMap(EditableTextTitle.Hosts, values)

    assertEquals(EditableTextTitle.Hosts, route.title)
    assertEquals(values, route.initialValues)
  }

  @Test
  fun editableTextListRouteCarriesTitleAndInitialValues() {
    val values = linkedSetOf("80", "8080")

    val route = EditableTextList(EditableTextTitle.SniffHttpPorts, values)

    assertEquals(EditableTextTitle.SniffHttpPorts, route.title)
    assertEquals(values, route.initialValues)
  }
}
