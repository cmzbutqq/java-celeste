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
    private static final double FAST_FALL_GRAVITY = 1.0; // 加速下落时的重力（2倍）
    private static final double JUMP_STRENGTH = -12;
    private static final double MOVE_SPEED = 3;
    private static final int GROUND_Y = 550; // 地面Y坐标
    
    private double x, y;
    private double velocityX, velocityY;
    private boolean onGround;
    private boolean leftPressed, rightPressed, upPressed, downPressed, jumpPressed, dashPressed, climbPressed;
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
    
    // 攀爬相关
    private boolean isClimbing = false; // 是否正在攀爬
    private boolean isTouchingWall = false; // 是否贴着墙壁
    private int wallDirection = 0; // 墙壁方向：-1左墙，1右墙，0无墙
    private double stamina = 100.0; // 体力条（0-100）
    private static final double MAX_STAMINA = 100.0; // 最大体力
    private static final double STAMINA_DRAIN_RATE = 0.4;
    private static final double WALL_SLIDE_SPEED = 1.0; // 贴墙下滑速度
    private static final double CLIMB_MOVE_SPEED = 2.0; // 攀爬移动速度
    
    // 跳跃冷却相关
    private int jumpCooldownTimer = 0; // 跳跃冷却计时器
    private static final int JUMP_COOLDOWN_DURATION = 10; // 跳跃冷却持续帧数（约0.17秒）
    
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
        
        // 检查墙壁碰撞
        checkWallCollision();
        
        // 处理攀爬逻辑
        handleClimbing(deltaTime);
        
        // 处理冲刺（冲刺优先级高于攀爬）
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
            // 处理水平移动（攀爬时屏蔽水平移动）
            if (!isClimbing) {
                if (leftPressed && !rightPressed) {
                    velocityX = -MOVE_SPEED;
                } else if (rightPressed && !leftPressed) {
                    velocityX = MOVE_SPEED;
                } else {
                    velocityX = 0;
                }
            }
            
            // 处理跳跃（攀爬时也可以跳跃）
            if (jumpPressed && (onGround || isClimbing) && jumpCooldownTimer <= 0) {
                if (isClimbing) {
                    // 攀爬跳跃：根据墙壁方向和移动方向决定跳跃方向
                    double jumpX = 0, jumpY = JUMP_STRENGTH;
                    
                    // 检查是否向外移动
                    boolean movingAwayFromWall = (wallDirection == 1 && leftPressed) || (wallDirection == -1 && rightPressed);
                    
                    if (movingAwayFromWall) {
                        // 向外移动时，向斜上方跳跃
                        jumpX = wallDirection == 1 ? -MOVE_SPEED * 2 : MOVE_SPEED * 2; // 水平速度是移动速度的2倍
                        jumpY = JUMP_STRENGTH * 0.8; // 垂直速度稍微减少
                        System.out.println("攀爬斜跳！方向: (" + jumpX + ", " + jumpY + ")");
                    } else {
                        // 没有向外移动时，传统向上跳跃
                        System.out.println("攀爬跳跃！");
                    }
                    
                    velocityX = jumpX;
                    velocityY = jumpY;
                    isClimbing = false; // 跳跃时停止攀爬
                } else {
                    // 普通跳跃
                    velocityY = JUMP_STRENGTH;
                }
                
                onGround = false;
                jumpCooldownTimer = JUMP_COOLDOWN_DURATION; // 开始冷却
            }
            
            // 应用重力（攀爬时不应用重力）
            if (!onGround && !isClimbing) {
                // 检查是否加速下落（排除冲刺、攀墙、沿墙滑落状态）
                if (downPressed && velocityY > 0 && !isDashing && !isTouchingWall) {
                    // 自由落体时按S键，重力翻倍
                    velocityY += FAST_FALL_GRAVITY;
                } else {
                    // 正常重力
                    velocityY += GRAVITY;
                }
            }
        }
        
        // 更新位置
        x += velocityX;
        y += velocityY;
        
        // 更新跳跃冷却
        if (jumpCooldownTimer > 0) {
            jumpCooldownTimer--;
        }
        
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
        
        // 落地体力条恢复满（地面、平台、实心物块）
        if (onGround) {
            stamina = MAX_STAMINA;
            isClimbing = false; // 落地时停止攀爬
        }
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
    
    /**
     * 检查是否贴着墙壁
     */
    private void checkWallCollision() {
        isTouchingWall = false;
        wallDirection = 0;
        
        // 检查实心物块的墙壁碰撞
        for (SolidBlock block : solidBlocks) {
            if (isTouchingWall(block)) {
                isTouchingWall = true;
                break;
            }
        }
    }
    
    /**
     * 检查是否贴着特定实心物块
     */
    private boolean isTouchingWall(SolidBlock block) {
        // 检查左墙碰撞（玩家右边缘贴着物块左边缘）
        if (x + PLAYER_WIDTH >= block.getX() && 
            x + PLAYER_WIDTH <= block.getX() + 5 && // 5像素的容错范围
            y < block.getY() + block.getHeight() && 
            y + PLAYER_HEIGHT > block.getY()) {
            wallDirection = 1; // 右墙
            return true;
        }
        
        // 检查右墙碰撞（玩家左边缘贴着物块右边缘）
        if (x <= block.getX() + block.getWidth() && 
            x >= block.getX() + block.getWidth() - 5 && // 5像素的容错范围
            y < block.getY() + block.getHeight() && 
            y + PLAYER_HEIGHT > block.getY()) {
            wallDirection = -1; // 左墙
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理攀爬逻辑
     */
    private void handleClimbing(double deltaTime) {
        // 1. 不贴墙时：攀爬无用（但正在攀爬时例外）
        if (!isTouchingWall && !isClimbing) {
            return;
        }
        
        // 2. 如果正在攀爬但不贴墙了，停止攀爬
        if (isClimbing && !isTouchingWall) {
            isClimbing = false;
            System.out.println("离开墙壁，停止攀爬");
            return;
        }
        
        // 3. 贴墙且非攀爬时：如果有向墙壁移动，会贴着墙壁匀速缓慢下落
        if (!isClimbing) {
            // 检查是否向墙壁移动
            boolean movingTowardsWall = (wallDirection == 1 && rightPressed) || (wallDirection == -1 && leftPressed);
            
            if (movingTowardsWall) {
                // 贴着墙壁匀速缓慢下落
                velocityY = WALL_SLIDE_SPEED;
                velocityX = 0; // 停止水平移动
            }
            // 如果没有向墙壁移动，则自由下落（重力会处理）
            
            // 检查是否可以开始攀爬（每帧都检查）
            if (climbPressed && stamina > 0) {
                isClimbing = true;
            }
        }
        
        // 4. 贴墙且攀爬时：可以上下移动
        if (isClimbing) {
            // 处理攀爬时的上下移动
            if (upPressed && !downPressed) {
                // 向上攀爬
                velocityY = -CLIMB_MOVE_SPEED;
                velocityX = 0; // 停止水平移动
            } else if (downPressed && !upPressed) {
                // 向下攀爬
                velocityY = CLIMB_MOVE_SPEED;
                velocityX = 0; // 停止水平移动
            } else {
                // 没有按上下键，静止在墙上
                velocityY = 0;
                velocityX = 0;
            }
            
            // 消耗体力条
            stamina -= STAMINA_DRAIN_RATE * deltaTime;
            if (stamina <= 0) {
                stamina = 0;
                isClimbing = false; // 体力耗尽，停止攀爬
                System.out.println("体力耗尽！停止攀爬");
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
        stamina = MAX_STAMINA; // 重生时恢复体力
        isDashing = false;
        dashTimer = 0;
        isClimbing = false; // 重生时停止攀爬
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
        isClimbing = false; // 冲刺时停止攀爬
        
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
        } else if (KeyBindings.isClimbKey(keyCode)) {
            climbPressed = true;
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
        } else if (KeyBindings.isClimbKey(keyCode)) {
            climbPressed = false;
            // 松开攀爬键时停止攀爬
            isClimbing = false;
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
        
        // 绘制体力条（在所有状态下都可能需要显示）
        renderStaminaBar(g);
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
        
        // 检查是否加速下落（自由落体时按S键，排除冲刺、攀墙、沿墙滑落状态）
        boolean isFastFalling = !onGround && !isDashing && !isClimbing && !isTouchingWall && downPressed && velocityY > 0;
        int renderWidth = isFastFalling ? PLAYER_WIDTH / 2 : PLAYER_WIDTH; // 加速下落时宽度减半
        int renderX = isFastFalling ? (int)x + PLAYER_WIDTH / 4 : (int)x; // 居中显示
        
        // 绘制玩家身体（碰撞箱下半部分）
        g.setColor(bodyColor);
        g.fillRect(renderX, (int)y + PLAYER_HEIGHT / 2, renderWidth, PLAYER_HEIGHT / 2);
        
        // 绘制玩家头部（碰撞箱上半部分）
        g.setColor(headColor);
        g.fillRect(renderX, (int)y, renderWidth, PLAYER_HEIGHT / 2);
        
        // 绘制边框（始终使用完整碰撞箱）
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
    
    /**
     * 绘制体力条
     */
    private void renderStaminaBar(Graphics g) {
        if (isTouchingWall || isClimbing) {
            // 体力条位置（玩家上方）
            int barX = (int)x - 10;
            int barY = (int)y - 15;
            int barWidth = PLAYER_WIDTH + 20;
            int barHeight = 6;
            
            // 背景（黑色）
            g.setColor(Color.BLACK);
            g.fillRect(barX, barY, barWidth, barHeight);
            
            // 体力条（绿色到红色渐变）
            int staminaWidth = (int)(barWidth * stamina / MAX_STAMINA);
            if (stamina > 50) {
                g.setColor(new Color(0, 255, 0)); // 绿色
            } else if (stamina > 25) {
                g.setColor(new Color(255, 255, 0)); // 黄色
            } else {
                g.setColor(new Color(255, 0, 0)); // 红色
            }
            g.fillRect(barX + 1, barY + 1, staminaWidth - 2, barHeight - 2);
            
            // 体力数值
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(10f));
            g.drawString((int)stamina + "/" + (int)MAX_STAMINA, barX, barY - 2);
        }
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public boolean isOnGround() { return onGround; }
    public boolean isDashing() { return isDashing; }
    public int getDashCount() { return dashCount; }
    public boolean isClimbing() { return isClimbing; }
    public boolean isTouchingWall() { return isTouchingWall; }
    public double getStamina() { return stamina; }
}

