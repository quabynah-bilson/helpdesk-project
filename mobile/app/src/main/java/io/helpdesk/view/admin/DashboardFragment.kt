package io.helpdesk.view.admin

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.helpdesk.R
import io.helpdesk.core.util.loadImage
import io.helpdesk.databinding.FragmentDashboardBinding
import io.helpdesk.view.home.DepthPageTransformer
import io.helpdesk.view.home.FaqsFragment
import io.helpdesk.view.home.TicketsFragment
import io.helpdesk.viewmodel.AuthViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Dashboard UI for Admin
 */
class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null
    private val userViewModel by activityViewModels<UsersViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        requireActivity().window?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.isStatusBarContrastEnforced = true
            }
        }
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.run {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.helpdesk_blue_800)
        }
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            binding?.run {
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
                                            findNavController().navigate(DashboardFragmentDirections.actionNavDashboardToNavLogin())
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
                val pagerAdapter = DashboardPagerAdapter(this@DashboardFragment)
                with(pager) {
                    adapter = pagerAdapter
                    setPageTransformer(DepthPageTransformer())
                }

                // link tab layout to pager
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    when (position) {
                        0 -> tab.text = getString(R.string.fragment_faqs)
                        1 -> tab.text = getString(R.string.fragment_tickets)
                        else -> tab.text = getString(R.string.fragment_live_chat)
                    }
                }.attach()

                executePendingBindings()
            }

        }


    }


}

/**
 * Pager adapter implementation
 */
class DashboardPagerAdapter constructor(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FaqsFragment()
            else -> TicketsFragment()
        }
    }
}
