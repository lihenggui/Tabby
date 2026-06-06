package com.github.kr328.clash.service.remote

import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface ILogObserver {
  fun newItem(log: String)
}
