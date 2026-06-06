package com.github.kr328.clash.service.data

import android.content.Context
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.database.AndroidDatabaseDriverFactory
import com.github.kr328.clash.database.ProfileDatabase

object SqlDelightDatabase {
  private var database: ProfileDatabase? = null

  @Synchronized
  fun profileDatabase(context: Context = Global.application): ProfileDatabase {
    return database
      ?: ProfileDatabase(AndroidDatabaseDriverFactory(context.applicationContext).createDriver())
        .also { database = it }
  }
}
