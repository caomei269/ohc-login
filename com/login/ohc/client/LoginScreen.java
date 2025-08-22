package com.login.ohc.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

/**
 * 登录界面
 * 用于玩家输入密码或设置新密码
 */
public class LoginScreen extends class_437 {
    
    // CustomPayload 记录类 - 客户端到服务端的密码响应
    public record ClientPasswordResponsePayload(String password, boolean isSettingPassword) implements class_8710 {
        public static final class_8710.class_9154<ClientPasswordResponsePayload> ID = new class_8710.class_9154<>(class_2960.method_60655("ohc_login", "client_password_response"));
        public static final class_9139<class_9129, ClientPasswordResponsePayload> CODEC = 
            class_9139.method_56435(
                class_9135.field_48554, ClientPasswordResponsePayload::password,
                class_9135.field_48547, ClientPasswordResponsePayload::isSettingPassword,
                ClientPasswordResponsePayload::new
            );
        
        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }
    
    // 静态初始化块 - 注册客户端到服务端的 CustomPayload
    static {
        PayloadTypeRegistry.playC2S().register(ClientPasswordResponsePayload.ID, ClientPasswordResponsePayload.CODEC);
    }
    
    private class_342 passwordField;
    private class_4185 confirmButton;
    private class_4185 cancelButton;
    
    private final boolean isSettingPassword;
    private final String promptMessage;
    private String errorMessage = "";
    
    public LoginScreen(boolean isSettingPassword, String promptMessage) {
        super(class_2561.method_43470(isSettingPassword ? "设置密码" : "登录"));
        this.isSettingPassword = isSettingPassword;
        this.promptMessage = promptMessage;
    }
    
    @Override
    protected void method_25426() {
        super.method_25426();
        
        int centerX = this.field_22789 / 2;
        int centerY = this.field_22790 / 2;
        
        // 密码输入框
        this.passwordField = new class_342(this.field_22793, centerX - 100, centerY - 10, 200, 20, class_2561.method_43470("密码"));
        this.passwordField.method_1880(20);
        this.passwordField.method_47404(class_2561.method_43470(isSettingPassword ? "请输入新密码 (4-20字符)" : "请输入密码"));
        this.passwordField.method_25365(true);
        this.method_25429(this.passwordField);
        
        // 确认按钮
        this.confirmButton = class_4185.method_46430(
                class_2561.method_43470(isSettingPassword ? "设置密码" : "登录"),
                button -> _onConfirm()
        ).method_46434(centerX - 100, centerY + 30, 95, 20).method_46431();
        this.method_37063(this.confirmButton);
        
        // 取消按钮（仅在设置密码时显示）
        if (isSettingPassword) {
            this.cancelButton = class_4185.method_46430(
                    class_2561.method_43470("取消"),
                    button -> _onCancel()
            ).method_46434(centerX + 5, centerY + 30, 95, 20).method_46431();
            this.method_37063(this.cancelButton);
        }
    }
    
    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        // 绘制半透明背景
        context.method_25294(0, 0, this.field_22789, this.field_22790, 0x80000000);
        
        super.method_25394(context, mouseX, mouseY, delta);
        
        int centerX = this.field_22789 / 2;
        int centerY = this.field_22790 / 2;
        
        // 绘制背景框
        context.method_25294(centerX - 120, centerY - 60, centerX + 120, centerY + 80, 0xC8404040);
        context.method_49601(centerX - 120, centerY - 60, 240, 140, 0xFF808080);
        
        // 绘制标题
        class_2561 title = class_2561.method_43470(isSettingPassword ? "设置登录密码" : "请输入密码");
        context.method_27534(this.field_22793, title, centerX, centerY - 45, 0xFFFFFF);
        
        // 绘制提示消息
        if (!promptMessage.isEmpty()) {
            context.method_27534(this.field_22793, class_2561.method_43470(promptMessage), centerX, centerY - 30, 0xCCCCCC);
        }
        
        // 绘制密码输入框
        this.passwordField.method_25394(context, mouseX, mouseY, delta);
        
        // 绘制错误消息
        if (!errorMessage.isEmpty()) {
            context.method_27534(this.field_22793, class_2561.method_43470(errorMessage), centerX, centerY + 55, 0xFF5555);
        }
        
        // 绘制密码长度提示
        if (isSettingPassword) {
            String lengthText = "密码长度: " + passwordField.method_1882().length() + "/20";
            int color = passwordField.method_1882().length() >= 4 ? 0x55FF55 : 0xFFAA00;
            context.method_27535(this.field_22793, class_2561.method_43470(lengthText), centerX - 100, centerY + 12, color);
        }
    }
    
    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        // Enter键确认
        if (keyCode == 257) { // GLFW_KEY_ENTER
            _onConfirm();
            return true;
        }
        
        // Escape键取消（仅在设置密码时）
        if (keyCode == 256 && isSettingPassword) { // GLFW_KEY_ESCAPE
            _onCancel();
            return true;
        }
        
        return super.method_25404(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean method_25421() {
        return false; // 不暂停游戏
    }
    
    @Override
    public boolean method_25422() {
        return isSettingPassword; // 只有设置密码时才允许ESC关闭
    }
    
    /**
     * 确认按钮处理
     */
    private void _onConfirm() {
        String password = passwordField.method_1882().trim();
        
        // 验证密码
        if (password.isEmpty()) {
            setErrorMessage("密码不能为空！");
            return;
        }
        
        if (isSettingPassword && password.length() < 4) {
            setErrorMessage("密码长度至少4个字符！");
            return;
        }
        
        if (password.length() > 20) {
            setErrorMessage("密码长度不能超过20个字符！");
            return;
        }
        
        // 发送密码到服务端
        _sendPasswordToServer(password);
        
        // 关闭界面
        this.method_25419();
    }
    
    /**
     * 取消按钮处理
     */
    private void _onCancel() {
        this.method_25419();
    }
    
    /**
     * 发送密码到服务端
     */
    private void _sendPasswordToServer(String password) {
        ClientPasswordResponsePayload payload = new ClientPasswordResponsePayload(password, isSettingPassword);
        ClientPlayNetworking.send(payload);
    }
    
    /**
     * 设置错误消息
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }
    
    /**
     * 清除错误消息
     */
    public void clearErrorMessage() {
        this.errorMessage = "";
    }
}