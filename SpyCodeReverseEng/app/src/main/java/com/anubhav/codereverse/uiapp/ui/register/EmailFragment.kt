package com.anubhav.codereverse.uiapp.ui.register

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.modules.initPictureSelector
import com.anubhav.codereverse.uiapp.ui.common.coordinateBtnAndInputs
import kotlinx.android.synthetic.main.fragment_register_email.*

class EmailFragment : Fragment() {

    private var imageUri: Uri? = null
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String, password: String, imageUri: Uri?)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(next_btn, email_input, password_input)

        profile_picture.setOnClickListener {
            requireContext().initPictureSelector(enableCompress = true) {
                profile_picture.setImageURI(it)
                imageUri = it
            }
        }

        next_btn.setOnClickListener {
            val email = email_input.text.toString()
            val password = password_input.text.toString()
            mListener.onNext(email, password, imageUri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}