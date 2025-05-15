package dev.xrustut;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EventHandler {
    public static String MessagesAmountReport(OkHttpTelegramClient client, Long CHAT_ID) {
        try (DynamoDbClient ddb = DynamoDbClient.create()) {
            List<Map<String, AttributeValue>> users = DDBHelper.findAllMessageCounter(ddb);
            users.sort(
                    Comparator.comparing(
                            user -> Integer.valueOf(user.get("messagesCounter").n()),
                            Comparator.reverseOrder()
                    )
            );

            DDBHelper.clearTableMessageCounter(ddb, users);

            List<ChatMember> admins = TelegramHelper.getChatAdministrators(client, CHAT_ID);
            DDBHelper.fillTableMessageCounterWithZeros(ddb, admins);

            int highestScore = Integer.parseInt(users.getFirst().get("messagesCounter").n());
            int lowestScore = Integer.parseInt(users.getLast().get("messagesCounter").n());
            StringBuilder messageReport = new StringBuilder();

            messageReport.append("Messages per last 24 hours:\n\n");

            for (Map<String, AttributeValue> user : users) {
                messageReport.append(user.get("username").s());
                messageReport.append(" - ");
                messageReport.append(user.get("messagesCounter").n());

                int messagesCounter = Integer.parseInt(user.get("messagesCounter").n());

                if (highestScore == lowestScore) {
                    messageReport.append(" \uD83D\uDC51");
                } else if (messagesCounter == highestScore) {
                    messageReport.append(" \uD83D\uDC51");
                } else if (messagesCounter == lowestScore) {
                    messageReport.append(" \uD83D\uDCA9");
                }

                messageReport.append("\n");
            }

            //crown
            //messageReport.append(" \uD83D\uDC51");

            //poop
            //messageReport.append(" \uD83D\uDCA9");

            SendMessage message = new SendMessage(String.valueOf(CHAT_ID), messageReport.toString());
            client.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
        return "Success [MessagesAmountReport]";
    }
}
