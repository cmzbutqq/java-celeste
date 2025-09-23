# Java Celeste - 横版跳跃游戏

一个使用Java Swing开发的简单横版跳跃游戏，灵感来源于经典游戏Celeste。

## 游戏特性

- 🎮 流畅的横版移动和跳跃
- 🌍 简单的物理系统（重力和碰撞检测）
- 🏗️ 多个平台供玩家跳跃
- 🎨 简洁的像素风格图形
- ⌨️ 键盘控制

## 控制方式

- **左移**: A 键 或 左箭头键
- **右移**: D 键 或 右箭头键
- **上移**: W 键 或 上箭头键 (预留功能)
- **下移**: S 键 或 下箭头键 (预留功能)
- **跳跃**: K 键 或 空格键 或 J 键

> 💡 **自定义按键**: 可以修改 `src/KeyBindings.java` 文件来自定义按键绑定，详见 [KEYBINDINGS.md](KEYBINDINGS.md)

## 运行游戏

### 方法1: 使用Maven运行

```bash
# 编译项目
mvn compile

# 运行游戏
mvn exec:java
```

### 方法2: 直接运行Java文件

```bash
# 编译
javac -d target/classes src/*.java

# 运行
java -cp target/classes Game
```

## 项目结构

```
java-celeste/
├── pom.xml                    # Maven配置文件
├── README.md                  # 项目说明
├── KEYBINDINGS.md             # 按键绑定配置说明
├── .gitignore                 # Git忽略文件
└── src/
    ├── Game.java             # 游戏主类
    ├── Player.java           # 玩家角色类
    ├── Platform.java         # 平台类
    ├── SolidBlock.java       # 实心物块类
    └── KeyBindings.java      # 按键绑定配置类
```

## 游戏目标

- 使用跳跃技能到达不同的平台
- 探索游戏世界
- 享受简单的横版跳跃乐趣！

## 技术实现

- **Java Swing**: 用于图形界面和游戏渲染
- **Maven**: 项目构建和依赖管理
- **多线程**: 游戏循环与UI更新分离
- **事件驱动**: 键盘输入处理

## 系统要求

- Java 11 或更高版本
- Maven 3.6 或更高版本（如果使用Maven运行）

---

享受游戏吧！🎮

