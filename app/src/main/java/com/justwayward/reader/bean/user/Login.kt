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
package com.justwayward.reader.bean.user

import com.justwayward.reader.bean.base.Base

/**
 * @author yuyh.
 * @date 16/9/4.
 */
class Login : Base() {


    /**
     * _id : 57cbf0278b37eb5f05496f8b
     * nickname : ✘。
     * avatar : /avatar/eb/a0/eba095c72cc992bdea6539ce1cfd0aff
     * exp : 0
     * lv : 1
     * gender : female
     * type : normal
     */

    var user: UserBean? = null
    /**
     * user : {"_id":"57cbf0278b37eb5f05496f8b","nickname":"✘。","avatar":"/avatar/eb/a0/eba095c72cc992bdea6539ce1cfd0aff","exp":0,"lv":1,"gender":"female","type":"normal"}
     * token : gmPcsbwQ41UfTQEc7yMnBiRY
     */

    var token: String? = null

    class UserBean {
        var _id: String? = null
        var nickname: String? = null
        var avatar: String? = null
        var exp: Int = 0
        var lv: Int = 0
        var gender: String? = null
        var type: String? = null

        override fun toString(): String {
            return "UserBean{" +
                    "_id='" + _id + '\''.toString() +
                    ", nickname='" + nickname + '\''.toString() +
                    ", avatar='" + avatar + '\''.toString() +
                    ", exp=" + exp +
                    ", lv=" + lv +
                    ", gender='" + gender + '\''.toString() +
                    ", type='" + type + '\''.toString() +
                    '}'.toString()
        }
    }


}
