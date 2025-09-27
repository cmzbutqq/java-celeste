// 简化项目结构，移除包声明

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * 能量豆类
 * 玩家接触时恢复冲刺数和体力，被消耗后一段时间恢复
 */
public class EnergyBean extends MapElement {
    private static final int DEFAULT_SIZE = 20; // 默认大小
    private static final int RECOVERY_TIME = 120; // 恢复时间（帧数，约2秒）
    private static final Color FILL_COLOR = new Color(144, 238, 144); // 淡绿色
    private static final Color BORDER_COLOR = Color.WHITE; // 白色边框
    private static final Color CONSUMED_BORDER_COLOR = Color.WHITE; // 被消耗后的白色虚线边框
    
    private boolean isConsumed = false; // 是否被消耗
    private int recoveryTimer = 0; // 恢复计时器
    
    /**
     * 构造函数
     * @param x X坐标
     * @param y Y坐标
     */
    public EnergyBean(int x, int y) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, FILL_COLOR);
    }
    
    /**
     * 构造函数（指定大小）
     * @param x X坐标
     * @param y Y坐标
     * @param size 大小
     */
    public EnergyBean(int x, int y, int size) {
        super(x, y, size, size, FILL_COLOR);
    }
    
    /**
     * 更新能量豆状态
     * @param deltaTime 时间增量
     */
    public void update(double deltaTime) {
        if (isConsumed) {
            recoveryTimer++;
            if (recoveryTimer >= RECOVERY_TIME) {
                // 恢复能量豆
                isConsumed = false;
                recoveryTimer = 0;
            }
        }
    }
    
    /**
     * 消耗能量豆
     * @return 是否成功消耗（只有未被消耗时才能消耗）
     */
    public boolean consume() {
        if (!isConsumed) {
            isConsumed = true;
            recoveryTimer = 0;
            return true;
        }
        return false;
    }
    
    /**
     * 检查是否被消耗
     * @return 是否被消耗
     */
    public boolean isConsumed() {
        return isConsumed;
    }
    
    /**
     * 获取恢复进度（0.0-1.0）
     * @return 恢复进度
     */
    public double getRecoveryProgress() {
        if (!isConsumed) {
            return 1.0;
        }
        return (double) recoveryTimer / RECOVERY_TIME;
    }
    
    /**
     * 渲染能量豆
     * @param g 图形上下文
     */
    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 计算菱形顶点
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        
        // 创建菱形多边形
        Polygon diamond = new Polygon();
        diamond.addPoint(centerX, centerY - halfHeight); // 上顶点
        diamond.addPoint(centerX + halfWidth, centerY);  // 右顶点
        diamond.addPoint(centerX, centerY + halfHeight); // 下顶点
        diamond.addPoint(centerX - halfWidth, centerY);  // 左顶点
        
        if (!isConsumed) {
            // 未被消耗：绘制填充的菱形
            g2d.setColor(FILL_COLOR);
            g2d.fillPolygon(diamond);
            
            // 绘制白色粗边框
            g2d.setColor(BORDER_COLOR);
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.drawPolygon(diamond);
        } else {
            // 被消耗：只绘制白色虚线边框
            g2d.setColor(CONSUMED_BORDER_COLOR);
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, 
                                        BasicStroke.JOIN_ROUND, 0.0f, 
                                        new float[]{5.0f, 5.0f}, 0.0f)); // 虚线
            g2d.drawPolygon(diamond);
        }
        
        g2d.dispose();
    }
    
    /**
     * 检查与玩家的碰撞
     * 使用正方形碰撞箱
     * @param playerX 玩家X坐标
     * @param playerY 玩家Y坐标
     * @param playerWidth 玩家宽度
     * @param playerHeight 玩家高度
     * @return 是否发生碰撞
     */
    @Override
    public boolean checkCollision(double playerX, double playerY, int playerWidth, int playerHeight) {
        return playerX < x + width && 
               playerX + playerWidth > x && 
               playerY < y + height && 
               playerY + playerHeight > y;
    }
    
    /**
     * 获取元素类型名称
     * @return 元素类型名称
     */
    @Override
    public String getElementType() {
        return "EnergyBean";
    }
    
    /**
     * 获取元素信息字符串
     * @return 元素信息
     */
    @Override
    public String getInfo() {
        String status = isConsumed ? "已消耗" : "可用";
        return String.format("%s: 位置(%d,%d) 尺寸(%dx%d) 状态(%s) 恢复进度(%.1f%%)", 
            getElementType(), x, y, width, height, status, getRecoveryProgress() * 100);
    }
}
