package org.project.fraudruleapi.shared.enums;

public enum TransactionType {
    DEPOSIT, // Inflow of funds
    WITHDRAWAL, // Outflow of funds
    TRANSFER, // Movement of funds to a different account
    BILL_PAYMENT, // Electronic payment to a merchant or biller
    CARD_PAYMENT, // Payment made using a debit or credit card
    P2P_PAYMENT, // Person-to-person transfers using a specific service
    MOBILE_PAYMENT, // Payment using a mobile wallet
    EFT, // A standard electronic funds transfer (e.g., direct deposit)
    WIRE_TRANSFER, // Wire transfers for high-value or international payments
    ACH_TRANSFER // Automated Clearing House transfer
}