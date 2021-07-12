package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

class Homew : Fragment() {
    private var mactivity: MainActivity3? = null
    var textView : TextView ? = null


    interface OpenSecondFrag {
        fun open(isKyc: Boolean)
    }

    private var openSecondFrag: OpenSecondFrag? = null
    var arg: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mactivity = activity as MainActivity3?



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_homew, container, false)
        val welcomeMessage: TextView = view.findViewById(R.id.startcon)
        welcomeMessage.setOnClickListener(View.OnClickListener {
            val intent = Intent(mactivity,RemoteActivity::class.java)
            startActivity(intent)
        })

        val status: TextView = view.findViewById(R.id.claimstatus)
        status.setOnClickListener(View.OnClickListener {
            val intent = Intent(mactivity,ClaimStatus::class.java)
            startActivity(intent)
        })



        // Inflate the layout for this fragment

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


//        var installmentsAdapter:PedingPaymentAdapter ? = null
//        var mLayoutManager = LinearLayoutManager(this @HomeActivity,
//        LinearLayoutManager.HORIZONTAL, false)
//        HomeFragment.rv_pending.setLayoutManager(mLayoutManager)
//        installmentsAdapter = PedingPaymentAdapter(this @HomeActivity,res !!.user.nondues)
//        HomeFragment.rv_pending.setAdapter(installmentsAdapter)
    }
//    public static HomeFragment newInstance(User firstArg) {

    //        HomeFragment f = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putSerializable("de", firstArg);
//        f.setArguments(args);
//        return f;
//    }
    companion object {
//        var rv_pending: RecyclerView? = null

        @JvmStatic
        fun newInstance(bundle: Bundle?) =
            Homew().apply {
                arguments = bundle
            }
    }
}
