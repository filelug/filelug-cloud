package org.clopuccino;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class UtilityTestCase {

    @Test
    public void testGenerateEncryptedUserComputerIdFrom() throws Exception {
        String userId = "730D1EE3A06C9290F798300AF9E8747576F7DD8468C3E23420CB1590B16E315452A06B37303E30FD8AB7AF8EB92383E3153CCD2CEAD2BE78FEF8B47975827B0F";
        Long computerId = 221L;

        String encryptedUserComputerId = Utility.generateEncryptedUserComputerIdFrom(userId, computerId);

        System.out.println(encryptedUserComputerId);

        assertTrue("Value not expected.", encryptedUserComputerId != null && encryptedUserComputerId.trim().length() > 0);
    }


}
