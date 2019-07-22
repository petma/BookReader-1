package com.justwayward.reader.ui.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.bean.support.BookMark
import com.yuyh.easyadapter.abslistview.EasyLVAdapter
import com.yuyh.easyadapter.abslistview.EasyLVHolder

/**
 * @author yuyh.
 * @date 2016/11/18.
 */
class BookMarkAdapter(context: Context, list: List<BookMark>) : EasyLVAdapter<BookMark>(context, list, R.layout.item_read_mark) {

    override fun convert(holder: EasyLVHolder, position: Int, item: BookMark) {
        val tv = holder.getView<TextView>(R.id.tvMarkItem)

        val spanText = SpannableString((position + 1).toString() + ". " + item.title + ": ")
        spanText.setSpan(ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.light_coffee)),
                0, spanText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        tv.text = spanText

        if (item.desc != null) {
            tv.append(item.desc
                    .replace("　".toRegex(), "")
                    .replace(" ".toRegex(), "")
                    .replace("\n".toRegex(), ""))
        }

    }
}
