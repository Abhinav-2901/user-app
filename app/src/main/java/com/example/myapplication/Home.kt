package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
class Home : Fragment() {
    var mactivity: MainActivity3? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mactivity = activity as MainActivity3?





    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val welcomeMessage: TextView = view.findViewById(R.id.startcon)
        welcomeMessage.setOnClickListener(View.OnClickListener {
            val intent = Intent(mactivity,RemoteActivity::class.java)
            startActivity(intent)
        })



        // Inflate the layout for this fragment
//        rv_pending = view.findViewById(R.id.rv_pending)
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
            Home().apply {
                arguments = bundle
            }
    }
}