package com.udeshcoffee.android.ui.main.library.nested

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import com.udeshcoffee.android.R
import com.udeshcoffee.android.data.MediaRepository
import com.udeshcoffee.android.doSharedTransaction
import com.udeshcoffee.android.interfaces.OnGridItemClickListener
import com.udeshcoffee.android.model.Artist
import com.udeshcoffee.android.openCollectionLongDialog
import com.udeshcoffee.android.recyclerview.EmptyRecyclerView
import com.udeshcoffee.android.recyclerview.GridItemDecor
import com.udeshcoffee.android.ui.adapters.ArtistAdapter
import com.udeshcoffee.android.ui.main.detail.artistdetail.ArtistDetailFragment
import com.udeshcoffee.android.ui.detail.albumdetail.ArtistDetailPresenter
import com.udeshcoffee.android.ui.main.MainActivity
import com.udeshcoffee.android.utils.Injection
import com.udeshcoffee.android.utils.SortManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by Udathari on 8/27/2017.
 */

class ArtistFragment : Fragment() {

    val TAG = "ArtistFragment"

    private lateinit var mediaRepository: MediaRepository

    private var disposable : Disposable? = null
    private var artistAdpt : ArtistAdapter? = null

    private var sortOrder: Int
        get() = SortManager.artistSortOrder
        set(value) {SortManager.artistSortOrder = value}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.frag_linear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaRepository = Injection.provideMediaRepository(context!!.applicationContext)

        val albumView = view.findViewById<EmptyRecyclerView>(R.id.linear_list)
        // specify an adapter (see also next example)
        artistAdpt = ArtistAdapter(ArtistAdapter.ITEM_TYPE_NORMAL, Glide.with(context), true)
        albumView.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        albumView.setEmptyView(view.findViewById(R.id.empty_view))
        albumView.addItemDecoration(GridItemDecor(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
        albumView.hasFixedSize()
        albumView.setItemViewCacheSize(20)
        albumView.isDrawingCacheEnabled = true
        albumView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
        albumView.isNestedScrollingEnabled = false

        artistAdpt?.listener = object : OnGridItemClickListener {
            override fun onItemClick(position: Int, shareElement: View) {
                artistAdpt?.getItem(position)?.let { showDetailUI(it, shareElement) }
            }

            override fun onItemLongClick(position: Int) {
                artistAdpt?.getItem(position)?.let {
                    mediaRepository.getArtistSongs(it.id)
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe({ songs ->
                                openCollectionLongDialog(it.name, songs)
                            })
                }
            }

            override fun onItemOptionClick() {

            }
        }
        albumView.adapter = artistAdpt
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.artist_sort, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (menu != null) {
            when (sortOrder) {
                SortManager.ArtistSort.DEFAULT -> menu.findItem(R.id.action_sort_default).isChecked = true
                SortManager.ArtistSort.NAME -> menu.findItem(R.id.action_sort_title).isChecked = true
            }
            menu.findItem(R.id.action_sort_ascending).isChecked = SortManager.artistsAscending
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var sortChanged = true

        when (item?.itemId) {
            R.id.action_sort_default -> sortOrder = SortManager.ArtistSort.DEFAULT
            R.id.action_sort_title -> sortOrder = SortManager.ArtistSort.NAME
            R.id.action_sort_ascending -> { SortManager.artistsAscending = !item.isChecked }
            else -> sortChanged = false
        }

        if (sortChanged) {
            fetchData()
            activity?.supportInvalidateOptionsMenu()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    override fun onPause() {
        super.onPause()
        dispose()
    }

    private fun fetchData() {
        dispose()
        disposable = mediaRepository.getArtists()
                .observeOn(AndroidSchedulers.mainThread())
                .map({ artists ->
                    SortManager.sortArtists(artists)

                    if (!SortManager.artistsAscending) {
                        Collections.reverse(artists)
                    }

                    return@map artists
                })
                .subscribe{artistAdpt?.accept(it)}
    }

    fun dispose(){
        disposable?.let {
            if (!it.isDisposed)
                it.dispose()
        }
    }

    fun showDetailUI(detail: Artist, shareElement: View) {
        val detailFragment = activity!!.supportFragmentManager.findFragmentByTag(MainActivity.Fragments.ARTIST_DETAIL)
                as ArtistDetailFragment? ?: ArtistDetailFragment()
        ArtistDetailPresenter(detail, detailFragment, Injection.provideMediaRepository(context!!.applicationContext),
                Injection.provideDataRepository(context!!.applicationContext))
        doSharedTransaction(R.id.main_container, detailFragment, MainActivity.Fragments.ARTIST_DETAIL, detail)
    }

}