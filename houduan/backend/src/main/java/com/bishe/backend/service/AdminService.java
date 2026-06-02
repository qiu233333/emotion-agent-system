package com.bishe.backend.service;

import com.bishe.backend.dto.AdminLoginResponse;
import com.bishe.backend.dto.AdminPasswordResetRequest;
import com.bishe.backend.dto.AdminUserCreateRequest;
import com.bishe.backend.dto.AdminUserResponse;
import com.bishe.backend.dto.AdminUserUpdateRequest;
import com.bishe.backend.dto.PageResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.entity.EmotionDiary;

/**
 * 后台管理业务接口。
 *
 * <p>后台管理端的登录、用户管理和日记管理都集中在该接口中定义，
 * Controller 只负责接收 HTTP 参数和返回统一 Result。</p>
 */
public interface AdminService {

    /**
     * 管理员登录后台。
     *
     * @param request 管理员提交的账号密码
     * @return 管理员登录成功后的 token 和基础信息
     */
    AdminLoginResponse login(UserLoginRequest request);

    /**
     * 分页查询用户列表。
     *
     * @param page 当前页码
     * @param size 每页数量
     * @param username 用户名模糊搜索条件
     * @param role 角色筛选条件
     * @param status 状态筛选条件
     * @return 分页用户列表
     */
    PageResponse<AdminUserResponse> listUsers(Long page, Long size, String username, String role, Integer status);

    /**
     * 后台新增用户。
     *
     * @param request 新用户资料
     * @return 新增后的用户基础资料
     */
    AdminUserResponse createUser(AdminUserCreateRequest request);

    /**
     * 后台修改用户资料。
     *
     * @param id 用户 ID
     * @param request 待修改的用户资料
     * @return 修改后的用户基础资料
     */
    AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request);

    /**
     * 后台重置用户密码。
     *
     * @param id 用户 ID
     * @param request 新密码请求
     */
    void resetUserPassword(Long id, AdminPasswordResetRequest request);

    /**
     * 后台禁用用户。
     *
     * @param id 用户 ID
     */
    void disableUser(Long id);

    /**
     * 分页查询全部日记列表。
     *
     * @param page 当前页码
     * @param size 每页数量
     * @param userId 用户 ID 筛选条件
     * @param startDate 开始日期筛选条件
     * @param endDate 结束日期筛选条件
     * @param emotionType 情绪类型筛选条件
     * @param emotionPolarity 情绪极性筛选条件
     * @param keyword 关键词模糊搜索条件
     * @return 分页日记列表
     */
    PageResponse<EmotionDiary> listDiaries(Long page, Long size, Long userId, String startDate, String endDate,
                                           String emotionType, String emotionPolarity, String keyword);

    /**
     * 查询一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情
     */
    EmotionDiary getDiary(Long id);

    /**
     * 后台新增日记。
     *
     * @param diary 管理员提交的完整日记资料
     * @return 新增后的日记
     */
    EmotionDiary createDiary(EmotionDiary diary);

    /**
     * 后台修改日记。
     *
     * @param id 日记 ID
     * @param diary 管理员提交的新日记资料
     * @return 修改后的日记
     */
    EmotionDiary updateDiary(Long id, EmotionDiary diary);

    /**
     * 后台物理删除日记。
     *
     * @param id 日记 ID
     */
    void deleteDiary(Long id);
}
