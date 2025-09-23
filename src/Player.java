// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 * 玩家角色类
 */
public class Player {
    private static final int PLAYER_WIDTH = 30;
    private static final int PLAYER_HEIGHT = 40;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -12;
    private static final double MOVE_SPEED = 3;
    private static final int GROUND_Y = 550; // 地面Y坐标
    
    private double x, y;
    private double velocityX, velocityY;
    private boolean onGround;
    private boolean leftPressed, rightPressed, upPressed, downPressed, jumpPressed, dashPressed;
    private SolidBlock[] solidBlocks; // 实心物块数组
    private Platform[] platforms; // 平台数组
    private Spike[] spikes; // 尖刺数组
    
    // 死亡和重生相关
    private boolean isDead = false;
    private int deathAnimationTimer = 0;
    private static final int DEATH_ANIMATION_DURATION = 30; // 死亡动画持续30帧
    private double respawnX, respawnY; // 重生位置
    
    // 冲刺相关
    private boolean isDashing = false;
    private int dashTimer = 0;
    private static final int DASH_DURATION = 15; // 冲刺持续帧数
    private static final double DASH_SPEED = 12.0; // 冲刺速度
    private int dashCount = 2; // 冲刺次数（最多2次）
    private static final int MAX_DASH_COUNT = 2; // 最大冲刺次数
    private double dashVelocityX = 0, dashVelocityY = 0; // 冲刺速度分量
    private Color dashColor; // 冲刺时的颜色（基于冲刺前的状态）
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        this.solidBlocks = new SolidBlock[0]; // 初始化为空数组
        this.platforms = new Platform[0]; // 初始化为空数组
        this.spikes = new Spike[0]; // 初始化为空数组
        this.respawnX = x; // 设置初始重生位置
        this.respawnY = y;
    }
    
    public void setSolidBlocks(SolidBlock[] solidBlocks) {
        this.solidBlocks = solidBlocks;
    }
    
    public void setPlatforms(Platform[] platforms) {
        this.platforms = platforms;
    }
    
    public void setSpikes(Spike[] spikes) {
        this.spikes = spikes;
    }
    
    public void setRespawnPoint(double x, double y) {
        this.respawnX = x;
        this.respawnY = y;
    }
    
    public void update(double deltaTime) {
        // 如果玩家死亡，处理死亡动画
        if (isDead) {
            deathAnimationTimer++;
            if (deathAnimationTimer >= DEATH_ANIMATION_DURATION) {
                // 死亡动画结束，重生
                respawn();
            }
            return;
        }
        
        // 处理冲刺
        if (dashPressed && dashCount > 0 && !isDashing) {
            startDash();
        }
        
        // 冲刺状态处理
        if (isDashing) {
            dashTimer++;
            if (dashTimer >= DASH_DURATION) {
                endDash();
            } else {
                // 冲刺期间使用冲刺速度
                velocityX = dashVelocityX;
                velocityY = dashVelocityY;
            }
        } else {
            // 正常移动逻辑
            // 处理水平移动
            if (leftPressed && !rightPressed) {
                velocityX = -MOVE_SPEED;
            } else if (rightPressed && !leftPressed) {
                velocityX = MOVE_SPEED;
            } else {
                velocityX = 0;
            }
            
            // 处理跳跃
            if (jumpPressed && onGround) {
                velocityY = JUMP_STRENGTH;
                onGround = false;
            }
            
            // 应用重力
            if (!onGround) {
                velocityY += GRAVITY;
            }
        }
        
        // 更新位置
        x += velocityX;
        y += velocityY;
        
        // 先重置onGround状态，然后通过碰撞检测来设置
        onGround = false;
        
        // 地面碰撞检测
        if (y >= GROUND_Y - PLAYER_HEIGHT) {
            y = GROUND_Y - PLAYER_HEIGHT;
            velocityY = 0;
            onGround = true;
            // 落地时恢复冲刺能力
            dashCount = MAX_DASH_COUNT; // 落地时恢复所有冲刺次数
        }
        
        // 平台碰撞检测
        checkPlatformCollision();
        
        // 实心物块碰撞检测
        checkSolidBlockCollision();
        
        // 尖刺碰撞检测
        checkSpikeCollision();
        
        // 边界检测
        if (x < 0) x = 0;
        if (x > 800 - PLAYER_WIDTH) x = 800 - PLAYER_WIDTH;
    }
    
    private void checkPlatformCollision() {
        for (Platform platform : platforms) {
            if (platform.checkLanding(x, y, PLAYER_WIDTH, PLAYER_HEIGHT, velocityY)) {
                // 玩家从上方着陆到平台
                y = platform.getTopY() - PLAYER_HEIGHT;
                velocityY = 0;
                onGround = true;
                dashCount = MAX_DASH_COUNT; // 落地时恢复所有冲刺次数 // 着陆到平台时恢复冲刺能力
            } else if (platform.isPlayerOnPlatform(x, y, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                // 玩家在平台上（但不是着陆）
                onGround = true;
                dashCount = MAX_DASH_COUNT; // 落地时恢复所有冲刺次数 // 在平台上时恢复冲刺能力
            }
        }
    }
    
    
    private void checkSolidBlockCollision() {
        for (SolidBlock block : solidBlocks) {
            if (block.checkCollision(x, y, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                // 计算碰撞方向并调整位置
                double overlapX = 0;
                double overlapY = 0;
                
                // 计算重叠量
                double leftOverlap = (x + PLAYER_WIDTH) - block.getX();
                double rightOverlap = (block.getX() + block.getWidth()) - x;
                double topOverlap = (y + PLAYER_HEIGHT) - block.getY();
                double bottomOverlap = (block.getY() + block.getHeight()) - y;
                
                // 找到最小的重叠量
                double minOverlap = Math.min(Math.min(leftOverlap, rightOverlap), 
                                          Math.min(topOverlap, bottomOverlap));
                
                if (minOverlap == leftOverlap) {
                    // 从左侧碰撞
                    x = block.getX() - PLAYER_WIDTH;
                    velocityX = 0;
                } else if (minOverlap == rightOverlap) {
                    // 从右侧碰撞
                    x = block.getX() + block.getWidth();
                    velocityX = 0;
                } else if (minOverlap == topOverlap) {
                    // 从上方碰撞
                    y = block.getY() - PLAYER_HEIGHT;
                    velocityY = 0;
                    onGround = true;
                    dashCount = MAX_DASH_COUNT; // 落地时恢复所有冲刺次数 // 在实心物块上时恢复冲刺能力
                } else if (minOverlap == bottomOverlap) {
                    // 从下方碰撞
                    y = block.getY() + block.getHeight();
                    velocityY = 0;
                }
            }
        }
    }
    
    private void checkSpikeCollision() {
        for (Spike spike : spikes) {
            if (spike.checkCollision(x, y, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                // 玩家碰到尖刺，死亡
                die();
                break;
            }
        }
    }
    
    private void die() {
        isDead = true;
        deathAnimationTimer = 0;
        velocityX = 0;
        velocityY = 0;
        System.out.println("玩家死亡！正在重生...");
    }
    
    private void respawn() {
        isDead = false;
        deathAnimationTimer = 0;
        x = respawnX;
        y = respawnY;
        velocityX = 0;
        velocityY = 0;
        onGround = false;
        dashCount = MAX_DASH_COUNT; // 重生时恢复所有冲刺次数
        isDashing = false;
        dashTimer = 0;
        System.out.println("玩家重生！");
    }
    
    private void startDash() {
        if (dashCount <= 0) return; // 没有冲刺次数了
        
        // 保存冲刺前的颜色
        if (dashCount >= 2) {
            dashColor = new Color(255, 192, 203); // 粉色
        } else if (dashCount == 1) {
            dashColor = new Color(255, 50, 50); // 红色
        } else {
            dashColor = new Color(50, 100, 255); // 蓝色
        }
        
        isDashing = true;
        dashTimer = 0;
        dashCount--; // 消耗一次冲刺机会
        
        // 根据当前按键状态确定冲刺方向
        double dashX = 0, dashY = 0;
        
        if (leftPressed) dashX = -1;
        if (rightPressed) dashX = 1;
        if (upPressed) dashY = -1;
        if (downPressed) dashY = 1;
        
        // 如果没有按任何方向键，默认向右冲刺
        if (dashX == 0 && dashY == 0) {
            dashX = 1;
        }
        
        // 标准化方向向量
        double length = Math.sqrt(dashX * dashX + dashY * dashY);
        dashX = dashX / length;
        dashY = dashY / length;
        
        // 设置冲刺速度
        dashVelocityX = dashX * DASH_SPEED;
        dashVelocityY = dashY * DASH_SPEED;
        
        System.out.println("冲刺！方向: (" + dashX + ", " + dashY + ") 剩余次数: " + dashCount);
    }
    
    private void endDash() {
        isDashing = false;
        dashTimer = 0;
        // 冲刺结束后保持当前速度，但会受到重力影响
        velocityX = dashVelocityX * 0.5; // 冲刺结束后速度减半
        velocityY = dashVelocityY * 0.5;
    }
    
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (KeyBindings.isLeftMoveKey(keyCode)) {
            leftPressed = true;
        } else if (KeyBindings.isRightMoveKey(keyCode)) {
            rightPressed = true;
        } else if (KeyBindings.isUpMoveKey(keyCode)) {
            upPressed = true;
        } else if (KeyBindings.isDownMoveKey(keyCode)) {
            downPressed = true;
        } else if (KeyBindings.isJumpKey(keyCode)) {
            jumpPressed = true;
        } else if (KeyBindings.isDashKey(keyCode)) {
            dashPressed = true;
        }
    }
    
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (KeyBindings.isLeftMoveKey(keyCode)) {
            leftPressed = false;
        } else if (KeyBindings.isRightMoveKey(keyCode)) {
            rightPressed = false;
        } else if (KeyBindings.isUpMoveKey(keyCode)) {
            upPressed = false;
        } else if (KeyBindings.isDownMoveKey(keyCode)) {
            downPressed = false;
        } else if (KeyBindings.isJumpKey(keyCode)) {
            jumpPressed = false;
        } else if (KeyBindings.isDashKey(keyCode)) {
            dashPressed = false;
        }
    }
    
    public void render(Graphics g) {
        if (isDead) {
            // 死亡动画：闪烁效果
            if (deathAnimationTimer % 4 < 2) { // 每4帧闪烁一次
                renderDeadPlayer(g);
            }
        } else {
            renderAlivePlayer(g);
        }
    }
    
    private void renderAlivePlayer(Graphics g) {
        if (isDashing) {
            // 冲刺时的视觉效果：蓝色，带拖尾效果
            renderDashingPlayer(g);
        } else {
            // 正常状态
            renderNormalPlayer(g);
        }
    }
    
    private void renderNormalPlayer(Graphics g) {
        // 根据冲刺次数选择颜色
        Color bodyColor = new Color(150,150,150), headColor;
        if (dashCount >= 2) {
            // 两次冲刺：粉色
            headColor = new Color(255, 192, 203); // 粉色
        } else if (dashCount == 1) {
            // 一次冲刺：红色
            headColor = new Color(207,100,90); // 红色
        } else {
            // 零次冲刺：蓝色
            headColor = new Color(100, 150, 255); // 蓝色
        }
        
        // 绘制玩家身体（碰撞箱下半部分）
        g.setColor(bodyColor);
        g.fillRect((int)x, (int)y + PLAYER_HEIGHT / 2, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制玩家头部（碰撞箱上半部分）
        g.setColor(headColor);
        g.fillRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制边框
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT); // 整体碰撞箱边框
    }
    
    private void renderDashingPlayer(Graphics g) {
        // 使用冲刺前的颜色
        Color bodyColor = dashColor;
        Color headColor = new Color(
            Math.min(255, dashColor.getRed() + 30),
            Math.min(255, dashColor.getGreen() + 30),
            Math.min(255, dashColor.getBlue() + 30)
        ); // 头部稍微亮一些
        
        // 绘制玩家身体（碰撞箱下半部分）
        g.setColor(bodyColor);
        g.fillRect((int)x, (int)y + PLAYER_HEIGHT / 2, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制玩家头部（碰撞箱上半部分）
        g.setColor(headColor);
        g.fillRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制边框
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT); // 整体碰撞箱边框
        
        // 绘制冲刺拖尾效果（使用冲刺前的颜色）
        g.setColor(new Color(dashColor.getRed(), dashColor.getGreen(), dashColor.getBlue(), 100)); // 半透明
        int trailLength = 20;
        int trailX = (int)x - (int)(dashVelocityX * trailLength / DASH_SPEED);
        int trailY = (int)y - (int)(dashVelocityY * trailLength / DASH_SPEED);
        g.fillRect(trailX, trailY, PLAYER_WIDTH, PLAYER_HEIGHT);
    }
    
    private void renderDeadPlayer(Graphics g) {
        // 死亡状态：红色，X形眼睛
        // 绘制玩家身体（碰撞箱下半部分）
        g.setColor(new Color(255, 0, 0)); // 红色
        g.fillRect((int)x, (int)y + PLAYER_HEIGHT / 2, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制头部（碰撞箱上半部分）
        g.setColor(new Color(200, 0, 0)); // 深红色
        g.fillRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT / 2);
        
        // 绘制X形死亡标志
        g.setColor(Color.WHITE);
        g.drawLine((int)x + 4, (int)y + PLAYER_HEIGHT / 4, (int)x + PLAYER_WIDTH - 4, (int)y + 3 * PLAYER_HEIGHT / 4);
        g.drawLine((int)x + PLAYER_WIDTH - 4, (int)y + PLAYER_HEIGHT / 4, (int)x + 4, (int)y + 3 * PLAYER_HEIGHT / 4);
        
        // 绘制边框
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y, PLAYER_WIDTH, PLAYER_HEIGHT); // 整体碰撞箱边框
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public boolean isOnGround() { return onGround; }
}

