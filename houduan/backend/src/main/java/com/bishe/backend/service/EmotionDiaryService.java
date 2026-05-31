package com.bishe.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bishe.backend.entity.EmotionDiary;

import java.util.List;

/**
 * 情绪日记业务接口。
 *
 * <p>Controller 只负责接收请求和返回响应，具体业务规则统一放在 Service 中，
 * 便于后续增加登录校验、数据权限、AI 分析等逻辑。</p>
 */
public interface EmotionDiaryService extends IService<EmotionDiary> {

    /**
     * 新增一篇情绪日记。
     *
     * @param diary 前端提交的日记数据
     * @return 保存后的日记数据
     */
    EmotionDiary createDiary(EmotionDiary diary);

    /**
     * 查询当前用户的情绪日记列表。
     *
     * @return 当前用户的日记列表
     */
    List<EmotionDiary> listCurrentUserDiaries();

    /**
     * 查询当前用户的一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情，不存在时返回 null
     */
    EmotionDiary getCurrentUserDiary(Long id);

    /**
     * 修改当前用户的一篇日记。
     *
     * @param id 日记 ID
     * @param diary 前端提交的新日记数据
     * @return 修改后的日记数据，不存在时返回 null
     */
    EmotionDiary updateDiary(Long id, EmotionDiary diary);

    /**
     * 删除当前用户的一篇日记。
     *
     * @param id 日记 ID
     * @return true 表示删除成功，false 表示数据不存在或删除失败
     */
    boolean deleteDiary(Long id);
}
