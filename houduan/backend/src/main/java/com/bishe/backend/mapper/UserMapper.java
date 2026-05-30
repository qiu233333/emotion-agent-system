package com.bishe.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bishe.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问接口。
 *
 * <p>继承 {@link BaseMapper} 后，MyBatis-Plus 会自动提供常用 CRUD 方法，
 * 例如按主键查询、插入、更新、删除和列表查询。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
