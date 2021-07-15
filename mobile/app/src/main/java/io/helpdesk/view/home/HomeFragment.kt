package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import io.helpdesk.core.util.visible
import io.helpdesk.databinding.FragmentHomeBinding
import io.helpdesk.view.shared.FaqsFragment
import io.helpdesk.view.shared.TicketsFragment
import io.helpdesk.viewmodel.AuthViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

/**
 * home page
 *
 * ref -> https://developer.android.com/guide/navigation/navigation-swipe-view-2#add_tabs_using_a_tablayout
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private val userViewModel by activityViewModels<UsersViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onDestroyView() {
        requireActivity().window?.run {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
        super.onDestroyView()
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.run {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.helpdesk_blue_800)
        }
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            binding?.run {
                // setup FAB
                fabPostTicket.setOnClickListener {
                    Toast.makeText(requireContext(), "Hello word", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_post_ticket)
                }

                userViewModel.currentUser().collectLatest { user ->
                    if (user != null) {
                        currentUser = user
                        userAvatar.run {
                            setOnClickListener {
                                MaterialAlertDialogBuilder(requireContext()).apply {
                                    setTitle(getString(R.string.logout_prompt_title))
                                    setMessage(getString(R.string.logout_prompt_desc))
                                    setPositiveButton("yes") { dialog, _ ->
                                        run {
                                            // logout
                                            dialog.dismiss()
                                            authViewModel.logout()
                                            findNavController().navigate(
                                                HomeFragmentDirections
                                                    .actionNavHomeToNavWelcome()
                                            )
                                        }
                                    }
                                    setNegativeButton("no") { dialog, _ -> dialog.cancel() }
                                    create()
                                }.show()
                            }
                        }
                    }
                }

                // setup view pager
                val pagerAdapter = HomePagerAdapter(this@HomeFragment)
                with(pager) {
                    adapter = pagerAdapter
                    setPageTransformer(DepthPageTransformer())
                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            // hide FAB for chat screen
                            binding?.fabPostTicket?.isVisible = position != 2
                            super.onPageSelected(position)
                        }
                    })
                }

                // link tab layout to pager
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    when (position) {
                        0 -> tab.text = getString(R.string.fragment_faqs)
                        1 -> tab.text = getString(R.string.fragment_tickets)
                        else -> tab.text = getString(R.string.fragment_live_chat)
                    }
                }.attach()

                // handle back press action
                requireActivity().onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            val currentItem = pager.currentItem
                            if (currentItem == 0) {
                                MaterialAlertDialogBuilder(requireContext()).apply {
                                    setTitle(getString(R.string.leave_app_prompt_title))
                                    setMessage(getString(R.string.leave_app_prompt_content))
                                    setPositiveButton("yes") { dialog, _ ->
                                        run {
                                            // leave app
                                            dialog.dismiss()
                                            requireActivity().finish()
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

                executePendingBindings()
            }
        }
    }

}

/**
 * Pager adapter implementation
 */
class HomePagerAdapter constructor(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FaqsFragment()
            else -> TicketsFragment()
        }
    }
}

// region page transformers

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