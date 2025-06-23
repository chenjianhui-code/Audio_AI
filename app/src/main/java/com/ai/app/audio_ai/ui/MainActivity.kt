package com.ai.app.audio_ai.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.ai.app.audio_ai.R
import com.ai.app.audio_ai.databinding.ActivityMainBinding
import com.ai.app.audio_ai.ui.category.CategoryFragment
import com.ai.app.audio_ai.ui.discover.DiscoverFragment
import com.ai.app.audio_ai.ui.home.HomeFragment
import com.ai.app.audio_ai.ui.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    
    // 缓存底部导航对应的Fragment实例
    private val homeFragment by lazy { HomeFragment() }
    private val discoverFragment by lazy { DiscoverFragment() }
    private val categoryFragment by lazy { CategoryFragment() }
    private val profileFragment by lazy { ProfileFragment() }
    
    // 当前显示的Fragment
    private var activeFragment: Fragment = homeFragment
    
    // 是否在主页面
    private var isInMainPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        
        // 检查是否已认证
        val isAuthenticated = false // TODO: Replace with actual auth check
        
        if (!isAuthenticated) {
            // 使用导航图中设置的起始目的地（欢迎界面）
            binding.mainContainer.visibility = View.GONE
            binding.navHostFragment.visibility = View.VISIBLE
            // 不需要手动导航，NavHostFragment会自动使用导航图中的起始目的地
            isInMainPage = false
        } else {
            // 已认证，设置底部导航和主页面
            binding.navHostFragment.visibility = View.GONE
            binding.mainContainer.visibility = View.VISIBLE
            setupBottomNavigation()
            isInMainPage = true
        }
        
        setupBackPressedHandler()
    }
    
    private fun setupBottomNavigation() {
        // 初始化所有Fragment
        supportFragmentManager.beginTransaction().apply {
            add(R.id.main_fragment_container, homeFragment, "home").hide(homeFragment)
            add(R.id.main_fragment_container, discoverFragment, "discover").hide(discoverFragment)
            add(R.id.main_fragment_container, categoryFragment, "category").hide(categoryFragment)
            add(R.id.main_fragment_container, profileFragment, "profile").hide(profileFragment)
            show(homeFragment)
            commit()
        }
        
        activeFragment = homeFragment
        
        // 设置底部导航点击事件
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.navigation_discover -> {
                    switchFragment(discoverFragment)
                    true
                }
                R.id.navigation_category -> {
                    switchFragment(categoryFragment)
                    true
                }
                R.id.navigation_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
        
        // 设置默认选中项
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }
    
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit()
        activeFragment = fragment
    }
    
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }
    
    // 为了兼容旧版本Android，同时实现onBackPressed方法
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBackPress()
    }
    
    // 统一处理回退逻辑
    private fun handleBackPress() {
        if (isInMainPage) {
            // 在主页面，显示退出提示
            showExitConfirmationDialog()
        } else {
            // 检查NavController是否可以回退
            val navController = navHostFragment.navController
            if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                navController.navigateUp()
            } else {
                // 如果已经在起始页面，显示退出提示
                showExitConfirmationDialog()
            }
        }
    }
    
    // 显示退出确认对话框
    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出应用")
            .setMessage("确定要退出应用吗？")
            .setPositiveButton("确定") { _, _ -> finish() }
            .setNegativeButton("取消", null)
            .show()
    }
    
    // 从登录/注册页面切换到主页面
    fun switchToMainPage() {
        binding.navHostFragment.visibility = View.GONE
        binding.mainContainer.visibility = View.VISIBLE
        setupBottomNavigation()
        isInMainPage = true
    }
}
