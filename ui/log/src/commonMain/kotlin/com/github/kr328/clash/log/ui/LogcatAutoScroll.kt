package com.github.kr328.clash.log.ui

import androidx.compose.foundation.lazy.LazyListState

internal fun logcatShouldAutoScrollToLatest(
  messageCount: Int,
  listAtBottom: Boolean,
): Boolean {
  return messageCount > 0 && listAtBottom
}

internal fun logcatListViewportIsAtBottom(
  totalItemsCount: Int,
  lastVisibleItemIndex: Int?,
  lastVisibleItemOffset: Int = 0,
  lastVisibleItemSize: Int = 0,
  viewportEndOffset: Int = 0,
): Boolean {
  return lastVisibleItemIndex == null ||
    lastVisibleItemIndex == totalItemsCount - 1 &&
      lastVisibleItemOffset + lastVisibleItemSize <= viewportEndOffset
}

internal fun LazyListState.isLogcatViewportAtBottom(): Boolean {
  val layoutInfo = layoutInfo
  val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

  return logcatListViewportIsAtBottom(
    totalItemsCount = layoutInfo.totalItemsCount,
    lastVisibleItemIndex = lastVisibleItem?.index,
    lastVisibleItemOffset = lastVisibleItem?.offset ?: 0,
    lastVisibleItemSize = lastVisibleItem?.size ?: 0,
    viewportEndOffset = layoutInfo.viewportEndOffset,
  )
}
