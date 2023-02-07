package com.otcengineering.white_app.apis;

import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomeTests {
    private static String s_className = "Welcome";
    private TestUtils mUtils = new TestUtils();

    @BeforeClass
    public static void before() throws Exception {

    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void Test1_W004() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.CHECK_EMAIL;
        String testId = mUtils.getId();
        Welcome.EmailVerification.Builder request = Welcome.EmailVerification.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: MISSING FIELDS
            try {
                mUtils.testRequestStart(s_className, testId, "MISSING FIELDS");

                response = ApiCaller.doCall(endpoint, request.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.REQUIRED_FIELDS_MISSING, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: EXISTING EMAIL
            try {
                mUtils.testRequestStart(s_className, testId, "EXISTING EMAIL");

                request.setEmail(TestConstants.email);
                response = ApiCaller.doCall(endpoint, request.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.EMAIL_ALREADY_USED, response.getStatus());

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

                request.setEmail(TestConstants.testEmail);
                response = ApiCaller.doCall(endpoint, request.build(), Shared.OTCResponse.class);
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
    public void Test1_W005() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.CHECK_USERNAME;
        String testId = mUtils.getId();
        Welcome.UsernameVerification.Builder builder = Welcome.UsernameVerification.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: MISSING FIELDS
            try {
                mUtils.testRequestStart(s_className, testId, "MISSING FIELDS");

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.REQUIRED_FIELDS_MISSING, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: EXISTING USER NAME
            try {
                mUtils.testRequestStart(s_className, testId, "EXISTING USER NAME");

                builder.setUsername(TestConstants.username);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.USERNAME_ALREADY_USED, response.getStatus());

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

                builder.setUsername(TestConstants.testUsername);
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
    public void Test1_W006() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.REGISTER;
        String testId = mUtils.getId();
        Welcome.UserRegistration.Builder builder = Welcome.UserRegistration.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: REQUIRED FIELDS MISSING
            try {
                mUtils.testRequestStart(s_className, testId, "REQUIRED FIELDS MISSING");

                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.REQUIRED_FIELDS_MISSING, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 2: USERNAME ALREADY USED
            try {
                mUtils.testRequestStart(s_className, testId, "USERNAME ALREADY USED");

                builder.setUsername(TestConstants.username);
                builder.setPassword(TestConstants.testPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.testEmail);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.USERNAME_ALREADY_USED, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 3: EMAIL ALREADY USED
            try {
                mUtils.testRequestStart(s_className, testId, "EMAIL ALREADY USED");

                builder.setUsername(TestConstants.testUsername);
                builder.setPassword(TestConstants.testPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.email);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.EMAIL_ALREADY_USED, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 4: MALFORMED USERNAME
            try {
                mUtils.testRequestStart(s_className, testId, "MALFORMED USERNAME");

                builder.setUsername(TestConstants.testBadUsername);
                builder.setPassword(TestConstants.testPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.testEmail);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.MALFORMED_USERNAME, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 5: MALFORMED PASSWORD
            try {
                mUtils.testRequestStart(s_className, testId, "MALFORMED PASSWORD");

                builder.setUsername(TestConstants.testUsername);
                builder.setPassword(TestConstants.testBadPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.testEmail);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.MALFORMED_PASSWORD, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 6: MALFORMED EMAIL
            try {
                mUtils.testRequestStart(s_className, testId, "MALFORMED EMAIL");

                builder.setUsername(TestConstants.testUsername);
                builder.setPassword(TestConstants.testPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.testBadEmail);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.MALFORMED_EMAIL, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 7: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                builder.setUsername(TestConstants.testUsername);
                builder.setPassword(TestConstants.testPassword);
                builder.setMobileIMEI(TestConstants.testImei);
                builder.setEmail(TestConstants.testEmail);
                builder.setMobilePhoneNumber(TestConstants.testPhone);
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
    public void Test1W016() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.ACTIVATE;
        String testId = mUtils.getId();
        Welcome.UserActivation.Builder builder = Welcome.UserActivation.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID USERNAME
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID USERNAME");

                builder.setUsername(TestConstants.testBadUsername);
                builder.setSecret("1234");
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
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

            // Test 2: INVALID SECRET
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID SECRET");

                builder.setUsername(TestConstants.testUsername);
                builder.setSecret("pizza");
                response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_PHONE_ACTIVATION_CODE, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Test 3: EXCEEDS MAX ATTEMPTS
            try {
                mUtils.testRequestStart(s_className, testId, "EXCEEDS MAX ATTEMPTS");

                builder.setUsername(TestConstants.testUsername);
                builder.setSecret("calamar");
                for (int i = 0; i < 5; ++i) {
                    response = ApiCaller.doCall(endpoint, builder.build(), Shared.OTCResponse.class);
                }
                assertEquals(Shared.OTCStatus.EXCEEDS_MAX_ATTEMPTS, response.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, response.getStatus().name());
                }
            }

            // Reenvia el SMS porfa
            ApiCaller.doCall("/v1/welcome/sms/resend", Welcome.SmsResend.newBuilder().setUsername(TestConstants.testUsername).build(), Shared.OTCResponse.class);
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
    public void Test1_W021() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.RESEND_SMS;
        String testId = mUtils.getId();
        Welcome.SmsResend.Builder builder = Welcome.SmsResend.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                builder.setUsername(TestConstants.testUsername);
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
}
