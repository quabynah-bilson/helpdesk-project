package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import io.helpdesk.databinding.TicketsFragmentBinding


class TicketInfoFragment : Fragment() {

    private var binding: TicketsFragmentBinding? = null
    private val args by navArgs<TicketInfoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketsFragmentBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ticket = args.ticket
        println("ticket found => $ticket")
    }

}