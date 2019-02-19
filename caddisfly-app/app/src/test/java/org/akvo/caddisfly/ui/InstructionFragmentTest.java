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

package org.akvo.caddisfly.ui;

import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.widget.RowView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.fragment.app.Fragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment;

@RunWith(RobolectricTestRunner.class)
public class InstructionFragmentTest {

    @Test
    public void testFragment() {
        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID);

        Fragment fragment = InstructionFragment.getInstance(testInfo);
        startFragment(fragment);
        assertNotNull(fragment);
    }

    @Test
    public void testInstruction() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID);

        Fragment fragment = InstructionFragment.getInstance(testInfo);
        startVisibleFragment(fragment, TestActivity.class, R.id.fragment_container);

        assertNotNull(fragment);

        View view = fragment.getView();
        assertNotNull(view);

        RowView rowView = view.findViewById(0);
        assertNotNull(rowView);
        assertEquals("Put on plastic gloves", rowView.getString());

        ImageView imageView = view.findViewById(3);
        assertNotNull(imageView);
        int drawableResId = shadowOf(imageView.getDrawable()).getCreatedFromResId();
        assertEquals(-1, drawableResId);
    }

}
