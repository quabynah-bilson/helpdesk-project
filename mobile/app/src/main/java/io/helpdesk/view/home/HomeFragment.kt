package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import io.helpdesk.core.util.visible
import io.helpdesk.databinding.FragmentHomeBinding
import kotlin.math.abs
import kotlin.math.max

/**
 * home page
 *
 * ref -> https://developer.android.com/guide/navigation/navigation-swipe-view-2#add_tabs_using_a_tablayout
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup view pager
        val pagerAdapter = HomePagerAdapter(this)
        binding?.pager?.adapter = pagerAdapter
        binding?.pager?.setPageTransformer(DepthPageTransformer())
        binding?.pager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // hide FAB for chat screen
                binding?.fabPostTicket?.visible(position != 2)
                super.onPageSelected(position)
            }
        })

        // link tab layout to pager
        TabLayoutMediator(binding!!.tabLayout, binding!!.pager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.fragment_faqs)
                1 -> tab.text = getString(R.string.fragment_tickets)
                else -> tab.text = getString(R.string.fragment_live_chat)
            }
        }.attach()

        // setup FAB
        binding?.fabPostTicket?.setOnClickListener { findNavController().navigate(R.id.nav_post_ticket) }

        // handle back press action
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentItem = binding?.pager?.currentItem
                    if (currentItem != null && currentItem == 0) {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(getString(R.string.leave_app_prompt_title))
                            setMessage(getString(R.string.leave_app_prompt_content))
                            setPositiveButton("yes") { dialog, _ ->
                                run {
                                    // leave app
                                    dialog.dismiss()
                                    findNavController().popBackStack(R.id.nav_welcome, true)
                                }
                            }
                            setNegativeButton("no") { dialog, _ -> dialog.cancel() }
                            create()
                        }.show()
                    } else {
                        // otherwise, select the initial page
                        binding?.pager?.currentItem = 0
                    }
                }
            })

    }

}

/**
 * Pager adapter implementation
 */
class HomePagerAdapter constructor(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FaqsFragment()
            1 -> TicketsFragment()
            else -> LiveChatFragment()
        }
    }
}

// region page transformers
/**
 * Zoom-out page transformer
 *
 * https://developer.android.com/training/animation/screen-slide-2
 */
class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}


@RequiresApi(21)
class DepthPageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.75f
    }

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Use the default slide transition when moving to the left page
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Fade the page out.
                    alpha = 1 - position

                    // Counteract the default slide transition
                    translationX = pageWidth * -position
                    // Move it behind the left page
                    translationZ = -1f

                    // Scale the page down (between MIN_SCALE and 1)
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}

// endregion