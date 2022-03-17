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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.akvo.caddisfly.databinding.FragmentCompartmentBagBinding
import org.akvo.caddisfly.model.Instruction
import org.akvo.caddisfly.ui.BaseFragment

class CompartmentBagFragment : BaseFragment() {
    private var mListener: OnCompartmentBagSelectListener? = null
    var key: String = ""
    private var useBlue: Boolean? = null
    private var _binding: FragmentCompartmentBagBinding? = null
    private val b get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            if (key.isEmpty()) {
                val arg = requireArguments().getString(ARG_RESULT_VALUES)
                if (arg != null) {
                    key = arg
                }
            }
            useBlue = requireArguments().getBoolean(ARG_USE_BLUE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompartmentBagBinding.inflate(inflater, container, false)
        var instruction: Instruction? = null
        if (arguments != null) {
            instruction = requireArguments().getParcelable(ARG_INSTRUCTION)
        }
        b.instruction = instruction
        b.compartments.key = key
        b.compartments.useBlueSelection(useBlue!!)
        b.compartments.setOnClickListener {
            key = b.compartments.key
            if (mListener != null) {
                mListener!!.onCompartmentBagSelect(key, fragmentId)
            }
        }
        return b.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnCompartmentBagSelectListener) {
            context
        } else {
            throw IllegalArgumentException(
                context
                    .toString() + " must implement OnCompartmentBagSelectListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnCompartmentBagSelectListener {
        fun onCompartmentBagSelect(key: String, fragmentId: Int)
    }

    companion object {
        private const val ARG_INSTRUCTION = "resultInstruction"
        private const val ARG_RESULT_VALUES = "result_key"
        private const val ARG_USE_BLUE = "use_blue_selection"
        fun newInstance(
            key: String?, id: Int,
            instruction: Instruction?, useBlue: Boolean
        ): CompartmentBagFragment {
            val fragment = CompartmentBagFragment()
            fragment.fragmentId = id
            val args = Bundle()
            args.putParcelable(ARG_INSTRUCTION, instruction)
            args.putString(ARG_RESULT_VALUES, key)
            args.putBoolean(ARG_USE_BLUE, useBlue)
            fragment.arguments = args
            return fragment
        }
    }
}