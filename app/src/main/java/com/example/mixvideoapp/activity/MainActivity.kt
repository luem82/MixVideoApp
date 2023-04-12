package com.example.mixvideoapp.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.mixvideoapp.R
import com.example.mixvideoapp.allxinfo.AllXInfoActivity
import com.example.mixvideoapp.databinding.ActivityMainBinding
import com.example.mixvideoapp.databinding.CustomDialogPasscodeBinding
import com.example.mixvideoapp.fragment.*
import com.example.mixvideoapp.hdtube.HDTubeActivity
import com.example.mixvideoapp.lesbian8.LesbianActivity
import com.example.mixvideoapp.model.room.MixRoomDatabase
import com.example.mixvideoapp.pornogids.PornogidsActivity
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.PreferenceManager
import com.example.mixvideoapp.xcuto.CutoActivity
import com.example.mixvideoapp.xvideos98.XVideos98Activity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var type: String

    companion object {
        lateinit var mixRoomDatabase: MixRoomDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)

        mixRoomDatabase = Room.databaseBuilder(
            this, MixRoomDatabase::class.java, "MixVideoTube.db"
        ).allowMainThreadQueries().build()

        initDrawer()
        binding.navMain.setNavigationItemSelectedListener(this)
        replaceFragment(
            resources.getString(R.string.str_nav_viet),
            Consts.TYPE_VIET,
            VideoFragment.newInstance(Consts.TYPE_VIET)
        )
        binding.navMain.setCheckedItem(R.id.nav_viet)
    }

    private fun replaceFragment(title: String, type: String, fragment: Fragment) {
        supportActionBar?.title = title
        this.type = type
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
        Log.e("TAG", "replaceFragment: ${this.type}")
    }

    private fun initDrawer() {
        setSupportActionBar(binding.toolBar)
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.str_open, R.string.str_close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        binding.toolBar.setNavigationIcon(R.drawable.ic_more)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.act_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                when (type) {
                    Consts.TYPE_VIET -> {
                        searchView!!.queryHint = "Nhập tiếng anh hoặc tiếng việt"
                        return true
                    }
                    else -> {
                        searchView!!.queryHint = "Nhập tiếng anh"
                        return true
                    }
                }
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView!!.queryHint = ""
                return true
            }
        })

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }

        if (searchView != null) {

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val key = query.trim()
                    if (!key.isNullOrEmpty()) {
                        searchVideo(key)
                        // hide keyboard
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(searchView.windowToken, 0)
                        searchView.clearFocus()
                        return true
                    } else {
                        return false
                    }
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })

        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchVideo(key: String) {
        var query = key.replace(" ", "+")
        val fragment = ListVideoFragment.newInstance(Consts.TYPE_SEARCH, type, query)
        replaceFragment("Tìm kiếm", type, fragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.root.isDrawerOpen(GravityCompat.START)) {
            binding.root.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_viet -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_viet),
                    Consts.TYPE_VIET,
                    VideoFragment.newInstance(Consts.TYPE_VIET)
                )
                return true
            }
            R.id.nav_en -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_en),
                    Consts.TYPE_EN,
                    VideoFragment.newInstance(Consts.TYPE_EN)
                )
                return true
            }
            R.id.nav_tran -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_tran),
                    Consts.TYPE_TRAN,
                    VideoFragment.newInstance(Consts.TYPE_TRAN)
                )
                return true
            }
            R.id.nav_ani -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_ani),
                    Consts.TYPE_ANI,
                    VideoFragment.newInstance(Consts.TYPE_ANI)
                )
                return true
            }
            R.id.nav_cate -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_cate),
                    Consts.TYPE_CATEGORY,
                    CategoryFragment.newInstance()
                )
                return true
            }
            R.id.nav_audio -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_audio),
                    Consts.TYPE_AUDIO,
                    ListAudioFragment.newInstance()
                )
                return true
            }
            R.id.nav_album -> {
                binding.root.closeDrawer(GravityCompat.START)
                replaceFragment(
                    resources.getString(R.string.str_nav_album),
                    Consts.TYPE_ALBUM,
                    AlbumFragment.newInstance()
                )
                return true
            }
            R.id.nav_cuto -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, CutoActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_les8 -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, LesbianActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_hdtube -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, HDTubeActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_allxinfo -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, AllXInfoActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_history -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, HistoryActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_favorite -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, FavoriteActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_pornogids -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, PornogidsActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            R.id.nav_xvideos98 -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, XVideos98Activity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return true
            }
            else -> {
                binding.root.closeDrawer(GravityCompat.START)
                showDialogPasscode()
                return true
            }
        }
    }

    private fun showDialogPasscode() {
        val view = layoutInflater.inflate(R.layout.custom_dialog_passcode, binding.root, false)
        val dialogBinding = CustomDialogPasscodeBinding.bind(view)
        var dialog = AppCompatDialog(this)
        dialog.setContentView(view)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
        dialog.show()

        val isLockChild = preferenceManager.getLockApp(Consts.PRE_KEY_LOCK)
        if (isLockChild) {
            dialogBinding.setPasscode.visibility = View.GONE
            dialogBinding.lockOpen.visibility = View.VISIBLE
        } else {
            dialogBinding.setPasscode.visibility = View.VISIBLE
            dialogBinding.lockOpen.visibility = View.GONE
        }

        dialogBinding.lockCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.lockSave.setOnClickListener {
            val pass = dialogBinding.lockInput.text.toString()
            if (pass.isNullOrEmpty()) {
                Toast.makeText(this, "Nhập mật mã", Toast.LENGTH_SHORT).show()
            } else {
                preferenceManager.putLockApp(Consts.PRE_KEY_LOCK, true)
                preferenceManager.putPassCode(Consts.PRE_KEY_PASS, pass)
                dialog.dismiss()
                startActivity(Intent(this, PasscodeActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }

        dialogBinding.lockOpen.setOnClickListener {
            preferenceManager.clearLockApp()
            dialog.dismiss()
            Toast.makeText(this, "Khóa trẻ em đã tắt", Toast.LENGTH_SHORT).show()
        }
    }
}