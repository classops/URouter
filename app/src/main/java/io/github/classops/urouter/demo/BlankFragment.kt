package io.github.classops.urouter.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.github.classops.urouter.Router
import io.github.classops.urouter.annotation.Param
import io.github.classops.urouter.annotation.Route

private const val ARG_CONTENT = "content"

@Route(path = "/frag/test", alias = [])
class BlankFragment : Fragment() {

    @Param(name = "content")
    var param1: String? = null

    @Param
    var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Router.get().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvContent).text = param1
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT, param1)
                }
            }
    }
}