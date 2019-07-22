package com.justwayward.reader.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.justwayward.reader.ui.fragment.EPubReaderFragment

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.SpineReference

class EPubReaderAdapter(fm: FragmentManager, private val mSpineReferences: List<SpineReference>,
                        private val mBook: Book, private val mEpubFileName: String, private val mIsSmilAvailable: Boolean) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return EPubReaderFragment.newInstance(position, mBook, mEpubFileName, mIsSmilAvailable)
    }

    override fun getCount(): Int {
        return mSpineReferences.size
    }

}