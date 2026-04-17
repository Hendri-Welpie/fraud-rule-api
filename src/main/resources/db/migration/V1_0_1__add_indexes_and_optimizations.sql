-- V1_0_1: Add indexes and performance optimizations

-- Indexes for fraud_events table
CREATE INDEX IF NOT EXISTS idx_fraud_events_account_id ON fraud.fraud_events(account_id);
CREATE INDEX IF NOT EXISTS idx_fraud_events_detected_at ON fraud.fraud_events(detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_fraud_events_severity ON fraud.fraud_events(severity);
CREATE INDEX IF NOT EXISTS idx_fraud_events_transaction_id ON fraud.fraud_events(transaction_id);
CREATE INDEX IF NOT EXISTS idx_fraud_events_rule_id ON fraud.fraud_events(rule_id);
CREATE INDEX IF NOT EXISTS idx_fraud_events_account_detected ON fraud.fraud_events(account_id, detected_at DESC);

-- Indexes for transaction table
CREATE INDEX IF NOT EXISTS idx_transaction_account_id ON fraud.transaction(account_id);
CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON fraud.transaction(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_account_timestamp ON fraud.transaction(account_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_transaction_id ON fraud.transaction(transaction_id);

-- Indexes for fraud_rules table (JSONB)
CREATE INDEX IF NOT EXISTS idx_fraud_rules_active ON fraud.fraud_rules(active) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_fraud_rules_data_gin ON fraud.fraud_rules USING GIN (data);

-- Add risk_score column to fraud_events if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                   AND table_name = 'fraud_events'
                   AND column_name = 'risk_score') THEN
        ALTER TABLE fraud.fraud_events ADD COLUMN risk_score INTEGER DEFAULT 0;
    END IF;
END $$;

-- Add processing_time_ms column to fraud_events if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                   AND table_name = 'fraud_events'
                   AND column_name = 'processing_time_ms') THEN
        ALTER TABLE fraud.fraud_events ADD COLUMN processing_time_ms BIGINT DEFAULT 0;
    END IF;
END $$;

-- Create a partial index for high severity items for faster queries
CREATE INDEX IF NOT EXISTS idx_fraud_events_high_severity
    ON fraud.fraud_events(detected_at DESC)
    WHERE severity IN ('HIGH', 'CRITICAL');

-- Add statistics hints for query optimizer
ANALYZE fraud.fraud_events;
ANALYZE fraud.transaction;
ANALYZE fraud.fraud_rules;
