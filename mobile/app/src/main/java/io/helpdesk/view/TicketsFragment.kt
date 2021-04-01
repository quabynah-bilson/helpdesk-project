package io.helpdesk.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.helpdesk.R
import io.helpdesk.viewmodel.TicketsViewModel

class TicketsFragment : Fragment() {

    companion object {
        fun newInstance() = TicketsFragment()
    }

    private lateinit var viewModel: TicketsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tickets_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TicketsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}