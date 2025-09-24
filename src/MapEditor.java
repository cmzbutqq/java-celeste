import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 地图编辑器主类
 * 提供可视化地图编辑功能，与游戏相对独立
 */
public class MapEditor extends JFrame {
    private static final int WINDOW_WIDTH = 2200;
    private static final int WINDOW_HEIGHT = 1200;
    private static final int CANVAS_WIDTH = 1920;
    private static final int CANVAS_HEIGHT = 1080;
    
    // 编辑器状态
    private enum EditMode {
        PLATFORM, SOLID_BLOCK, SPIKE, CHECKPOINT, SELECT, DELETE
    }
    
    private EditMode currentMode = EditMode.PLATFORM;
    private MapDesign.MapData currentMap;
    private MapElement selectedElement = null;
    private boolean isDragging = false;
    private Point dragStart = null;
    private Point lastMousePos = null;
    
    // UI组件
    private MapCanvas mapCanvas;
    private JPanel toolbar;
    private JLabel statusLabel;
    private JTextField mapNameField;
    private JTextField mapDescField;
    
    // 地图元素列表
    private JList<String> elementList;
    private DefaultListModel<String> elementListModel;
    
    public MapEditor() {
        setTitle("Java Celeste 地图编辑器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // 初始化地图数据
        currentMap = MapDesign.createEmptyMap();
        
        // 创建UI
        createUI();
        
        // 设置键盘快捷键
        setupKeyboardShortcuts();
        
        // 更新状态
        updateStatus();
    }
    
    /**
     * 创建用户界面
     */
    private void createUI() {
        setLayout(new BorderLayout());
        
        // 创建工具栏
        createToolbar();
        
        // 创建主编辑区域
        createMainArea();
        
        // 创建状态栏
        createStatusBar();
    }
    
    /**
     * 创建工具栏
     */
    private void createToolbar() {
        toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createTitledBorder("工具"));
        
        // 模式选择按钮
        JButton platformBtn = new JButton("平台");
        platformBtn.addActionListener(e -> setEditMode(EditMode.PLATFORM));
        platformBtn.setBackground(Color.GREEN);
        
        JButton blockBtn = new JButton("实心物块");
        blockBtn.addActionListener(e -> setEditMode(EditMode.SOLID_BLOCK));
        blockBtn.setBackground(Color.BLUE);
        
        JButton spikeBtn = new JButton("尖刺");
        spikeBtn.addActionListener(e -> setEditMode(EditMode.SPIKE));
        spikeBtn.setBackground(Color.RED);
        
        JButton checkpointBtn = new JButton("重生点");
        checkpointBtn.addActionListener(e -> setEditMode(EditMode.CHECKPOINT));
        checkpointBtn.setBackground(new Color(100, 150, 255));
        
        JButton selectBtn = new JButton("选择");
        selectBtn.addActionListener(e -> setEditMode(EditMode.SELECT));
        selectBtn.setBackground(Color.YELLOW);
        
        JButton deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(e -> setEditMode(EditMode.DELETE));
        deleteBtn.setBackground(Color.ORANGE);
        
        toolbar.add(platformBtn);
        toolbar.add(blockBtn);
        toolbar.add(spikeBtn);
        toolbar.add(checkpointBtn);
        toolbar.add(selectBtn);
        toolbar.add(deleteBtn);
        
        // 文件操作按钮
        JButton newBtn = new JButton("新建");
        newBtn.addActionListener(e -> newMap());
        
        JButton loadBtn = new JButton("加载");
        loadBtn.addActionListener(e -> loadMap());
        
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> saveMap());
        
        toolbar.add(new JSeparator());
        toolbar.add(newBtn);
        toolbar.add(loadBtn);
        toolbar.add(saveBtn);
        
