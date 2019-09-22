/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.ui

import junit.framework.TestCase.assertNotNull
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.sensor.cbt.CbtResultFragment
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment

@RunWith(RobolectricTestRunner::class)
class InstructionFragmentTest {

    @Test
    fun testFragment() {
        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID)

        val fragment = CbtResultFragment.newInstance(testInfo.results.size)
        startFragment(fragment)
        assertNotNull(fragment)
    }

    @Test
    fun testInstruction() {

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID)

        val fragment = CbtResultFragment.newInstance(testInfo.results.size)
        startVisibleFragment(fragment, TestActivity::class.java, R.id.fragment_container)

        assertNotNull(fragment)

        val view = fragment.view
        assertNotNull(view)

//        val rowView = view!!.findViewById<RowView>(0)
//        assertNotNull(rowView)
//        assertEquals("Prepare the work area and put on plastic gloves.", rowView.string)
//
//        val imageView = view.findViewById<ImageView>(3)
//        assertNotNull(imageView)
//        val drawableResId = shadowOf(imageView.drawable).createdFromResId
//        assertEquals(R.drawable.in_1, drawableResId)
    }
}
