import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON地图加载器
 * 负责从JSON文件加载地图配置
 */
public class JsonMapLoader {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 地图配置数据结构
     */
    public static class MapConfig {
        @JsonProperty("name")
        public String name;
        
        @JsonProperty("description")
        public String description;
        
        @JsonProperty("platforms")
        public List<ElementData> platforms;
        
        @JsonProperty("solidBlocks")
        public List<ElementData> solidBlocks;
        
        @JsonProperty("spikes")
        public List<ElementData> spikes;
    }
    
    /**
     * 地图元素数据结构
     */
    public static class ElementData {
        @JsonProperty("x")
        public int x;
        
        @JsonProperty("y")
        public int y;
        
        @JsonProperty("width")
        public int width;
        
        @JsonProperty("height")
        public int height;
        
        @JsonProperty("comment")
        public String comment;
    }
    
    /**
     * 从JSON文件加载地图
     */
    public static MapDesign.MapData loadMapFromJson(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("地图文件不存在: " + filePath);
        }
        
        MapConfig config = objectMapper.readValue(file, MapConfig.class);
        return convertToMapData(config);
    }
    
    /**
     * 从JSON文件加载地图（带默认值）
     */
    public static MapDesign.MapData loadMapFromJsonOrDefault(String filePath) {
        try {
            return loadMapFromJson(filePath);
        } catch (IOException e) {
            System.err.println("加载地图失败: " + e.getMessage());
            System.out.println("使用空地图");
            return MapDesign.createEmptyMap();
        }
    }
    
    /**
     * 将配置转换为地图数据
     */
    private static MapDesign.MapData convertToMapData(MapConfig config) {
        MapDesign.MapBuilder builder = new MapDesign.MapBuilder();
        
        // 添加平台
        if (config.platforms != null) {
            for (ElementData platform : config.platforms) {
                builder.addPlatform(platform.x, platform.y, platform.width, platform.height);
            }
        }
        
        // 添加实心物块
        if (config.solidBlocks != null) {
            for (ElementData block : config.solidBlocks) {
                builder.addSolidBlock(block.x, block.y, block.width, block.height);
            }
        }
        
        // 添加尖刺
        if (config.spikes != null) {
            for (ElementData spike : config.spikes) {
                builder.addSpike(spike.x, spike.y, spike.width, spike.height);
            }
        }
        
        return builder.build();
    }
    
    /**
     * 获取地图信息
     */
    public static String getMapInfo(String filePath) {
        try {
            MapConfig config = objectMapper.readValue(new File(filePath), MapConfig.class);
            return String.format("地图: %s - %s", config.name, config.description);
        } catch (IOException e) {
            return "无法读取地图信息: " + e.getMessage();
        }
    }
    
}
