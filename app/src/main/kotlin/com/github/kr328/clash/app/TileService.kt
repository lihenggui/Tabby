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
    when (tabbyTileClickAction(qsTile?.tabbyTileClickState())) {
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

    tile.state = presentation.toPlatformTileState()

    tile.label =
      tabbyTilePresentationLabel(
        presentation = presentation,
        defaultLabel = getString(CommonR.string.tabby),
      )

    tile.icon = Icon.createWithResource(this, CommonR.drawable.ic_tabby_small)

    tile.updateTile()
  }

  private val receiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        tileState =
          when (val plan = tabbyTileBroadcastPlan(intent?.tabbyTileBroadcastAction())) {
            is TabbyTileBroadcastPlan.Reduce -> reduceTabbyTileState(tileState, plan.event)
            TabbyTileBroadcastPlan.LoadCurrentProfile ->
              reduceTabbyTileState(
                tileState,
                tabbyTileProfileLoadedEvent(StatusClient(this@TileService).currentProfile()),
              )
            TabbyTileBroadcastPlan.Ignore -> return
          }

        updateTile()
      }
    }
}

private fun Intent.tabbyTileBroadcastAction(): TabbyTileBroadcastAction? =
  tabbyTileBroadcastActionFromString(
    action = action,
    clashStartedAction = Intents.ACTION_CLASH_STARTED,
    clashStoppedAction = Intents.ACTION_CLASH_STOPPED,
    serviceRecreatedAction = Intents.ACTION_SERVICE_RECREATED,
    profileLoadedAction = Intents.ACTION_PROFILE_LOADED,
  )

private fun Tile.tabbyTileClickState(): TabbyTileClickState {
  return when (state) {
    Tile.STATE_ACTIVE -> TabbyTileClickState.Active
    Tile.STATE_INACTIVE -> TabbyTileClickState.Inactive
    else -> TabbyTileClickState.Other
  }
}

private fun TabbyTilePresentation.toPlatformTileState(): Int {
  return if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
}
