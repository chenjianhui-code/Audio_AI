package com.ai.app.audio_ai.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 用户数据访问对象接口
 */
@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    User getLoggedInUser();

    @Query("UPDATE users SET isLoggedIn = :isLoggedIn WHERE id = :userId")
    void updateLoginStatus(int userId, boolean isLoggedIn);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}
