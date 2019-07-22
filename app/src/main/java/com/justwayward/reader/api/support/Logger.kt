package com.justwayward.reader.api.support

import com.justwayward.reader.utils.LogUtils

/**
 * @author yuyh.
 * @date 2016/12/13.
 */
class Logger : LoggingInterceptor.Logger {

    override fun log(message: String) {
        LogUtils.i("http : $message")
    }
}
