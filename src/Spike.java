// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 尖刺类 - 致命的障碍物，玩家碰到后会死亡
 */
public class Spike {
    private int x, y, width, height;
    private Color color;
    
    public Spike(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(139, 0, 0); // 深红色
    }
    
    public void render(Graphics g) {
        // 绘制尖刺主体
        g.setColor(color);
        g.fillRect(x, y, width, height);
        
        // 绘制尖刺纹理
        g.setColor(new Color(178, 34, 34)); // 稍亮的红色
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // 绘制尖刺顶部（三角形）
        int[] xPoints = {x + width/2, x, x + width};
        int[] yPoints = {y, y + height/2, y + height/2};
        g.setColor(new Color(220, 20, 60)); // 深红色
        g.fillPolygon(xPoints, yPoints, 3);
        
        // 绘制边框
        g.setColor(new Color(101, 0, 0)); // 深色边框
        g.drawRect(x, y, width, height);
        g.drawPolygon(xPoints, yPoints, 3);
    }
    
    // 检查玩家是否与尖刺碰撞
    public boolean checkCollision(double playerX, double playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && 
               playerX + playerWidth > x && 
               playerY < y + height && 
               playerY + playerHeight > y;
    }
    
    // 获取尖刺边界
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
