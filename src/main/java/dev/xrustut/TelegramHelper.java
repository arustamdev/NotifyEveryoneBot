package dev.xrustut;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


public class TelegramHelper {
    public static List<ChatMember> getChatAdministrators(OkHttpTelegramClient client, Long CHAT_ID) throws TelegramApiException {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators(String.valueOf(CHAT_ID));
        return client.execute(getChatAdministrators);
    }
}
