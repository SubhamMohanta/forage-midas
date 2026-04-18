package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.jpmc.midascore.foundation.Incentive;

@Component
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TransactionListener(UserRepository userRepository,
                               TransactionRecordRepository transactionRepository,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    public void listen(Transaction transaction) {

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // validation
        if (sender == null || recipient == null) return;

        if (sender.getBalance() < transaction.getAmount()) return;

        // 🔥 CALL INCENTIVE API
        Incentive incentive = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                transaction,
                Incentive.class
        );

        double incentiveAmount = (incentive != null) ? incentive.getAmount() : 0;

        // 🔥 UPDATE BALANCES
        sender.setBalance(sender.getBalance() - (float) transaction.getAmount());

        recipient.setBalance(
                recipient.getBalance()
                        + (float) transaction.getAmount()
                        + (float) incentiveAmount   // 🔥 ADD THIS
        );

        userRepository.save(sender);
        userRepository.save(recipient);

        // 🔥 SAVE TRANSACTION WITH INCENTIVE
        TransactionRecord record = new TransactionRecord(
                (float) transaction.getAmount(),
                (float) incentiveAmount,
                sender,
                recipient
        );

        transactionRepository.save(record);
    }
}