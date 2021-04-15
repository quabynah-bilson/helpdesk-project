package io.helpdesk.view.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.helpdesk.R
import io.helpdesk.databinding.FragmentDashboardBinding
import io.helpdesk.view.home.DepthPageTransformer
import io.helpdesk.view.home.FaqsFragment
import io.helpdesk.view.home.TicketsFragment

/**
 * Dashboard UI for Admin
 */
class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {
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
