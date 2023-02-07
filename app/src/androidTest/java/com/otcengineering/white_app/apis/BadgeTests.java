package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BadgeTests {
    private static final String className = "Badge";
    private TestUtils mUtils = new TestUtils();

    @Test
    public void BA001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_BADGES;
        String funId = mUtils.getId();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            pass = mUtils.simplifiedRequest(true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }

    @Test
    public void BA002() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_BADGES_VERSION;
        String funId = mUtils.getId();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            pass = mUtils.simplifiedRequest(true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }

    @Test
    public void BA003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_BADGES_USER;
        String funId = mUtils.getId();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest( Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            pass = mUtils.simplifiedRequest(true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }
}
