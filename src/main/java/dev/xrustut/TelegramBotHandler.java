package dev.xrustut;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class TelegramBotHandler implements RequestHandler<Map<String, Object>, String> {
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
    private static final OkHttpTelegramClient client = new OkHttpTelegramClient(BOT_TOKEN);
    private static final long CHAT_ID = Long.parseLong(System.getenv("CHAT_ID"));
    private final List<String> MENTIONS = new ArrayList<>(Arrays.asList(
            "/notify",
            "@all"
    ));

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        if (input.containsKey("routeKey")) {
            return processUpdate(input);
        }

        return processEvent(input);
    }

    private Update parseUpdate(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Update.class); // Parse JSON to Update object
        } catch (Exception e) {
            return null;
        }
    }

    private String processEvent(Map<String, Object> input) {
        String event = (String) input.get("event");

        if (event.equals("MessagesAmountReport")) {
            return EventHandler.MessagesAmountReport(client, CHAT_ID);
        }
        return "Unknown event";
    }
    private String processUpdate(Map<String, Object> input) {
        String requestBody = (String) input.get("body");
        Update update = parseUpdate(requestBody);

        if (update == null) {
            return "Failed, no update obj found";
        }

        if (!update.hasMessage()) {
            return "No message found";
        }

        Long chatId = update.getMessage().getChatId();

        if (chatId != CHAT_ID) {
            return "Wrong chat id";
        }

        DDBHelper.incrementMessageCountForUser(update.getMessage().getFrom().getUserName());

        if (!update.getMessage().hasText()) {
            return "No text found in message";
        }

        String receivedText = update.getMessage().getText();

        for (String mention : MENTIONS) {
            if (!receivedText.contains(mention)) {
                continue;
            }

            StringBuilder notifyEveryoneText = new StringBuilder();

            try {
                List<ChatMember> admins = TelegramHelper.getChatAdministrators(client, CHAT_ID);

                for (ChatMember admin : admins) {
                    if (!admin.getUser().getIsBot()) {
                        notifyEveryoneText.append("@" + admin.getUser().getUserName() + " ");
                    }
                }

                SendMessage message = new SendMessage(chatId.toString(), notifyEveryoneText.toString());
                client.execute(message);

            } catch (TelegramApiException e) {
                throw new RuntimeException("Something went wrong!");
            }

            return "Success";
        }
        return "Success, no action";
    }
}


