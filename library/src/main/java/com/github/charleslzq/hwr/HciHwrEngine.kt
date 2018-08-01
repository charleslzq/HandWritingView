package com.github.charleslzq.hwr

import android.content.Context
import android.os.Environment
import android.util.Log
import com.github.charleslzq.hwr.support.Preference
import com.sinovoice.hcicloudsdk.api.HciCloudSys
import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr
import com.sinovoice.hcicloudsdk.common.AuthExpireTime
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.InitParam
import com.sinovoice.hcicloudsdk.common.Session
import com.sinovoice.hcicloudsdk.common.hwr.HwrAssociateWordsResult
import com.sinovoice.hcicloudsdk.common.hwr.HwrConfig
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object HciHwrEngine {
    const val TAG = "HciHwrEngine"
    lateinit var configuration: Configuration
        private set

    @JvmOverloads
    fun setup(context: Context,
              developerKey: String,
              appKey: String,
              additionalParams: MutableMap<String, String> = mutableMapOf()) {
        configuration = Configuration(context, developerKey, appKey, additionalParams)
        try {
            hciSysInit(context)
            checkAuthAndUpdateAuth()
            hwrInit(context)
        } catch (e: Throwable) {
            Log.e(TAG, "hci init failed", e)
        }
    }

    fun release() {
        HciCloudHwr.hciHwrRelease()
        HciCloudSys.hciRelease()
    }

    @Throws(HciRecogFailException::class, HciSessionException::class)
    fun recognize(strokes: ShortArray) = workInSession(Configuration.Caps.FREE_STYLUS) {
        HwrRecogResult().apply {
            val errorCode = HciCloudHwr.hciHwrRecog(it, strokes, "", this)
            if (errorCode != HciErrorCode.HCI_ERR_NONE) {
                throw HciRecogFailException(errorCode)
            }
        }
    }

    fun associate(word: String) = workInSession(Configuration.Caps.ASSOCIATE) {
        HwrAssociateWordsResult().apply {
            val errorCode = HciCloudHwr.hciHwrAssociateWords(it, "", word, this)
            if (errorCode != HciErrorCode.HCI_ERR_NONE) {
                throw HciAssociateFailException(errorCode)
            }
        }
    }

    private fun <R> workInSession(cap: Configuration.Caps, process: (Session) -> R): R {
        val session = openSession(cap)
        val result = process(session)
        HciCloudHwr.hciHwrSessionStop(session)
        return result
    }

    @Throws(HciSessionException::class)
    private fun openSession(cap: Configuration.Caps) = Session().apply {
        val sessionConfig = HwrConfig().apply {
            addParam(HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY, cap.capKey)
        }
        val errCode = HciCloudHwr.hciHwrSessionStart(sessionConfig.stringConfig, this)
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            throw HciSessionException(errCode)
        }
    }

    @Throws(HciSysInitException::class)
    private fun hciSysInit(context: Context) {
        val errCode = HciCloudSys.hciInit(getInitParam().stringConfig, context)
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            throw HciSysInitException(errCode)
        }
    }

    private fun getInitParam() = InitParam().apply {
        addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, configuration.authFilePath)
        addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, configuration.cloudUrl)
        addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, configuration.developerKey)
        addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, configuration.appKey)
        configuration.additionalParams.forEach { addParam(it.key, it.value) }
    }

    @Throws(HciAuthFailException::class)
    private fun checkAuthAndUpdateAuth() {
        // 获取系统授权到期时间
        val objExpireTime = AuthExpireTime()
        var initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime)
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            // 显示授权日期,如用户不需要关注该值,此处代码可忽略
            val date = Date(objExpireTime.expireTime * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            Log.i(HandWritingView.TAG, "expire time: " + sdf.format(date))

            if (objExpireTime.expireTime * 1000 > System.currentTimeMillis()) {
                Log.i(HandWritingView.TAG, "checkAuth success")
            }
        }

        // 获取过期时间失败或者已经过期
        initResult = HciCloudSys.hciCheckAuth()
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            Log.i(HandWritingView.TAG, "checkAuth success")
        } else {
            throw HciAuthFailException(initResult)
        }
    }

    @Throws(HciHwrInitException::class)
    private fun hwrInit(context: Context) {
        val files = arrayOf("fwlib.dic", "wwlib.dic", "letter.dic", "letter.conf", "wa.system.dct", "wa.user.dct")
        File(configuration.dataPath).let {
            if (!it.exists() || it.isFile) {
                it.mkdirs()
            }
        }
        files.forEach {
            FileOutputStream(configuration.dataPath + it).use { output ->
                context.assets.open(it).use { input ->
                    val buffer = ByteArray(1024)
                    var len = input.read(buffer)
                    while (len != -1) {
                        output.write(buffer, 0, len)
                        len = input.read(buffer)
                    }
                    output.flush()
                }
            }
        }
        val errCode = HwrInitParam().apply {
            addParam(HwrInitParam.PARAM_KEY_DATA_PATH, configuration.dataPath)
            addParam(HwrInitParam.PARAM_KEY_INIT_CAP_KEYS, configuration.initCapKeys)
        }.let { HciCloudHwr.hciHwrInit(it.stringConfig) }
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            throw HciHwrInitException(errCode)
        }
    }

    class Configuration
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
}