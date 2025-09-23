// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 平台类 - 可以从下方穿过，只能从上方着陆的平台
 */
public class Platform {
    private int x, y, width, height;
    private Color color;
    
    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(139, 69, 19); // 棕色
    }
    
    public void render(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        
        // 添加一些纹理效果
        g.setColor(new Color(160, 82, 45)); // 稍亮的棕色
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // 添加边框
        g.setColor(new Color(101, 50, 20)); // 深色边框
        g.drawRect(x, y, width, height);
    }
    
    // 检查玩家是否在平台上（脚部接触平台顶部）
    public boolean isPlayerOnPlatform(double playerX, double playerY, int playerWidth, int playerHeight) {
        // 检查玩家是否在平台的水平范围内
        boolean horizontallyOnPlatform = playerX + playerWidth > x && playerX < x + width;
        
        // 检查玩家是否在平台的垂直范围内（脚部接触平台顶部）
        boolean verticallyOnPlatform = playerY + playerHeight >= y && playerY + playerHeight <= y + height;
        
        return horizontallyOnPlatform && verticallyOnPlatform;
    }
    
    // 检查玩家是否从上方着陆到平台
    public boolean checkLanding(double playerX, double playerY, int playerWidth, int playerHeight, double velocityY) {
        if (isPlayerOnPlatform(playerX, playerY, playerWidth, playerHeight) && velocityY > 0) {
            return true;
        }
        return false;
    }
    
    // 获取平台顶部Y坐标（用于设置玩家位置）
    public int getTopY() {
        return y;
    }
    
    // 获取物块边界
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
