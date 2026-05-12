package ai.tnj.haui.core.utils

import android.util.Log

/**
 * LogUtil
 * 
 * 一个仅在 Debug 模式下输出日志的工具类。
 * A utility class for logging that only prints logs in debug mode.
 */
object LogUtil {
    
    var isDebug: Boolean = BuildConfig.DEBUG

    // Lambda versions for lazy evaluation
    inline fun v(tag: String, tr: Throwable? = null, msg: () -> String) {
        if (isDebug) {
            val message = msg()
            if (tr == null) Log.v(tag, message) else Log.v(tag, message, tr)
        }
    }

    inline fun d(tag: String, tr: Throwable? = null, msg: () -> String) {
        if (isDebug) {
            val message = msg()
            if (tr == null) Log.d(tag, message) else Log.d(tag, message, tr)
        }
    }

    inline fun i(tag: String, tr: Throwable? = null, msg: () -> String) {
        if (isDebug) {
            val message = msg()
            if (tr == null) Log.i(tag, message) else Log.i(tag, message, tr)
        }
    }

    inline fun w(tag: String, tr: Throwable? = null, msg: () -> String) {
        if (isDebug) {
            val message = msg()
            if (tr == null) Log.w(tag, message) else Log.w(tag, message, tr)
        }
    }

    inline fun e(tag: String, tr: Throwable? = null, msg: () -> String) {
        if (isDebug) {
            val message = msg()
            if (tr == null) Log.e(tag, message) else Log.e(tag, message, tr)
        }
    }

    // String versions for convenience/compatibility
    fun v(tag: String, msg: String, tr: Throwable? = null) = v(tag, tr) { msg }
    fun d(tag: String, msg: String, tr: Throwable? = null) = d(tag, tr) { msg }
    fun i(tag: String, msg: String, tr: Throwable? = null) = i(tag, tr) { msg }
    fun w(tag: String, msg: String, tr: Throwable? = null) = w(tag, tr) { msg }
    fun e(tag: String, msg: String, tr: Throwable? = null) = e(tag, tr) { msg }
}
