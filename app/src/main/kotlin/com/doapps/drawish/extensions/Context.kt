package com.doapps.drawish.extensions

import android.content.Context
import com.doapps.drawish.helpers.Config

val Context.config: Config get() = Config.newInstance(applicationContext)
