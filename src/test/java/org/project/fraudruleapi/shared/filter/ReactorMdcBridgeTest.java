package org.project.fraudruleapi.shared.filter;

import org.junit.jupiter.api.Test;

class ReactorMdcBridgeTest {

    @Test
    void setup_shouldInstallBridge() {
        ReactorMdcBridge bridge = new ReactorMdcBridge();
        bridge.setup();
        // Test that no exception is thrown
    }
}
