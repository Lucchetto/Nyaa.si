package com.zhenxiang.nyaasi

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenxiang.nyaasi.db.LocalNyaaDbViewModel

class ViewedReleasesFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_viewed_releases, container, false)

        val localNyaaDbViewModel = ViewModelProvider(this).get(LocalNyaaDbViewModel::class.java)
        val releasesListAdapter = ReleasesListAdapter()
        releasesListAdapter.setFooterVisible(false)
        localNyaaDbViewModel.viewedReleases.observe(viewLifecycleOwner, {
            releasesListAdapter.setItems(it)
        })
        val viewedReleasesList = fragmentView.findViewById<RecyclerView>(R.id.viewed_releases_list)
        viewedReleasesList.layoutManager = LinearLayoutManager(fragmentView.context)
        viewedReleasesList.adapter = releasesListAdapter

        return fragmentView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ViewedReleasesFragment().apply {
            }
    }
}