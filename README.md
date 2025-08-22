# OHC Login

为OHCraft服务器制作的登录验证系统。

## 🚀 快速开始

### 系统要求
- **Minecraft版本**: 1.21.8
- **Fabric Loader**: 0.17.2+
- **Java版本**: 21+
- **Fabric API**: 0.131.0+

### 安装步骤

1. **下载mod文件**
   ```
   ohc_login-2.5.0.jar
   ```

2. **安装到服务器**
   - 将jar文件放入服务器的`mods`文件夹
   - 确保已安装Fabric Loader和Fabric API

3. **启动服务器**
   - 首次启动会自动生成配置文件
   - 配置文件位置：`config/login_messages.json`

## 🎯 使用指南

### 玩家命令

| 命令 | 描述 |
|------|------|
| `/sp <密码>` | 设置密码（仅首次） |
| `/l <密码>` | 登录验证 |
| `/rp <旧密码> <新密码>` | 修改密码 |

### 管理员命令

| 命令 | 描述 |
|------|------|
| `/ohc-reload` | 重载配置文件 |
| `/dp <玩家名>` | 删除玩家密码 |
| `/drp <玩家名> <新密码>` | 修改玩家密码 |

### 假人控制命令
本mod修改了地毯假人的指令，并且阻止创建与服务器已有真实玩家昵称相同的假人。
| 命令 | 描述 | 注意事项 |
|------|------|----------|
| `/player <(bot_)名称> spawn` | 召唤假人 | 使用“bot_”前缀可让假人跳过登录验证 |
| `/player <bot_名称> <其余参数>` | 控制假人 | 您无法控制没有“bot_”前缀的假人 |

## ⚙️ 配置说明

### 主要配置选项
在您服务器config目录中的“login_messages.json”
```json
{
  "settings": {
    "max_login_attempts": 5,           // 最大登录尝试次数
    "login_cooldown_seconds": 300,     // 登录冷却时间（秒）
    "ban_duration_minutes": 60,        // 封停持续时间（秒）
    "password_min_length": 4,          // 密码最小长度
    "password_max_length": 20,         // 密码最大长度
    "enable_actionbar_hints": true,    // 启用ActionBar提示
    "enable_admin_notifications": true, // 启用管理员通知
    "enable_blindness_effect": true,   // 启用失明效果
    "blindness_effect_level": 2,       // 失明效果等级
    "blindness_effect_duration": 999999 // 失明效果持续时间
  }
}
```

## 🔧 技术特性

### 核心组件
- **PlayerDataManager**: 玩家数据管理
- **ServerEventHandler**: 服务器事件处理
- **PlayerRestrictionManager**: 玩家行为限制管理
- **LoginPacketHandler**: 登录数据包处理
- **MessageConfig**: 消息配置管理
- **LoggedPlayersConfig**: 已登录玩家记录管理

### Mixin注入点
- **PlayerChatMixin**: 聊天和命令拦截
- **PlayerItemMixin**: 物品使用和丢弃拦截
- **ServerPlayNetworkHandlerMixin**: 网络包处理拦截
- **PlayerEntityMixin**: 玩家实体行为拦截

## 📁 项目结构

```
src/main/java/com/login/ohc/
├── Ohc_Login.java                    // 主类
├── commands/                         // 命令处理
│   ├── ConfigCommands.java
│   └── LoginCommands.java
├── config/                          // 配置管理
│   ├── LoggedPlayersConfig.java
│   └── MessageConfig.java
├── data/                            // 数据管理
│   ├── PlayerData.java
│   └── PlayerDataManager.java
├── events/                          // 事件处理
│   └── ServerEventHandler.java
├── mixins/                          // Mixin注入
│   ├── PlayerChatMixin.java
│   ├── PlayerEntityMixin.java
│   ├── PlayerItemMixin.java
│   └── ServerPlayNetworkHandlerMixin.java
├── network/                         // 网络处理
│   └── LoginPacketHandler.java
├── restrictions/                    // 限制管理
│   └── PlayerRestrictionManager.java
└── utils/                          // 工具类
    └── PasswordUtils.java
```

## 🛠️ 开发构建

### 环境要求
- **JDK**: 21+
- **Gradle**: 8.0+
- **IDE**: IntelliJ IDEA 或 Eclipse

### 构建命令

```bash
# 编译项目
./gradlew build

# 生成开发环境
./gradlew genEclipseRuns
./gradlew genIdeaRuns

# 运行测试服务器
./gradlew runServer
```

### 开发调试

```bash
# 启用调试模式
./gradlew runServer --debug-jvm
```

## 🤝 欢迎贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目采用 [GPL-3.0](LICENSE) 许可证。
