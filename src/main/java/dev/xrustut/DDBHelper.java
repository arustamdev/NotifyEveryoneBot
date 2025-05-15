package dev.xrustut;

import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

import java.util.*;

public class DDBHelper {
    public static List<Map<String, AttributeValue>> findAllMessageCounter(DynamoDbClient ddb) {
        ScanIterable scanIterable = ddb.scanPaginator(ScanRequest.builder()
                .tableName("MessageCounter")
                .build()
        );

        List<Map<String, AttributeValue>> result = new ArrayList<>();

        for (ScanResponse scanResponse : scanIterable) {
            result.addAll(scanResponse.items());
        }

        return result;
    }

    public static void clearTableMessageCounter(DynamoDbClient ddb, List<Map<String, AttributeValue>> users) {
        try {
            for (Map<String, AttributeValue> user : users) {
                HashMap<String, AttributeValue> deleteKey = new HashMap<>();
                deleteKey.put("username", user.get("username"));

                ddb.deleteItem(DeleteItemRequest.builder()
                        .tableName("MessageCounter")
                        .key(deleteKey)
                        .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void fillTableMessageCounterWithZeros(DynamoDbClient ddb, List<ChatMember> admins) {
        List<String> usernames = admins.stream().map((admin) -> {
            if (admin.getUser().getIsBot()) {
                return null;
            }
            return admin.getUser().getUserName();
        }).filter(Objects::nonNull).toList();

        for (String username : usernames) {

            HashMap<String, AttributeValue> itemKey = new HashMap<>();
            itemKey.put("username", AttributeValue.builder().s(username).build());

            HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
            updatedValues.put("messagesCounter", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().n("0").build())
                    .action(AttributeAction.ADD).build()
            );

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName("MessageCounter")
                    .key(itemKey)
                    .attributeUpdates(updatedValues)
                    .build();

            ddb.updateItem(request);
        }
    }

    public static void incrementMessageCountForUser(String username) {
        try (DynamoDbClient ddb = DynamoDbClient.create()) {

            HashMap<String, AttributeValue> itemKey = new HashMap<>();
            itemKey.put("username", AttributeValue.builder().s(username).build());

            HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
            updatedValues.put("messagesCounter", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().n("1").build())
                    .action(AttributeAction.ADD).build()
            );

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName("MessageCounter")
                    .key(itemKey)
                    .attributeUpdates(updatedValues)
                    .build();

            ddb.updateItem(request);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
