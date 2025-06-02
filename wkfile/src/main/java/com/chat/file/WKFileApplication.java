package com.chat.file;

import android.content.Context;
import android.content.Intent;

import com.chat.base.WKBaseApplication;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChatFunctionMenu;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.endpoint.entity.MsgConfig;
import com.chat.base.endpoint.entity.SearchChatContentMenu;
import com.chat.base.endpoint.entity.SendFileMenu;
import com.chat.base.entity.AppModule;
import com.chat.base.msg.IConversationContext;
import com.chat.base.msgitem.WKContentType;
import com.chat.base.msgitem.WKMsgItemViewManager;
import com.chat.base.utils.WKToastUtils;
import com.chat.file.msgitem.FileContent;
import com.chat.file.msgitem.FileProvider;
import com.chat.file.search.SearchChatFileActivity;
import com.chat.file.search.remote.SearchWithFileActivity;
import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKSendOptions;
import com.xinbida.wukongim.message.type.WKMsgContentType;

import java.lang.ref.WeakReference;

/**
 * 2020-08-06 15:01
 * 文件
 */
public class WKFileApplication {

    private WKFileApplication() {
    }

    private static class FileApplicationBinder {
        final static WKFileApplication FILE = new WKFileApplication();
    }

    public static WKFileApplication getInstance() {
        return FileApplicationBinder.FILE;
    }


    public void init(Context context) {
        AppModule appModule = WKBaseApplication.getInstance().getAppModuleWithSid("file");
        if (!WKBaseApplication.getInstance().appModuleIsInjection(appModule)) {
            return;
        }
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + WKContentType.WK_FILE, object -> new MsgConfig(true));
        WKIM.getInstance().getMsgManager().registerContentMsg(FileContent.class);
        WKMsgItemViewManager.getInstance().addChatItemViewProvider(WKMsgContentType.WK_FILE, new FileProvider());
        initFileListener(context);
    }

    private WeakReference<IConversationContext> iConversationContext;

    private void initFileListener(Context context) {
        EndpointManager.getInstance().setMethod("", EndpointCategory.chatShowBubble, object -> {
            int type = (int) object;
            return type == WKContentType.WK_FILE;
        });
        EndpointManager.getInstance().setMethod(EndpointCategory.chatFunction + "_chooseFile", EndpointCategory.chatFunction, 94, object -> new ChatFunctionMenu("", R.mipmap.icon_func_file, context.getString(R.string.str_file_file), (iConversationContext) -> {
            WKFileApplication.this.iConversationContext = new WeakReference<>((IConversationContext) object);
            if (iConversationContext.getChatChannelInfo().flame == 1) {
                WKToastUtils.getInstance().showToast(iConversationContext.getChatActivity().getString(R.string.flame_can_not_send_file));
                return;
            }
            Intent intent = new Intent(context, ChooseFileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }));

        // 搜索聊天文件
        EndpointManager.getInstance().setMethod("search_with_file", EndpointCategory.wkSearchChatContent, 91, object -> new SearchChatContentMenu(context.getString(R.string.str_file_file), (channelID, channelType) -> {
            Intent intent = new Intent(context, SearchWithFileActivity.class);
            intent.putExtra("channel_id", channelID);
            intent.putExtra("channel_type", channelType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }));

        // 搜索聊天文件
//        EndpointManager.getInstance().setMethod("search_chat_file", EndpointCategory.wkSearchChatContent, 91, object -> new SearchChatContentMenu(context.getString(R.string.str_file_file), (channelID, channelType) -> {
//            Intent intent = new Intent(context, SearchChatFileActivity.class);
//            intent.putExtra("channel_id", channelID);
//            intent.putExtra("channel_type", channelType);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        }));

        // 需要发送文件获取文件消息
        EndpointManager.getInstance().setMethod("forward_file", object -> {
            SendFileMenu mSendFileMenu = (SendFileMenu) object;
            FileContent fileContent = new FileContent();
            fileContent.name = mSendFileMenu.getName();
            fileContent.localPath = mSendFileMenu.getLocalPath();
            fileContent.size = mSendFileMenu.getSize();

            EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(list -> {
                for (WKChannel channel : list) {
                    WKSendOptions options = new WKSendOptions();
                    options.setting.receipt = channel.receipt;
                    WKIM.getInstance().getMsgManager().sendWithOptions(fileContent, channel, options);
                }
            }), fileContent));

            return fileContent;
        });
    }

    void sendMessage(FileContent fileContent) {
        if (iConversationContext != null) {
            iConversationContext.get().sendMessage(fileContent);
            iConversationContext = null;
        }
    }
}
