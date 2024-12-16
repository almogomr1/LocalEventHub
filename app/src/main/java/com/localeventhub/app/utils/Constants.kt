package com.localeventhub.app.utils

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.localeventhub.app.R
import com.localeventhub.app.model.User

class Constants {

    companion object{
        var loggedUserId:String = ""
        var loggedUser: User? = null
        var alert: AlertDialog? = null

        fun showAlert(context: Context, message: String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                .create().show()
        }

        fun showAlert(context: Context, message: String,listener: DialogInterface.OnClickListener) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, which -> listener.onClick(dialog,which) }
                .create().show()
        }

        fun startLoading(context: Context) {
            if(alert == null){
                val builder = MaterialAlertDialogBuilder(context)
                val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
                builder.setView(layout)
                builder.setCancelable(false)
                alert = builder.create()
                alert?.show()
            }
        }

        fun dismiss() {
            alert?.dismiss()
        }
    }

}