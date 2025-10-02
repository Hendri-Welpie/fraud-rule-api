package org.project.fraudruleapi.fraud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "fraud", name = "transaction")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "transaction_id")
    private String transactionId;
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "currency")
    private String currency;
    @Column(name = "amount")
    private Double transferAmount;
    @Column(name = "timestamp")
    private Instant timeStamp;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "channel")
    private String channel;
    @Column(name = "merchant_id")
    private String merchantId;
    @Column(name = "merchant_name")
    private String merchantName;
    @Column(name = "beneficiary_account")
    private Long beneficiaryAccount;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "location")
    private String location;
    @Column(name = "status")
    private String status;
}