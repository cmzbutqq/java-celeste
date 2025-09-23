# 按键绑定配置说明

## 设计模式

本游戏使用 **"一个指令对应一个按键列表"** 的设计模式：
- 每个游戏指令（如移动、跳跃）对应一个按键列表
- 系统自动生成按键到指令的反向查询表
- 支持为每个指令绑定多个按键
- 支持动态添加/移除按键绑定

## 当前默认按键绑定

### 移动控制
- **左移**: A 键 或 左箭头键
- **右移**: D 键 或 右箭头键
- **上移**: W 键 或 上箭头键 (预留功能)
- **下移**: S 键 或 下箭头键 (预留功能)

### 跳跃控制
- **跳跃**: K 键 或 空格键 或 J 键

## 如何修改按键绑定

### 方法1: 直接修改 KeyBindings.java 文件

在 `src/KeyBindings.java` 文件中找到 `actionToKeys` 的初始化部分，修改按键列表：

```java
static {
    // 移动指令 - 每个指令对应一个按键列表
    actionToKeys.put("MOVE_LEFT", Arrays.asList(KeyEvent.VK_A, KeyEvent.VK_LEFT));
    actionToKeys.put("MOVE_RIGHT", Arrays.asList(KeyEvent.VK_D, KeyEvent.VK_RIGHT));
    actionToKeys.put("MOVE_UP", Arrays.asList(KeyEvent.VK_W, KeyEvent.VK_UP));
    actionToKeys.put("MOVE_DOWN", Arrays.asList(KeyEvent.VK_S, KeyEvent.VK_DOWN));
    
    // 跳跃指令
    actionToKeys.put("JUMP", Arrays.asList(KeyEvent.VK_K, KeyEvent.VK_SPACE, KeyEvent.VK_J));
}
```

### 方法2: 在运行时添加新的按键绑定

```java
// 添加新的跳跃按键（例如：L 键）
KeyBindings.addKeyBinding("JUMP", KeyEvent.VK_L);

// 添加暂停功能
KeyBindings.addKeyBinding("PAUSE", KeyEvent.VK_ESCAPE);

// 设置整个指令的按键列表（替换现有按键）
KeyBindings.setKeysForAction("JUMP", Arrays.asList(KeyEvent.VK_K, KeyEvent.VK_L));
```

### 方法3: 移除按键绑定

```java
// 移除特定按键
KeyBindings.removeKeyBinding("JUMP", KeyEvent.VK_J);

// 移除整个指令
KeyBindings.removeAction("MOVE_UP");
```

## 常用按键码参考

| 按键 | 按键码 | 按键 | 按键码 |
|------|--------|------|--------|
| A | VK_A | 0 | VK_0 |
| B | VK_B | 1 | VK_1 |
| C | VK_C | 2 | VK_2 |
| ... | ... | ... | ... |
| Z | VK_Z | 9 | VK_9 |
| 空格 | VK_SPACE | 回车 | VK_ENTER |
| 左箭头 | VK_LEFT | 右箭头 | VK_RIGHT |
| 上箭头 | VK_UP | 下箭头 | VK_DOWN |
| Shift | VK_SHIFT | Ctrl | VK_CONTROL |
| Alt | VK_ALT | Esc | VK_ESCAPE |

## 示例：修改为 WASD 控制

```java
static {
    // 移动指令 - 使用 WASD
    actionToKeys.put("MOVE_LEFT", Arrays.asList(KeyEvent.VK_A));
    actionToKeys.put("MOVE_RIGHT", Arrays.asList(KeyEvent.VK_D));
    actionToKeys.put("MOVE_UP", Arrays.asList(KeyEvent.VK_W));
    actionToKeys.put("MOVE_DOWN", Arrays.asList(KeyEvent.VK_S));
    
    // 跳跃指令
    actionToKeys.put("JUMP", Arrays.asList(KeyEvent.VK_SPACE, KeyEvent.VK_K));
}
```

## 示例：添加更多功能按键

```java
static {
    // 原有指令...
    
    // 添加新功能
    actionToKeys.put("PAUSE", Arrays.asList(KeyEvent.VK_ESCAPE, KeyEvent.VK_P));
    actionToKeys.put("RESTART", Arrays.asList(KeyEvent.VK_R, KeyEvent.VK_F5));
    actionToKeys.put("DEBUG", Arrays.asList(KeyEvent.VK_F3));
}
```

## 新设计模式的优势

### 1. 更清晰的配置
- 每个指令对应一个按键列表，一目了然
- 不需要 `_ALT` 后缀，配置更简洁

### 2. 自动反向查询
- 系统自动生成按键到指令的映射表
- 查询效率更高，代码更简洁

### 3. 灵活的扩展性
- 轻松添加/移除按键
- 支持动态修改按键绑定
- 支持替换整个指令的按键列表

### 4. 更好的维护性
- 配置集中管理
- 减少重复代码
- 易于理解和修改

## 注意事项

1. **重新编译**: 修改 `KeyBindings.java` 后需要重新编译项目
2. **按键冲突**: 确保不同指令使用不同的按键，避免冲突
3. **备用按键**: 建议为每个指令设置多个备用按键，提高用户体验
4. **测试**: 修改后请测试所有按键是否正常工作

## 编译和运行

```bash
# 编译项目
mvn compile

# 运行游戏
mvn exec:java
```