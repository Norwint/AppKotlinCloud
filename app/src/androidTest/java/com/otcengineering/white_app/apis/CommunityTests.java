package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.DateUtils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class CommunityTests {
    private static final String className = "Community";
    private TestUtils mUtils = new TestUtils();

    private static long usrTestID, hiddenID, postID, myId;

    @BeforeClass
    public static void before() throws Exception {
        TestUtils utils = new TestUtils();
        Community.SearchUsers search = Community.SearchUsers.newBuilder().setPage(1).setSearchText(TestConstants.username).build();
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, utils.getUserToken(), search, Community.SearchUsersResponse.class);
        usrTestID = resp.getUsersList().stream().findFirst().get().getUserId();
        Community.SearchUsers search2 = Community.SearchUsers.newBuilder().setPage(1).setSearchText("user").build();
        Community.SearchUsersResponse resp2 = ApiCaller.doCall(Endpoints.SEARCH_USERS, utils.getUserToken(), search2, Community.SearchUsersResponse.class);
        hiddenID = resp2.getUsersList().stream().findFirst().get().getUserId();
        Community.UserPosts posts = Community.UserPosts.newBuilder().setPage(1).setUserId(usrTestID).build();
        Community.UserPostsResponse resp3 = ApiCaller.doCall(Endpoints.USER_POSTS, utils.getUserToken(), posts, Community.UserPostsResponse.class);
        postID = resp3.getPostsList().stream().findAny().get().getPostId();
        myId = ApiCaller.doCall(Endpoints.LOGIN, Welcome.Login.newBuilder()
                .setUsername(TestConstants.testUsername)
                .setPassword(TestConstants.testPassword)
                .setMobileIMEI(TestConstants.testImei).build(), Welcome.LoginResponse.class).getUserId();
    }

    @Test
    public void CO002() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.SEARCH_USERS;
        String funId = mUtils.getId();
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: NO DATA
            builder.setPage(1);
            builder.setSearchText("Cogombre de mar");
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.NO_DATA);

            // TEST 4: SUCCESS
            builder.setSearchText(TestConstants.username);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.SEARCH_USERS_NEAR;
        String funId = mUtils.getId();
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, funId, endpoint);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: NO DATA
            builder.setPage(1);
            builder.setSearchText("Cogombre de mar");
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.NO_DATA);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO004() throws Exception {
        boolean pass = true;
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.FRIENDS);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            builder.setPage(1);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO005() throws Exception {
        boolean pass = true;
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.FRIEND_REQUESTS);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            pass = mUtils.simplifiedRequest(builder.setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setPage(1), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO006() throws Exception {
        boolean pass = true;

        General.UserId.Builder builder = General.UserId.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_PROFILE);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: USER NOT FOUND
            pass = mUtils.simplifiedRequest(builder.setUserId(-1), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 3: PROFILE NOT VISIBLE
            pass = mUtils.simplifiedRequest(builder.setUserId(hiddenID), true, Shared.OTCStatus.PROFILE_NOT_VISIBLE);

            // TEST 4: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO007() throws Exception {
        boolean pass = true;

        Community.SendRequest.Builder builder = Community.SendRequest.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.SEND_REQUEST);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: USER NOT FOUND
            mUtils.simplifiedRequest(builder.setUserId(-1), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 3: MESSAGE EXCEEDS MAX LENGTH
            mUtils.simplifiedRequest(builder.setUserId(usrTestID).setMessage(Strings.repeat("HOLA", 100)), true, Shared.OTCStatus.MESSAGE_EXCEEDS_MAX_LENGTH);

            // TEST 4: SUCCESS
            mUtils.simplifiedRequest(builder.setMessage("Hola"), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO008() throws Exception {
        boolean pass = true;

        Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_USER_MILEAGE);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DATE
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart("HOLA"), true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 3: USER NOT FOUND
            pass = mUtils.simplifiedRequest(builder.setUserId(-1).setTypeTime(General.TimeType.MONTHLY).setDateStart("2019-08-01"), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 4: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart(DateUtils.getUtcString(DateUtils.FMT_SRV_DATE)).build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO009() throws Exception {
        boolean pass = true;

        Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_USER_ECO);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DATE
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart("HOLA"), true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 3: USER NOT FOUND
            pass = mUtils.simplifiedRequest(builder.setUserId(-1).setTypeTime(General.TimeType.MONTHLY).setDateStart("2019-08-01"), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 4: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart(DateUtils.getUtcString(DateUtils.FMT_SRV_DATE)).build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO010() throws Exception {
        boolean pass = true;

        Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_USER_SAFETY);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DATE
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart("HOLA"), true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 3: USER NOT FOUND
            pass = mUtils.simplifiedRequest(builder.setUserId(-1).setTypeTime(General.TimeType.MONTHLY).setDateStart("2019-08-01"), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 4: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setDateStart(DateUtils.getUtcString(DateUtils.FMT_SRV_DATE)).build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO011() throws Exception {
        boolean pass = true;

        Community.UserPosts.Builder builder = Community.UserPosts.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_POSTS);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: USER NOT FOUND
            pass = mUtils.simplifiedRequest(builder.setUserId(-1).setPage(1), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 4: SUCCESS
            pass = mUtils.simplifiedRequest(builder.setUserId(usrTestID).setPage(1).build(), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO012() throws Exception {
        boolean pass = true;

        Community.PostId.Builder builder = Community.PostId.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.POST_LIKE);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: POST NOT FOUND
            builder.setPostId(-1);
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.POST_NOT_FOUND);

            // TEST 3: SUCCESS
            builder.setPostId(postID);
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);

            // TEST 4: ALREADY LIKED
            builder.setPostId(postID);
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.ALREADY_LIKED);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void CO015() throws Exception {
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.POSTS);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            mUtils.simplifiedRequest(builder.setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(builder.setPage(1), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO016() throws Exception {
        General.Page.Builder builder = General.Page.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.FRIENDS_POSTS);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            mUtils.simplifiedRequest(builder.setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(builder.setPage(1), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO017() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.SEND_POST);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.SendPost.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DATE
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.RANKING_MILEAGE).setDateRankingStart("ñ").setDateRankingEnd("k"), true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 3: MESSAGE EXCEEDS MAX LENGTH
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.TEXT).setMessage(Strings.repeat("HELLO THERE", 140)), true, Shared.OTCStatus.MESSAGE_EXCEEDS_MAX_LENGTH);

            // TEST 4: ROUTE NOT FOUND
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.ROUTE).setRouteId(-1), true, Shared.OTCStatus.ROUTE_NOT_FOUND);

            // TEST 5: MALFORMED YOUTUBE URL
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.VIDEO).setVideoUrl("https://www.google.com"), true, Shared.OTCStatus.MALFORMED_YOUTUBE_URL);

            // TEST 6: EXCEEDS MAX FILE SIZE
            byte[] arr = new byte[20_000_000];
            Random rand = new Random();
            System.arraycopy(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10}, 0, arr, 0, 8);
            for (int i = 8; i < arr.length; ++i) {
                arr[i] = (byte) ((rand.nextInt() & 0xFF) ^ 0xCD);
            }
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.IMAGE).setImage(ByteString.copyFrom(arr)).setImageName("jojo.jpg"), true, Shared.OTCStatus.EXCEEDS_MAX_FILE_SIZE);

            // TEST 7: SUCCESS
            mUtils.simplifiedRequest(Community.SendPost.newBuilder().setType(Community.PostType.TEXT).setMessage("Hola"), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO022() throws Exception {
        ApiCaller.doCall(Endpoints.SEND_REQUEST, mUtils.login(TestConstants.username, TestConstants.password, TestConstants.imei),
                Community.SendRequest.newBuilder().setUserId(myId).build(), Shared.OTCResponse.class);

        long invId = ApiCaller.doCall(Endpoints.FRIEND_REQUESTS, mUtils.getUserToken(), General.Page.newBuilder().setPage(1).build(), Community.FriendRequests.class).getUsersList().stream().findFirst().get().getInvitationId();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.ANSWER_REQUEST);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.AnswerRequest.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVITATION NOT FOUND
            mUtils.simplifiedRequest(Community.AnswerRequest.newBuilder().setRequestId(-1), true, Shared.OTCStatus.REQUEST_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(Community.AnswerRequest.newBuilder().setRequestId(invId), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO024() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.REPORT_POST);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.PostReport.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: POST NOT FOUND
            mUtils.simplifiedRequest(Community.PostReport.newBuilder().setPostId(-1), true, Shared.OTCStatus.POST_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(Community.PostReport.newBuilder().setPostId(postID), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO025() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.UNFRIEND);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.Unfriend.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(Community.Unfriend.newBuilder().addUsersId(usrTestID), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO026() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.BLOCK_USER);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.BlockUser.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: USER NOT FOUND
            mUtils.simplifiedRequest(Community.BlockUser.newBuilder().setUserId(-1), true, Shared.OTCStatus.USER_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(Community.BlockUser.newBuilder().setUserId(hiddenID).setType(Community.BlockType.BLOCK), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO027() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_FRIENDS);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.UserFriends.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            mUtils.simplifiedRequest(Community.UserFriends.newBuilder().setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(Community.UserFriends.newBuilder().setUserId(usrTestID).setPage(1), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO028() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_NUMBER_REQUEST);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO029() throws Exception {
        MyTrip.RoutesResponse routes = ApiCaller.doCall(Endpoints.ROUTES, mUtils.login(TestConstants.username, TestConstants.password, TestConstants.imei),
                MyTrip.Routes.newBuilder().setPage(1).setRouteType(General.RouteType.AUTOSAVED).build(), MyTrip.RoutesResponse.class);
        long rouId = routes.getRoutesList().stream().findAny().get().getId();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.ROUTE_LIKE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(MyTrip.RouteId.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: ROUTE NOT FOUND
            mUtils.simplifiedRequest(MyTrip.RouteId.newBuilder().setRouteId(-1), true, Shared.OTCStatus.ROUTE_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(MyTrip.RouteId.newBuilder().setRouteId(rouId), true, Shared.OTCStatus.SUCCESS);

            // TEST 4: ALREADY LIKED
            mUtils.simplifiedRequest(MyTrip.RouteId.newBuilder().setRouteId(rouId), true, Shared.OTCStatus.ALREADY_LIKED);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO031() throws Exception {
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
        builder.setPage(1);
        builder.setSearchText(TestConstants.username);
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, mUtils.getUserToken(), builder.build(), Community.SearchUsersResponse.class);
        long fileID = resp.getUsers(0).getImage();
        byte[] image = ApiCaller.getImage(Endpoints.FILE_GET + fileID, new String(mUtils.getUserToken()));

        long rouId = ApiCaller.doCall(Endpoints.ROUTES,  mUtils.getUserToken(), MyTrip.Routes.newBuilder().setPage(1).setRouteType(General.RouteType.PLANNED).build(), MyTrip.RoutesResponse.class).getRoutesList().stream().findAny().get().getId();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.ROUTE_IMAGE_GENERATE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Community.RouteImageGenerator.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: ROUTE NOT FOUND
            mUtils.simplifiedRequest(Community.RouteImageGenerator.newBuilder().setRouteId(-1).setImage(ByteString.copyFrom(image)), true, Shared.OTCStatus.ROUTE_NOT_FOUND);

            // TEST 3: INVALID FILE
            mUtils.simplifiedRequest(Community.RouteImageGenerator.newBuilder().setRouteId(rouId).setImage(ByteString.copyFrom(new byte[]{1, 2, 3})), true, Shared.OTCStatus.INVALID_FILE);

            // TEST 4: SUCCESS
            mUtils.simplifiedRequest(Community.RouteImageGenerator.newBuilder().setImage(ByteString.copyFrom(image)).setRouteId(rouId), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void CO032() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.POST_STATS);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }
}
