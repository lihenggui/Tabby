package com.github.kr328.clash.service.document

import com.github.kr328.clash.common.document.Flag

class VirtualDocument(
  override val id: String,
  override val name: String,
  override val mimeType: String,
  override val size: Long,
  override val updatedAt: Long,
  override val flags: Set<Flag>,
) : Document
