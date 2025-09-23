// 简化项目结构，移除包声明

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按键绑定配置类 - 管理游戏中的所有按键绑定
 * 使用"一个指令对应一个按键列表"的设计模式
 * 用户可以在这里自由修改控制方式
 */
public class KeyBindings {
    
    // 指令到按键列表的映射表
    private static final Map<String, List<Integer>> actionToKeys = new HashMap<>();
    
    // 按键到指令的反向查询表（自动生成）
    private static final Map<Integer, String> keyToAction = new HashMap<>();
    
    // 初始化默认按键绑定
    static {
        // 移动指令
        actionToKeys.put("MOVE_LEFT", Arrays.asList(KeyEvent.VK_A));
        actionToKeys.put("MOVE_RIGHT", Arrays.asList(KeyEvent.VK_D));
        actionToKeys.put("MOVE_UP", Arrays.asList(KeyEvent.VK_W));
        actionToKeys.put("MOVE_DOWN", Arrays.asList(KeyEvent.VK_S));
        
        // 跳跃指令
        actionToKeys.put("JUMP", Arrays.asList(KeyEvent.VK_K));
        
        // 冲刺指令
        actionToKeys.put("DASH", Arrays.asList(KeyEvent.VK_J));
        
        // 可以添加更多指令
        // actionToKeys.put("PAUSE", Arrays.asList(KeyEvent.VK_ESCAPE));
        // actionToKeys.put("RESTART", Arrays.asList(KeyEvent.VK_R));
        
        // 自动生成反向查询表
        generateReverseLookup();
    }
    
    /**
     * 自动生成按键到指令的反向查询表
     */
    private static void generateReverseLookup() {
        keyToAction.clear();
        for (Map.Entry<String, List<Integer>> entry : actionToKeys.entrySet()) {
            String action = entry.getKey();
            List<Integer> keys = entry.getValue();
            for (Integer key : keys) {
                keyToAction.put(key, action);
            }
        }
    }
    
    /**
     * 获取指令对应的所有按键
     * @param action 动作名称
     * @return 按键列表，如果未找到返回空列表
     */
    public static List<Integer> getKeysForAction(String action) {
        return actionToKeys.getOrDefault(action, new ArrayList<>());
    }
    
    /**
     * 获取按键对应的指令
     * @param keyCode 按键码
     * @return 指令名称，如果未找到返回null
     */
    public static String getActionForKey(int keyCode) {
        return keyToAction.get(keyCode);
    }
    
    /**
     * 检查按键是否绑定到跳跃动作
     */
    public static boolean isJumpKey(int keyCode) {
        return "JUMP".equals(getActionForKey(keyCode));
    }
    
    /**
     * 检查按键是否绑定到冲刺动作
     */
    public static boolean isDashKey(int keyCode) {
        return "DASH".equals(getActionForKey(keyCode));
    }
    
    /**
     * 检查按键是否绑定到左移动作
     */
    public static boolean isLeftMoveKey(int keyCode) {
        return "MOVE_LEFT".equals(getActionForKey(keyCode));
    }
    
    /**
     * 检查按键是否绑定到右移动作
     */
    public static boolean isRightMoveKey(int keyCode) {
        return "MOVE_RIGHT".equals(getActionForKey(keyCode));
    }
    
    /**
     * 检查按键是否绑定到上移动作
     */
    public static boolean isUpMoveKey(int keyCode) {
        return "MOVE_UP".equals(getActionForKey(keyCode));
    }
    
    /**
     * 检查按键是否绑定到下移动作
     */
    public static boolean isDownMoveKey(int keyCode) {
        return "MOVE_DOWN".equals(getActionForKey(keyCode));
    }
    
    
    /**
     * 打印当前按键绑定配置
     */
    public static void printKeyBindings() {
        System.out.println("=== 当前按键绑定配置 ===");
        
        for (Map.Entry<String, List<Integer>> entry : actionToKeys.entrySet()) {
            String action = entry.getKey();
            List<Integer> keys = entry.getValue();
            
            String actionName = getActionDisplayName(action);
            String keyNames = keys.stream()
                    .map(KeyBindings::getKeyName)
                    .reduce((a, b) -> a + " / " + b)
                    .orElse("无");
            
            String suffix = action.contains("MOVE_UP") || action.contains("MOVE_DOWN") ? " (预留功能)" : "";
            System.out.println(actionName + ": " + keyNames + suffix);
        }
        
        System.out.println("========================");
    }
    
    /**
     * 获取指令的显示名称
     * @param action 指令名称
     * @return 显示名称
     */
    private static String getActionDisplayName(String action) {
        switch (action) {
            case "MOVE_LEFT": return "左移";
            case "MOVE_RIGHT": return "右移";
            case "MOVE_UP": return "上移";
            case "MOVE_DOWN": return "下移";
            case "JUMP": return "跳跃";
            case "DASH": return "冲刺";
            case "PAUSE": return "暂停";
            case "RESTART": return "重启";
            default: return action;
        }
    }
    
    /**
     * 获取按键名称（用于显示）
     * @param keyCode 按键码
     * @return 按键名称
     */
    private static String getKeyName(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }
}
