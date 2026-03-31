package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.SystemConfig;
import com.qfedu.smartcampusseckill.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 抢课全局配置服务：
 * - 从 system_config 表读取全局抢课时间段与开关
 * - 提供公共校验方法供抢课逻辑调用
 */
@Service
public class SeckillConfigService {

    public static final String KEY_START = "seckill_global_start";
    public static final String KEY_END = "seckill_global_end";
    public static final String KEY_SWITCH = "seckill_switch";
    /** 退课截止时间：yyyy-MM-dd HH:mm:ss；空表示不限制退课 */
    public static final String KEY_DROP_DEADLINE = "drop_deadline";

    /**
     * 推荐配置格式：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    /**
     * 判断当前是否允许抢课：开关开启 且 当前时间在全局时间段内。
     * 兼容开关值：ON/OFF、1/0、true/false。
     * <p>约定：{@code seckill_switch} 为 null/空/无法识别时，后端视为<strong>关闭</strong>，接口会拒绝抢课；
     * 学生端若在展示层把「未配置」默认当开启，仅影响按钮样式/提示，与接口判定可能不一致，需产品侧知情。
     */
    public boolean isSeckillAllowedNow() {
        String sw = getValue(KEY_SWITCH);
        if (!isSwitchOn(sw)) {
            return false;
        }

        String startStr = getValue(KEY_START);
        String endStr = getValue(KEY_END);
        if (startStr == null || endStr == null) {
            return false;
        }

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(startStr, FORMATTER);
            end = LocalDateTime.parse(endStr, FORMATTER);
        } catch (Exception e) {
            // 配置格式错误时直接视为不允许抢课，避免放行
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(start) && !now.isAfter(end);
    }

    /**
     * 当前是否允许退课：未配置退课截止时间，或当前时间未超过截止时间（含截止时刻仍可退）。
     */
    public boolean isDropAllowedNow() {
        String deadlineStr = getValue(KEY_DROP_DEADLINE);
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            return true;
        }
        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(deadlineStr.trim(), FORMATTER);
        } catch (Exception e) {
            return false;
        }
        return !LocalDateTime.now().isAfter(deadline);
    }

    /** 学生端 / JSON 驼峰别名，与下划线键值相同，便于前端兼容两种字段名 */
    public static final String KEY_START_CAMEL = "seckillGlobalStart";
    public static final String KEY_END_CAMEL = "seckillGlobalEnd";

    /**
     * 查询当前抢课配置（管理端、GET /api/config/seckill 共用）。
     * 含下划线键与驼峰键：seckill_global_start / seckillGlobalStart 等，值一致；未配置时为 null。
     */
    public Map<String, String> getSeckillConfig() {
        Map<String, String> map = new LinkedHashMap<>();
        String start = getValue(KEY_START);
        String end = getValue(KEY_END);
        map.put(KEY_START, start);
        map.put(KEY_END, end);
        map.put(KEY_START_CAMEL, start);
        map.put(KEY_END_CAMEL, end);
        map.put(KEY_SWITCH, getValue(KEY_SWITCH));
        map.put(KEY_DROP_DEADLINE, getValue(KEY_DROP_DEADLINE));
        return map;
    }

    private boolean isSwitchOn(String sw) {
        if (sw == null) {
            return false;
        }
        String v = sw.trim();
        return "ON".equalsIgnoreCase(v)
                || "1".equals(v)
                || "true".equalsIgnoreCase(v)
                || "YES".equalsIgnoreCase(v);
    }

    /**
     * 抢课总开关是否开启（与全局时间段无关，仅看 seckill_switch）。
     * 关闭时：不允许抢课、不允许退课。
     */
    public boolean isSeckillSwitchOn() {
        return isSwitchOn(getValue(KEY_SWITCH));
    }

    /**
     * 修改全局配置（管理端用）。
     */
    @Transactional
    public void updateSeckillConfig(String start, String end, String onOff) {
        String startV = trimToNull(start);
        String endV = trimToNull(end);
        String swV = trimToNull(onOff);

        if (startV == null || endV == null || swV == null) {
            throw new IllegalArgumentException("配置不能为空");
        }

        LocalDateTime s;
        LocalDateTime e;
        try {
            s = LocalDateTime.parse(startV, FORMATTER);
            e = LocalDateTime.parse(endV, FORMATTER);
        } catch (Exception ex) {
            throw new IllegalArgumentException("时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
        if (e.isBefore(s)) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }

        // 兼容保存：允许 ON/OFF、1/0、true/false，建议库里统一保存 1/0
        String normalizedSwitch = isSwitchOn(swV) ? "1" : "0";

        saveValue(KEY_START, startV);
        saveValue(KEY_END, endV);
        saveValue(KEY_SWITCH, normalizedSwitch);
    }

    /**
     * 更新退课截止时间（可单独调用；空字符串表示取消限制）。
     */
    @Transactional
    public void updateDropDeadline(String dropDeadline) {
        if (dropDeadline == null) {
            return;
        }
        String v = dropDeadline.trim();
        if (v.isEmpty()) {
            saveValue(KEY_DROP_DEADLINE, "");
            return;
        }
        try {
            LocalDateTime.parse(v, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("退课截止时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
        }
        saveValue(KEY_DROP_DEADLINE, v);
    }

    /**
     * 一次保存抢课配置 + 可选退课截止时间（管理端同一表单提交）。
     *
     * @param dropDeadline 可空：不传则不改；传空串则清空限制
     */
    @Transactional
    public void updateSeckillConfig(String start, String end, String onOff, String dropDeadline) {
        updateSeckillConfig(start, end, onOff);
        if (dropDeadline != null) {
            updateDropDeadline(dropDeadline);
        }
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private String getValue(String key) {
        Optional<SystemConfig> cfg = systemConfigRepository.findByConfigKey(key);
        return cfg.map(SystemConfig::getConfigValue).orElse(null);
    }

    private void saveValue(String key, String value) {
        SystemConfig cfg = systemConfigRepository.findByConfigKey(key)
                .orElseGet(() -> {
                    SystemConfig c = new SystemConfig();
                    c.setConfigKey(key);
                    return c;
                });
        cfg.setConfigValue(value);
        systemConfigRepository.save(cfg);
    }
}

