package com.login.ohc;

import com.login.ohc.client.LoginScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public class Ohc_LoginClient implements ClientModInitializer {
	// CustomPayload 记录类 - 与服务端保持一致
	public record SetPasswordRequestPayload(String message) implements class_8710 {
		public static final class_8710.class_9154<SetPasswordRequestPayload> ID = new class_8710.class_9154<>(class_2960.method_60655("ohc_login", "set_password_request"));
		public static final class_9139<class_9129, SetPasswordRequestPayload> CODEC = 
			class_9139.method_56434(class_9135.field_48554, SetPasswordRequestPayload::message, SetPasswordRequestPayload::new);
		
		@Override
		public class_8710.class_9154<? extends class_8710> method_56479() {
			return ID;
		}
	}
	
	public record LoginRequestPayload(String message) implements class_8710 {
		public static final class_8710.class_9154<LoginRequestPayload> ID = new class_8710.class_9154<>(class_2960.method_60655("ohc_login", "login_request"));
		public static final class_9139<class_9129, LoginRequestPayload> CODEC = 
			class_9139.method_56434(class_9135.field_48554, LoginRequestPayload::message, LoginRequestPayload::new);
		
		@Override
		public class_8710.class_9154<? extends class_8710> method_56479() {
			return ID;
		}
	}
	
	public record LoginResponsePayload(String message) implements class_8710 {
		public static final class_8710.class_9154<LoginResponsePayload> ID = new class_8710.class_9154<>(class_2960.method_60655("ohc_login", "login_response"));
		public static final class_9139<class_9129, LoginResponsePayload> CODEC = 
			class_9139.method_56434(class_9135.field_48554, LoginResponsePayload::message, LoginResponsePayload::new);
		
		@Override
		public class_8710.class_9154<? extends class_8710> method_56479() {
			return ID;
		}
	}

	@Override
	public void onInitializeClient() {
		// 注册 CustomPayload 类型
		PayloadTypeRegistry.playS2C().register(SetPasswordRequestPayload.ID, SetPasswordRequestPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(LoginRequestPayload.ID, LoginRequestPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(LoginResponsePayload.ID, LoginResponsePayload.CODEC);
		
		// 注册设置密码请求处理器
		ClientPlayNetworking.registerGlobalReceiver(SetPasswordRequestPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				class_310.method_1551().method_1507(new LoginScreen(true, payload.message()));
			});
		});
		
		// 注册登录请求处理器
		ClientPlayNetworking.registerGlobalReceiver(LoginRequestPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				class_310.method_1551().method_1507(new LoginScreen(false, payload.message()));
			});
		});
		
		// 注册登录响应处理器
		ClientPlayNetworking.registerGlobalReceiver(LoginResponsePayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				// 简化处理：直接显示消息或关闭界面
				if (payload.message().equals("登录成功！")) {
					// 登录成功，关闭当前界面
					if (class_310.method_1551().field_1755 instanceof LoginScreen) {
						class_310.method_1551().method_1507(null);
					}
				} else {
					// 登录失败，显示错误消息
					if (class_310.method_1551().field_1755 instanceof LoginScreen loginScreen) {
						loginScreen.setErrorMessage(payload.message());
					} else {
						// 如果当前没有登录界面，重新打开一个
						LoginScreen newScreen = new LoginScreen(false, "请输入密码");
						newScreen.setErrorMessage(payload.message());
						class_310.method_1551().method_1507(newScreen);
					}
				}
			});
		});
	}
}