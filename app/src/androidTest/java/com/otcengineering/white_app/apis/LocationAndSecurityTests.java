package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.LocationAndSecurity;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LocationAndSecurityTests {
    private static String s_className = "LocationAndSecurity";
    private TestUtils mUtils = new TestUtils();

    @Test
    public void LS001() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.CAR;
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
    public void LS007() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.VEHICLE_STATUS;
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
    public void LS008() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.PHONE;
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
    public void LS009() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.GEOFENCING;
        String testId = mUtils.getId();
        LocationAndSecurity.Geofencing.Builder builder = LocationAndSecurity.Geofencing.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: INVALID_AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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

                builder.setPoint1Latitude(-1);
                builder.setPoint1Longitude(-1);
                builder.setPoint2Latitude(1);
                builder.setPoint2Longitude(-1);
                builder.setPoint3Latitude(1);
                builder.setPoint3Longitude(1);
                builder.setPoint4Latitude(-1);
                builder.setPoint4Longitude(1);
                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCResponse.class);
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
