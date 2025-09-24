// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 地图元素基类
 * 所有地图元素（平台、实心物块、尖刺等）的通用基类
 */
public abstract class MapElement {
    protected int x, y, width, height;
    protected Color color;
    
    /**
     * 构造函数
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param color 颜色
     */
    public MapElement(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }
    
    /**
     * 渲染元素
     * 子类需要实现具体的渲染逻辑
     * @param g 图形上下文
     */
    public abstract void render(Graphics g);
    
    /**
     * 检查与玩家的碰撞
     * 子类可以重写此方法实现特定的碰撞逻辑
     * @param playerX 玩家X坐标
     * @param playerY 玩家Y坐标
     * @param playerWidth 玩家宽度
     * @param playerHeight 玩家高度
     * @return 是否发生碰撞
     */
    public boolean checkCollision(double playerX, double playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && 
               playerX + playerWidth > x && 
               playerY < y + height && 
               playerY + playerHeight > y;
    }
    
    /**
     * 获取X坐标
     */
    public int getX() { 
        return x; 
    }
    
    /**
     * 获取Y坐标
     */
    public int getY() { 
        return y; 
    }
    
    /**
     * 获取宽度
     */
    public int getWidth() { 
        return width; 
    }
    
    /**
     * 获取高度
     */
    public int getHeight() { 
        return height; 
    }
    
    /**
     * 获取颜色
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * 设置位置
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 设置尺寸
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * 设置颜色
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * 获取元素类型名称
     * 子类可以重写此方法返回具体的类型名称
     */
    public String getElementType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 获取元素信息字符串
     */
    public String getInfo() {
        return String.format("%s: 位置(%d,%d) 尺寸(%dx%d) 颜色(%s)", 
            getElementType(), x, y, width, height, color.toString());
    }
}
