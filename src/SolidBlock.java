// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 实心物块类 - 不能从任何方向穿过的障碍物
 */
public class SolidBlock extends MapElement {
    
    public SolidBlock(int x, int y, int width, int height) {
        super(x, y, width, height, new Color(101, 67, 33)); // 深棕色
    }
    
    @Override
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
    
    // 注意：checkCollision() 方法已从基类继承，但这里可以重写以提供更具体的碰撞逻辑
    // 注意：getX(), getY(), getWidth(), getHeight() 方法已从基类继承
}
