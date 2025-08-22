package com.login.ohc.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import com.login.ohc.config.MessageConfig;

/**
 * 玩家登录数据类
 * 存储玩家的密码、登录状态、错误次数等信息
 */
public class PlayerData {
    private String playerName;
    private String hashedPassword;
    private String salt;
    private boolean isLoggedIn;
    private boolean hasPassword;
    private int failedAttempts;
    private long cooldownEndTime;
    private long banEndTime;
    private long lastLoginTime;
    
    public PlayerData(String playerName) {
        this.playerName = playerName;
        this.hashedPassword = null;
        this.salt = null;
        this.isLoggedIn = false;
        this.hasPassword = false;
        this.failedAttempts = 0;
        this.cooldownEndTime = 0;
        this.banEndTime = 0;
        this.lastLoginTime = 0;
    }
    
    /**
     * 设置密码（首次设置）
     */
    public void setPassword(String password) {
        this.salt = _generateSalt();
        this.hashedPassword = _hashPassword(password, this.salt);
        this.hasPassword = true;
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String password) {
        if (!hasPassword || hashedPassword == null || salt == null) {
            return false;
        }
        String inputHash = _hashPassword(password, this.salt);
        return hashedPassword.equals(inputHash);
    }
    
    /**
     * 生成随机盐值
     */
    private String _generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 使用SHA-256和盐值对密码进行哈希
     */
    private String _hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
    
    /**
     * 登录成功
     */
    public void loginSuccess() {
        this.isLoggedIn = true;
        this.failedAttempts = 0;
        this.cooldownEndTime = 0;
        this.lastLoginTime = System.currentTimeMillis();
    }
    
    /**
     * 登录失败
     */
    public void loginFailed() {
        this.failedAttempts++;
        
        MessageConfig config = MessageConfig.getInstance();
        int maxAttempts = config.getMaxLoginAttempts();
        int cooldownSeconds = config.getLoginCooldownSeconds();
        int banDurationMinutes = config.getBanDurationMinutes();
        
        // 连续两次错误：应用冷却时间
        if (failedAttempts == 2) {
            this.cooldownEndTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        }
        // 达到最大尝试次数：应用封停
        else if (failedAttempts >= maxAttempts) {
            this.banEndTime = System.currentTimeMillis() + (banDurationMinutes * 60 * 1000L);
        }
    }
    
    /**
     * 登出
     */
    public void logout() {
        this.isLoggedIn = false;
    }
    
    /**
     * 清除密码（管理员功能）
     */
    public void clearPassword() {
        this.hashedPassword = null;
        this.salt = null;
        this.hasPassword = false;
        this.isLoggedIn = false;
        this.failedAttempts = 0;
        this.cooldownEndTime = 0;
        this.banEndTime = 0;
    }
    
    /**
     * 检查是否在冷却期
     */
    public boolean isInCooldown() {
        return System.currentTimeMillis() < cooldownEndTime;
    }
    
    /**
     * 检查是否被封停
     */
    public boolean isBanned() {
        return System.currentTimeMillis() < banEndTime;
    }
    
    /**
     * 获取剩余冷却时间（秒）
     */
    public long getRemainingCooldownSeconds() {
        if (!isInCooldown()) return 0;
        return (cooldownEndTime - System.currentTimeMillis()) / 1000;
    }
    
    /**
     * 获取剩余封停时间（小时）
     */
    public long getRemainingBanHours() {
        if (!isBanned()) return 0;
        return (banEndTime - System.currentTimeMillis()) / (1000 * 60 * 60);
    }
    
    // Getters
    public String getPlayerName() { return playerName; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public boolean hasPassword() { return hasPassword; }
    public int getFailedAttempts() { return failedAttempts; }
    public long getLastLoginTime() { return lastLoginTime; }
    
    // Setters for serialization
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setLoggedIn(boolean loggedIn) { this.isLoggedIn = loggedIn; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public void setCooldownEndTime(long cooldownEndTime) { this.cooldownEndTime = cooldownEndTime; }
    public void setBanEndTime(long banEndTime) { this.banEndTime = banEndTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    
    public String getHashedPassword() { return hashedPassword; }
    public String getSalt() { return salt; }
    public long getCooldownEndTime() { return cooldownEndTime; }
    public long getBanEndTime() { return banEndTime; }
}