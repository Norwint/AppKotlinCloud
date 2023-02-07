package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Configuration;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ConfigurationTests {
    private static final String className = "Configuration";
    private TestUtils mUtils = new TestUtils();


    @Test
    public void CN001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.FIRMWARE;
        String funId = mUtils.getId();

        Configuration.FirmwareVersion.Builder builder = Configuration.FirmwareVersion.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED FIRMWARE VERSION
            builder.setVersion("6,022");
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.MALFORMED_FIRMWARE_VERSION);

            // TEST 3: NEW VERSION NOT FOUND
            builder.setVersion("3.2");
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.NEW_VERSION_NOT_FOUND);

            // TEST 4: SUCCESS
            builder.setVersion("0.1");
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }

    @Test
    public void CN003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.VERSION;
        String funId = mUtils.getId();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: SUCCESS
            pass = mUtils.simplifiedRequest(Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }
}
