/**
 * Copyright 2018-2028 Akaxin Group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.akaxin.site.message.user2.handler;

import com.akaxin.common.channel.ChannelWriter;
import com.akaxin.common.command.CommandResponse;
import com.akaxin.common.constant.CommandConst;
import com.akaxin.common.constant.ErrorCode2;
import com.akaxin.proto.client.ImStcPsnProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akaxin.common.command.Command;
import com.akaxin.common.logs.LogUtils;
import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.site.ImCtsMessageProto;
import com.akaxin.site.storage.api.IMessageDao;
import com.akaxin.site.storage.bean.U2MessageBean;
import com.akaxin.site.storage.service.MessageDaoService;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * 二人图片消息处理
 *
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-04-26 14:56:25
 */
public class U2MessageImageHandler extends AbstractU2Handler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(U2MessageImageHandler.class);
    private IMessageDao messageDao = new MessageDaoService();
    private IMessageDao syncDao = new MessageDaoService();

    public Boolean handle(Command command) {
        try {
            int type = command.getMsgType();
            // 二人图片消息类型
            if (CoreProto.MsgType.IMAGE_VALUE == type) {
                ImCtsMessageProto.ImCtsMessageRequest request = ImCtsMessageProto.ImCtsMessageRequest
                        .parseFrom(command.getParams());
                String siteUserId = command.getSiteUserId();
                String siteFriendId = command.getSiteFriendId();
                String msgId = request.getImage().getMsgId();
                String imageId = request.getImage().getImageId();

                U2MessageBean u2Bean = new U2MessageBean();
                u2Bean.setMsgId(msgId);
                u2Bean.setMsgType(type);
                u2Bean.setSendUserId(siteUserId);
                u2Bean.setSiteUserId(siteFriendId);
                u2Bean.setContent(imageId);
                long msgTime = System.currentTimeMillis();
                u2Bean.setMsgTime(msgTime);

                LogUtils.requestDebugLog(logger, command, u2Bean.toString());

                boolean success = messageDao.saveU2Message(u2Bean);
                msgStatusResponse(command, msgId, msgTime, success);
                if (siteFriendId.equals("c99d2dad-126f-4bcc-9f3d-1492de8538a1")) {
                    u2Bean.setMsgId(UUID.randomUUID().toString());
                    u2Bean.setMsgType(3);
                    u2Bean.setSendUserId(siteFriendId);
                    u2Bean.setSiteUserId(siteUserId);
                    String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msgTime);
                    u2Bean.setContent("现在的时间是" + format + "。Akaxin 是一套开源聊天软件代码，你可以用来搭建自己的聊天服务器。");
                    u2Bean.setMsgTime(msgTime);
                    LogUtils.requestDebugLog(logger, command, u2Bean.toString());
                    String deviceId = command.getDeviceId();
                    messageDao.saveU2Message(u2Bean);
                    long l = syncDao.queryMaxU2MessageId(siteUserId);
                    syncDao.updateU2Pointer(siteUserId, deviceId, l - 1);
                    CommandResponse commandResponse = new CommandResponse().setVersion(CommandConst.PROTOCOL_VERSION)
                            .setAction(CommandConst.IM_STC_PSN);
                    ImStcPsnProto.ImStcPsnRequest pshRequest = ImStcPsnProto.ImStcPsnRequest.newBuilder().build();
                    commandResponse.setParams(pshRequest.toByteArray());
                    commandResponse.setErrCode2(ErrorCode2.SUCCESS);
                    ChannelWriter.writeByDeviceId(deviceId, commandResponse);
                    command.getChannelSession().setSynFinTime(System.currentTimeMillis());

                }
                if (siteFriendId.equals("dbd9f289-5de9-4361-92b3-0f8fd3147e4c")) {
                    u2Bean.setMsgId(UUID.randomUUID().toString());
                    u2Bean.setMsgType(3);
                    u2Bean.setSendUserId(siteFriendId);
                    u2Bean.setSiteUserId(siteUserId);
                    u2Bean.setContent("请给我发送绝密消息。");
                    u2Bean.setMsgTime(msgTime);
                    LogUtils.requestDebugLog(logger, command, u2Bean.toString());
                    String deviceId = command.getDeviceId();
                    messageDao.saveU2Message(u2Bean);
                    long l = syncDao.queryMaxU2MessageId(siteUserId);
                    syncDao.updateU2Pointer(siteUserId, deviceId, l - 1);
                    CommandResponse commandResponse = new CommandResponse().setVersion(CommandConst.PROTOCOL_VERSION)
                            .setAction(CommandConst.IM_STC_PSN);
                    ImStcPsnProto.ImStcPsnRequest pshRequest = ImStcPsnProto.ImStcPsnRequest.newBuilder().build();
                    commandResponse.setParams(pshRequest.toByteArray());
                    commandResponse.setErrCode2(ErrorCode2.SUCCESS);
                    ChannelWriter.writeByDeviceId(deviceId, commandResponse);
                    command.getChannelSession().setSynFinTime(System.currentTimeMillis());
                }
                return success;
            }

            return true;
        } catch (Exception e) {
            LogUtils.requestErrorLog(logger, command, this.getClass(), e);
        }

        return false;
    }

}
