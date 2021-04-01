package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import io.helpdesk.databinding.FragmentPostTicketBinding

class PostTicketFragment : Fragment() {
    private val args by navArgs<PostTicketFragmentArgs>()
    private var binding: FragmentPostTicketBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostTicketBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val question = args.question
        if (question == null) {
        } else {
            println("question => $question")
        }

    }
}