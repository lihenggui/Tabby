package com.github.kr328.clash.glue.remote

data class FilesClientDocument(
  val id: String,
  val name: String,
  val size: Long,
  val lastModified: Long,
  val isDirectory: Boolean,
)
