package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestFunctions;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SecondRegisterTests {
    private static String s_className = "Welcome";
    private TestUtils mUtils = new TestUtils();

    @BeforeClass
    public static void before() throws Exception {

    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void Test3_W007() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.DEALERSHIPS;
        String testId = mUtils.getId();
        Welcome.Dealerships.Builder builder = Welcome.Dealerships.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: COUNTRY NOT FOUND
            try {
                mUtils.testRequestStart(s_className, testId, "COUNTRY NOT FOUND");

                builder.setCountryId(69);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.COUNTRY_NOT_FOUND, response.getStatus());

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

                builder.setCountryId(2);
                builder.setLatitude(TestConstants.latitudeJakarta);
                builder.setLongitude(TestConstants.longitudeJakarta);

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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
    public void Test3_W008() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.DEALERSHIPS_BY_NAME;
        String testId = mUtils.getId();
        Welcome.DealershipName.Builder builder = Welcome.DealershipName.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: MALFORMED DEALERSHIP NAME
            try {
                mUtils.testRequestStart(s_className, testId, "MALFORMED DEALERSHIP NAME");

                builder.setName(TestConstants.testBadDealerName);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.MALFORMED_DEALERSHIP_NAME, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: NO DATA
            try {
                mUtils.testRequestStart(s_className, testId, "NO DATA");

                builder.setName(TestConstants.testDealerNot);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.NO_DATA, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 3: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                builder.setName(TestConstants.testDealer);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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
        } finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }

    @Test
    public void Test3_W009() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.GET_COUNTRIES;
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
    public void Test3_W010() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.GET_TERMS_ACCEPTANCE;
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
    public void Test3_W011() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.MODEL;
        String testId = mUtils.getId();
        Welcome.Model.Builder builder = Welcome.Model.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: MALFORMED VIN
            try {
                mUtils.testRequestStart(s_className, testId, "MALFORMED VIN");

                builder.setVin(TestConstants.testBadVin);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.MALFORMED_VIN, response.getStatus());

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

                builder.setVin(TestConstants.testVin);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.SUCCESS, response.getStatus());
                Welcome.ModelResponse mr = response.getData().unpack(Welcome.ModelResponse.class);
                System.out.println(mr);

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
    public void Test3_W015() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.REGIONS;
        String testId = mUtils.getId();
        Welcome.Regions.Builder builder = Welcome.Regions.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: COUNTRY NOT FOUND
            try {
                mUtils.testRequestStart(s_className, testId, "COUNTRY NOT FOUND");

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.COUNTRY_NOT_FOUND, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: SUCCESS INDIA
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS INDIA");

                builder.setCountryId(1);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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

            // Test 3: SUCCESS INDONESIA
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS INDONESIA");

                builder.setCountryId(2);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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
    public void Test3_W018() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.CITIES;
        String testId = mUtils.getId();
        Welcome.CityRequest.Builder builder = Welcome.CityRequest.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: COUNTRY NOT FOUND
            try {
                mUtils.testRequestStart(s_className, testId, "COUNTRY NOT FOUND");

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.COUNTRY_NOT_FOUND, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: SUCCESS INDIA
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS INDIA");

                builder.setCountryId(1);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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

            // Test 3: SUCCESS INDONESIA
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS INDONESIA");

                builder.setCountryId(2);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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
    public void Test3_W019() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ENABLE_USER;
        String testId = mUtils.getId();
        Welcome.UserEnabled.Builder builder = Welcome.UserEnabled.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: USER PROFILE REQUIRED
            builder.setUsername(TestConstants.testUsername);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.USER_PROFILE_REQUIRED);
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
    public void Test4_W014() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.PROFILE;
        String testId = mUtils.getId();
        General.UserProfile.Builder builder = General.UserProfile.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: REQUIRED FIELDS MISSING
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.REQUIRED_FIELDS_MISSING);

            // Test 3: MALFORMED DATE
            builder = TestFunctions.getUserProfileBadDate();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DATE);

            // Test 4: MALFORMED VIN
            builder = TestFunctions.getUserProfileBadVin();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_VIN);

            // Test 5: MALFORMED PLATE
            builder = TestFunctions.getUserProfileBadPlate();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_PLATE);

            // Test 6: COUNTRY NOT FOUND
            builder = TestFunctions.getUserProfileBadCountry();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.COUNTRY_NOT_FOUND);

            // Test 7: MALFORMED DONGLE SERIAL NUMBER
            builder = TestFunctions.getUserProfile();
            builder.setDongleSerialNumber("qwerty");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DONGLE_SERIAL_NUMBER);

            // Test 8: DONGLE SERIAL NUMBER NOT FOUND
            builder = TestFunctions.getUserProfile();
            builder.setDongleSerialNumber("9876987698769876");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.DONGLE_SERIAL_NUMBER_NOT_FOUND);

            // Test 9: DONGLE SERIAL NUMBER NOT UNIQUE
            builder = TestFunctions.getUserProfile();
            builder.setDongleSerialNumber(TestConstants.sn);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.DONGLE_SERIAL_NUMBER_NOT_UNIQUE);

            // Test 10: SUCCESS
            builder = TestFunctions.getUserProfile();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.SUCCESS);

            // Test 11: PROFILE ALREADY EXISTS
            builder = TestFunctions.getUserProfile();
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.PROFILE_ALREADY_EXISTS);

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
    public void Test4_W019() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ENABLE_USER;
        String testId = mUtils.getId();
        Welcome.UserEnabled.Builder builder = Welcome.UserEnabled.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setUsername(TestConstants.testUsername);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.SUCCESS);
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
    public void W017() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.LOGOUT;
        String testId = mUtils.getId();
        Welcome.Logout.Builder builder = Welcome.Logout.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                builder.setUsername(TestConstants.testBadUsername);
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

            // Test 2: INVALID USERNAME OR PASSWORD
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID USERNAME OR PASSWORD");

                builder.setUsername(TestConstants.testBadUsername);
                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 3: UNAUTHORIZED
            try {
                mUtils.testRequestStart(s_className, testId, "UNAUTHORIZED");

                builder.setUsername(TestConstants.username);
                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.UNAUTHORIZED, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 4: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                builder.setUsername(TestConstants.testUsername);
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

    @Test
    public void W002() throws Exception {
        String endpoint = Endpoints.LOGIN;
        String testId = mUtils.getId();
        mUtils.simplifiedTestStart(s_className, testId, endpoint);

        Welcome.Login.Builder builder = Welcome.Login.newBuilder();

        // Required fields missing
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.REQUIRED_FIELDS_MISSING);

        // Invalid Username or Password
        builder.setUsername(TestConstants.testBadUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.testImei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD);

        // Success
        builder.setUsername(TestConstants.testUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.testImei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.SUCCESS);

        // New Mobile
        builder.setUsername(TestConstants.testUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.imei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.NEW_MOBILE);

        mUtils.simplifiedTestEnd(true);
    }

    @Test
    public void W026() throws Exception {
        String endpoint = Endpoints.DEVICE_SPECS;
        String testId = mUtils.getId();
        mUtils.simplifiedTestStart(s_className, testId, endpoint);

        Welcome.DeviceSpecs.Builder builder = Welcome.DeviceSpecs.newBuilder();

        // SUCCESS
        builder.setAppVersion("ninja");
        builder.setMobileIMEI(TestConstants.testImei);
        builder.setMobileSO("Windows 10 Pro");
        mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);

        mUtils.simplifiedTestEnd(true);
    }

    @Test
    public void W027() throws Exception {
        String endpoint = Endpoints.CHANGE_PHONE;
        String testId = mUtils.getId();
        mUtils.simplifiedTestStart(s_className, testId, endpoint);

        Welcome.ChangePhone.Builder builder = Welcome.ChangePhone.newBuilder();

        // REQUIRED FIELDS MISSING
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.REQUIRED_FIELDS_MISSING);

        // INVALID USERNAME OR PASSWORD
        builder.setUsername("bad");
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.testImei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_USERNAME_OR_PASSWORD);

        // MALFORMED IMEI
        builder.setUsername(TestConstants.testUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI("notaimei");
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.MALFORMED_IMEI);

        // INVALID IMEI
        builder.setUsername(TestConstants.testUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.testImei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_IMEI);

        // SUCCESS
        builder.setUsername(TestConstants.testUsername);
        builder.setPassword(TestConstants.testPassword);
        builder.setMobileIMEI(TestConstants.imei);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.SUCCESS);

        mUtils.simplifiedTestEnd(true);

        builder.setMobileIMEI(TestConstants.testImei);
        ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
        Welcome.UserEnabled un = Welcome.UserEnabled.newBuilder().setUsername(TestConstants.testUsername).build();
        ApiCaller.doCall(Endpoints.ENABLE_USER, un, Shared.OTCResponse.class);
    }

    @Test
    public void W003() throws Exception {
        String endpoint = Endpoints.PASSWORD_RECOVERY;
        String testId = mUtils.getId();
        mUtils.simplifiedTestStart(s_className, testId, endpoint);

        Welcome.PasswordRecovery.Builder builder = Welcome.PasswordRecovery.newBuilder();

        // EMAIL NOT FOUND
        builder.setEmail(TestConstants.testBadEmail);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.EMAIL_NOT_FOUND);

        // SUCCESS
        builder.setEmail(TestConstants.testEmail);
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.SUCCESS);

        // PASSWORD RECOVERY ALREADY EXISTS
        mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.PASSWORD_RECOVERY_ALREADY_EXISTS);

        mUtils.simplifiedTestEnd(true);
    }
}
