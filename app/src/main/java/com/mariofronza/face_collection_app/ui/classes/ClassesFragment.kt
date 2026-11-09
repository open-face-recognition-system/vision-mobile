package com.mariofronza.face_collection_app.ui.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.FragmentClassesBinding
import com.mariofronza.face_collection_app.ui.handle
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch

class ClassesFragment : Fragment() {

    private var _binding: FragmentClassesBinding? = null
    private val binding get() = _binding!!
    private val adapter = ClassesAdapter { lecture -> startActivity(RecognizeActivity.intent(requireContext(), lecture)) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClassesBinding.inflate(inflater, container, false)
        binding.rvClasses.adapter = adapter
        binding.rvClasses.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            safeCall { requireContext().vision.api.lectures() }
                .onSuccess { page -> adapter.submit(page.data.sortedByDescending { it.date + it.startHour }) }
                .onFailure { requireActivity().handle(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
