package com.login.ohc;

import com.login.ohc.commands.ConfigCommands;
import com.login.ohc.commands.LoginCommands;
import com.login.ohc.data.PlayerDataManager;
import com.login.ohc.events.ServerEventHandler;
import com.login.ohc.network.LoginPacketHandler;
import com.login.ohc.restrictions.PlayerRestrictionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ohc_Login implements ModInitializer {
	public static final String MOD_ID = "ohc_login";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-loadable state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing Ohc_Login mod...");
		
		// 初始化玩家数据管理器将在服务器启动时进行
		
		// 注册服务端事件
		ServerEventHandler.registerEvents();
		
		// 注册网络包处理器
		LoginPacketHandler.registerPacketHandlers();
		
		// 注册玩家行为限制
		PlayerRestrictionManager.registerRestrictions();
		
		// 注册登录命令
        LoginCommands.registerCommands();
        
        // 注册配置管理命令
        ConfigCommands.registerCommands();
		
		// 注册服务器生命周期事件
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// 服务器关闭时保存所有数据
			PlayerDataManager.getInstance().saveAllData();
			LOGGER.info("Ohc_Login mod data saved on server shutdown");
		});

		LOGGER.info("Ohc_Login mod initialized successfully!");
	}
}