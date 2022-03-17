package org.akvo.caddisfly.sensor.cbt

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.SparseArray
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.firebase.analytics.FirebaseAnalytics
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey.*
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.databinding.FragmentInstructionBinding
import org.akvo.caddisfly.helper.InstructionHelper
import org.akvo.caddisfly.helper.TestConfigHelper
import org.akvo.caddisfly.model.Instruction
import org.akvo.caddisfly.model.PageIndex
import org.akvo.caddisfly.model.PageType
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.cbt.CompartmentBagFragment.OnCompartmentBagSelectListener
import org.akvo.caddisfly.sensor.manual.ResultPhotoFragment
import org.akvo.caddisfly.sensor.manual.ResultPhotoFragment.OnPhotoTakenListener
import org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils
import org.akvo.caddisfly.sensor.striptest.utils.ResultUtils
import org.akvo.caddisfly.ui.BaseActivity
import org.akvo.caddisfly.ui.BaseFragment
import org.akvo.caddisfly.util.StringUtil
import org.akvo.caddisfly.widget.ButtonType
import org.akvo.caddisfly.widget.CustomViewPager
import org.akvo.caddisfly.widget.PageIndicatorView
import org.akvo.caddisfly.widget.SwipeDirection
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class CbtActivity : BaseActivity(), OnCompartmentBagSelectListener, OnPhotoTakenListener {
    private lateinit var viewModel: CbtViewModel
    private var resultFragment: CbtResultFragment? = null
    private val resultPhotoFragment = SparseArray<ResultPhotoFragment>()
    private val inputFragment = SparseArray<CompartmentBagFragment>()
    private val inputIndexes = ArrayList<Int>()
    private val cbtResultKeys = SparseArray<String?>()
    private lateinit var imagePageRight: ImageView
    private var imagePageLeft: ImageView? = null
    private val pageIndex = PageIndex()
    private var imageFileName: String? = ""
    private var currentPhotoPath: String? = null
    private var testInfo: TestInfo? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private val instructionList = ArrayList<Instruction>()
    private var totalPageCount = 0
    private var footerLayout: RelativeLayout? = null
    private lateinit var pagerIndicator: PageIndicatorView
    private var showSkipMenu = true
    private lateinit var viewPager: CustomViewPager
    private var testPhase = 0
    private var scale = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[CbtViewModel::class.java]
        setContentView(R.layout.activity_test_steps)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        scale = resources.displayMetrics.density
        viewPager = findViewById(R.id.viewPager)
        pagerIndicator = findViewById(R.id.pager_indicator)
        footerLayout = findViewById(R.id.layout_footer)

        if (testInfo == null) {
            testInfo = intent.getParcelableExtra(TEST_INFO)
        }
        if (testInfo == null) {
            return
        }
        testPhase = intent.getIntExtra(TEST_PHASE, 0)
        if (testPhase == 2) {
            InstructionHelper.setupInstructions(
                testInfo!!.instructions2,
                instructionList,
                pageIndex,
                false
            )
        } else {
            InstructionHelper.setupInstructions(
                testInfo!!.instructions,
                instructionList,
                pageIndex,
                false
            )
        }
        totalPageCount = instructionList.size
        if (savedInstanceState == null) {
            createFragments()
        }
        viewPager.adapter = SectionsPagerAdapter(
            supportFragmentManager
        )
        pagerIndicator.showDots(true)
        pagerIndicator.setPageCount(totalPageCount)
        imagePageRight = findViewById(R.id.image_pageRight)
        imagePageRight.setOnClickListener { nextPage() }
        imagePageLeft = findViewById(R.id.image_pageLeft)
        imagePageLeft?.visibility = View.INVISIBLE
        imagePageLeft?.setOnClickListener { pageBack() }
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (resultPhotoFragment[position - 1] != null &&
                    !resultPhotoFragment[position - 1].isValid
                ) {
                    if (position > totalPageCount - 1) {
                        pageBack()
                        return
                    }
                }
                pagerIndicator.setActiveIndex(position)
                showHideFooter()
                if (pageIndex.getType(position) == PageType.PHOTO && position > 2) {
                    if (cbtResultKeys.size() > 0) {
                        if (cbtResultKeys[inputIndexes[0]] == "11111") {
                            viewPager.setCurrentItem(viewPager.currentItem + 3, true)
                        }
                    }
                }
                if (pageIndex.getType(position) == PageType.INPUT && position > 3) {
                    if (cbtResultKeys.size() > 0) {
                        if (cbtResultKeys[inputIndexes[0]] == "11111") {
                            viewPager.setCurrentItem(pageIndex.getInputPageIndex(0), true)
                        }
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        showHideFooter()

        val fm = supportFragmentManager
        val listener = FragmentOnAttachListener { _, _ -> showHideFooter() }
        fm.addFragmentOnAttachListener(listener)
    }

    private fun createFragments() {
        if (resultFragment == null) {
            resultFragment = CbtResultFragment.newInstance(testInfo!!.results.size)
            resultFragment!!.fragmentId = pageIndex.resultIndex
            viewModel.resultCount = testInfo!!.results.size
            viewModel.sampleQuantity = testInfo!!.sampleQuantity
//            resultFragment!!.setResult(viewModel.result1, testInfo!!.sampleQuantity)
//            resultFragment!!.setResult2(viewModel.result2, testInfo!!.sampleQuantity)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_PHOTO_PATH, currentPhotoPath)
        outState.putString(CURRENT_IMAGE_FILE_NAME, imageFileName)
        if (inputIndexes.size > 0) {
            outState.putInt(CBT_RESULT_INDEX_1, inputIndexes[0])
        }
        if (inputIndexes.size > 1) {
            outState.putInt(CBT_RESULT_INDEX_2, inputIndexes[1])
        }
        outState.putString(CBT_RESULT_1, viewModel.result1)
        outState.putString(CBT_RESULT_2, viewModel.result2)
        outState.putParcelable(TEST_INFO, testInfo)
        super.onSaveInstanceState(outState)
    }

    public override fun onRestoreInstanceState(inState: Bundle) {
        for (i in supportFragmentManager.fragments.indices) {
            val fragment = supportFragmentManager.fragments[i]
            if (fragment is ResultPhotoFragment) {
                resultPhotoFragment.put(
                    (fragment as BaseFragment).fragmentId,
                    fragment
                )
            }
        }

        currentPhotoPath = inState.getString(CURRENT_PHOTO_PATH)
        imageFileName = inState.getString(CURRENT_IMAGE_FILE_NAME)
        viewModel.result1 = inState.getString(CBT_RESULT_1).toString()
        viewModel.result2 = inState.getString(CBT_RESULT_2).toString()
        viewModel.index1 = inState.getInt(CBT_RESULT_INDEX_1)
        viewModel.index2 = inState.getInt(CBT_RESULT_INDEX_2)
        if (viewModel.index1 > 0) {
            inputIndexes.add(viewModel.index1)
        }
        if (viewModel.index2 > 0) {
            inputIndexes.add(viewModel.index2)
        }
        cbtResultKeys.put(viewModel.index1, viewModel.result1)
        cbtResultKeys.put(viewModel.index2, viewModel.result2)
        if (inputFragment[viewModel.index1] != null) {
            inputFragment[viewModel.index1].key = viewModel.result1
        } else {
            if (viewModel.result1.isNotEmpty()) {
                inputFragment.put(
                    viewModel.index1, CompartmentBagFragment.newInstance(
                        viewModel.result1, viewModel.index1,
                        instructionList[viewModel.index1], false
                    )
                )
            }
        }

        if (inputFragment[viewModel.index2] != null) {
            inputFragment[viewModel.index2].key = viewModel.result2
        } else {
            if (viewModel.result2.isNotEmpty()) {
                inputFragment.put(
                    viewModel.index2, CompartmentBagFragment.newInstance(
                        viewModel.result2, viewModel.index2,
                        instructionList[viewModel.index2], true
                    )
                )
            }
        }
        testInfo = inState.getParcelable(TEST_INFO)

        createFragments()

        super.onRestoreInstanceState(inState)
    }

//    override fun onAttachFragment(fragment: Fragment) {
//        super.onAttachFragment(fragment)

//    }

    private fun pageBack() {
        viewPager.currentItem = max(0, viewPager.currentItem - 1)
    }

    private fun nextPage() {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    private fun showHideFooter() {
        if (imagePageLeft == null) {
            return
        }
        viewPager.setAllowedSwipeDirection(SwipeDirection.all)
        imagePageLeft?.visibility = View.VISIBLE
        imagePageRight.visibility = View.VISIBLE
        pagerIndicator.visibility = View.VISIBLE
        footerLayout!!.visibility = View.VISIBLE
        title = testInfo!!.name
        showSkipMenu = viewPager.currentItem < pageIndex.skipToIndex - 2
        if (viewPager.currentItem > pageIndex.skipToIndex) {
            showSkipMenu = viewPager.currentItem < pageIndex.skipToIndex2 - 2
        }
        when (pageIndex.getType(viewPager.currentItem)) {
            PageType.PHOTO -> if (resultPhotoFragment[viewPager.currentItem] != null) {
                if (resultPhotoFragment[viewPager.currentItem].isValid) {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.all)
                    imagePageRight.visibility = View.VISIBLE
                } else {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.left)
                    imagePageRight.visibility = View.INVISIBLE
                }
            }
            PageType.INPUT -> setTitle(R.string.setCompartmentColors)
            PageType.RESULT -> setTitle(R.string.result)
            else -> {
                footerLayout!!.visibility = View.VISIBLE
                viewPager.setAllowedSwipeDirection(SwipeDirection.all)
            }
        }

        // Last page
        if (viewPager.currentItem == totalPageCount - 1) {
            imagePageRight.visibility = View.INVISIBLE
            if (testPhase == 2) {
                if (scale <= 1.5) {
                    // don't show footer page indicator for smaller screens
                    Handler()
                        .postDelayed({ footerLayout!!.visibility = View.GONE }, 400)
                }
            }
        }

        // First page
        if (viewPager.currentItem == 0) {
            imagePageLeft?.visibility = View.INVISIBLE
        }
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        title = testInfo!!.name
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (showSkipMenu) {
            menuInflater.inflate(R.menu.menu_instructions, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (viewPager.currentItem == 0) {
                onBackPressed()
            } else {
                viewPager.currentItem = 0
                showHideFooter()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            pageBack()
        }
    }

    /**
     * Show CBT incubation times instructions in a dialog.
     *
     * @param view the view
     */
    fun onClickIncubationTimes(view: View?) {
        val newFragment: DialogFragment = IncubationTimesDialogFragment()
        newFragment.show(supportFragmentManager, "incubationTimes")
    }

    fun onSkipClick(item: MenuItem?) {
        viewPager.currentItem = pageIndex.skipToIndex
        if (AppPreferences.analyticsEnabled()) {
            val bundle = Bundle()
            bundle.putString(
                "InstructionsSkipped", testInfo!!.name +
                        " (" + testInfo!!.brand + ")"
            )
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation")
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo!!.uuid)
            mFirebaseAnalytics!!.logEvent("instruction_skipped", bundle)
        }
    }

    override fun onCompartmentBagSelect(key: String, fragmentId: Int) {
        var secondFragmentId = 0

        // Get the id of the TC input fragment
        if (inputIndexes.size > 1) {
            secondFragmentId = inputIndexes[1]
        }
        cbtResultKeys.put(fragmentId, key)
        for (i in inputIndexes.indices) {
            inputFragment[inputIndexes[0]]
        }
        val secondResult: String
        var secondFragment: CompartmentBagFragment? = null
        if (fragmentId == inputIndexes[0]) {
            var newSecondResult = key.replace("1", "2")
            if (cbtResultKeys[fragmentId] != null) {
                viewModel.result1 = cbtResultKeys[fragmentId]!!
            }
            resultFragment!!.showResult()
            if (inputIndexes.size > 1) {
                secondFragment = inputFragment[secondFragmentId]
                secondResult = secondFragment.key
            } else {
                secondResult = newSecondResult
            }
            for (i in secondResult.indices) {
                if (secondResult[i] == '1' && newSecondResult[i] != '2') {
                    val chars = newSecondResult.toCharArray()
                    chars[i] = '1'
                    newSecondResult = String(chars)
                }
            }
            if (secondFragment != null) {
                viewModel.result2 = newSecondResult
                secondFragment.key = newSecondResult
            }
        }

        if (fragmentId == secondFragmentId) {
            viewModel.result2 = key
            resultFragment!!.showResult()
        }

        // If E.coli input fragment then add or remove TC part based on input
        if (fragmentId != secondFragmentId) {
            InstructionHelper.setupInstructions(
                testInfo!!.instructions2,
                instructionList,
                pageIndex,
                key == "11111"
            )
            totalPageCount = instructionList.size
            pagerIndicator.setPageCount(totalPageCount)
            viewPager.adapter!!.notifyDataSetChanged()
            pagerIndicator.visibility = View.GONE
            pagerIndicator.invalidate()
            pagerIndicator.visibility = View.VISIBLE
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onNextClick(view: View?) {
        nextPage()
    }

    private fun sendResults() {
        val results = SparseArray<String>()
        val resultIntent = Intent()
        var resultBitmap: Bitmap? = null
        if (resultPhotoFragment[pageIndex.getPhotoPageIndex(0)] != null) {
            imageFileName = UUID.randomUUID().toString() + ".jpg"
            val resultImagePath = resultPhotoFragment[pageIndex.getPhotoPageIndex(0)].imageFileName
            val bitmap1 = BitmapFactory.decodeFile(resultImagePath)
            if (resultPhotoFragment[pageIndex.getPhotoPageIndex(1)] != null) {
                val result1ImagePath =
                    resultPhotoFragment[pageIndex.getPhotoPageIndex(1)].imageFileName
                var bitmap2 = BitmapFactory.decodeFile(result1ImagePath)
                if (bitmap1 != null && bitmap2 != null) {
                    if (abs(bitmap1.width - bitmap2.width) > 50) {
                        bitmap2 = BitmapUtils.RotateBitmap(bitmap2, 90f)
                    }
                    resultBitmap = if (bitmap1.width > bitmap1.height) {
                        BitmapUtils.concatTwoBitmapsHorizontal(bitmap1, bitmap2)
                    } else {
                        BitmapUtils.concatTwoBitmapsVertical(bitmap1, bitmap2)
                    }
                    bitmap1.recycle()
                    bitmap2!!.recycle()
                    File(result1ImagePath).delete()
                    File(resultImagePath).delete()
                }
            } else {
                resultBitmap = bitmap1
            }
        }
        val mpnValue = TestConfigHelper.getMpnValueForKey(
            viewModel.result1, testInfo!!.sampleQuantity
        )
        val mpnTcValue = TestConfigHelper.getMpnValueForKey(
            viewModel.result2, testInfo!!.sampleQuantity
        )
        results.put(
            1, StringUtil.getStringResourceByName(
                this,
                mpnValue.riskCategory, "en"
            ).toString()
        )
        results.put(2, mpnValue.mpn)
        results.put(3, mpnValue.confidence.toString())
        results.put(4, mpnTcValue.mpn)
        results.put(5, mpnTcValue.confidence.toString())
        val resultJson = TestConfigHelper.getJsonResult(
            this, testInfo,
            results, null, imageFileName
        )
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString())
        if (imageFileName!!.isNotEmpty() && resultBitmap != null) {
            resultIntent.putExtra(SensorConstants.IMAGE, imageFileName)
            val stream = ByteArrayOutputStream()
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            resultBitmap.recycle()
            resultIntent.putExtra(SensorConstants.IMAGE_BITMAP, stream.toByteArray())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onPhotoTaken() {
        nextPage()
    }

    class IncubationTimesDialogFragment : DialogFragment() {
        @SuppressLint("InflateParams")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            val inflater = requireActivity().layoutInflater
            builder.setView(
                inflater.inflate(
                    R.layout.dialog_incubation_times,
                    null
                )
            ).setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            return builder.create()
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private lateinit var fragmentInstructionBinding: FragmentInstructionBinding
        var instruction: Instruction? = null
        private var showButton: ButtonType? = null
        private var resultLayout: LinearLayout? = null
        private var viewRoot: ViewGroup? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            fragmentInstructionBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_instruction, container, false
            )

            viewRoot = container
            if (arguments != null) {
                instruction = requireArguments().getParcelable(ARG_SECTION_NUMBER)
                showButton = requireArguments().getSerializable(ARG_SHOW_OK) as ButtonType?
                fragmentInstructionBinding.instruction = instruction
            }
            val view = fragmentInstructionBinding.root

            view.findViewById<View>(R.id.buttonSubmit).setOnClickListener {
                (requireActivity() as CbtActivity).sendResults()
            }

            view.findViewById<View>(R.id.buttonClose).setOnClickListener {
                requireActivity().setResult(RESULT_FIRST_USER, Intent())
                requireActivity().finish()
            }

            when (showButton) {
                ButtonType.START -> view.findViewById<View>(R.id.buttonStart).visibility =
                    View.VISIBLE
                ButtonType.CLOSE -> view.findViewById<View>(R.id.buttonClose).visibility =
                    View.VISIBLE
                ButtonType.SUBMIT -> view.findViewById<View>(R.id.buttonSubmit).visibility =
                    View.VISIBLE
            }
            resultLayout = view.findViewById(R.id.layout_results)
            return view
        }

        fun setResult(testInfo: TestInfo?) {
            if (testInfo != null) {
                val inflater = requireActivity()
                    .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                resultLayout!!.removeAllViews()
                val results = SparseArray<String>()
                results.put(1, testInfo.results[0].resultValue.toString())
                results.put(2, testInfo.results[1].resultValue.toString())
                for (result in testInfo.results) {
                    val valueString = ResultUtils.createValueUnitString(
                        result.resultValue, result.unit,
                        getString(R.string.no_result)
                    )
                    val itemResult: LinearLayout = inflater.inflate(
                        R.layout.item_result,
                        viewRoot, false
                    ) as LinearLayout
                    val textTitle = itemResult.findViewById<TextView>(R.id.text_title)
                    textTitle.text = result.name
                    val textResult = itemResult.findViewById<TextView>(R.id.text_result)
                    textResult.text = valueString
                    resultLayout!!.addView(itemResult)
                }
                resultLayout!!.visibility = View.VISIBLE
            }
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"
            private const val ARG_SHOW_OK = "show_ok"

            /**
             * Returns a new instance of this fragment for the given section number.
             *
             * @param instruction The information to to display
             * @param showButton  The button to be shown
             * @return The instance
             */
            fun newInstance(
                instruction: Instruction?,
                showButton: ButtonType?
            ): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putParcelable(ARG_SECTION_NUMBER, instruction)
                args.putSerializable(ARG_SHOW_OK, showButton)
                fragment.arguments = args
                return fragment
            }
        }
    }

    /**
     * A [SectionsPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    internal inner class SectionsPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(
        fm!!
    ) {
        override fun getItem(position: Int): Fragment {
            return if (pageIndex.resultIndex == position) {
                resultFragment!!
            } else if (pageIndex.getType(position) == PageType.INPUT) {
                if (inputFragment[position] == null) {
                    var key = "00000"
                    var useBlue = false
                    if (inputIndexes.size > 0) {
                        val firstFragmentId = inputIndexes[0]
                        if (cbtResultKeys[firstFragmentId] == null) {
                            cbtResultKeys.put(firstFragmentId, key)
                        }
                        key = cbtResultKeys[firstFragmentId]!!.replace("1", "2")
                        resultFragment!!.showResult()
                    }
                    if (inputFragment.size() > 0) {
                        useBlue = true
                    }
                    inputFragment.put(
                        position, CompartmentBagFragment.newInstance(
                            key, position,
                            instructionList[position], useBlue
                        )
                    )
                    inputIndexes.add(position)
                }
                inputFragment[position]
            } else if (pageIndex.getType(position) == PageType.PHOTO) {
                if (resultPhotoFragment[position] == null) {
                    resultPhotoFragment.put(
                        position, ResultPhotoFragment.newInstance(
                            "", instructionList[position], position
                        )
                    )
                }
                resultPhotoFragment[position]
            } else if (position == totalPageCount - 1) {
                if (testPhase == 2) {
                    PlaceholderFragment.newInstance(
                        instructionList[position], ButtonType.SUBMIT
                    )
                } else PlaceholderFragment.newInstance(
                    instructionList[position], ButtonType.CLOSE
                )
            } else {
                PlaceholderFragment.newInstance(
                    instructionList[position], ButtonType.NONE
                )
            }
        }

        override fun getCount(): Int {
            return totalPageCount
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }
}