package com.bishe.backend.service;

/**
 * 日记上下文业务接口。
 *
 * <p>该模块专门为 AI 陪伴对话整理当前登录用户的日记信息，
 * 让大语言模型能够了解用户今天的完整状态和近期情绪趋势。</p>
 */
public interface DiaryContextService {

    /**
     * 构建当前登录用户用于 AI 陪伴对话的日记上下文。
     *
     * @return 包含今日日记完整信息和近期日记摘要的上下文文本
     */
    String buildCurrentUserChatContext();
}
