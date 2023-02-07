package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestFunctions;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class ProfileAndSettingsTests {
    private static final String className = "ProfileAndSettings";
    private TestUtils mUtils = new TestUtils();

    @Test
    public void PS004() throws Exception {
        boolean pass = true;

        ProfileAndSettings.UserUpdate.Builder builder = ProfileAndSettings.UserUpdate.newBuilder();
        General.UserProfile.Builder profile;

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_UPDATE);

            // TEST 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: REQUIRED FIELDS MISSING
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.REQUIRED_FIELDS_MISSING);

            // TEST 3: USERNAME ALREADY USED
            profile = TestFunctions.getUserProfile();
            builder.setProfile(profile).setUsername(TestConstants.username).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.USERNAME_ALREADY_USED);

            // TEST 4: EMAIL ALREADY USED
            profile = TestFunctions.getUserProfile();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.email);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.EMAIL_ALREADY_USED);

            // TEST 5: MALFORMED PASSWORD
            profile = TestFunctions.getUserProfile();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword("ñ");
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_PASSWORD);

            // TEST 6: MALFORMED USERNAME
            profile = TestFunctions.getUserProfile();
            builder.setProfile(profile).setUsername("a 12 x").setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_USERNAME);

            // TEST 7: MALFORMED EMAIL
            profile = TestFunctions.getUserProfile();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail("ç").setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_EMAIL);

            // TEST 8: MALFORMED DATE
            profile = TestFunctions.getUserProfileBadDate();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 9: MALFORMED VIN
            /*profile = TestFunctions.getUserProfileBadVin();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_VIN);*/

            // TEST 10: MALFORMED PLATE
            profile = TestFunctions.getUserProfileBadPlate();
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_PLATE);

            // TEST 11: COUNTRY NOT FOUND
            profile = TestFunctions.getUserProfile();
            profile.setCountryId(50);
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.COUNTRY_NOT_FOUND);

            // TEST 12: MALFORMED DONGLE SERIAL NUMBER
            /*profile = TestFunctions.getUserProfile();
            profile.setDongleSerialNumber("patata fregida 123");
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_DONGLE_SERIAL_NUMBER);*/

            // TEST 13: VEHICLE NOT FOUND
            /*profile = TestFunctions.getUserProfile();
            profile.setVin("6KX6FG164YZJYS7JZ");
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.VEHICLE_NOT_FOUND);*/

            // TEST 14: SUCCESS
            profile = TestFunctions.getUserProfile();
            profile.setVin(TestConstants.testVin);
            builder.setProfile(profile).setUsername(TestConstants.testUsername).setPhone(TestConstants.testPhone).setEmail(TestConstants.testEmail).setPassword(TestConstants.password);
            pass = mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd(pass);
        }
    }

    @Test
    public void PS006() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.TYPE_UPDATE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.ProfilePrivacyTypeUpdate.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.ProfilePrivacyTypeUpdate.newBuilder().setProfileType(General.ProfileType.ONLY_FRIENDS), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS009() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_INFO);

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
    public void PS010() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_TERMS);

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
    public void PS014() throws Exception {
        General.TermAcceptance.Builder builder = General.TermAcceptance.newBuilder();

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_TERMS_UPDATE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.UserTerms.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DATE
            builder.setTimestamp("hola");
            mUtils.simplifiedRequest(ProfileAndSettings.UserTerms.newBuilder().addTerms(builder), true, Shared.OTCStatus.MALFORMED_DATE);

            // TEST 3: SUCCESS
            builder.setTimestamp("2019-08-30 08:00:00");
            builder.setType(General.TermType.DISCLAIMER);
            builder.setMobileIdentifier(TestConstants.testImei);
            builder.setVersion("1.0");
            mUtils.simplifiedRequest(ProfileAndSettings.UserTerms.newBuilder().addTerms(builder), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS015() throws Exception {
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
        builder.setPage(1);
        builder.setSearchText(TestConstants.username);
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, mUtils.getUserToken(), builder.build(), Community.SearchUsersResponse.class);
        long fileID = resp.getUsers(0).getImage();
        byte[] image = ApiCaller.getImage(Endpoints.FILE_GET + fileID, new String(mUtils.getUserToken()));

        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_IMAGE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.UserImage.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: EXCEEDS MAX FILE SIZE
            byte[] arr = new byte[20_000_000];
            Random rand = new Random();
            System.arraycopy(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10}, 0, arr, 0, 8);
            for (int i = 8; i < arr.length; ++i) {
                arr[i] = (byte) ((rand.nextInt() & 0xFF) ^ 0xCD);
            }
            mUtils.simplifiedRequest(ProfileAndSettings.UserImage.newBuilder().setData(ByteString.copyFrom(arr)).setName("jojo.png"), true, Shared.OTCStatus.EXCEEDS_MAX_FILE_SIZE);

            // TEST 3: INVALID FILE
            mUtils.simplifiedRequest(ProfileAndSettings.UserImage.newBuilder().setData(ByteString.copyFrom(new byte[]{1, 2, 3})).setName("jojo.png"), true, Shared.OTCStatus.INVALID_FILE);

            // TEST 4: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.UserImage.newBuilder().setData(ByteString.copyFrom(image)).setName("img.png"), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS016() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.USER_IMAGE_DELETE);

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
    public void PS017() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_NOTIFICATIONS);

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
    public void PS018() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.NOTIFICATIONS_UPDATE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.NotificationsStatus.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.NotificationsStatus.newBuilder().setNewFriendRequest(true), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS021() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.SOCIAL_NETWORK);

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
    public void PS022() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.SOCIAL_NETWORK_UPDATE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.SocialNetworkStatus.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.SocialNetworkStatus.newBuilder().setSaveRecentTripEnabled(true), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS023() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.GET_USER_NOTIFICATIONS);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(General.Page.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: INVALID PAGE NUMBER
            mUtils.simplifiedRequest(General.Page.newBuilder().setPage(-1), true, Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(General.Page.newBuilder().setPage(1), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS024() throws Exception {
        long notifId = ApiCaller.doCall(Endpoints.GET_USER_NOTIFICATIONS, mUtils.getUserToken(), General.Page.newBuilder().setPage(1).build(), ProfileAndSettings.UserNotifications.class)
                .getNotificationListList().stream().findAny().get().getId();
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.NOTIFICATIONS_DELETE);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: NOTIFICATION NOT FOUND
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder().setId(-1), true, Shared.OTCStatus.NOTIFICATION_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder().setId(notifId), true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS025() throws Exception {
        long notifId = ApiCaller.doCall(Endpoints.GET_USER_NOTIFICATIONS, mUtils.getUserToken(), General.Page.newBuilder().setPage(1).build(), ProfileAndSettings.UserNotifications.class)
                .getNotificationListList().stream().findAny().get().getId();
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.READ_NOTIFICATION);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder(), false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: NOTIFICATION NOT FOUND
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder().setId(-1), true, Shared.OTCStatus.NOTIFICATION_NOT_FOUND);

            // TEST 3: SUCCESS
            mUtils.simplifiedRequest(ProfileAndSettings.IdUserNotification.newBuilder().setId(notifId), true, Shared.OTCStatus.SUCCESS);

        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS026() throws Exception {
        ProfileAndSettings.ExpirationExtension.Builder builder = ProfileAndSettings.ExpirationExtension.newBuilder();
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.LAST_DONGLE_CONNECTION);

            // TEST 1: INVALID AUTHORIZATION
            mUtils.simplifiedRequest(builder, false, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // TEST 2: MALFORMED DONGLE SERIAL NUMBER
            builder.setDongleMAC(TestConstants.testMac);
            builder.setDongleSerialNumber("ç a ♂");
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.MALFORMED_DONGLE_SERIAL_NUMBER);

            // TEST 3: INVALID DONGLE SERIAL NUMBER
            builder.setDongleSerialNumber(TestConstants.sn);
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.INVALID_DONGLE_SERIAL_NUMBER);

            // TEST 4: INVALID DONGLE MAC
            builder.setDongleSerialNumber(TestConstants.testSN);
            builder.setDongleMAC("patates fregides");
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.INVALID_DONGLE_MAC);

            // TEST 5: SUCCESS
            builder.setDongleMAC(TestConstants.testMac);
            mUtils.simplifiedRequest(builder, true, Shared.OTCStatus.SUCCESS);
        } catch (Exception e) {

        } finally {
            mUtils.simplifiedTestEnd();
        }
    }

    @Test
    public void PS027() throws Exception {
        try {
            mUtils.simplifiedTestStart(className, mUtils.getId(), Endpoints.NOTIFICATION_COUNT);

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
