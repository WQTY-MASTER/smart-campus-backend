package com.qfedu.smartcampusseckill.controller.admin;

import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.service.SeckillConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端：抢课全局配置（开始时间/结束时间/开关）。
 *
 * 说明：
 * - 配置存储在 system_config 表中
 * - 时间格式要求：yyyy-MM-dd HH:mm:ss
 */
@RestController
@RequestMapping("/admin/config")
@CrossOrigin(origins = "*")
public class AdminSeckillConfigController {

    private final SeckillConfigService seckillConfigService;

    public AdminSeckillConfigController(SeckillConfigService seckillConfigService) {
        this.seckillConfigService = seckillConfigService;
    }

    /**
     * 查询抢课配置。
     */
    @GetMapping("/seckill")
    public Result<Map<String, String>> get() {
        return Result.success(seckillConfigService.getSeckillConfig());
    }

    /**
     * 更新抢课配置。
     *
     * @param start 全局开始时间（yyyy-MM-dd HH:mm:ss）
     * @param end   全局结束时间（yyyy-MM-dd HH:mm:ss）
     * @param onOff 开关：ON/OFF、1/0、true/false 均可（最终库里会落 1/0）
     */
    @PostMapping("/seckill")
    public Result<Void> update(@RequestParam String start,
                               @RequestParam String end,
                               @RequestParam String onOff,
                               @RequestParam(required = false) String dropDeadline) {
        // dropDeadline 可选：不传则保留原退课截止时间；传空串则取消退课时间限制
        seckillConfigService.updateSeckillConfig(start, end, onOff, dropDeadline);
        return Result.success(null);
    }
}

