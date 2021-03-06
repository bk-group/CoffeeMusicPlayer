package com.udeshcoffee.android.ui.common.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.udeshcoffee.android.R
import com.udeshcoffee.android.data.DataRepository
import com.udeshcoffee.android.extensions.playSong
import com.udeshcoffee.android.extensions.queueSong
import com.udeshcoffee.android.model.Song
import com.udeshcoffee.android.ui.main.detail.albumdetail.AlbumDetailFragment
import com.udeshcoffee.android.ui.main.detail.artistdetail.ArtistDetailFragment
import com.udeshcoffee.android.ui.main.editor.EditorFragment
import org.koin.android.ext.android.inject

/**
 * Created by Udathari on 9/27/2017.
 */
class SongLongDialog : androidx.fragment.app.DialogFragment() {

    private val dataRepository: DataRepository by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val song = this.arguments!!.getParcelable<Song>(ARGUMENT_SONG)
        if (song == null) {
            dialog?.dismiss()
        }

        val items = arrayOf(getString(R.string.action_play),
                getString(R.string.action_play_next),
                getString(R.string.action_queue),
                getString(R.string.action_go_to_album),
                getString(R.string.action_go_to_artist),
                getString(R.string.action_add_to_playlist),
                getString(R.string.action_edit),
                getString(R.string.action_delete),
                getString(R.string.action_add_to_favorites),
                getString(R.string.action_share))

        if (dataRepository.isFavoriteSync(song!!.id)) {
            items[8] = getString(R.string.action_rem_from_favorites)
        }

        val builder = AlertDialog.Builder(context!!)

        builder.setTitle(song.title).setItems(items) { _, which ->
            when (which) {
                0 -> playSong(0, ArrayList<Song>().also {
                    it.add(song)
                }, true)
                1 -> queueSong(ArrayList<Song>().also {
                    it.add(song)
                }, true)
                2 -> queueSong(ArrayList<Song>().also {
                    it.add(song)
                }, false)
                3 -> showAlbum(song)
                4 -> showArtist(song)
                5 -> {
                    val addToPlaylistDialog = AddToPlaylistDialog()
                    val bundle = Bundle()
                    val tempSongs = java.util.ArrayList<Song>()
                    tempSongs.add(song)
                    bundle.putParcelableArrayList(AddToPlaylistDialog.ARGUMENT_SONGS, tempSongs)
                    addToPlaylistDialog.arguments = bundle
                    fragmentManager?.let { addToPlaylistDialog.show(it, "AddToPlaylistDialog") }

                }
                6 -> showEditor(song)
                7 -> {
                    fragmentManager?.let { DeleteSongDialog.create(song).show(it, "DeleteSongPlaylistDialog") }
                }
                8 -> {
                    dataRepository.toggleFavorite(song.id) {
                        if (it) {
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                9 -> {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    val audioUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + song.id)
                    shareIntent.putExtra(Intent.EXTRA_STREAM, audioUri)
                    shareIntent.type = "audio/*"
                    startActivity(Intent.createChooser(shareIntent, "Send with"))
                }
            }
        }

        return builder.create()
    }


    private fun showAlbum(song: Song) {
        dismiss()
        findNavController().navigate(R.id.albumDetailFragment, AlbumDetailFragment.createBundle(song.getAlbum()))
    }

    private fun showEditor(song: Song) {
        dismiss()
        Log.d("SongLongDialog", "Show editor")
        if (view == null)
            Log.d("SongLongDialog", "Fuck")
        findNavController().navigate(R.id.editorFragment, EditorFragment.createBundle(song))
    }

    private fun showArtist(song: Song) {
        dismiss()
        findNavController().navigate(R.id.artistDetailFragment, ArtistDetailFragment.createBundle(song.getArtist()))
    }

    companion object {
        const val ARGUMENT_SONG = "ARGUMENT_SONG"
    }
}