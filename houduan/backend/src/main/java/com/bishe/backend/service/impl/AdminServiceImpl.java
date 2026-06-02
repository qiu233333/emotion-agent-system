package com.bishe.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bishe.backend.dto.AdminLoginResponse;
import com.bishe.backend.dto.AdminPasswordResetRequest;
import com.bishe.backend.dto.AdminUserCreateRequest;
import com.bishe.backend.dto.AdminUserResponse;
import com.bishe.backend.dto.AdminUserUpdateRequest;
import com.bishe.backend.dto.PageResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.entity.User;
import com.bishe.backend.security.JwtUtil;
import com.bishe.backend.service.AdminService;
import com.bishe.backend.service.EmotionDiaryService;
import com.bishe.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 后台管理业务实现类。
 *
 * <p>该类负责管理员登录、用户完整管理和日记完整管理。后台日记新增和修改不会自动触发
 * 情绪分析，确保管理员手动填写的情绪字段不会被覆盖。</p>
 */
@Service
public class AdminServiceImpl implements AdminService {

    /**
     * 用户业务对象，用于复用 MyBatis-Plus 用户表基础操作。
     */
    @Resource
    private UserService userService;

    /**
     * 情绪日记业务对象，用于复用 MyBatis-Plus 日记表基础操作。
     */
    @Resource
    private EmotionDiaryService emotionDiaryService;

