package com.bishe.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.mapper.EmotionDiaryMapper;
import com.bishe.backend.security.AuthContext;
import com.bishe.backend.service.EmotionDiaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪日记业务实现类。
 *
 * <p>当前模块通过 {@link AuthContext} 获取 JWT 中的登录用户 ID，
 * 所有新增、查询、修改和删除操作都只作用于当前登录用户自己的日记。</p>
 */
@Service
public class EmotionDiaryServiceImpl extends ServiceImpl<EmotionDiaryMapper, EmotionDiary>
        implements EmotionDiaryService {

    /**
     * 新增一篇情绪日记，并补充当前登录用户 ID 和时间字段。
     *
     * @param diary 前端提交的日记数据
     * @return 保存后的日记数据
     */
    @Override
    public EmotionDiary createDiary(EmotionDiary diary) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = getCurrentUserId();

        diary.setId(null);
        diary.setUserId(currentUserId);
        diary.setCreateTime(now);
        diary.setUpdateTime(now);

        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(now);
        }

        save(diary);
        return diary;
    }

    /**
     * 查询当前登录用户的日记列表，并按日记日期倒序排列。
     *
     * @return 当前用户的日记列表
     */
    @Override
    public List<EmotionDiary> listCurrentUserDiaries() {
        Long currentUserId = getCurrentUserId();

        return list(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getUserId, currentUserId)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId));
    }

    /**
     * 查询当前登录用户的一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情，不存在时返回 null
     */
    @Override
    public EmotionDiary getCurrentUserDiary(Long id) {
        Long currentUserId = getCurrentUserId();

        return getOne(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getId, id)
                .eq(EmotionDiary::getUserId, currentUserId));
    }

    /**
     * 修改当前登录用户的一篇日记。
     *
     * @param id 日记 ID
     * @param diary 前端提交的新日记数据
     * @return 修改后的日记数据，不存在时返回 null
     */
    @Override
    public EmotionDiary updateDiary(Long id, EmotionDiary diary) {
        EmotionDiary oldDiary = getCurrentUserDiary(id);
        if (oldDiary == null) {
            return null;
        }

        diary.setId(id);
        diary.setUserId(oldDiary.getUserId());
        diary.setCreateTime(oldDiary.getCreateTime());
        diary.setUpdateTime(LocalDateTime.now());

        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(oldDiary.getDiaryDate());
        }

        updateById(diary);
        return getCurrentUserDiary(id);
    }

    /**
     * 删除当前登录用户的一篇日记。
     *
     * @param id 日记 ID
     * @return true 表示删除成功，false 表示日记不存在
     */
    @Override
    public boolean deleteDiary(Long id) {
        EmotionDiary diary = getCurrentUserDiary(id);
        if (diary == null) {
            return false;
        }

        return removeById(id);
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Long currentUserId = AuthContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("未登录，请先登录");
        }
        return currentUserId;
    }
}
