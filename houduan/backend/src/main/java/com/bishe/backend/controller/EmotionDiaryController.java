package com.bishe.backend.controller;

import com.bishe.backend.common.Result;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.service.EmotionDiaryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 情绪日记接口控制器。
 *
 * <p>该控制器提供情绪日记的新增、列表、详情、修改和删除接口。
 * 项目配置了统一上下文路径 {@code /api}，因此这里的 {@code /diary}
 * 对外完整访问路径是 {@code /api/diary}。</p>
 */
@RestController
@RequestMapping("/diary")
public class EmotionDiaryController {

    /**
     * 情绪日记业务对象，用于处理具体业务逻辑。
     */
    @Resource
    private EmotionDiaryService emotionDiaryService;

    /**
     * 新增情绪日记。
     *
     * @param diary 前端提交的日记数据
     * @return 新增后的日记数据
     */
    @PostMapping
    public Result<EmotionDiary> createDiary(@RequestBody EmotionDiary diary) {
        return Result.success(emotionDiaryService.createDiary(diary));
    }

    /**
     * 查询情绪日记列表。
     *
     * @return 当前用户的日记列表
     */
    @GetMapping("/list")
    public Result<List<EmotionDiary>> listDiaries() {
        return Result.success(emotionDiaryService.listCurrentUserDiaries());
    }

    /**
     * 查询情绪日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情，找不到时返回错误提示
     */
    @GetMapping("/{id}")
    public Result<EmotionDiary> getDiary(@PathVariable Long id) {
        EmotionDiary diary = emotionDiaryService.getCurrentUserDiary(id);
        if (diary == null) {
            return Result.error("日记不存在");
        }

        return Result.success(diary);
    }

    /**
     * 修改情绪日记。
     *
     * @param id 日记 ID
     * @param diary 前端提交的新日记数据
     * @return 修改后的日记数据，找不到时返回错误提示
     */
    @PutMapping("/{id}")
    public Result<EmotionDiary> updateDiary(@PathVariable Long id, @RequestBody EmotionDiary diary) {
        EmotionDiary updatedDiary = emotionDiaryService.updateDiary(id, diary);
        if (updatedDiary == null) {
            return Result.error("日记不存在");
        }

        return Result.success(updatedDiary);
    }

    /**
     * 删除情绪日记。
     *
     * @param id 日记 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        boolean deleted = emotionDiaryService.deleteDiary(id);
        if (!deleted) {
            return Result.error("日记不存在");
        }

        return Result.success();
    }
}
