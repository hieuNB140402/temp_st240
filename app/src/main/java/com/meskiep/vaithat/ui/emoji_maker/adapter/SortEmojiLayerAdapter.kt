package com.meskiep.vaithat.ui.emoji_maker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.data.model.SortEmojiLayerModel
import com.meskiep.vaithat.data.model.draw.Draw
import com.meskiep.vaithat.data.model.draw.DrawableDraw
import com.meskiep.vaithat.databinding.ItemEmojiMakerLayerSortBinding

class SortEmojiLayerAdapter : ListAdapter<SortEmojiLayerModel, SortEmojiLayerAdapter.SortEmojiLayerViewHolder>(SortEmojiLayerDiffCallback) {

    val onVisibilityToggled: ((DrawableDraw) -> Unit) = {}
    val onLockToggled: ((DrawableDraw) -> Unit) = {}
    val onDeleteClicked: ((DrawableDraw) -> Unit) = {}
    val onLayerSelected: ((DrawableDraw) -> Unit) = {}
    var onLockClick: ((DrawableDraw, Int) -> Unit) = {_,_ ->}
    var onVisibleClick: ((DrawableDraw, Int) -> Unit) = {_,_ ->}

    inner class SortEmojiLayerViewHolder(private val binding: ItemEmojiMakerLayerSortBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SortEmojiLayerModel) {
            binding.apply {
                val resIsVisible = if (!item.drawable.isHide) R.drawable.ic_emoji_maker_show else R.drawable.ic_emoji_maker_hide
                val resIsLock = if (item.drawable.isLock) R.drawable.ic_emoji_maker_locked else R.drawable.ic_emoji_maker_unlock
                val resSelected = if (item.drawable.isSelect) R.color.yellow_FBE46E else R.color.transparent

                btnShow.setImageResource(resIsVisible)
                btnLock.setImageResource(resIsLock)
                main.setBackgroundResource(resSelected)

                imvImage.setImageDrawable(item.drawable.drawable)

                btnLock.tap { onLockClick.invoke(item.drawable, bindingAdapterPosition) }
                btnShow.tap { onVisibleClick.invoke(item.drawable, bindingAdapterPosition) }
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SortEmojiLayerViewHolder {
        return SortEmojiLayerViewHolder(ItemEmojiMakerLayerSortBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(p0: SortEmojiLayerViewHolder, p1: Int) {
        p0.bind(getItem(p1))
    }

    private object SortEmojiLayerDiffCallback : DiffUtil.ItemCallback<SortEmojiLayerModel>() {
        override fun areItemsTheSame(old: SortEmojiLayerModel, new: SortEmojiLayerModel) = old.id == new.id
        override fun areContentsTheSame(old: SortEmojiLayerModel, new: SortEmojiLayerModel) = old == new
    }
}