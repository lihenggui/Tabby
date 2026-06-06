package com.github.kr328.clash.log.model

internal fun selectLogFiles(fileNames: List<String>): List<LogFile> {
  return fileNames.mapNotNull(LogFile::parse)
}
