package com.mandarly.boot.module.edu.service.classroom;

import com.mandarly.boot.module.edu.service.classroom.dto.LcicCreateRoomRequest;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicRoom;

/**
 * LCIC 视频课堂客户端抽象
 *
 * <p>实现类通过 mandarly.lcic.mode 切换:
 * <ul>
 *   <li>mock — {@link LcicClientMock} 不调腾讯云 API,返回 stub URL,主路径 e2e 用</li>
 *   <li>real — {@link LcicClientReal} 调腾讯云 LCIC OpenAPI(凭证就位后启用)</li>
 * </ul>
 */
public interface LcicClient {

    /**
     * 创建课堂房间。
     *
     * <p>失败语义:抛 RuntimeException;调用方(ClassroomService)负责捕获 + 落 stub 占位 + 标失败状态等异步重试。
     */
    LcicRoom createRoom(LcicCreateRoomRequest request);

    /**
     * 为指定 user 生成 join URL + token(实时生成,token 短期有效)。
     *
     * @param lcicClassId 房间 id(meeting.lcic_class_id)
     * @param userId      学生或教师 user id
     * @param role        student | teacher
     */
    LcicJoinInfo generateJoinInfo(String lcicClassId, Long userId, String role);

}
