package com.bishe.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类。
 *
 * <p>该类与数据库中的 {@code user} 表对应。MyBatis-Plus 会根据 Java 驼峰字段名
 * 自动映射到数据库下划线字段名，例如 {@code createTime} 对应 {@code create_time}。</p>
 */
@Data
@TableName("`user`")
public class User {

    /**
     * 用户主键 ID，对应数据库自增主键 {@code id}。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，用于登录和唯一识别用户。
     */
    private String username;

    /**
     * 用户密码。正式业务中应保存加密后的密码，不能保存明文密码。
     */
    private String password;

    /**
     * 用户昵称，用于页面展示。
     */
    private String nickname;

    /**
     * 用户头像地址，可以为空。
     */
    private String avatar;

    /**
     * 用户邮箱，可以用于找回密码或接收通知。
     */
    private String email;

    /**
     * 用户角色，例如普通用户 {@code user} 或管理员 {@code admin}。
     */
    private String role;

    /**
     * 用户状态，1 表示正常，0 表示禁用。
     */
    private Integer status;

    /**
     * 记录创建时间，对应数据库字段 {@code create_time}。
     */
    private LocalDateTime createTime;

    /**
     * 记录更新时间，对应数据库字段 {@code update_time}。
     */
    private LocalDateTime updateTime;
}
