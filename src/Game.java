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
    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private static final int FPS = 60;
    
    private final GamePanel gamePanel;
    private final Player player;
    private final boolean running = true;
    private MapElement[] mapElements; // 统一的地图元素数组
    private SolidBlock[] solidBlocks;
    private Platform[] platforms;
    private Spike[] spikes;
    private Checkpoint[] checkpoints;
    private EnergyBean[] energyBeans;
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
        
        // 加载地图设计
        loadMapFromJson("maps/default.json");
        
        // 获取初始重生点位置
        Checkpoint initialCheckpoint = getLatestActivatedCheckpoint();
        double initialX = 100; // 默认位置
        double initialY = 900;
        
        if (initialCheckpoint != null) {
            initialX = initialCheckpoint.getRespawnX();
            initialY = initialCheckpoint.getRespawnY();
            System.out.println("使用激活的重生点作为初始位置: (" + initialX + ", " + initialY + ")");
        } else {
            System.out.println("没有激活的重生点，使用默认初始位置: (" + initialX + ", " + initialY + ")");
        }
        
        // 初始化游戏对象
        player = new Player(initialX, initialY);
        
        // 设置地图元素
        player.setPlatforms(platforms);
        player.setSolidBlocks(solidBlocks);
        player.setSpikes(spikes);
        player.setCheckpoints(checkpoints);
        player.setEnergyBeans(energyBeans);
        
        // 设置统一的多态数组
        if (mapElements != null) {
            player.setMapElements(mapElements);
        }
        
        // 设置初始重生点（选择时间上最近激活的重生点）
        setInitialRespawnPoint();
        
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
        
        // 更新能量豆
        updateEnergyBeans(deltaTime);
        
        // 检查重生点激活
        checkCheckpointActivation();
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
            
            // 使用多态统一渲染所有地图元素
            renderMapElements(g);
            
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
        checkpoints = mapData.checkpoints.toArray(new Checkpoint[0]);
        energyBeans = mapData.energyBeans.toArray(new EnergyBean[0]);
        
        // 创建统一的多态数组
        createUnifiedMapElementsArray();
        
        // 打印地图统计信息
        System.out.println(MapDesign.getMapStats(mapData));
    }
    
    /**
     * 创建统一的多态地图元素数组
     */
    private void createUnifiedMapElementsArray() {
        // 计算总元素数量
        int totalElements = platforms.length + solidBlocks.length + spikes.length + 
                          checkpoints.length + energyBeans.length;
        
        // 创建统一数组
        mapElements = new MapElement[totalElements];
        int index = 0;
        
        // 添加所有元素到统一数组
        for (Platform platform : platforms) {
            mapElements[index++] = platform;
        }
        
        for (SolidBlock block : solidBlocks) {
            mapElements[index++] = block;
        }
        
        for (Spike spike : spikes) {
            mapElements[index++] = spike;
        }
        
        for (Checkpoint checkpoint : checkpoints) {
            mapElements[index++] = checkpoint;
        }
        
        for (EnergyBean energyBean : energyBeans) {
            mapElements[index++] = energyBean;
        }
        
        System.out.println("创建统一地图元素数组，包含 " + totalElements + " 个元素");
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
    
    /**
     * 设置初始重生点（选择时间上最近激活的重生点）
     */
    private void setInitialRespawnPoint() {
        Checkpoint latestActivatedCheckpoint = getLatestActivatedCheckpoint();
        
        if (latestActivatedCheckpoint != null) {
            player.setRespawnPoint(latestActivatedCheckpoint.getRespawnX(), latestActivatedCheckpoint.getRespawnY());
            System.out.println("设置初始重生点: (" + latestActivatedCheckpoint.getRespawnX() + ", " + latestActivatedCheckpoint.getRespawnY() + ")");
        } else {
            // 如果没有激活的重生点，使用默认位置
            player.setRespawnPoint(100, 900);
            System.out.println("没有激活的重生点，使用默认重生点: (100, 900)");
        }
    }
    
    /**
     * 更新能量豆
     */
    private void updateEnergyBeans(double deltaTime) {
        for (EnergyBean energyBean : energyBeans) {
            energyBean.update(deltaTime);
        }
    }
    
    /**
     * 检查重生点激活
     */
    private void checkCheckpointActivation() {
        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.isPlayerInActivationBox(player.getX(), player.getY(), 30, 40)) {
                if (!checkpoint.isActivated()) {
                    checkpoint.activate();
                    System.out.println("重生点已激活: (" + checkpoint.getRespawnX() + ", " + checkpoint.getRespawnY() + ")");
                }
            }
        }
    }
    
    /**
     * 获取时间上最近激活的重生点
     */
    public Checkpoint getLatestActivatedCheckpoint() {
        Checkpoint latestCheckpoint = null;
        long latestActivationTime = 0;
        
        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.isActivated() && checkpoint.getActivationTime() > latestActivationTime) {
                latestActivationTime = checkpoint.getActivationTime();
                latestCheckpoint = checkpoint;
            }
        }
        
        return latestCheckpoint;
    }
    
    /**
     * 使用多态统一渲染所有地图元素
     * @param g 图形上下文
     */
    private void renderMapElements(Graphics g) {
        if (mapElements != null) {
            // 使用多态渲染所有地图元素
            for (MapElement element : mapElements) {
                element.render(g);
            }
        } else {
            // 回退到原有的分别渲染方式（保持兼容性）
            for (Platform platform : platforms) {
                platform.render(g);
            }
            
            for (SolidBlock block : solidBlocks) {
                block.render(g);
            }
            
            for (Spike spike : spikes) {
                spike.render(g);
            }
            
            for (Checkpoint checkpoint : checkpoints) {
                checkpoint.render(g);
            }
            
            for (EnergyBean energyBean : energyBeans) {
                energyBean.render(g);
            }
        }
    }
    
}
