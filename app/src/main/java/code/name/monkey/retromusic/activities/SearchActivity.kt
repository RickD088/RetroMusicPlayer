package code.name.monkey.retromusic.activities

import android.app.Activity
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.BufferType
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.abram.RemoteConfig
import code.name.monkey.retromusic.activities.base.AbsMusicServiceActivity
import code.name.monkey.retromusic.activities.base.AbsSlidingMusicPanelActivity
import code.name.monkey.retromusic.adapter.SearchAdapter
import code.name.monkey.retromusic.dialogs.RewardDialog
import code.name.monkey.retromusic.mvp.presenter.RewardPresenter
import code.name.monkey.retromusic.mvp.presenter.SearchPresenter
import code.name.monkey.retromusic.mvp.presenter.SearchView
import code.name.monkey.retromusic.rest.music.model.Prize
import code.name.monkey.retromusic.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.textfield.TextInputEditText
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.tag_text.view.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

class SearchActivity : AbsMusicServiceActivity(), OnQueryTextListener, TextWatcher, SearchView {
    @Inject
    lateinit var searchPresenter: SearchPresenter

    private var searchAdapter: SearchAdapter? = null
    private var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setLightNavigationBar(true)

        App.musicComponent.inject(this)
        searchPresenter.attachView(this)

        setupRecyclerView()
        setUpToolBar()
        setupSearchView()

        if (intent.getBooleanExtra(EXTRA_SHOW_MIC, false)) {
            startMicSearch()
        }

        back.setOnClickListener { onBackPressed() }
        voiceSearch.setOnClickListener { startMicSearch() }
        clearText.setOnClickListener { searchView.clearText() }
        searchContainer.backgroundTintList =
                ColorStateList.valueOf(ATHUtil.resolveColor(this, R.attr.colorSurface))

        keyboardPopup.setOnClickListener {
            searchView?.let {
                it.requestFocus()
                val inputManager = getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        keyboardPopup.backgroundTintList = ColorStateList.valueOf(ThemeStore.accentColor(this))
        ColorStateList.valueOf(
                MaterialValueHelper.getPrimaryTextColor(
                        this,
                        ColorUtil.isColorLight(ThemeStore.accentColor(this))
                )
        ).apply {
            keyboardPopup.setTextColor(this)
            keyboardPopup.iconTint = this
        }
        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY)
        }
    }


    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(this, mutableListOf()) { data ->
            searchAdapter?.let {
                data.loading = true
                it.getDataSet()?.let { dataSet -> it.notifyItemChanged(dataSet.indexOf(data)) }
                searchPresenter.loadMore(it.getOnlineSongsSize())
            }
        }
        searchAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                empty.visibility = if (tagFlowLayout.visibility == View.GONE
                        && searchAdapter!!.itemCount < 1) View.VISIBLE else View.GONE
            }
        })
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    keyboardPopup.shrink()
                } else if (dy < 0) {
                    keyboardPopup.extend()
                }
            }
        })
    }

    private fun setupSearchView() {
        searchView.addTextChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchPresenter.detachView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(QUERY, query)
    }

    private fun setUpToolBar() {
        title = null
    }

    private fun search(query: String) {
        this.query = query
        TransitionManager.beginDelayedTransition(appBarLayout)
        voiceSearch.visibility = if (query.isNotEmpty()) View.GONE else View.VISIBLE
        clearText.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        tagFlowLayout.visibility = if (query.isEmpty()) View.VISIBLE else View.GONE
        searchPresenter.search(query)
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        query?.let { search(it) }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        hideSoftKeyboard()
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        search(newText)
        return false
    }

    private fun hideSoftKeyboard() {
        RetroUtil.hideSoftKeyboard(this@SearchActivity)
        if (searchView != null) {
            searchView.clearFocus()
        }
    }

    override fun showEmptyView() {
        searchAdapter?.swapDataSet(ArrayList())
    }

    override fun showData(data: MutableList<Any>) {
        searchAdapter?.swapDataSet(data)
    }

    override fun showKeywords(data: MutableList<Any>) {
        tagFlowLayout.visibility = View.VISIBLE
        tagFlowLayout.adapter = object : TagAdapter<String>(data as List<String>) {
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val button = this@SearchActivity.layoutInflater.inflate(R.layout.tag_text, tagFlowLayout, false) as ConstraintLayout
                val text: String
                val image: String
                if (t.isNullOrBlank()) {
                    text = ""; image = ""
                } else if (t.startsWith(Constants.SEPARATOR_SEARCH_KEYWORDS)) {
                    text = t.removePrefix(Constants.SEPARATOR_SEARCH_KEYWORDS); image = ""
                    button.tv.setTextColor(this@SearchActivity.resources.getColor(android.R.color.tab_indicator_text))
                } else if (t.contains(Constants.SEPARATOR_SEARCH_KEYWORDS)) {
                    val ts = t.split(Constants.SEPARATOR_SEARCH_KEYWORDS)
                    text = ts[0]; image = ts[1]
                } else {
                    text = t; image = ""
                }
                button.tv.text = text
                if (image.isNotBlank() && image.contains("http", ignoreCase = false)) {
                    Glide.with(App.getContext())
                            .load(image)
                            .asBitmap()
                            .error(R.drawable.ic_account_white_24dp)
                            .placeholder(R.drawable.ic_account_white_24dp)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .into(button.iv)
                    button.iv.visibility = View.VISIBLE
                }
                return button
            }
        }

        tagFlowLayout.setOnTagClickListener { _: View, i: Int, _: FlowLayout ->
            if (data.isNotEmpty() && data.size > i && data[i].isNotBlank()) {
                search(data[i])
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result: ArrayList<String>? =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    query = result?.get(0)
                    searchView.setText(query, BufferType.EDITABLE)
                    searchPresenter.search(query!!)
                }
            }
        }
    }

    private fun startMicSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(newText: CharSequence, start: Int, before: Int, count: Int) {
        search(newText.toString())
    }

    override fun afterTextChanged(s: Editable) {
    }

    override fun onBackPressed() {
        if (tagFlowLayout.visibility == View.VISIBLE) {
            super.onBackPressed()
        } else {
            showEmptyView()
            tagFlowLayout.visibility = View.VISIBLE
            empty.visibility = View.GONE
        }
    }

    companion object {
        val TAG: String = SearchActivity::class.java.simpleName

        const val EXTRA_SHOW_MIC = "extra_show_mic"
        const val QUERY: String = "query"

        private const val REQ_CODE_SPEECH_INPUT = 9002
    }
}

fun TextInputEditText.clearText() {
    text = null
}