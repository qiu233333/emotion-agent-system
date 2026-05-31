package com.bishe.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.mapper.EmotionDiaryMapper;
import com.bishe.backend.service.EmotionDiaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情绪日记业务实现类。
 *
 * <p>当前阶段暂时不做 JWT 登录认证，所以所有接口都使用固定用户 ID 1。
 * 后续接入登录后，只需要把 {@link #CURRENT_USER_ID} 替换为登录用户 ID。</p>
 */
@Service
public class EmotionDiaryServiceImpl extends ServiceImpl<EmotionDiaryMapper, EmotionDiary>
        implements EmotionDiaryService {

    /**
     * 临时固定用户 ID。后续接入 JWT 后应从 token 或登录上下文中获取。
     */
    private static final long CURRENT_USER_ID = 1L;

    /**
     * 新增一篇情绪日记，并补充用户 ID 和时间字段。
     *
     * @param diary 前端提交的日记数据
     * @return 保存后的日记数据
     */
    @Override
    public EmotionDiary createDiary(EmotionDiary diary) {
        LocalDateTime now = LocalDateTime.now();
        diary.setId(null);
        diary.setUserId(CURRENT_USER_ID);
        diary.setCreateTime(now);
        diary.setUpdateTime(now);

        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(now);
        }

        save(diary);
        return diary;
    }

    /**
     * 查询当前固定用户的日记列表，并按日记日期倒序排列。
     *
     * @return 当前用户的日记列表
     */
    @Override
    public List<EmotionDiary> listCurrentUserDiaries() {
        return list(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getUserId, CURRENT_USER_ID)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId));
    }

    /**
     * 查询当前固定用户的一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情，不存在时返回 null
     */
    @Override
    public EmotionDiary getCurrentUserDiary(Long id) {
        return getOne(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getId, id)
                .eq(EmotionDiary::getUserId, CURRENT_USER_ID));
    }

    /**
     * 修改当前固定用户的一篇日记。
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
        diary.setUserId(CURRENT_USER_ID);
        diary.setCreateTime(oldDiary.getCreateTime());
        diary.setUpdateTime(LocalDateTime.now());

        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(oldDiary.getDiaryDate());
        }

        updateById(diary);
        return getCurrentUserDiary(id);
    }

    /**
     * 删除当前固定用户的一篇日记。
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
}
