package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CommunityMDTests {
    private static final String className = "CommunityMMCDealer";
    private TestUtils mUtils = new TestUtils();

    @Test
    public void CD001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.CONNECTECH_POSTS;
        String funId = mUtils.getId();
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            builder.setPage(1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD002() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.DEALER_POSTS;
        String funId = mUtils.getId();
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            builder.setPage(1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.CONNECTECH_MESSAGES;
        String funId = mUtils.getId();
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            builder.setPage(1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD004() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.DEALER_MESSAGES;
        String funId = mUtils.getId();
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder.build(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            builder.setPage(1);
            pass = mUtils.simplifiedRequest(builder.build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {
        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD005() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_DEALER;
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
        }
    }

    @Test
    public void CD006() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.DEALER_REPORT;
        String funId = mUtils.getId();

        // Get some posts
        long id = mUtils.getPostID(true);

        Community.PostId.Builder builder = Community.PostId.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: POST NOT FOUND
            builder.setPostId(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.POST_NOT_FOUND);

            // TEST 3: SUCCESS
            builder.setPostId(id);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD007() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.CONNECTECH_LIKE;
        String funId = mUtils.getId();

        // Get some posts
        long id = mUtils.getPostID(false);

        Community.PostId.Builder builder = Community.PostId.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: POST NOT FOUND
            builder.setPostId(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.POST_NOT_FOUND);

            // TEST 3: SUCCESS
            builder.setPostId(id);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);

            // TEST 4: ALREADY LIKED
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.ALREADY_LIKED);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CD009() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.DEALER_LIKE;
        String funId = mUtils.getId();

        // Get some posts
        long id = mUtils.getPostID(true);

        Community.PostId.Builder builder = Community.PostId.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: POST NOT FOUND
            builder.setPostId(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.POST_NOT_FOUND);

            // TEST 3: SUCCESS
            builder.setPostId(id);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);

            // TEST 4: ALREADY LIKED
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.ALREADY_LIKED);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }
}
