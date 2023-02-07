package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DashboardAndStatusTests {
    private static String s_className = "DashboardAndStatus";
    private TestUtils mUtils = new TestUtils();
    @BeforeClass
    public static void before() throws Exception {

    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void DS001() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.DASHBOARD;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: INVALID_AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                response = ApiCaller.doCall(endpoint, null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_AUTHORIZATION, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.SUCCESS, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }

    @Test
    public void DS002() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.VEHICLE_CONDITION;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: INVALID_AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                response = ApiCaller.doCall(endpoint, null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_AUTHORIZATION, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.SUCCESS, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }

    @Test
    public void DS003() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.VEHICLE_CONDITION_DESCRIPTION;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                response = ApiCaller.doCall(endpoint, null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.SUCCESS, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }

    @Test
    public void DS004() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.DASHBOARD_CAR_PHOTO;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: INVALID_AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                response = ApiCaller.doCall(endpoint, null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_AUTHORIZATION, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), null, Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.SUCCESS, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }
}
