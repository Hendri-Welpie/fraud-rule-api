CREATE TABLE IF NOT EXISTS fraud.audit_trail
(
    id              BIGSERIAL PRIMARY KEY,
    trace_id        VARCHAR(255),
    principal       VARCHAR(255) NOT NULL DEFAULT 'anonymous',
    http_method     VARCHAR(10)  NOT NULL,
    uri             VARCHAR(1024) NOT NULL,
    response_status INTEGER,
    timestamp       TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

CREATE INDEX idx_audit_trail_principal ON fraud.audit_trail (principal);
CREATE INDEX idx_audit_trail_timestamp ON fraud.audit_trail (timestamp);
CREATE INDEX idx_audit_trail_uri ON fraud.audit_trail (uri);

