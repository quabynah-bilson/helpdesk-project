package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import io.helpdesk.databinding.FragmentWelcomeBinding

/**
 * welcome page
 */
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var binding: FragmentWelcomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle button clicks
        // todo -> nav to login
        binding?.authButton?.setOnClickListener { findNavController().navigate(R.id.nav_user_type) }
        binding?.skipButton?.setOnClickListener {
            findNavController().navigate(
                WelcomeFragmentDirections.actionNavWelcomeToNavHome()
            )
        }
    }

}