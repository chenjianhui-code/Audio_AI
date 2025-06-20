package com.ai.app.audio_ai

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ai.app.audio_ai.data.User
import com.ai.app.audio_ai.data.UserManager
import com.ai.app.audio_ai.databinding.ActivityMainBinding
import com.ai.app.audio_ai.service.FloatingWindowManager
import com.ai.app.audio_ai.service.TextToSpeechService
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FloatingWindowManager.CommandListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var floatingWindowManager: FloatingWindowManager
    private lateinit var textToSpeechService: TextToSpeechService
    private var isFloatingWindowActive = false

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 初始化导航控制器
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 配置ActionBar
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_discover, R.id.navigation_profile)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 设置底部导航栏
        binding.bottomNavigation.setupWithNavController(navController)

        // 初始化TextToSpeechService
        textToSpeechService = TextToSpeechService(this)
        textToSpeechService.setOnInitListener {
            Log.d(TAG, "TextToSpeechService初始化完成")
        }

        // 设置悬浮按钮点击事件
        binding.fab.setOnClickListener { view ->
            if (!isFloatingWindowActive) {
                floatingWindowManager.show()
                isFloatingWindowActive = true
                Snackbar.make(view, "悬浮窗已显示", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            } else {
                floatingWindowManager.hide()
                isFloatingWindowActive = false
                Snackbar.make(view, "悬浮窗已隐藏", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        // 初始化浮动窗口管理器
        floatingWindowManager = FloatingWindowManager(this)
        floatingWindowManager.setCommandListener(this)

        // 观察用户状态变化
        UserManager.getInstance().getUserLiveData().observe(this) { user ->
            setupBottomNavigation(user)
        }
    }

    private fun setupBottomNavigation(user: User?) {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (user == null || !user.isLoggedIn) {
                // 如果用户未登录，阻止导航并显示提示
                Toast.makeText(this@MainActivity, "请先登录", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false
            }

            // 创建导航选项，避免创建重复的目标实例
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build()

            when (item.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        navController.navigate(R.id.navigation_home, null, navOptions)
                    }
                    true
                }
                R.id.navigation_discover -> {
                    if (navController.currentDestination?.id != R.id.navigation_discover) {
                        navController.navigate(R.id.navigation_discover, null, navOptions)
                    }
                    true
                }
                R.id.navigation_profile -> {
                    if (navController.currentDestination?.id != R.id.navigation_profile) {
                        navController.navigate(R.id.navigation_profile, null, navOptions)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingWindowManager.release()
        textToSpeechService.release()
    }

    override fun onCommandReceived(command: String) {
        // 处理从悬浮窗接收到的命令
        textToSpeechService.speak(command)
    }
}
