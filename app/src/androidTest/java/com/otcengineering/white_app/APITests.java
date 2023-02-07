package com.otcengineering.white_app;

import android.os.Environment;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class APITests {
    private TestUtils mUtils = new TestUtils();

    @BeforeClass
    public static void before() throws Exception {

    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void prepareObjects() throws Exception {
        // Canvia el imei del usuari al del mobil actual
        Welcome.ChangePhone cp = Welcome.ChangePhone.newBuilder().setUsername(TestConstants.username).setPassword(TestConstants.password).setMobileIMEI(mUtils.getImei()).build();
        Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.CHANGE_PHONE, cp, Shared.OTCResponse.class);
        Assert.assertEquals(Shared.OTCStatus.SUCCESS, resp.getStatus());
        resp = ApiCaller.doCall(Endpoints.ENABLE_USER, mUtils.getUserToken(), Welcome.UserEnabled.newBuilder().setUsername(TestConstants.username).build(), Shared.OTCResponse.class);
        Assert.assertEquals(Shared.OTCStatus.SUCCESS, resp.getStatus());
    }

    @Test
    public void finishTests() throws AssertionError {
        String location = Environment.getExternalStorageDirectory().getPath() + "/logs.zip";
        Assert.assertTrue(mUtils.zipFileAtPath(mUtils.LOGS_FOLDER, location));
    }

    @Test
    public void clear() {
        File fp = new File(mUtils.LOGS_FOLDER);
        Assert.assertTrue(mUtils.deleteDirectory(fp));
        Assert.assertTrue(mUtils.deleteFile( new File(Environment.getExternalStorageDirectory() + "/logs.zip")));
    }

    private static final String s_className = "Welcome";

    @Test
    public void pattern() throws Exception {
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
    public void Test2W016() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ACTIVATE;
        String testId = mUtils.getId();
        Welcome.UserActivation.Builder builder = Welcome.UserActivation.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Ok
            // Posa aquí el valor
            builder.setSecret("9243");
            builder.setUsername(TestConstants.testUsername);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, null, builder.build(), Shared.OTCStatus.SUCCESS);
        } catch (Exception e){
        } finally {
            mUtils.testEnd(s_className, testId, pass);
            if (!pass) {
                throw new Exception("Unit test not passed");
            }
        }
    }
}
