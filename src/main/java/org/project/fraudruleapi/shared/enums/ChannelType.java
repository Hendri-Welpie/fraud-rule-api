package org.project.fraudruleapi.shared.enums;

public enum ChannelType {
    MOBILE_APP, // Customer is interacting directly with the bank's main mobile application
    WEB,  // Customer is using the bank's website or online banking portal via a web browser
    ATM,  // Customer is using an Automated Teller Machine
    BRANCH,  // Customer is interacting with bank staff at a physical branch location
    SCHEDULED_TRANSACTION, // Alias for SYSTEM, used for recurring payments, scheduled transfers etc.
}