package ru.otus.messagesystem.client.src.main.java.ru.otus.messagesystem;

import ru.otus.messagesystem.client.src.main.java.ru.otus.messagesystem.client.ResultDataType;
import ru.otus.messagesystem.client.src.main.java.ru.otus.messagesystem.message.MessageType;

public interface HandlersStore {
    RequestHandler<? extends ResultDataType> getHandlerByType(String messageTypeName);

    void addHandler(MessageType messageType, RequestHandler<? extends ResultDataType> handler);
}
