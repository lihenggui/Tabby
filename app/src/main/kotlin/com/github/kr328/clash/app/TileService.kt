package com.github.kr328.clash.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.compat.registerReceiverCompat
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.constants.Permissions
import com.github.kr328.clash.glue.remote.StatusClient
import com.github.kr328.clash.glue.util.startClashService
import com.github.kr328.clash.glue.util.stopClashService

class TileService : android.service.quicksettings.TileService() {
  private var tileState = TabbyTileState()

  override fun onClick() {
    val tile = qsTile ?: return

    when (tabbyTileClickAction(tile.tabbyTileClickState())) {
      TabbyTileClickAction.StartClash -> startClashService()
      TabbyTileClickAction.StopClash -> stopClashService()
      TabbyTileClickAction.Ignore -> Unit
    }
  }

  override fun onStartListening() {
    super.onStartListening()

    registerReceiverCompat(
      receiver,
      IntentFilter().apply {
        addAction(Intents.ACTION_CLASH_STARTED)
        addAction(Intents.ACTION_CLASH_STOPPED)
        addAction(Intents.ACTION_PROFILE_LOADED)
        addAction(Intents.ACTION_SERVICE_RECREATED)
      },
      Permissions.RECEIVE_SELF_BROADCASTS,
      null,
    )

    tileState = tabbyTileInitialState(StatusClient(this).currentProfile())

    updateTile()
  }

  override fun onStopListening() {
    super.onStopListening()

    unregisterReceiver(receiver)
  }

  private fun updateTile() {
    val tile = qsTile ?: return
    val presentation = tabbyTilePresentation(tileState)

    tile.state = if (presentation.active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    tile.label = presentation.profileName ?: getText(CommonR.string.tabby)

    tile.icon = Icon.createWithResource(this, CommonR.drawable.ic_tabby_small)

    tile.updateTile()
  }

  private val receiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        val event =
          when (intent?.action) {
            Intents.ACTION_CLASH_STARTED -> TabbyTileEvent.ClashStarted
            Intents.ACTION_CLASH_STOPPED -> TabbyTileEvent.ClashStopped
            Intents.ACTION_SERVICE_RECREATED -> TabbyTileEvent.ServiceRecreated
            Intents.ACTION_PROFILE_LOADED ->
              TabbyTileEvent.ProfileLoaded(StatusClient(this@TileService).currentProfile())
            else -> return
          }

        tileState = reduceTabbyTileState(tileState, event)

        updateTile()
      }
    }
}

private fun Tile.tabbyTileClickState(): TabbyTileClickState =
  when (state) {
    Tile.STATE_ACTIVE -> TabbyTileClickState.Active
    Tile.STATE_INACTIVE -> TabbyTileClickState.Inactive
    else -> TabbyTileClickState.Other
  }
