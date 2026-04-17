package org.project.fraudruleapi.fraud.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "fraud", name = "transaction")
public class TransactionEntity {
    @Id
    private Long id;
    @Column("transaction_id")
    private String transactionId;
    @Column("account_id")
    private Long accountId;
    @Column("user_id")
    private Long userId;
    @Column("currency")
    private String currency;
    @Column("amount")
    private Double transferAmount;
    @Column("timestamp")
    private Instant timeStamp;
    @Column("transaction_type")
    private String transactionType;
    @Column("channel")
    private String channel;
    @Column("merchant_id")
    private String merchantId;
    @Column("merchant_name")
    private String merchantName;
    @Column("beneficiary_account")
    private Long beneficiaryAccount;
    @Column("ip_address")
    private String ipAddress;
    @Column("device_id")
    private String deviceId;
    @Column("location")
    private String location;
    @Column("status")
    private String status;
}