package com.udeshcoffee.android.ui.common.dialogs

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.udeshcoffee.android.R

/**
 * Created by Udathari on 10/17/2017.
 */
class RenamePlaylistDialog: androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = this.arguments!!.getString(ARGUMENT_TITLE)
        val playlistId = this.arguments!!.getLong(ARGUMENT_ID)

        val builder = AlertDialog.Builder(context!!)
        // Get the layout inflater
        val inflater = activity!!.layoutInflater

        val dialogView = inflater.inflate(R.layout.dialog_single_edittext, null)
        val playlist = dialogView.findViewById<EditText>(R.id.new_name)
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        playlist.setText(title)
        builder.setTitle("Rename $title")
                .setView(dialogView)
                // Add action buttons
                .setPositiveButton("Rename") { _, _ ->
                    val name = playlist.text.toString()
                    if (name != "") {
                        val playlistResolver = context!!.contentResolver
                        val mInserts = ContentValues()
                        mInserts.put(MediaStore.Audio.Playlists.NAME, name)
                        val where = MediaStore.Audio.Playlists._ID + "=?"
                        val whereVal = arrayOf("$playlistId")
                        playlistResolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts, where, whereVal)
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> this@RenamePlaylistDialog.dialog?.cancel() }
        return builder.create()
    }

    companion object {
        const val ARGUMENT_TITLE = "ARGUMENT_TITLE"
        const val ARGUMENT_ID = "ARGUMENT_ID"

        fun create(playlistId: Long, playlistTitle: String): RenamePlaylistDialog {
            val mDialog = RenamePlaylistDialog()
            val bundle1 = Bundle()
            bundle1.putLong(ARGUMENT_ID, playlistId)
            bundle1.putString(ARGUMENT_TITLE, playlistTitle)
            mDialog.arguments = bundle1
            return mDialog
        }
    }
}