package com.bishe.backend.controller;

import com.bishe.backend.common.Result;
import com.bishe.backend.dto.AdminLoginResponse;
import com.bishe.backend.dto.AdminPasswordResetRequest;
import com.bishe.backend.dto.AdminUserCreateRequest;
import com.bishe.backend.dto.AdminUserResponse;
import com.bishe.backend.dto.AdminUserUpdateRequest;
import com.bishe.backend.dto.PageResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理接口控制器。
 *
 * <p>该控制器提供管理员登录、用户管理和日记管理接口。项目配置了统一上下文路径
 * {@code /api}，因此这里的 {@code /admin} 对外完整访问路径是 {@code /api/admin}。</p>
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    /**
     * 后台管理业务对象，用于处理管理员登录和数据管理逻辑。
     */
    @Resource
    private AdminService adminService;

    /**
     * 管理员登录接口。
     *
     * @param request 管理员提交的账号密码
     * @return 管理员 token 和基础信息
     */
    @PostMapping("/auth/login")
    public Result<AdminLoginResponse> login(@RequestBody UserLoginRequest request) {
        return Result.success(adminService.login(request));
    }

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
    @GetMapping("/users")
    public Result<PageResponse<AdminUserResponse>> listUsers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.listUsers(page, size, username, role, status));
    }

    /**
     * 后台新增用户。
     *
     * @param request 新用户资料
     * @return 新增后的用户基础资料
     */
    @PostMapping("/users")
    public Result<AdminUserResponse> createUser(@RequestBody AdminUserCreateRequest request) {
        return Result.success(adminService.createUser(request));
    }

    /**
     * 后台修改用户资料。
     *
     * @param id 用户 ID
     * @param request 待修改的用户资料
     * @return 修改后的用户基础资料
     */
    @PutMapping("/users/{id}")
    public Result<AdminUserResponse> updateUser(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return Result.success(adminService.updateUser(id, request));
    }

    /**
     * 后台重置用户密码。
     *
     * @param id 用户 ID
     * @param request 新密码请求
     * @return 重置结果
     */
    @PutMapping("/users/{id}/password")
    public Result<Void> resetUserPassword(@PathVariable Long id, @RequestBody AdminPasswordResetRequest request) {
        adminService.resetUserPassword(id, request);
        return Result.success();
    }

    /**
     * 后台禁用用户。
     *
     * @param id 用户 ID
     * @return 禁用结果
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return Result.success();
    }

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
    @GetMapping("/diaries")
    public Result<PageResponse<EmotionDiary>> listDiaries(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String emotionType,
            @RequestParam(required = false) String emotionPolarity,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.listDiaries(page, size, userId, startDate, endDate,
                emotionType, emotionPolarity, keyword));
    }

    /**
     * 查询一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情
     */
    @GetMapping("/diaries/{id}")
    public Result<EmotionDiary> getDiary(@PathVariable Long id) {
        return Result.success(adminService.getDiary(id));
    }

    /**
     * 后台新增日记。
     *
     * @param diary 管理员提交的完整日记资料
     * @return 新增后的日记
     */
    @PostMapping("/diaries")
    public Result<EmotionDiary> createDiary(@RequestBody EmotionDiary diary) {
        return Result.success(adminService.createDiary(diary));
    }

    /**
     * 后台修改日记。
     *
     * @param id 日记 ID
     * @param diary 管理员提交的新日记资料
     * @return 修改后的日记
     */
    @PutMapping("/diaries/{id}")
    public Result<EmotionDiary> updateDiary(@PathVariable Long id, @RequestBody EmotionDiary diary) {
        return Result.success(adminService.updateDiary(id, diary));
    }

    /**
     * 后台物理删除日记。
     *
     * @param id 日记 ID
     * @return 删除结果
     */
    @DeleteMapping("/diaries/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        adminService.deleteDiary(id);
        return Result.success();
    }
}
