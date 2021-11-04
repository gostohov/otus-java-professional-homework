package ru.otus.crm.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cachehw.HwCache;
import ru.otus.core.repository.DataTemplate;
import ru.otus.crm.model.Client;
import ru.otus.core.sessionmanager.TransactionManager;

import java.util.List;
import java.util.Optional;

public class DbServiceClientImpl implements DBServiceClient {
    private static final Logger log = LoggerFactory.getLogger(DbServiceClientImpl.class);

    private final DataTemplate<Client> clientDataTemplate;
    private final TransactionManager transactionManager;
    private final SessionFactory sessionFactory;
    private final HwCache<String, Client> cache;

    public DbServiceClientImpl(TransactionManager transactionManager, DataTemplate<Client> clientDataTemplate, SessionFactory sessionFactory, HwCache<String, Client> cache) {
        this.transactionManager = transactionManager;
        this.clientDataTemplate = clientDataTemplate;
        this.sessionFactory = sessionFactory;
        this.cache = cache;
    }

    @Override
    public Client saveClient(Client client) {
        return transactionManager.doInTransaction(session -> {
            if (client.getId() == null) {
                clientDataTemplate.insert(session, client);
                log.info("created client: {}", client);
                if (cache != null) {
                    cache.put(buildKey(client.getId()), client);
                    log.info("client cached: {}", cache.toString());
                }
                return client;
            }
            clientDataTemplate.update(session, client);
            log.info("updated client: {}", client);
            return client;
        });
    }

    @Override
    public Optional<Client> getClient(long id) {
        if (cache != null) {
            Client client = cache.get(buildKey(id));
            if (client != null) {
                return Optional.of(client);
            }
        }
        try (var session = sessionFactory.openSession()) {
            try {
                Optional<Client> clientOptional = Optional.ofNullable(session.find(Client.class, id));
                log.info("client: {}", clientOptional.orElse(null));
                clientOptional.ifPresent(cl -> {
                    assert cache != null;
                    cache.put(buildKey(cl.getId()), cl);
                });
                return clientOptional;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() {
        return transactionManager.doInTransaction(session -> {
            var clientList = clientDataTemplate.findAll(session);
            log.info("clientList:{}", clientList);
            return clientList;
       });
    }

    private String buildKey(long id) {
        return String.valueOf(id);
    }
}