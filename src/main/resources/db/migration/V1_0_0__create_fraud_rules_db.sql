CREATE TABLE IF NOT EXISTS fraud.fraud_rules
(
    rule_id   VARCHAR(255) NOT NULL,
    data      JSONB,
    version   BIGINT,
    active    BOOLEAN      NOT NULL,
    create_at TIMESTAMP WITHOUT TIME ZONE,
    update_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_fraud_rules PRIMARY KEY (rule_id)
);

ALTER TABLE fraud.fraud_rules
    ADD CONSTRAINT uc_fraud_rules_rule UNIQUE (rule_id);

CREATE TABLE IF NOT EXISTS fraud.transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    account_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    currency VARCHAR(10),
    amount DOUBLE PRECISION,
    timestamp TIMESTAMPTZ NOT NULL,
    transaction_type VARCHAR(50),
    channel VARCHAR(50),
    merchant_id VARCHAR(255),
    merchant_name VARCHAR(255),
    beneficiary_account BIGINT,
    ip_address VARCHAR(50),
    device_id VARCHAR(255),
    location TEXT,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS fraud.fraud_events (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL,
    rule_id VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    reason TEXT,
    severity VARCHAR(50),
    detected_at TIMESTAMPTZ DEFAULT now()
    );

-- =============================================

-- DUMP SCRIPT FOR TRANSACTION AND FRAUD TABLES

-- =============================================

INSERT INTO fraud.fraud_rules (
    rule_id,
    data,
    version,
    active,
    create_at,
    update_at
) VALUES (
    'fraud-rule-001',
    '{
      "created_by": "fraud-admin",
      "created_at": "2025-09-30T16:00:00Z",
      "updated_at": "2025-09-30T16:25:00Z",
      "rules": [
        {
          "id": "rule-0001",
          "name": "High value transfer to new beneficiary",
          "description": "Block transfers > 1000000.00 to a new beneficiary",
          "condition": {
            "type": "AND",
            "operands": [
              {
                "type": "EQUALS",
                "field": "transferType",
                "value": "TRANSFER"
              },
              {
                "type": "GREATER_THAN",
                "field": "transferAmount",
                "value": 1000000.00
              },
              {
                "type": "EQUALS",
                "field": "currency",
                "value": "ZAR"
              }
            ]
          }
        },
         {
              "id": "rule-0002",
              "name": "Block accounts for beneficiary",
              "description": "Block transfer for certain blacklisted beneficiaries",
              "condition": {
                "type": "AND",
                "operands": [
                  {
                    "type": "EQUALS",
                    "field": "transactionType",
                    "value": "TRANSFER"
                  },
                  {
                    "type": "EQUALS",
                    "field": "currency",
                    "value": "ZAR"
                  },
                  {
                    "type": "INCLUDE",
                    "field": "beneficiary_account",
                    "value": ["32142347", "57437955", "80923904"]
                  }
                ]
              }
            }
      ],
      "metadata": {
        "tags": [
          "beneficiary",
          "high-value",
          "new-beneficiary"
        ],
        "ttl_days": 365,
        "version": 3
      }
    }'::jsonb,
    3,
    true,
    '2025-09-30 16:00:00',
    '2025-09-30 16:25:00'
)ON CONFLICT(rule_id) DO NOTHING;

-- Insert Rule 0002
INSERT INTO fraud.fraud_rules (
    rule_id,
    data,
    version,
    active,
    create_at,
    update_at
) VALUES (
    'rule-0002',
    '{
      "created_by": "fraud-admin",
      "created_at": "2025-09-30T16:00:00Z",
      "updated_at": "2025-09-30T16:25:00Z",
      "name": "High volume transfer to new beneficiary in 60 seconds",
      "description": "Block transfers to many transaction in given time to a new beneficiary",
      "condition": {
        "type": "AND",
        "operands": [
          {
            "type": "EQUALS",
            "field": "transactionType",
            "value": "TRANSFER"
          },
          {
            "type": "GREATER_THAN",
            "field": "accountId",
            "value": 4
          }
        ]
      },
      "metadata": {
        "tags": ["beneficiary", "high-value", "new-beneficiary"],
        "ttl_days": 365,
        "version": 3
      }
    }'::jsonb,
    2,
    false,
    '2025-09-30 16:00:00',
    '2025-09-30 16:25:00'
)ON CONFLICT(rule_id) DO NOTHING;

INSERT INTO fraud.transaction (
    transaction_id,
    account_id,
    user_id,
    currency,
    amount,
    timestamp,
    transaction_type,
    channel,
    merchant_id,
    merchant_name,
    beneficiary_account,
    ip_address,
    device_id,
    location,
    status
) VALUES
      ('TXN100001', 1001, 501, 'ZAR', 150.75, NOW() - INTERVAL '10 minutes', 'TRANSFER', 'MOBILE', 'M001', 'Amazon', 2001, '192.168.1.10', 'DEV001', 'New York, USA', 'COMPLETED'),
      ('TXN100002', 1002, 502, 'ZAR', 1200.00, NOW() - INTERVAL '5 minutes', 'TRANSFER', 'WEB', 'M002', 'Walmart', 2002, '192.168.1.20', 'DEV002', 'Los Angeles, USA', 'COMPLETED'),
      ('TXN100003', 1001, 501, 'ZAR', 5000.00, NOW() - INTERVAL '1 minute', 'TRANSFER', 'MOBILE', 'M003', 'Ebay', 65765765, '192.168.1.30', 'DEV003', 'Chicago, USA', 'COMPLETED'),
      ('TXN100004', 1003, 503, 'ZAR', 50.00, NOW() - INTERVAL '15 minutes', 'TRANSFER', 'POS', 'M004', 'Target', 56774523, '192.168.1.40', 'DEV004', 'Paris, France', 'COMPLETED'),
      ('TXN100005', 1001, 501, 'ZAR', 200.00, NOW(), 'TRANSFER', 'WEB', 'M005', 'BestBuy', 16887854, '192.168.1.50', 'DEV005', 'New York, USA', 'COMPLETED');

INSERT INTO fraud.fraud_events (
    rule_id,
    account_id,
    reason,
    type,
    severity,
    transaction_date,
    transaction_id,
    detected_at
) VALUES
      (1, 1001, 'Transaction velocity exceeded', 'VELOCITY_THRESHOLD', 'HIGH', NOW() - INTERVAL '10 minutes', 'TXN100001', NOW() - INTERVAL '9 minutes'),
      (2, 1002, 'Amount exceeded threshold', 'AMOUNT_THRESHOLD', 'MEDIUM', NOW() - INTERVAL '5 minutes', 'TXN100002', NOW() - INTERVAL '4 minutes'),
      (3, 1001, 'Beneficiary account is blacklisted', 'MERCHANT_BLACKLIST', 'HIGH', NOW() - INTERVAL '1 minute', 'TXN100003', NOW() - INTERVAL '1 minute'),
      (2, 1003, 'Amount exceeded threshold', 'AMOUNT_THRESHOLD', 'LOW', NOW() - INTERVAL '15 minutes', 'TXN100004', NOW() - INTERVAL '14 minutes'),
      (3, 1001, 'Beneficiary account is blacklisted', 'MERCHANT_BLACKLIST', 'HIGH', NOW(), 'TXN100005', NOW());

