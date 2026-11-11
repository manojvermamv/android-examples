package com.android.interviewdemo.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ListItemBinding
import com.android.interviewdemo.model.models.User
import com.squareup.picasso.Picasso

class CustomListAdapter constructor(
    context: Context,
    list: ArrayList<User>
) : ArrayAdapter<User>(context, R.layout.list_item, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.tag as? ListItemBinding ?: ListItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )

        val item = getItem(position)
        if (item != null) {
            binding.tvTitle.text = item.getFullName()
            binding.tvTotalPrice.text = "${item.getPrice()} rs"
            Picasso.get().load(item.getImageUrl()).placeholder(R.drawable.img_not_available).error(R.drawable.img_not_available).into(binding.ivProfile)

            binding.ivMinus.setOnClickListener {
                var count = binding.tvCount.text.toString().toInt()
                if (count > 1) {
                    count -= 1
                    binding.tvCount.text = count.toString()
                    binding.tvTotalPrice.text = "${item.getPriceByCount(count)} rs"
                }
            }

            binding.ivPlus.setOnClickListener {
                var count = binding.tvCount.text.toString().toInt()
                if (count < 10) {
                    count += 1
                    binding.tvCount.text = count.toString()
                    binding.tvTotalPrice.text = "${item.getPriceByCount(count)} rs"
                }
            }
        }

        binding.root.tag = binding
        return binding.root
    }

}