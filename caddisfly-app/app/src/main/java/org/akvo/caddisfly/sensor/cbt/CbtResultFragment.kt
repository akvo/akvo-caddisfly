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
package org.akvo.caddisfly.sensor.cbt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.akvo.caddisfly.databinding.FragmentCbtResultBinding
import org.akvo.caddisfly.helper.TestConfigHelper
import org.akvo.caddisfly.model.MpnValue
import org.akvo.caddisfly.ui.BaseFragment
import org.akvo.caddisfly.util.StringUtil

class CbtResultFragment : BaseFragment() {
    private val viewModel: CbtViewModel by activityViewModels()
    private var _binding: FragmentCbtResultBinding? = null
    private val b get() = _binding!!
    private var mpnValue: MpnValue? = null
    private var mpnValue2: MpnValue? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCbtResultBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onResume() {
        super.onResume()
        showResult()
    }

    fun showResult() {
        if (isAdded && activity != null) {
            mpnValue =
                TestConfigHelper.getMpnValueForKey(viewModel.result1, viewModel.sampleQuantity)
            mpnValue2 =
                TestConfigHelper.getMpnValueForKey(viewModel.result2, viewModel.sampleQuantity)

            if (mpnValue == null) {
                return
            }

            val results = StringUtil.getStringResourceByName(
                requireActivity(),
                mpnValue!!.riskCategory
            ).toString().split("/").toTypedArray()
            b.textSubRisk.text = ""
            if (viewModel.resultCount > 3) {
                b.layoutResult2.visibility = View.VISIBLE
                b.layoutRisk.visibility = View.GONE
                b.layoutRisk2.visibility = View.VISIBLE
                b.textName1.visibility = View.VISIBLE
                b.textRisk1.text = results[0].trim { it <= ' ' }
                if (results.size > 1) {
                    b.textSubRisk2.text = results[1].trim { it <= ' ' }
                    b.textSubRisk2.visibility = View.VISIBLE
                } else {
                    b.textSubRisk2.visibility = View.GONE
                }
            } else {
                b.layoutResult2.visibility = View.GONE
                b.layoutRisk.visibility = View.VISIBLE
                b.layoutRisk2.visibility = View.GONE
                b.textName1.visibility = View.GONE
                b.textRisk.text = results[0].trim { it <= ' ' }
                if (results.size > 1) {
                    b.textSubRisk.text = results[1].trim { it <= ' ' }
                    b.textSubRisk.visibility = View.VISIBLE
                } else {
                    b.textSubRisk.visibility = View.GONE
                }
            }
            b.layoutRisk.setBackgroundColor(mpnValue!!.backgroundColor1)
            b.layoutRisk2.setBackgroundColor(mpnValue!!.backgroundColor1)
            b.textResult1.text = mpnValue!!.mpn
            if (mpnValue2 != null) {
                b.textResult2.text = mpnValue2!!.mpn
            }
        }
    }

    companion object {
        private const val ARG_RESULT_COUNT = "result_count"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CbtResultFragment.
         */
        fun newInstance(resultCount: Int): CbtResultFragment {
            val fragment = CbtResultFragment()
            val args = Bundle()
            args.putInt(ARG_RESULT_COUNT, resultCount)
            fragment.arguments = args
            return fragment
        }
    }
}