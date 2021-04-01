package io.helpdesk.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.helpdesk.R
import io.helpdesk.viewmodel.LiveChatViewModel

class LiveChatFragment : Fragment() {

    companion object {
        fun newInstance() = LiveChatFragment()
    }

    private lateinit var viewModel: LiveChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.live_chat_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LiveChatViewModel::class.java)
        // TODO: Use the ViewModel
    }

}