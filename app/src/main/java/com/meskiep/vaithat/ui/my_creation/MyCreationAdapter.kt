package com.meskiep.vaithat.ui.my_creation

import android.R.attr.path
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.gone
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.visible
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.data.model.MyCreationModel
import com.meskiep.vaithat.databinding.ItemMyCreationBinding

class MyCreationAdapter(val context: Context, val creationType: Int) :
    ListAdapter<MyCreationModel, MyCreationAdapter.MyCreationViewHolder>(MyCreationDiffCallback()) {
    var onItemClick: (MyCreationModel) -> Unit = {}
    var onItemLongClick: (Int) -> Unit = {}
    var onItemSelectClick: (Int) -> Unit = {}

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyCreationViewHolder {
        return MyCreationViewHolder(ItemMyCreationBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun onBindViewHolder(p0: MyCreationViewHolder, p1: Int) {
        p0.bind(getItem(p1))
    }

    override fun onBindViewHolder(holder: MyCreationViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {

            val combinedPayloads = payloads
                .filterIsInstance<List<MyCreationPayload>>()
                .flatten()

            val item = getItem(position)

            combinedPayloads.forEach { payload ->
                when (payload) {

                    is MyCreationPayload.SelectedChanged -> {
                        holder.updateSelected(payload.isSelected)
                    }

                    is MyCreationPayload.ShowSelectChanged -> {
                        holder.updateShowSelection(payload.isShowSelection)
                    }
                }
            }

            return
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    inner class MyCreationViewHolder(val binding: ItemMyCreationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyCreationModel) {
            binding.apply {

                loadImage(item.thumbPath, imvImage)

                if (creationType == ValueKey.EDIT_CREATION) {
                    btnEdit.visible()
                } else {
                    btnEdit.gone()
                }

                updateSelected(item.isSelected)
                updateShowSelection(item.isShowSelection)

                root.setOnClickListener { onItemClick(item) }

                btnSelect.setOnClickListener { onItemSelectClick(bindingAdapterPosition) }

                root.setOnLongClickListener {
                    onItemLongClick(bindingAdapterPosition)
                    true
                }
            }

        }

        fun updateSelected(isSelected: Boolean) {
            val res = if (isSelected) R.drawable.ic_my_creation_selected else R.drawable.ic_my_creation_unselect
            binding.btnSelect.setImageResource(res)
            binding.vSelected.isVisible = isSelected
        }

        fun updateShowSelection(isShow: Boolean) {
            binding.btnSelect.isVisible = isShow
            binding.btnDelete.isVisible = !isShow
        }
    }

    class MyCreationDiffCallback : DiffUtil.ItemCallback<MyCreationModel>() {

        override fun areItemsTheSame(oldItem: MyCreationModel, newItem: MyCreationModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MyCreationModel, newItem: MyCreationModel): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: MyCreationModel, newItem: MyCreationModel): Any? {
            val payloads = mutableListOf<MyCreationPayload>()

            if (oldItem.isSelected != newItem.isSelected) {
                payloads.add(MyCreationPayload.SelectedChanged(newItem.isSelected))
            }

            if (oldItem.isShowSelection != newItem.isShowSelection) {
                payloads.add(MyCreationPayload.ShowSelectChanged(newItem.isShowSelection))
            }

            return if (payloads.isEmpty()) null else payloads
        }
    }

}