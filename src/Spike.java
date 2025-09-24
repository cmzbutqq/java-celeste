// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 尖刺类 - 致命的障碍物，玩家碰到后会死亡
 */
public class Spike extends MapElement {
    
    public Spike(int x, int y, int width, int height) {
        super(x, y, width, height, new Color(139, 0, 0)); // 深红色
    }
    
    @Override
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
    
    // 注意：checkCollision() 方法已从基类继承，但这里可以重写以提供更具体的碰撞逻辑
    // 注意：getX(), getY(), getWidth(), getHeight() 方法已从基类继承
}
