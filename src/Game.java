// 简化项目结构，移除包声明

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 游戏主类 - 横版跳跃游戏
 */
public class Game extends JFrame implements KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int FPS = 60;
    
    private final GamePanel gamePanel;
    private final Player player;
    private final boolean running = true;
    private SolidBlock[] solidBlocks;
    private Platform[] platforms;
    private Spike[] spikes;
    private MapDesign.MapData currentMap;
    
    public Game() {
        setTitle("Java Celeste - 横版跳跃游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // 创建游戏面板
        gamePanel = new GamePanel();
        add(gamePanel);
        
        // 设置窗口大小
        pack();
        setLocationRelativeTo(null);
        
        // 初始化游戏对象
        player = new Player(100, 400);
        
        // 加载地图设计
        loadMapFromJson("maps/default.json");
        player.setPlatforms(platforms);
        player.setSolidBlocks(solidBlocks);
        player.setSpikes(spikes);
        player.setRespawnPoint(100, 400); // 设置重生点
        
        // 添加键盘监听
        addKeyListener(this);
        setFocusable(true);
        
        // 显示按键绑定配置
        KeyBindings.printKeyBindings();
        
        // 启动游戏循环
        startGameLoop();
    }
    
    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            double nsPerFrame = 1000000000.0 / FPS;
            
            while (running) {
                long now = System.nanoTime();
                double deltaTime = (now - lastTime) / nsPerFrame;
                lastTime = now;
                
                // 更新游戏逻辑
                update(deltaTime);
                
                // 重绘画面
                repaint();
                
                // 控制帧率
                try {
                    Thread.sleep((long) (1000.0 / FPS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        gameThread.start();
    }
    
    private void update(double deltaTime) {
        player.update(deltaTime);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // 不需要实现
    }
    
    /**
     * 游戏面板 - 负责渲染
     */
    private class GamePanel extends JPanel {
        public GamePanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setBackground(Color.CYAN);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // 绘制背景
            g.setColor(new Color(135, 206, 235)); // 天蓝色
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // 绘制地面
            g.setColor(new Color(34, 139, 34)); // 森林绿
            g.fillRect(0, WINDOW_HEIGHT - 50, WINDOW_WIDTH, 50);
            
            // 绘制平台
            for (Platform platform : platforms) {
                platform.render(g);
            }
            
            // 绘制实心物块
            for (SolidBlock block : solidBlocks) {
                block.render(g);
            }
            
            // 绘制尖刺
            for (Spike spike : spikes) {
                spike.render(g);
            }
            
            // 绘制玩家
            player.render(g);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Game().setVisible(true);
        });
    }
    
    /**
     * 加载地图设计
     */
    private void loadMap(MapDesign.MapData mapData) {
        currentMap = mapData;
        
        // 转换List为Array
        platforms = mapData.platforms.toArray(new Platform[0]);
        solidBlocks = mapData.solidBlocks.toArray(new SolidBlock[0]);
        spikes = mapData.spikes.toArray(new Spike[0]);
        
        // 打印地图统计信息
        System.out.println(MapDesign.getMapStats(mapData));
    }
    
    /**
     * 从JSON文件加载地图
     */
    private void loadMapFromJson(String jsonPath) {
        System.out.println("正在加载地图: " + jsonPath);
        System.out.println(JsonMapLoader.getMapInfo(jsonPath));
        
        MapDesign.MapData mapData = MapDesign.createMapFromConfig(jsonPath);
        loadMap(mapData);
    }
    
}
