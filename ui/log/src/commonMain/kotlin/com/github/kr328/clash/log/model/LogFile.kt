package com.github.kr328.clash.log.model

data class LogFile(val fileName: String, val created: Long) {
  companion object {
    private val REGEX_FILE = "clash-(\\d+)\\.log".toRegex()

    fun parse(fileName: String): LogFile? {
      return REGEX_FILE.matchEntire(fileName)?.run {
        groupValues[1].toLongOrNull()?.let { created -> LogFile(fileName, created) }
      }
    }

    fun fromCreatedTime(created: Long): LogFile {
      return LogFile("clash-$created.log", created)
    }
  }
}
