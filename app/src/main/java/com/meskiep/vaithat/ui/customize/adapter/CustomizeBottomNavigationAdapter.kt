package com.meskiep.vaithat.ui.customize.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerDrawable
import com.meskiep.vaithat.core.extension.loadImage
import com.meskiep.vaithat.core.extension.tap
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.data.model.custom.NavigationModel
import com.meskiep.vaithat.databinding.ItemCustomizeBottomNavigationBinding

class CustomizeBottomNavigationAdapter : ListAdapter<NavigationModel, CustomizeBottomNavigationAdapter.BottomNavViewHolder>(DiffCallback) {
    var onItemClick: (Int) -> Unit = {}

    inner class BottomNavViewHolder(private val binding: ItemCustomizeBottomNavigationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NavigationModel, position: Int) = with(binding) {
            val shimmerDrawable = ShimmerDrawable().apply { setShimmer(DataLocal.shimmer) }

            vFocus.isVisible = item.isSelected
            Glide.with(root).load(item.imageNavigation).override(256, 256).placeholder(shimmerDrawable).into(imvImage)
            loadImage(item.imageNavigation, imvImage)

            root.tap { onItemClick.invoke(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavViewHolder {
        val binding = ItemCustomizeBottomNavigationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BottomNavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomNavViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<NavigationModel>() {
            override fun areItemsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}