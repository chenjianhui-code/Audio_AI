package com.ai.app.audio_ai.data.model

/**
 * 通用API响应模型
 * @param code 响应状态码，200表示成功
 * @param message 响应消息
 */
open class ApiResponse(
    val code: Int,
    val message: String
)

/**
 * Banner响应模型
 * @param code 响应状态码
 * @param message 响应消息
 * @param data Banner数据列表
 */
data class BannerResponse(
    val data: List<Banner>
) : ApiResponse(200, "success")

/**
 * 音频内容列表响应模型
 * @param code 响应状态码
 * @param message 响应消息
 * @param data 音频内容数据列表
 */
data class AudioContentResponse(
    val data: List<AudioContent>
) : ApiResponse(200, "success")

/**
 * 音频内容详情响应模型
 * @param code 响应状态码
 * @param message 响应消息
 * @param data 音频内容详情数据
 */
data class AudioContentDetailResponse(
    val data: AudioContent
) : ApiResponse(200, "success")
