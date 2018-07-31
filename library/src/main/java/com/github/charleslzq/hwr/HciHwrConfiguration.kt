package com.github.charleslzq.hwr

import android.content.Context
import android.os.Environment
import com.github.charleslzq.hwr.support.Preference
import java.io.File

class HciHwrConfiguration
@JvmOverloads
constructor(
        context: Context,
        developerKey: String,
        appKey: String,
        val additionalParams: MutableMap<String, String> = mutableMapOf()
) {
    val authFilePath = context.filesDir.absolutePath
    var cloudUrl = "http://api.hcicloud.com:8888"
    val dataPath = (Environment.getExternalStorageDirectory().absolutePath + File.separator
            + "sinovoice" + File.separator
            + context.packageName + File.separator
            + "data" + File.separator)
    val initCapKeys = Caps.values().joinToString(";") { it.capKey }
    var developerKey by Preference(context, "com.github.charleslzq.hrw.developerKey", developerKey)
    var appKey by Preference(context, "com.github.charleslzq.hrw.appKey", appKey)

    enum class Caps(val capKey: String) {
        FREE_STYLUS("hwr.local.freestylus"),
        ASSOCIATE("hwr.local.associateword");
    }
}