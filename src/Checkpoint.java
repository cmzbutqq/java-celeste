// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;

/**
 * 重生点类
 * 包含激活框和重生点，用于角色重生和场景转换
 */
public class Checkpoint extends MapElement {
    private int respawnOffsetX, respawnOffsetY; // 重生点相对于激活框的偏移
    private boolean isActivated; // 是否已激活
    private final boolean defaultActivated; // 是否默认激活
    
    // 颜色定义
    private static final Color INACTIVE_BOX_COLOR = new Color(128, 128, 128, 100); // 灰色半透明
    private static final Color ACTIVE_BOX_COLOR = new Color(100, 150, 255, 100); // 淡蓝色半透明
    private static final Color INACTIVE_POINT_COLOR = new Color(128, 128, 128); // 灰色
    private static final Color ACTIVE_POINT_COLOR = new Color(100, 150, 255); // 淡蓝色
    
    /**
     * 构造函数
     * @param x 激活框X坐标
     * @param y 激活框Y坐标
     * @param width 激活框宽度
     * @param height 激活框高度
     * @param respawnOffsetX 重生点X偏移
     * @param respawnOffsetY 重生点Y偏移
     * @param defaultActivated 是否默认激活
     */
    public Checkpoint(int x, int y, int width, int height, 
                     int respawnOffsetX, int respawnOffsetY, 
                     boolean defaultActivated) {
        super(x, y, width, height, INACTIVE_BOX_COLOR);
        this.respawnOffsetX = respawnOffsetX;
        this.respawnOffsetY = respawnOffsetY;
        this.defaultActivated = defaultActivated;
        this.isActivated = defaultActivated;
    }
    
    /**
     * 渲染重生点
     */
    @Override
    public void render(Graphics g) {
        // 绘制激活框
        Color boxColor = isActivated ? ACTIVE_BOX_COLOR : INACTIVE_BOX_COLOR;
        g.setColor(boxColor);
        g.fillRect(x, y, width, height);
        
        // 绘制激活框边框
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        
        // 绘制重生点（十字形）
        int respawnX = x + respawnOffsetX;
        int respawnY = y + respawnOffsetY;
        Color pointColor = isActivated ? ACTIVE_POINT_COLOR : INACTIVE_POINT_COLOR;
        g.setColor(pointColor);
        
        // 绘制十字形重生点
        int crossSize = 8;
        // 水平线
        g.drawLine(respawnX - crossSize, respawnY, respawnX + crossSize, respawnY);
        // 垂直线
        g.drawLine(respawnX, respawnY - crossSize, respawnX, respawnY + crossSize);
        
        // 绘制重生点中心点
        g.fillOval(respawnX - 2, respawnY - 2, 4, 4);
    }
    
    /**
     * 检查玩家是否进入激活框
     * @param playerX 玩家X坐标
     * @param playerY 玩家Y坐标
     * @param playerWidth 玩家宽度
     * @param playerHeight 玩家高度
     * @return 是否进入激活框
     */
    public boolean isPlayerInActivationBox(double playerX, double playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && 
               playerX + playerWidth > x && 
               playerY < y + height && 
               playerY + playerHeight > y;
    }
    
    /**
     * 激活重生点
     */
    public void activate() {
        isActivated = true;
    }
    
    /**
     * 获取重生点X坐标
     */
    public int getRespawnX() {
        return x + respawnOffsetX;
    }
    
    /**
     * 获取重生点Y坐标
     */
    public int getRespawnY() {
        return y + respawnOffsetY;
    }
    
    /**
     * 是否已激活
     */
    public boolean isActivated() {
        return isActivated;
    }
    
    /**
     * 是否默认激活
     */
    public boolean isDefaultActivated() {
        return defaultActivated;
    }
    
    /**
     * 设置重生点偏移
     */
    public void setRespawnOffset(int offsetX, int offsetY) {
        this.respawnOffsetX = offsetX;
        this.respawnOffsetY = offsetY;
    }
    
    /**
     * 获取重生点X偏移
     */
    public int getRespawnOffsetX() {
        return respawnOffsetX;
    }
    
    /**
     * 获取重生点Y偏移
     */
    public int getRespawnOffsetY() {
        return respawnOffsetY;
    }
    
    /**
     * 计算到指定点的距离
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 距离
     */
    public double getDistanceTo(double targetX, double targetY) {
        int respawnX = getRespawnX();
        int respawnY = getRespawnY();
        return Math.sqrt(Math.pow(respawnX - targetX, 2) + Math.pow(respawnY - targetY, 2));
    }
    
    /**
     * 获取元素信息字符串
     */
    @Override
    public String getInfo() {
        return String.format("Checkpoint: 激活框(%d,%d,%dx%d) 重生点(%d,%d) 激活状态:%s 默认激活:%s", 
            x, y, width, height, getRespawnX(), getRespawnY(), 
            isActivated ? "是" : "否", defaultActivated ? "是" : "否");
    }
}
