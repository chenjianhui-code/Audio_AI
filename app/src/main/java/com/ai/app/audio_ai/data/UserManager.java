package com.ai.app.audio_ai.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户管理类，用于处理用户登录状态和信息
 */
public class UserManager {
    private static UserManager instance;
    private final ExecutorService executorService;
    private User currentUser;
    
    // 添加LiveData支持
    private final MutableLiveData<User> _userLiveData = new MutableLiveData<>();
    private final LiveData<User> userLiveData = _userLiveData;

    private UserManager() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * 获取当前登录用户
     * @param context 上下文
     * @param callback 回调
     */
    public void getCurrentUser(Context context, UserCallback callback) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            UserDao userDao = db.userDao();
            User user = userDao.getLoggedInUser();
            currentUser = user;
            // 更新LiveData
            _userLiveData.postValue(user);
            if (callback != null) {
                callback.onUserLoaded(user);
            }
        });
    }

    /**
     * 登录用户
     * @param context 上下文
     * @param username 用户名
     * @param password 密码
     * @param callback 回调
     */
    public void login(Context context, String username, String password, LoginCallback callback) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            UserDao userDao = db.userDao();
            User user = userDao.login(username, password);
            if (user != null) {
                // 更新登录状态
                user.setLoggedIn(true);
                userDao.update(user);
                currentUser = user;
                // 更新LiveData
                _userLiveData.postValue(user);
                if (callback != null) {
                    callback.onLoginSuccess(user);
                }
            } else {
                if (callback != null) {
                    callback.onLoginFailed("用户名或密码错误");
                }
            }
        });
    }

    /**
     * 注销用户
     * @param context 上下文
     * @param callback 回调
     */
    public void logout(Context context, LogoutCallback callback) {
        if (currentUser == null) {
            if (callback != null) {
                callback.onLogoutSuccess();
            }
            return;
        }

        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            UserDao userDao = db.userDao();
            currentUser.setLoggedIn(false);
            userDao.update(currentUser);
            currentUser = null;
            // 更新LiveData
            _userLiveData.postValue(null);
            if (callback != null) {
                callback.onLogoutSuccess();
            }
        });
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 获取用户LiveData，用于观察用户状态变化
     * @return 用户LiveData
     */
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }
    
    /**
     * 检查用户是否已登录
     * @return 如果用户已登录则返回true，否则返回false
     */
    public boolean isLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }

    /**
     * 用户回调接口
     */
    public interface UserCallback {
        void onUserLoaded(User user);
    }

    /**
     * 登录回调接口
     */
    public interface LoginCallback {
        void onLoginSuccess(User user);
        void onLoginFailed(String errorMessage);
    }

    /**
     * 注销回调接口
     */
    public interface LogoutCallback {
        void onLogoutSuccess();
    }

    /**
     * 注册回调接口
     */
    public interface RegisterCallback {
        void onRegisterSuccess(User user);
        void onRegisterFailed(String errorMessage);
    }

    /**
     * 注册用户
     * @param context 上下文
     * @param user 用户信息
     * @param callback 回调
     */
    public void register(Context context, User user, RegisterCallback callback) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            UserDao userDao = db.userDao();
            
            // 检查用户名是否已存在
            if (userDao.getUserByUsername(user.getUsername()) != null) {
                if (callback != null) {
                    callback.onRegisterFailed("用户名已存在");
                }
                return;
            }

            try {
                // 插入新用户
                long userId = userDao.insert(user);
                if (userId > 0) {
                    user.setId((int) userId);
                    if (callback != null) {
                        callback.onRegisterSuccess(user);
                    }
                } else {
                    if (callback != null) {
                        callback.onRegisterFailed("注册失败");
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onRegisterFailed("注册失败: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 关闭执行器服务
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}