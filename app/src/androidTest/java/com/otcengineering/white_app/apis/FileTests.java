package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.FileProto;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.blecontrol.utils.Crc32;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FileTests {
    private static String s_className = "File";
    private TestUtils mUtils = new TestUtils();
    private static Long fileID;

    @BeforeClass
    public static void before() throws Exception {
        TestUtils mUtils = new TestUtils();

        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
        builder.setPage(1);
        builder.setSearchText(TestConstants.username);
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, mUtils.getUserToken(), builder.build(), Community.SearchUsersResponse.class);
        fileID = resp.getUsers(0).getImage();
    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void F001() throws Exception {
        boolean pass = true;
        byte[] response = null;
        String endpoint = Endpoints.FILE_GET;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);
            // Test 1: INVALID_AUTHORIZATION
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID AUTHORIZATION");

                response = ApiCaller.getImage(endpoint + fileID, null);
                Assert.assertNotNull(response);
                Shared.OTCResponse resp = Shared.OTCResponse.parseFrom(response);
                assertEquals(Shared.OTCStatus.INVALID_AUTHORIZATION, resp.getStatus());

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, "");
                }
            }

            // Test 2: SUCCESS
            try {
                mUtils.testRequestStart(s_className, testId, "SUCCESS");

                response = ApiCaller.getImage(endpoint + fileID, new String(mUtils.getUserToken()));
                Assert.assertNotNull(response);

                mUtils.testRequestEnd(s_className, testId, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mUtils.testRequestIsRunning()) {
                    pass = false;
                    mUtils.testRequestFails(s_className, testId, "");
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
    public void F002() throws Exception {
        boolean pass = true;
        Shared.OTCResponse response = null;
        String endpoint = Endpoints.FILE_UPLOAD;
        String testId = mUtils.getId();

        FileProto.UploadFile.Builder builder = FileProto.UploadFile.newBuilder();

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

            // Test 2: REQUIRED FIELDS MISSING
            try {
                mUtils.testRequestStart(s_className, testId, "REQUIRED FIELDS MISSING");

                builder.setFileName("test.txt");
                builder.setFileData(ByteString.EMPTY);
                builder.setFileCRC(1);

                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCResponse.class);
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

            // Test 3: INVALID CRC
            try {
                mUtils.testRequestStart(s_className, testId, "INVALID CRC");

                builder.setFileName("test.txt");
                builder.setFileData(ByteString.copyFrom(new byte[] {1, 2, 3}));
                builder.setFileCRC(1);

                response = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCResponse.class);
                assertEquals(Shared.OTCStatus.INVALID_CRC, response.getStatus());

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

                builder.setFileName("test.txt");
                builder.setFileData(ByteString.copyFrom(new byte[] {1, 2, 3}));
                builder.setFileCRC(Crc32.computeBuffer(builder.getFileData().toByteArray()));

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
