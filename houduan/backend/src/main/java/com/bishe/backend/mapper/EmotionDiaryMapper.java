package com.bishe.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bishe.backend.entity.EmotionDiary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 情绪日记表数据访问接口。
 *
 * <p>继承 {@link BaseMapper} 后，MyBatis-Plus 会自动提供新增、查询、
 * 修改、删除等基础数据库操作。</p>
 */
@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {

}
