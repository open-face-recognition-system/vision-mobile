package com.mariofronza.face_collection_app.ui.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.FragmentPhotosBinding
import com.mariofronza.face_collection_app.ui.handle
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch

class PhotosFragment : Fragment() {

    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private val adapter = PhotosAdapter { photo -> startActivity(CapturePhotoActivity.intent(requireContext(), photo)) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        binding.rvPhotos.adapter = adapter
        binding.rvPhotos.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            safeCall { requireContext().vision.api.photos() }
                .onSuccess(adapter::submit)
                .onFailure { requireActivity().handle(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