        add(toolbar, BorderLayout.NORTH);
    }
    
    /**
     * 创建主编辑区域
     */
    private void createMainArea() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // 左侧：地图画布
        mapCanvas = new MapCanvas();
        mapCanvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        mapCanvas.setBorder(BorderFactory.createTitledBorder("地图编辑区域"));
        
        // 右侧：属性面板
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // 地图信息
        JPanel mapInfoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        mapInfoPanel.setBorder(BorderFactory.createTitledBorder("地图信息"));
        mapInfoPanel.add(new JLabel("地图名称:"));
        mapNameField = new JTextField("新地图");
        mapInfoPanel.add(mapNameField);
        mapInfoPanel.add(new JLabel("描述:"));
        mapDescField = new JTextField("地图描述");
        mapInfoPanel.add(mapDescField);
        mapInfoPanel.add(new JLabel("元素总数:"));
        JLabel elementCountLabel = new JLabel("0");
        mapInfoPanel.add(elementCountLabel);
        
        // 元素列表
        JPanel elementPanel = new JPanel(new BorderLayout());
        elementPanel.setBorder(BorderFactory.createTitledBorder("地图元素"));
        elementListModel = new DefaultListModel<>();
        elementList = new JList<>(elementListModel);
        elementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectElementFromList();
            }
        });
        JScrollPane elementScrollPane = new JScrollPane(elementList);
        elementPanel.add(elementScrollPane, BorderLayout.CENTER);
        
        // 元素操作按钮
        JPanel elementButtonPanel = new JPanel(new FlowLayout());
        JButton deleteElementBtn = new JButton("删除选中");
        deleteElementBtn.addActionListener(e -> deleteSelectedElement());
        JButton duplicateElementBtn = new JButton("复制选中");
        duplicateElementBtn.addActionListener(e -> duplicateSelectedElement());
        elementButtonPanel.add(deleteElementBtn);
        elementButtonPanel.add(duplicateElementBtn);
        elementPanel.add(elementButtonPanel, BorderLayout.SOUTH);
        
        rightPanel.add(mapInfoPanel, BorderLayout.NORTH);
        rightPanel.add(elementPanel, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(mapCanvas);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(CANVAS_WIDTH);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建状态栏
     */
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusLabel = new JLabel("就绪");
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        JLabel modeLabel = new JLabel("模式: " + currentMode.toString());
        statusBar.add(modeLabel, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * 设置键盘快捷键
     */
    private void setupKeyboardShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        // 模式切换快捷键
        inputMap.put(KeyStroke.getKeyStroke("1"), "platform");
        inputMap.put(KeyStroke.getKeyStroke("2"), "solidBlock");
        inputMap.put(KeyStroke.getKeyStroke("3"), "spike");
        inputMap.put(KeyStroke.getKeyStroke("4"), "checkpoint");
        inputMap.put(KeyStroke.getKeyStroke("5"), "select");
        inputMap.put(KeyStroke.getKeyStroke("6"), "delete");
        
        actionMap.put("platform", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.PLATFORM); }
        });
        actionMap.put("solidBlock", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.SOLID_BLOCK); }
        });
        actionMap.put("spike", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.SPIKE); }
        });
        actionMap.put("checkpoint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.CHECKPOINT); }
        });
        actionMap.put("select", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.SELECT); }
        });
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { setEditMode(EditMode.DELETE); }
        });
        
        // 文件操作快捷键
        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "new");
        inputMap.put(KeyStroke.getKeyStroke("ctrl O"), "open");
        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
        
        actionMap.put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { newMap(); }
        });
        actionMap.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { loadMap(); }
        });
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { saveMap(); }
        });
    }
    
    /**
     * 设置编辑模式
     */
    private void setEditMode(EditMode mode) {
        currentMode = mode;
        selectedElement = null;
        updateStatus();
        mapCanvas.repaint();
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus() {
        statusLabel.setText(String.format("模式: %s | 元素总数: %d", 
            currentMode.toString(), currentMap.getTotalElementCount()));
        updateElementList();
    }
    
    /**
     * 更新元素列表
     */
    private void updateElementList() {
        elementListModel.clear();
        
        for (int i = 0; i < currentMap.platforms.size(); i++) {
            Platform p = currentMap.platforms.get(i);
            elementListModel.addElement(String.format("平台 %d: (%d,%d) %dx%d", 
                i + 1, p.getX(), p.getY(), p.getWidth(), p.getHeight()));
        }
        
        for (int i = 0; i < currentMap.solidBlocks.size(); i++) {
            SolidBlock b = currentMap.solidBlocks.get(i);
            elementListModel.addElement(String.format("实心物块 %d: (%d,%d) %dx%d", 
                i + 1, b.getX(), b.getY(), b.getWidth(), b.getHeight()));
        }
        
        for (int i = 0; i < currentMap.spikes.size(); i++) {
            Spike s = currentMap.spikes.get(i);
            elementListModel.addElement(String.format("尖刺 %d: (%d,%d) %dx%d", 
                i + 1, s.getX(), s.getY(), s.getWidth(), s.getHeight()));
        }
    }
    
    /**
     * 从列表选择元素
     */
    private void selectElementFromList() {
        int selectedIndex = elementList.getSelectedIndex();
        if (selectedIndex >= 0) {
            // 计算实际元素索引
            int platformCount = currentMap.platforms.size();
            int solidBlockCount = currentMap.solidBlocks.size();
            
            if (selectedIndex < platformCount) {
                selectedElement = currentMap.platforms.get(selectedIndex);
            } else if (selectedIndex < platformCount + solidBlockCount) {
                selectedElement = currentMap.solidBlocks.get(selectedIndex - platformCount);
            } else {
                selectedElement = currentMap.spikes.get(selectedIndex - platformCount - solidBlockCount);
            }
            
            mapCanvas.repaint();
        }
    }
    
    /**
     * 删除选中的元素
     */
    private void deleteSelectedElement() {
        if (selectedElement != null) {
            if (selectedElement instanceof Platform) {
                currentMap.platforms.remove(selectedElement);
            } else if (selectedElement instanceof SolidBlock) {
                currentMap.solidBlocks.remove(selectedElement);
            } else if (selectedElement instanceof Spike) {
                currentMap.spikes.remove(selectedElement);
            }
            selectedElement = null;
            updateStatus();
            mapCanvas.repaint();
        }
    }
    
    /**
     * 复制选中的元素
     */
    private void duplicateSelectedElement() {
        if (selectedElement != null) {
            if (selectedElement instanceof Platform) {
                Platform p = (Platform) selectedElement;
                currentMap.platforms.add(new Platform(p.getX() + 50, p.getY() + 50, p.getWidth(), p.getHeight()));
            } else if (selectedElement instanceof SolidBlock) {
                SolidBlock b = (SolidBlock) selectedElement;
                currentMap.solidBlocks.add(new SolidBlock(b.getX() + 50, b.getY() + 50, b.getWidth(), b.getHeight()));
            } else if (selectedElement instanceof Spike) {
                Spike s = (Spike) selectedElement;
                currentMap.spikes.add(new Spike(s.getX() + 50, s.getY() + 50, s.getWidth(), s.getHeight()));
            }
            updateStatus();
            mapCanvas.repaint();
        }
    }
    
    /**
     * 新建地图
     */
    private void newMap() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要新建地图吗？当前未保存的更改将丢失。", 
            "新建地图", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            currentMap = MapDesign.createEmptyMap();
            mapNameField.setText("新地图");
            mapDescField.setText("地图描述");
            selectedElement = null;
            updateStatus();
            mapCanvas.repaint();
        }
    }
    
    /**
     * 加载地图
     */
    private void loadMap() {
        JFileChooser fileChooser = new JFileChooser("maps");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON地图文件", "json"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                JsonMapLoader.MapConfig config = new ObjectMapper().readValue(file, JsonMapLoader.MapConfig.class);
                
                currentMap = MapDesign.createMapFromConfig(file.getPath());
                mapNameField.setText(config.name);
                mapDescField.setText(config.description);
                selectedElement = null;
                updateStatus();
                mapCanvas.repaint();
                
                JOptionPane.showMessageDialog(this, "地图加载成功！");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "加载地图失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 保存地图
     */
    private void saveMap() {
        JFileChooser fileChooser = new JFileChooser("maps");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON地图文件", "json"));
        fileChooser.setSelectedFile(new File(mapNameField.getText() + ".json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                saveMapToFile(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "地图保存成功！");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "保存地图失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 保存地图到文件
     */
    private void saveMapToFile(File file) throws IOException {
        JsonMapLoader.MapConfig config = new JsonMapLoader.MapConfig();
        config.name = mapNameField.getText();
        config.description = mapDescField.getText();
        
        // 转换地图元素
        config.platforms = new ArrayList<>();
        for (Platform p : currentMap.platforms) {
            JsonMapLoader.ElementData data = new JsonMapLoader.ElementData();
            data.x = p.getX();
            data.y = p.getY();
            data.width = p.getWidth();
            data.height = p.getHeight();
            data.comment = "平台";
            config.platforms.add(data);
        }
        
        config.solidBlocks = new ArrayList<>();
        for (SolidBlock b : currentMap.solidBlocks) {
            JsonMapLoader.ElementData data = new JsonMapLoader.ElementData();
            data.x = b.getX();
            data.y = b.getY();
            data.width = b.getWidth();
            data.height = b.getHeight();
            data.comment = "实心物块";
            config.solidBlocks.add(data);
        }
        
        config.spikes = new ArrayList<>();
        for (Spike s : currentMap.spikes) {
            JsonMapLoader.ElementData data = new JsonMapLoader.ElementData();
            data.x = s.getX();
            data.y = s.getY();
            data.width = s.getWidth();
            data.height = s.getHeight();
            data.comment = "尖刺";
            config.spikes.add(data);
        }
        
        config.checkpoints = new ArrayList<>();
        for (Checkpoint c : currentMap.checkpoints) {
            JsonMapLoader.CheckpointData data = new JsonMapLoader.CheckpointData();
            data.x = c.getX();
            data.y = c.getY();
            data.width = c.getWidth();
            data.height = c.getHeight();
            data.respawnOffsetX = c.getRespawnOffsetX();
            data.respawnOffsetY = c.getRespawnOffsetY();
            data.defaultActivated = c.isDefaultActivated();
            data.comment = "重生点";
            config.checkpoints.add(data);
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(file, config);
    }
    
    
    /**
     * 地图画布类
     */
    private class MapCanvas extends JPanel {
        public MapCanvas() {
            setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
            setBackground(new Color(135, 206, 235)); // 天蓝色背景
            
            // 添加鼠标监听器
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMousePressed(e);
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    handleMouseReleased(e);
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouseDragged(e);
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // 绘制网格
            drawGrid(g);
            
            // 绘制地面
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, CANVAS_HEIGHT - 50, CANVAS_WIDTH, 50);
            
            // 绘制所有地图元素
            currentMap.renderAll(g);
            
            // 绘制选中的元素高亮
            if (selectedElement != null) {
                g.setColor(Color.YELLOW);
                g.drawRect(selectedElement.getX() - 2, selectedElement.getY() - 2, 
                    selectedElement.getWidth() + 4, selectedElement.getHeight() + 4);
            }
            
            // 绘制当前模式提示
            drawModeHint(g);
        }
        
        /**
         * 绘制网格
         */
        private void drawGrid(Graphics g) {
            g.setColor(new Color(200, 200, 200, 100));
            int gridSize = 20;
            
            for (int x = 0; x < CANVAS_WIDTH; x += gridSize) {
                g.drawLine(x, 0, x, CANVAS_HEIGHT);
            }
            
            for (int y = 0; y < CANVAS_HEIGHT; y += gridSize) {
                g.drawLine(0, y, CANVAS_WIDTH, y);
            }
        }
        
        /**
         * 绘制模式提示
         */
        private void drawModeHint(Graphics g) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            
            String hint = "";
            switch (currentMode) {
                case PLATFORM:
                    hint = "点击并拖拽创建平台";
                    break;
                case SOLID_BLOCK:
                    hint = "点击并拖拽创建实心物块";
                    break;
                case SPIKE:
                    hint = "点击并拖拽创建尖刺";
                    break;
                case CHECKPOINT:
                    hint = "点击并拖拽创建重生点";
                    break;
                case SELECT:
                    hint = "点击选择元素";
                    break;
                case DELETE:
                    hint = "点击删除元素";
                    break;
            }
            
            g.drawString(hint, 10, 30);
        }
        
        /**
         * 处理鼠标按下
         */
        private void handleMousePressed(MouseEvent e) {
            Point mousePos = e.getPoint();
            
            if (currentMode == EditMode.SELECT) {
                // 选择模式：查找点击的元素
                selectedElement = findElementAt(mousePos);
                repaint();
            } else if (currentMode == EditMode.DELETE) {
                // 删除模式：删除点击的元素
                MapElement element = findElementAt(mousePos);
                if (element != null) {
                    if (element instanceof Platform) {
                        currentMap.platforms.remove(element);
                    } else if (element instanceof SolidBlock) {
                        currentMap.solidBlocks.remove(element);
                    } else if (element instanceof Spike) {
                        currentMap.spikes.remove(element);
                    } else if (element instanceof Checkpoint) {
                        currentMap.checkpoints.remove(element);
                    }
                    updateStatus();
                    repaint();
                }
            } else {
                // 创建模式：开始拖拽
                isDragging = true;
                dragStart = mousePos;
                lastMousePos = mousePos;
            }
        }
        
        /**
         * 处理鼠标释放
         */
        private void handleMouseReleased(MouseEvent e) {
            if (isDragging && dragStart != null) {
                Point mousePos = e.getPoint();
                
                // 创建新元素
                if (currentMode == EditMode.PLATFORM) {
                    createPlatform(dragStart, mousePos);
                } else if (currentMode == EditMode.SOLID_BLOCK) {
                    createSolidBlock(dragStart, mousePos);
                } else if (currentMode == EditMode.SPIKE) {
                    createSpike(dragStart, mousePos);
                } else if (currentMode == EditMode.CHECKPOINT) {
                    createCheckpoint(dragStart, mousePos);
                }
                
                isDragging = false;
                dragStart = null;
                updateStatus();
                repaint();
            }
        }
        
        /**
         * 处理鼠标拖拽
         */
        private void handleMouseDragged(MouseEvent e) {
            if (isDragging && selectedElement != null) {
                // 拖拽选中的元素
                Point mousePos = e.getPoint();
                int deltaX = mousePos.x - lastMousePos.x;
                int deltaY = mousePos.y - lastMousePos.y;
                
                selectedElement.setPosition(
                    selectedElement.getX() + deltaX,
                    selectedElement.getY() + deltaY
                );
                
                lastMousePos = mousePos;
                repaint();
            }
        }
        
        /**
         * 查找指定位置的地图元素
         */
        private MapElement findElementAt(Point pos) {
            // 检查平台
            for (Platform p : currentMap.platforms) {
                if (pos.x >= p.getX() && pos.x <= p.getX() + p.getWidth() &&
                    pos.y >= p.getY() && pos.y <= p.getY() + p.getHeight()) {
                    return p;
                }
            }
            
            // 检查实心物块
            for (SolidBlock b : currentMap.solidBlocks) {
                if (pos.x >= b.getX() && pos.x <= b.getX() + b.getWidth() &&
                    pos.y >= b.getY() && pos.y <= b.getY() + b.getHeight()) {
                    return b;
                }
            }
            
            // 检查尖刺
            for (Spike s : currentMap.spikes) {
                if (pos.x >= s.getX() && pos.x <= s.getX() + s.getWidth() &&
                    pos.y >= s.getY() && pos.y <= s.getY() + s.getHeight()) {
                    return s;
                }
            }
            
            // 检查重生点
            for (Checkpoint c : currentMap.checkpoints) {
                if (pos.x >= c.getX() && pos.x <= c.getX() + c.getWidth() &&
                    pos.y >= c.getY() && pos.y <= c.getY() + c.getHeight()) {
                    return c;
                }
            }
            
            return null;
        }
        
        /**
         * 创建平台
         */
        private void createPlatform(Point start, Point end) {
            int x = Math.min(start.x, end.x);
            int y = Math.min(start.y, end.y);
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);
            
            if (width > 10 && height > 10) { // 最小尺寸限制
                currentMap.platforms.add(new Platform(x, y, width, height));
            }
        }
        
        /**
         * 创建实心物块
         */
        private void createSolidBlock(Point start, Point end) {
            int x = Math.min(start.x, end.x);
            int y = Math.min(start.y, end.y);
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);
            
            if (width > 10 && height > 10) { // 最小尺寸限制
                currentMap.solidBlocks.add(new SolidBlock(x, y, width, height));
            }
        }
        
        /**
         * 创建尖刺
         */
        private void createSpike(Point start, Point end) {
            int x = Math.min(start.x, end.x);
            int y = Math.min(start.y, end.y);
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);
            
            if (width > 5 && height > 5) { // 最小尺寸限制
                currentMap.spikes.add(new Spike(x, y, width, height));
            }
        }
        
        private void createCheckpoint(Point start, Point end) {
            int x = Math.min(start.x, end.x);
            int y = Math.min(start.y, end.y);
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);
            
            if (width > 20 && height > 20) { // 最小尺寸限制
                // 默认重生点偏移到激活框中心
                int respawnOffsetX = width / 2;
                int respawnOffsetY = height / 2;
                
                // 弹出对话框设置重生点属性
                CheckpointDialog dialog = new CheckpointDialog(MapEditor.this, respawnOffsetX, respawnOffsetY, false);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    currentMap.checkpoints.add(new Checkpoint(x, y, width, height, 
                        dialog.getRespawnOffsetX(), dialog.getRespawnOffsetY(), 
                        dialog.isDefaultActivated()));
                }
            }
        }
    }
    
    /**
     * 重生点属性设置对话框
     */
    private static class CheckpointDialog extends JDialog {
        private JTextField respawnOffsetXField;
        private JTextField respawnOffsetYField;
        private javax.swing.JCheckBox defaultActivatedCheckBox;
        private boolean confirmed = false;
        
        public CheckpointDialog(JFrame parent, int respawnOffsetX, int respawnOffsetY, boolean defaultActivated) {
            super(parent, "重生点属性", true);
            setSize(300, 200);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            panel.add(new JLabel("重生点X偏移:"));
            respawnOffsetXField = new JTextField(String.valueOf(respawnOffsetX));
            panel.add(respawnOffsetXField);
            
            panel.add(new JLabel("重生点Y偏移:"));
            respawnOffsetYField = new JTextField(String.valueOf(respawnOffsetY));
            panel.add(respawnOffsetYField);
            
            panel.add(new JLabel("默认激活:"));
            defaultActivatedCheckBox = new javax.swing.JCheckBox();
            defaultActivatedCheckBox.setSelected(defaultActivated);
            panel.add(defaultActivatedCheckBox);
            
            JButton okButton = new JButton("确定");
            okButton.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            JButton cancelButton = new JButton("取消");
            cancelButton.addActionListener(e -> dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public int getRespawnOffsetX() {
            try {
                return Integer.parseInt(respawnOffsetXField.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        public int getRespawnOffsetY() {
            try {
                return Integer.parseInt(respawnOffsetYField.getText());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        public boolean isDefaultActivated() {
            return defaultActivatedCheckBox.isSelected();
        }
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MapEditor().setVisible(true);
        });
    }
}
