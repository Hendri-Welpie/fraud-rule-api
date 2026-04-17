package org.project.fraudruleapi.shared.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigurationTest {

    @Test
    void gettersAndSetters_shouldWork() {
        ApplicationConfiguration config = new ApplicationConfiguration();

        ApplicationConfiguration.Schedule schedule = new ApplicationConfiguration.Schedule();
        schedule.setUpdateRules("0 0 * * *");
        config.setSchedule(schedule);

        ApplicationConfiguration.FraudConfiguration fraud = new ApplicationConfiguration.FraudConfiguration();
        ApplicationConfiguration.EvaluationConfig evaluation = new ApplicationConfiguration.EvaluationConfig();
        evaluation.setParallelThreshold(10);
        evaluation.setTimeoutMs(10000);
        evaluation.setMaxConcurrentEvaluations(200);
        fraud.setEvaluation(evaluation);

        ApplicationConfiguration.VelocityConfig velocity = new ApplicationConfiguration.VelocityConfig();
        velocity.setEnabled(false);
        velocity.setWindowSeconds(120);
        velocity.setMaxTransactions(20);
        fraud.setVelocity(velocity);

        ApplicationConfiguration.RiskConfig risk = new ApplicationConfiguration.RiskConfig();
        risk.setHighThreshold(90);
        risk.setMediumThreshold(60);
        risk.setLowThreshold(30);
        fraud.setRisk(risk);

        ApplicationConfiguration.AmountConfig amount = new ApplicationConfiguration.AmountConfig();
        amount.setHighValueThreshold(new BigDecimal("2000000.00"));
        amount.setSuspiciousThreshold(new BigDecimal("1000000.00"));
        fraud.setAmount(amount);

        config.setFraud(fraud);

        assertEquals("0 0 * * *", config.getSchedule().getUpdateRules());
        assertEquals(10, config.getFraud().getEvaluation().getParallelThreshold());
        assertEquals(10000, config.getFraud().getEvaluation().getTimeoutMs());
        assertEquals(200, config.getFraud().getEvaluation().getMaxConcurrentEvaluations());
        assertFalse(config.getFraud().getVelocity().isEnabled());
        assertEquals(120, config.getFraud().getVelocity().getWindowSeconds());
        assertEquals(20, config.getFraud().getVelocity().getMaxTransactions());
        assertEquals(90, config.getFraud().getRisk().getHighThreshold());
        assertEquals(60, config.getFraud().getRisk().getMediumThreshold());
        assertEquals(30, config.getFraud().getRisk().getLowThreshold());
        assertEquals(new BigDecimal("2000000.00"), config.getFraud().getAmount().getHighValueThreshold());
        assertEquals(new BigDecimal("1000000.00"), config.getFraud().getAmount().getSuspiciousThreshold());
    }
}
