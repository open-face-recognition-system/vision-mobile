package com.mariofronza.face_collection_app.ui.classes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mariofronza.face_collection_app.api.Lecture
import com.mariofronza.face_collection_app.databinding.ClassItemBinding
import com.mariofronza.face_collection_app.ui.formatDay
import com.mariofronza.face_collection_app.ui.formatTime

class ClassesAdapter(private val onClick: (Lecture) -> Unit) : RecyclerView.Adapter<ClassesAdapter.Holder>() {

    class Holder(val binding: ClassItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var lectures: List<Lecture> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(lectures: List<Lecture>) {
        this.lectures = lectures
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ClassItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val lecture = lectures[position]
        with(holder.binding) {
            tvClassItemSubjectName.text = lecture.subject.name
            tvClassItemDate.text = formatDay(lecture.date)
            tvClassItemStartHour.text = formatTime(lecture.startHour)
            tvClassItemEndHour.text = formatTime(lecture.endHour)
            cvClass.setOnClickListener { onClick(lecture) }
        }
    }

    override fun getItemCount() = lectures.size
}
