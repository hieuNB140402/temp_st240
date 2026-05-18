package com.meskiep.vaithat.ui.emoji_maker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.data.model.SortEmojiLayerModel
import com.meskiep.vaithat.databinding.ItemEmojiMakerLayerSortBinding

class SortEmojiLayerAdapter :
    ListAdapter<SortEmojiLayerModel, SortEmojiLayerAdapter.SortEmojiLayerViewHolder>(SortEmojiLayerDiffCallback) {

    val onVisibilityToggled: ((SortEmojiLayerModel) -> Unit) = {}
    val onLockToggled: ((SortEmojiLayerModel) -> Unit) = {}
    val onDeleteClicked: ((SortEmojiLayerModel) -> Unit) = {}
    val onLayerSelected: ((SortEmojiLayerModel) -> Unit) = {}
    val onLayerReordered: ((SortEmojiLayerModel) -> Unit) = {}

    inner class SortEmojiLayerViewHolder(private val binding: ItemEmojiMakerLayerSortBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SortEmojiLayerModel) {

        }
    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): SortEmojiLayerViewHolder {
        return SortEmojiLayerViewHolder(ItemEmojiMakerLayerSortBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(
        p0: SortEmojiLayerViewHolder,
        p1: Int
    ) {
        p0.bind(getItem(p1))
    }

    private object SortEmojiLayerDiffCallback : DiffUtil.ItemCallback<SortEmojiLayerModel>() {
        override fun areItemsTheSame(old: SortEmojiLayerModel, new: SortEmojiLayerModel) = old.id == new.id
        override fun areContentsTheSame(old: SortEmojiLayerModel, new: SortEmojiLayerModel) = old == new
    }
}