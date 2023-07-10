package com.example.happyplace.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplace.activities.AddHappyPlaceActivity
import com.example.happyplace.activities.MainActivity
import com.example.happyplace.database.DatabaseHandler
import com.example.happyplace.databinding.HappyPlaceRowBinding
import com.example.happyplace.models.HappyPlaceModel

class HappyPlacesAdapter(private val HappyPlacesList : ArrayList<HappyPlaceModel>):
    RecyclerView.Adapter<HappyPlacesAdapter.HappyPlaceHolder>() {

    private var onClickListener : OnClickListener ?= null
    var context : Context?= null

        class HappyPlaceHolder(binding: HappyPlaceRowBinding) :RecyclerView.ViewHolder(binding.root){
            val image = binding.ivImage
            val name = binding.tvTitle
            val description = binding.tvDescription
        }

    fun setOnClickListener(onClickListener : OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int , model : HappyPlaceModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlaceHolder {
        return HappyPlaceHolder(HappyPlaceRowBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: HappyPlaceHolder, position: Int) {
        context =holder.itemView.context
        val item = HappyPlacesList[position]
        if (holder is HappyPlaceHolder) {
            holder.image.setImageURI(Uri.parse(item.image))
            holder.name.text = item.title
            holder.description.text = item.description

            holder.itemView.setOnClickListener{
                if (onClickListener != null){
                    onClickListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return HappyPlacesList.size
    }

    fun notifyEditItem(activity : Activity,position: Int, requestCode : Int){
        val intent = Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,HappyPlacesList[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }

    fun notifyDeleteItem(position : Int){
        val dbHandler = DatabaseHandler(context!!)
        Log.i("Tag","position of item is${HappyPlacesList[position]}")
        val isdeleted = dbHandler.deleteHappyPlace(HappyPlacesList[position])
        if (isdeleted > 0){
            HappyPlacesList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

}

