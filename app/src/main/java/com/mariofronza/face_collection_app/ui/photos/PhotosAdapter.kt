package com.mariofronza.face_collection_app.ui.photos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mariofronza.face_collection_app.R
import com.mariofronza.face_collection_app.api.Photo
import com.mariofronza.face_collection_app.databinding.PhotoItemBinding
import com.mariofronza.face_collection_app.ui.PHOTO_TYPES
import com.mariofronza.face_collection_app.ui.formatTimestamp

class PhotosAdapter(private val onClick: (Photo) -> Unit) : RecyclerView.Adapter<PhotosAdapter.Holder>() {

    class Holder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var photos: List<Photo> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(photos: List<Photo>) {
        this.photos = photos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(PhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val photo = photos[position]
        with(holder.binding) {
            tvPhotoItemType.text = PHOTO_TYPES[photo.photoType] ?: photo.photoType
            tvPhotoItemDate.text = photo.updatedAt?.let(::formatTimestamp) ?: "Pendente"
            Glide.with(ivPhotoItem).load(photo.url).placeholder(R.drawable.no_profile).error(R.drawable.no_profile).into(ivPhotoItem)
            cvPhoto.setOnClickListener { onClick(photo) }
        }
    }

    override fun getItemCount() = photos.size
}
