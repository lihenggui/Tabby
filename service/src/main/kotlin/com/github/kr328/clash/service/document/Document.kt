package com.github.kr328.clash.service.document

import com.github.kr328.clash.common.document.Flag

sealed interface Document {
  val id: String
  val name: String
  val mimeType: String
  val size: Long
  val updatedAt: Long
  val flags: Set<Flag>
}
