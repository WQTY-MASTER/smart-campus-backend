package com.qfedu.smartcampusseckill.controller.admin;

import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.service.AdminSelectionExportService;
import com.qfedu.smartcampusseckill.service.AdminSelectionService;
import com.qfedu.smartcampusseckill.service.AdminSelectionService.CourseStat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 管理端：选课统计接口。
 */
@RestController
@RequestMapping("/admin/selection")
@CrossOrigin(origins = "*")
public class AdminSelectionController {

    private final AdminSelectionService adminSelectionService;
    private final AdminSelectionExportService adminSelectionExportService;

    public AdminSelectionController(AdminSelectionService adminSelectionService,
                                    AdminSelectionExportService adminSelectionExportService) {
        this.adminSelectionService = adminSelectionService;
        this.adminSelectionExportService = adminSelectionExportService;
    }

    /**
     * 选课统计：GET /admin/selection/stats
     */
    @GetMapping("/stats")
    public Result<List<CourseStat>> stats() {
        return Result.success(adminSelectionService.stats());
    }

    /**
     * 按课程导出已选名单（Excel 下载）：GET /admin/selection/export?courseId=1
     */
    @GetMapping("/export")
    public void export(@RequestParam Long courseId, HttpServletResponse response) throws Exception {
        adminSelectionExportService.exportByCourse(courseId, response);
    }
}

