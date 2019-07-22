/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.justwayward.reader.utils

import com.justwayward.reader.common.TTSEventProcess
import com.sinovoice.hcicloudsdk.android.tts.player.TTSPlayer
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig
import com.sinovoice.hcicloudsdk.common.tts.TtsInitParam

/**
 * @author yuyh.
 * @date 2016/8/16.
 */
object TTSPlayerUtils {

    val ttsPlayer: TTSPlayer
        get() {
            val mTtsPlayer = TTSPlayer()
            val ttsInitParam = TtsInitParam()
            ttsInitParam.addParam(TtsInitParam.PARAM_KEY_FILE_FLAG, "none")
            mTtsPlayer.init(ttsInitParam.stringConfig, TTSEventProcess())
            return mTtsPlayer
        }

    // 发音人
    // 音频格式
    val ttsConfig: TtsConfig
        get() {
            val ttsConfig = TtsConfig()
            ttsConfig.addParam(TtsConfig.SessionConfig.PARAM_KEY_CAP_KEY, "tts.cloud.xiaokun")
            ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit")
            return ttsConfig
        }
}
