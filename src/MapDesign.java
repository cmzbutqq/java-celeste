import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * 地图设计管理类
 * 负责管理游戏中的平台、实心物块和尖刺的布局
 * 支持动态地图创建和配置
 */
public class MapDesign {
    
    /**
     * 地图数据类 - 包含所有地图元素
     */
    public static class MapData {
        public List<Platform> platforms;
        public List<SolidBlock> solidBlocks;
        public List<Spike> spikes;
        public List<Checkpoint> checkpoints;
        public List<EnergyBean> energyBeans;
        
        public MapData() {
            this.platforms = new ArrayList<>();
            this.solidBlocks = new ArrayList<>();
            this.spikes = new ArrayList<>();
            this.checkpoints = new ArrayList<>();
            this.energyBeans = new ArrayList<>();
        }
        
        /**
         * 渲染所有地图元素
         */
        public void renderAll(Graphics g) {
            // 渲染所有平台
            for (Platform platform : platforms) {
                platform.render(g);
            }
            
            // 渲染所有实心物块
            for (SolidBlock block : solidBlocks) {
                block.render(g);
            }
            
            // 渲染所有尖刺
            for (Spike spike : spikes) {
                spike.render(g);
            }
            
            // 渲染所有重生点
            for (Checkpoint checkpoint : checkpoints) {
                checkpoint.render(g);
            }
            
            // 渲染所有能量豆
            for (EnergyBean energyBean : energyBeans) {
                energyBean.render(g);
            }
        }
        
        /**
         * 获取所有地图元素的总数
         */
        public int getTotalElementCount() {
            return platforms.size() + solidBlocks.size() + spikes.size() + checkpoints.size() + energyBeans.size();
        }
        
        /**
         * 获取所有地图元素的详细信息
         */
        public String getAllElementsInfo() {
            StringBuilder info = new StringBuilder();
            info.append("=== 地图元素详情 ===\n");
            
            info.append("平台 (").append(platforms.size()).append("个):\n");
            for (int i = 0; i < platforms.size(); i++) {
                info.append("  ").append(i + 1).append(". ").append(platforms.get(i).getInfo()).append("\n");
            }
            
            info.append("实心物块 (").append(solidBlocks.size()).append("个):\n");
            for (int i = 0; i < solidBlocks.size(); i++) {
                info.append("  ").append(i + 1).append(". ").append(solidBlocks.get(i).getInfo()).append("\n");
            }
            
            info.append("尖刺 (").append(spikes.size()).append("个):\n");
            for (int i = 0; i < spikes.size(); i++) {
                info.append("  ").append(i + 1).append(". ").append(spikes.get(i).getInfo()).append("\n");
            }
            
            info.append("重生点 (").append(checkpoints.size()).append("个):\n");
            for (int i = 0; i < checkpoints.size(); i++) {
                info.append("  ").append(i + 1).append(". ").append(checkpoints.get(i).getInfo()).append("\n");
            }
            
            info.append("能量豆 (").append(energyBeans.size()).append("个):\n");
            for (int i = 0; i < energyBeans.size(); i++) {
                info.append("  ").append(i + 1).append(". ").append(energyBeans.get(i).getInfo()).append("\n");
            }
            
            return info.toString();
        }
    }
    
    /**
     * 地图构建器 - 用于动态创建地图
     */
    public static class MapBuilder {
        private final MapData mapData;
        
        public MapBuilder() {
            this.mapData = new MapData();
        }
        
        /**
         * 添加平台
         */
        public MapBuilder addPlatform(int x, int y, int width, int height) {
            mapData.platforms.add(new Platform(x, y, width, height));
            return this;
        }
        
        /**
         * 添加实心物块
         */
        public MapBuilder addSolidBlock(int x, int y, int width, int height) {
            mapData.solidBlocks.add(new SolidBlock(x, y, width, height));
            return this;
        }
        
        /**
         * 添加尖刺
         */
        public MapBuilder addSpike(int x, int y, int width, int height) {
            mapData.spikes.add(new Spike(x, y, width, height));
            return this;
        }
        
        /**
         * 添加重生点
         */
        public MapBuilder addCheckpoint(int x, int y, int width, int height, 
                                      int respawnOffsetX, int respawnOffsetY, 
                                      boolean defaultActivated) {
            mapData.checkpoints.add(new Checkpoint(x, y, width, height, 
                                                 respawnOffsetX, respawnOffsetY, 
                                                 defaultActivated));
            return this;
        }
        
        /**
         * 添加能量豆
         */
        public MapBuilder addEnergyBean(int x, int y) {
            mapData.energyBeans.add(new EnergyBean(x, y));
            return this;
        }
        
        /**
         * 添加能量豆（指定大小）
         */
        public MapBuilder addEnergyBean(int x, int y, int size) {
            mapData.energyBeans.add(new EnergyBean(x, y, size));
            return this;
        }
        
        /**
         * 构建地图
         */
        public MapData build() {
            return mapData;
        }
    }
    
    
    /**
     * 从JSON配置文件创建地图
     */
    public static MapData createMapFromConfig(String configPath) {
        return JsonMapLoader.loadMapFromJsonOrDefault(configPath);
    }
    
    
    /**
     * 创建空地图
     */
    public static MapData createEmptyMap() {
        return new MapData();
    }
    
    /**
     * 获取地图统计信息
     */
    public static String getMapStats(MapData mapData) {
        return String.format("地图统计: 平台%d个, 实心物块%d个, 尖刺%d个, 重生点%d个, 能量豆%d个, 总计%d个元素", 
            mapData.platforms.size(), 
            mapData.solidBlocks.size(), 
            mapData.spikes.size(),
            mapData.checkpoints.size(),
            mapData.energyBeans.size(),
            mapData.getTotalElementCount());
    }
    
    /**
     * 获取所有地图元素的通用信息
     */
    public static String getAllElementsInfo(MapData mapData) {
        return mapData.getAllElementsInfo();
    }
}