    /**
     * BCrypt 密码编码器，用于校验和保存密码。
     */
    @Resource
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    /**
     * JWT 工具对象，用于管理员登录成功后签发 token。
     */
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 管理员登录后台。
     *
     * @param request 管理员提交的账号密码
     * @return 管理员登录成功后的 token 和基础信息
     */
    @Override
    public AdminLoginResponse login(UserLoginRequest request) {
        String username = trimToNull(request.getUsername());
        String password = trimToNull(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = getUserByUsername(username);
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!"admin".equals(user.getRole())) {
            throw new IllegalArgumentException("非管理员账号，不能登录后台");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("管理员账号已禁用");
        }

        upgradePlainPasswordIfNeeded(user, password);
        return toAdminLoginResponse(user);
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
    @Override
    public PageResponse<AdminUserResponse> listUsers(Long page, Long size, String username, String role, Integer status) {
        Long pageNumber = normalizePage(page);
        Long pageSize = normalizeSize(size);

        Long total = userService.count(buildUserQueryWrapper(username, role, status));
        List<AdminUserResponse> records = userService.list(buildUserQueryWrapper(username, role, status)
                        .orderByDesc(User::getCreateTime)
                        .orderByDesc(User::getId)
                        .last(buildLimitSql(pageNumber, pageSize)))
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
        return PageResponse.of(records, total, pageNumber, pageSize);
    }

    /**
     * 后台新增用户。
     *
     * @param request 新用户资料
     * @return 新增后的用户基础资料
     */
    @Override
    public AdminUserResponse createUser(AdminUserCreateRequest request) {
        String username = trimToNull(request.getUsername());
        String password = trimToNull(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (getUserByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(bcryptPasswordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setAvatar(trimToNull(request.getAvatar()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setRole(StringUtils.hasText(request.getRole()) ? normalizeRole(request.getRole()) : "user");
        user.setStatus(request.getStatus() == null ? 1 : normalizeStatus(request.getStatus()));
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userService.save(user);
        return toAdminUserResponse(user);
    }

    /**
     * 后台修改用户资料。
     *
     * @param id 用户 ID
     * @param request 待修改的用户资料
     * @return 修改后的用户基础资料
     */
    @Override
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = requireUser(id);
        user.setNickname(trimToNull(request.getNickname()));
        user.setAvatar(trimToNull(request.getAvatar()));
        user.setEmail(trimToNull(request.getEmail()));
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(normalizeRole(request.getRole()));
        }
        if (request.getStatus() != null) {
            user.setStatus(normalizeStatus(request.getStatus()));
        }
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        return toAdminUserResponse(userService.getById(id));
    }

    /**
     * 后台重置用户密码。
     *
     * @param id 用户 ID
     * @param request 新密码请求
     */
    @Override
    public void resetUserPassword(Long id, AdminPasswordResetRequest request) {
        User user = requireUser(id);
        String password = trimToNull(request.getPassword());
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        user.setPassword(bcryptPasswordEncoder.encode(password));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
    }

    /**
     * 后台禁用用户。
     *
     * @param id 用户 ID
     */
    @Override
    public void disableUser(Long id) {
        User user = requireUser(id);
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
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
    @Override
    public PageResponse<EmotionDiary> listDiaries(Long page, Long size, Long userId, String startDate, String endDate,
                                                  String emotionType, String emotionPolarity, String keyword) {
        Long pageNumber = normalizePage(page);
        Long pageSize = normalizeSize(size);
        LocalDateTime parsedStartDate = parseDateTime(startDate, false);
        LocalDateTime parsedEndDate = parseDateTime(endDate, true);

        Long total = emotionDiaryService.count(buildDiaryQueryWrapper(userId, parsedStartDate, parsedEndDate,
                emotionType, emotionPolarity, keyword));
        List<EmotionDiary> records = emotionDiaryService.list(buildDiaryQueryWrapper(userId, parsedStartDate,
                        parsedEndDate, emotionType, emotionPolarity, keyword)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId)
                .last(buildLimitSql(pageNumber, pageSize)));
        return PageResponse.of(records, total, pageNumber, pageSize);
    }

    /**
     * 查询一篇日记详情。
     *
     * @param id 日记 ID
     * @return 日记详情
     */
    @Override
    public EmotionDiary getDiary(Long id) {
        EmotionDiary diary = emotionDiaryService.getById(id);
        if (diary == null) {
            throw new IllegalArgumentException("日记不存在");
        }
        return diary;
    }

    /**
     * 后台新增日记。
     *
     * @param diary 管理员提交的完整日记资料
     * @return 新增后的日记
     */
    @Override
    public EmotionDiary createDiary(EmotionDiary diary) {
        validateDiary(diary);
        LocalDateTime now = LocalDateTime.now();
        diary.setId(null);
        diary.setCreateTime(now);
        diary.setUpdateTime(now);
        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(now);
        }
        emotionDiaryService.save(diary);
        return diary;
    }

    /**
     * 后台修改日记。
     *
     * @param id 日记 ID
     * @param diary 管理员提交的新日记资料
     * @return 修改后的日记
     */
    @Override
    public EmotionDiary updateDiary(Long id, EmotionDiary diary) {
        EmotionDiary oldDiary = getDiary(id);
        validateDiary(diary);

        diary.setId(id);
        diary.setCreateTime(oldDiary.getCreateTime());
        diary.setUpdateTime(LocalDateTime.now());
        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(oldDiary.getDiaryDate());
        }
        emotionDiaryService.updateById(diary);
        return emotionDiaryService.getById(id);
    }

    /**
     * 后台物理删除日记。
     *
     * @param id 日记 ID
     */
    @Override
    public void deleteDiary(Long id) {
        boolean removed = emotionDiaryService.removeById(id);
        if (!removed) {
            throw new IllegalArgumentException("日记不存在");
        }
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体；不存在时返回 null
     */
    private User getUserByUsername(String username) {
        return userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username), false);
    }

    /**
     * 查询用户并在不存在时抛出业务异常。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    private User requireUser(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    /**
     * 构造用户列表查询条件。
     *
     * @param username 用户名模糊搜索条件
     * @param role 角色筛选条件
     * @param status 状态筛选条件
     * @return 用户查询条件包装器
     */
    private LambdaQueryWrapper<User> buildUserQueryWrapper(String username, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username.trim());
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, normalizeRole(role));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, normalizeStatus(status));
        }
        return wrapper;
    }

    /**
     * 构造日记列表查询条件。
     *
     * @param userId 用户 ID 筛选条件
     * @param startDate 开始日期筛选条件
     * @param endDate 结束日期筛选条件
     * @param emotionType 情绪类型筛选条件
     * @param emotionPolarity 情绪极性筛选条件
     * @param keyword 关键词模糊搜索条件
     * @return 日记查询条件包装器
     */
    private LambdaQueryWrapper<EmotionDiary> buildDiaryQueryWrapper(Long userId, LocalDateTime startDate,
                                                                    LocalDateTime endDate, String emotionType,
                                                                    String emotionPolarity, String keyword) {
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(EmotionDiary::getUserId, userId);
        }
        if (startDate != null) {
            wrapper.ge(EmotionDiary::getDiaryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(EmotionDiary::getDiaryDate, endDate);
        }
        if (StringUtils.hasText(emotionType)) {
            wrapper.eq(EmotionDiary::getEmotionType, emotionType.trim());
        }
        if (StringUtils.hasText(emotionPolarity)) {
            wrapper.eq(EmotionDiary::getEmotionPolarity, emotionPolarity.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(query -> query.like(EmotionDiary::getTitle, text)
                    .or()
                    .like(EmotionDiary::getContent, text)
                    .or()
                    .like(EmotionDiary::getKeywords, text));
        }
        return wrapper;
    }

    /**
     * 构造安全的 MySQL 分页 SQL 片段。
     *
     * @param page 当前页码
     * @param size 每页数量
     * @return LIMIT SQL 片段
     */
    private String buildLimitSql(Long page, Long size) {
        long offset = (page - 1) * size;
        return "LIMIT " + offset + ", " + size;
    }

    /**
     * 校验管理员提交的日记基础数据。
     *
     * @param diary 待保存或修改的日记
     */
    private void validateDiary(EmotionDiary diary) {
        if (diary.getUserId() == null || userService.getById(diary.getUserId()) == null) {
            throw new IllegalArgumentException("日记所属用户不存在");
        }
        if (!StringUtils.hasText(diary.getTitle()) || !StringUtils.hasText(diary.getContent())) {
            throw new IllegalArgumentException("日记标题和内容不能为空");
        }
        if (diary.getEmotionScore() != null && (diary.getEmotionScore() < 1 || diary.getEmotionScore() > 5)) {
            throw new IllegalArgumentException("情绪强度必须在 1 到 5 之间");
        }

        diary.setTitle(diary.getTitle().trim());
        diary.setContent(diary.getContent().trim());
        diary.setMoodTag(trimToNull(diary.getMoodTag()));
        diary.setEmotionPolarity(trimToNull(diary.getEmotionPolarity()));
        diary.setEmotionType(trimToNull(diary.getEmotionType()));
        diary.setKeywords(trimToNull(diary.getKeywords()));
        diary.setAiSummary(trimToNull(diary.getAiSummary()));
        diary.setAiSuggestion(trimToNull(diary.getAiSuggestion()));
    }

    /**
     * 生成管理员登录响应对象。
     *
     * @param user 管理员用户实体
     * @return 管理员登录响应对象
     */
    private AdminLoginResponse toAdminLoginResponse(User user) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        return response;
    }

    /**
     * 生成后台用户响应对象，过滤密码字段。
     *
     * @param user 用户实体
     * @return 后台用户响应对象
     */
    private AdminUserResponse toAdminUserResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }

    /**
     * 校验并规范化用户角色。
     *
     * @param role 前端提交的角色
     * @return 规范化后的角色
     */
    private String normalizeRole(String role) {
        String value = trimToNull(role);
        if (!"user".equals(value) && !"admin".equals(value)) {
            throw new IllegalArgumentException("用户角色只能是 user 或 admin");
        }
        return value;
    }

    /**
     * 校验并规范化用户状态。
     *
     * @param status 前端提交的状态
     * @return 规范化后的状态
     */
    private Integer normalizeStatus(Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("用户状态只能是 0 或 1");
        }
        return status;
    }

    /**
     * 规范化分页页码。
     *
     * @param page 前端提交的页码
     * @return 有效页码
     */
    private Long normalizePage(Long page) {
        return page == null || page < 1 ? 1L : page;
    }

    /**
     * 规范化分页大小，避免一次查询过多数据。
     *
     * @param size 前端提交的每页数量
     * @return 有效每页数量
     */
    private Long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return 10L;
        }
        return Math.min(size, 100L);
    }

    /**
     * 解析日期筛选条件。
     *
     * @param text 前端提交的日期文本，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
     * @param endOfDay true 表示日期缺少时间时使用当天 23:59:59
     * @return 解析后的日期时间；空值返回 null
     */
    private LocalDateTime parseDateTime(String text, boolean endOfDay) {
        String value = trimToNull(text);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            if (value.length() == 10) {
                LocalDate date = LocalDate.parse(value);
                return endOfDay ? LocalDateTime.of(date, LocalTime.of(23, 59, 59)) : date.atStartOfDay();
            }
            return LocalDateTime.parse(value.replace(" ", "T"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
    }

    /**
     * 校验用户提交的密码是否正确。
     *
     * @param rawPassword 用户输入的明文密码
     * @param storedPassword 数据库中保存的密码
     * @return true 表示密码正确
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptPassword(storedPassword)) {
            return bcryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /**
     * 判断数据库密码是否已经是 BCrypt 格式。
     *
     * @param storedPassword 数据库中保存的密码
     * @return true 表示已是 BCrypt 密文
     */
    private boolean isBcryptPassword(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    /**
     * 兼容开发阶段旧数据：如果旧密码是明文，登录成功后立即升级为 BCrypt。
     *
     * @param user 当前登录用户
     * @param rawPassword 用户输入的明文密码
     */
    private void upgradePlainPasswordIfNeeded(User user, String rawPassword) {
        if (isBcryptPassword(user.getPassword())) {
            return;
        }

        user.setPassword(bcryptPasswordEncoder.encode(rawPassword));
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
    }

    /**
     * 去除字符串首尾空格，空字符串统一转为 null。
     *
     * @param text 原始字符串
     * @return 处理后的字符串；空值返回 null
     */
    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = text.trim();
        return trimmedText.isEmpty() ? null : trimmedText;
    }
}
