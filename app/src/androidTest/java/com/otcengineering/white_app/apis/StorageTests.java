package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Wallet;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StorageTests {
    private static String s_className = "Storage";
    private TestUtils mUtils = new TestUtils();
    private static ByteString testImage;

    @BeforeClass
    public static void before() throws Exception {
        TestUtils mUtils = new TestUtils();
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
        builder.setPage(1);
        builder.setSearchText(TestConstants.username);
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, mUtils.getUserToken(), builder.build(), Community.SearchUsersResponse.class);
        long fileID = resp.getUsers(0).getImage();
        byte[] image = ApiCaller.getImage(Endpoints.FILE_GET + fileID, new String(mUtils.getUserToken()));
        testImage = ByteString.copyFrom(image);
    }

    @Test
    public void ST001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.STORAGE_USER;
        String funId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, funId, endpoint, null, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            pass = mUtils.simplifiedRequest(s_className, funId, endpoint, mUtils.getUserToken(), null, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.testEnd(s_className, funId, pass);
            if (!pass) {
                throw new Exception();
            }
        }
    }

    @Test
    public void ST002() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.UPLOAD;
        String funId = mUtils.getId();

        Wallet.UploadDoc.Builder builder = Wallet.UploadDoc.newBuilder();

        try {
            mUtils.simplifiedTestStart(s_className, funId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID FILE
            builder.setType(Wallet.DocType.CAR_PICTURES);
            builder.setData(ByteString.EMPTY);
            builder.setName("Test.jpg");
            builder.setIndex(1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.INVALID_FILE);

            // Test 3: SUCCESS

            builder.setData(testImage);
            builder.setType(Wallet.DocType.CAR_PICTURES);
            builder.setIndex(1);
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
    public void ST003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.DELETE;
        String funId = mUtils.getId();
        Wallet.DeleteDoc.Builder builder = Wallet.DeleteDoc.newBuilder();

        ApiCaller.doCall(Endpoints.UPLOAD, mUtils.getUserToken(), Wallet.UploadDoc.newBuilder()
                .setIndex(1).setData(testImage).setName("test.jpg").setType(Wallet.DocType.DRIVING_LICENSE)
                .build(), Shared.OTCResponse.class);

        // Preparar fitxer
        Wallet.Docs docs = ApiCaller.doCall(Endpoints.STORAGE_USER, mUtils.getUserToken(), null, Wallet.Docs.class);
        Wallet.Doc doc = docs.getDocsList()
                .stream()
                .filter(d -> d.getType() == Wallet.DocType.DRIVING_LICENSE)
                .filter(d -> d.getIndex() == 1)
                .findFirst()
                .get();
        long id = doc.getId();

        try {
            mUtils.simplifiedTestStart(s_className, funId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.addDocsId(id);
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
    public void ST004() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.MANUAL;
        String funId = mUtils.getId();

        try {
            mUtils.simplifiedTestStart(s_className, funId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
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
