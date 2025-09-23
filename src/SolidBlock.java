// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 实心物块类 - 不能从任何方向穿过的障碍物
 */
public class SolidBlock {
    private int x, y, width, height;
    private Color color;
    
    public SolidBlock(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(101, 67, 33); // 深棕色
    }
    
    public void render(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        
        // 添加一些纹理效果
        g.setColor(new Color(139, 90, 43)); // 稍亮的棕色
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // 添加边框
        g.setColor(new Color(69, 45, 20)); // 深色边框
        g.drawRect(x, y, width, height);
    }
    
    // 检查玩家是否与物块碰撞
    public boolean checkCollision(double playerX, double playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && 
               playerX + playerWidth > x && 
               playerY < y + height && 
               playerY + playerHeight > y;
    }
    
    // 获取物块边界
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
