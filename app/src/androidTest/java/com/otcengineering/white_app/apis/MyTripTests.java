package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.TestConstants;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MyTripTests {
    private static String s_className = "MyTrip";
    private TestUtils mUtils = new TestUtils();

    // Variables
    private static Long routeID;
    private static Long poiID;

    @BeforeClass
    public static void before() throws Exception {
        // Upload route
        MyTrip.RouteNew.Builder builder = MyTrip.RouteNew.newBuilder();
        builder.setType(General.RouteType.PLANNED);
        builder.setDateStart("2019-01-01 00:00:00");
        builder.setDateEnd("2019-02-01 00:00:00");
        builder.setLocalDateStart("2019-01-01 01:00:00");
        builder.setTitle("Unit test");
        builder.setDescription("Test");
        builder.setDuration(44640);
        builder.setDistance(2E4);
        builder.setConsumption(952);
        builder.setAvgConsumption(21);

        TestUtils utils = new TestUtils();

        MyTrip.RouteId rouId = ApiCaller.doCall(Endpoints.ROUTE_NEW, utils.getUserToken(), builder.build(), MyTrip.RouteId.class);
        routeID = rouId.getRouteId();
    }

    private long findRouteWithPoi() throws ApiCaller.OTCException {
        MyTrip.Routes rts = MyTrip.Routes.newBuilder()
                .setRouteType(General.RouteType.PLANNED).setPage(1).build();
        MyTrip.RoutesResponse rr = ApiCaller.doCall(Endpoints.ROUTES, mUtils.getUserToken(), rts, MyTrip.RoutesResponse.class);
        for (MyTrip.Route r : rr.getRoutesList()) {
            MyTrip.RouteId id = MyTrip.RouteId.newBuilder().setRouteId(r.getId()).build();
            MyTrip.RoutePoisResponse rpr = ApiCaller.doCall(Endpoints.ROUTE_POIS, mUtils.getUserToken(), id, MyTrip.RoutePoisResponse.class);
            if (rpr.getPoisCount() > 0) {
                return rpr.getPois(0).getPoiId();
            }
        }
        return -1;
    }

    private long findPoiWithImage() throws ApiCaller.OTCException {
        MyTrip.Routes.Builder rts = MyTrip.Routes.newBuilder()
                .setRouteType(General.RouteType.PLANNED).setPage(1);
        for (int i = 0; i < 10; ++i) {
            rts.setPage(i + 1);
            MyTrip.RoutesResponse rr = ApiCaller.doCall(Endpoints.ROUTES, mUtils.getUserToken(), rts.build(), MyTrip.RoutesResponse.class);
            for (MyTrip.Route r : rr.getRoutesList()) {
                MyTrip.RouteId id = MyTrip.RouteId.newBuilder().setRouteId(r.getId()).build();
                MyTrip.RoutePoisResponse rpr = ApiCaller.doCall(Endpoints.ROUTE_POIS, mUtils.getUserToken(), id, MyTrip.RoutePoisResponse.class);
                for (General.POI poi : rpr.getPoisList()) {
                    if (poi.getImagesCount() > 0) {
                        return poi.getImages(0);
                    }
                }
            }
        }
        return -1;
    }

    private void createPoi() throws ApiCaller.OTCException {
        String endpoint = Endpoints.ROUTE_ADD_POI;
        General.POI.Builder builder = General.POI.newBuilder();
        builder.setLatitude(1.0);
        builder.setLongitude(1.0);
        builder.setTitle("TEST");
        builder.setType(General.PoiType.GAS_STATION);
        builder.setRouId(routeID);
        MyTrip.PoiId pid = ApiCaller.doCall(endpoint, mUtils.getUserToken(), builder.build(), MyTrip.PoiId.class);
        poiID = pid.getPoiId();
    }

    private void createRoute() throws ApiCaller.OTCException {
        MyTrip.RouteNew.Builder builder = MyTrip.RouteNew.newBuilder();
        builder.setType(General.RouteType.PLANNED);
        builder.setDateStart("2019-01-01 00:00:00");
        builder.setDateEnd("2019-02-01 00:00:00");
        builder.setLocalDateStart("2019-01-01 01:00:00");
        builder.setTitle("Unit test");
        builder.setDescription("Test");
        builder.setDuration(44640);
        builder.setDistance(2E4);
        builder.setConsumption(952);
        builder.setAvgConsumption(21);

        TestUtils utils = new TestUtils();

        MyTrip.RouteId rouId = ApiCaller.doCall(Endpoints.ROUTE_NEW, utils.getUserToken(), builder.build(), MyTrip.RouteId.class);
        routeID = rouId.getRouteId();
    }

    private ByteString getImage() throws ApiCaller.OTCException {
        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
        builder.setPage(1);
        builder.setSearchText(TestConstants.username);
        Community.SearchUsersResponse resp = ApiCaller.doCall(Endpoints.SEARCH_USERS, mUtils.getUserToken(), builder.build(), Community.SearchUsersResponse.class);
        long fileID = resp.getUsers(0).getImage();
        byte[] image = ApiCaller.getImage(Endpoints.FILE_GET + fileID, new String(mUtils.getUserToken()));
        return ByteString.copyFrom(image);
    }

    @Test
    public void MT001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTES;
        String testId = mUtils.getId();
        MyTrip.Routes.Builder builder = MyTrip.Routes.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setRouteType(General.RouteType.PLANNED);
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
    public void MT003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_FAV;
        String testId = mUtils.getId();
        MyTrip.RouteId.Builder builder = MyTrip.RouteId.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: ROUTE NOT FOUND
            builder.setRouteId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setRouteId(routeID);
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
    public void MT004() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_UNFAV;
        String testId = mUtils.getId();
        MyTrip.RouteId.Builder builder = MyTrip.RouteId.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: ROUTE NOT FOUND
            builder.setRouteId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setRouteId(routeID);
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
    public void MT005() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_STATUS;
        String testId = mUtils.getId();
        MyTrip.Status.Builder builder = MyTrip.Status.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: ROUTE NOT FOUND
            builder.setRouteId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setRouteId(routeID);
            builder.setStatus(MyTrip.RouteStatus.DELETED);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.SUCCESS);

            // Test 4: ROUTE ALREADY DELETED
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_ALREADY_DELETED);
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
    public void MT006() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_INFO;
        String testId = mUtils.getId();
        MyTrip.RouteInfo.Builder builder = MyTrip.RouteInfo.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.addPoints(General.Point.newBuilder().setLatitude(0).setLongitude(0).build());
            builder.addPoints(General.Point.newBuilder().setLatitude(1).setLongitude(0).build());
            builder.addPoints(General.Point.newBuilder().setLatitude(2).setLongitude(0).build());
            builder.addPoints(General.Point.newBuilder().setLatitude(3).setLongitude(0).build());
            builder.setPointStart("Center");
            builder.setPointEnd("Arbitrary");
            builder.setDuration(120);
            builder.setDistance(333000);
            builder.setFuelLevel(35);
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
    public void MT008() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_NEW;
        String testId = mUtils.getId();
        MyTrip.RouteNew.Builder builder = MyTrip.RouteNew.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID ROUTE TYPE
            builder.setType(General.RouteType.AUTOSAVED);
            builder.setDateStart("2019-01-01 00:00:00");
            builder.setDateEnd("2019-02-01 00:00:00");
            builder.setLocalDateStart("2019-01-01 01:00:00");
            builder.setTitle("Unit test");
            builder.setDescription("Test");
            builder.setDuration(44640);
            builder.setDistance(2E4);
            builder.setConsumption(952);
            builder.setAvgConsumption(21);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_ROUTE_TYPE);

            // Test 3: MALFORMED DATE
            builder.setType(General.RouteType.PLANNED);
            builder.setDateStart("qwerty");
            builder.setDateEnd("qwerty");
            builder.setLocalDateStart("azerty");
            builder.setTitle("Unit test");
            builder.setDescription("Test");
            builder.setDuration(44640);
            builder.setDistance(2E4);
            builder.setConsumption(952);
            builder.setAvgConsumption(21);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DATE);

            // Test 4: SUCCESS
            builder.setType(General.RouteType.PLANNED);
            builder.setDateStart("2019-01-01 00:00:00");
            builder.setDateEnd("2019-02-01 00:00:00");
            builder.setLocalDateStart("2019-01-01 01:00:00");
            builder.setTitle("Unit test");
            builder.setDescription("Test");
            builder.setDuration(44640);
            builder.setDistance(2E4);
            builder.setConsumption(952);
            builder.setAvgConsumption(21);
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
    public void MT009() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_UPDATE;
        String testId = mUtils.getId();
        MyTrip.RouteUpdate.Builder builder = MyTrip.RouteUpdate.newBuilder();
        createRoute();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: REQUIRED FIELDS MISSING
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.REQUIRED_FIELDS_MISSING);

            // Test 3: ROUTE NOT FOUND
            builder.setRouteId(-1);
            builder.setTitle("TEST !");
            builder.setDescription("A");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 4: SUCCESS
            builder.setRouteId(routeID);
            builder.setTitle("TEST !");
            builder.setDescription("A");
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
    public void MT013() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_POI_TYPES;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, null, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), null, Shared.OTCStatus.SUCCESS);
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
    public void MT015() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTES_COUNT_AUTOSAVED;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, null, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), null, Shared.OTCStatus.SUCCESS);
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
    public void MT017() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_ROUTES_FILTER;
        String testId = mUtils.getId();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, null, Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), null, Shared.OTCStatus.SUCCESS);
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
    public void MT019() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTES_FAVOURITE;
        String testId = mUtils.getId();
        MyTrip.RoutesFavourite.Builder builder = MyTrip.RoutesFavourite.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setCommunityDone(true).setCommunityPlanned(true).setFriendsDone(true).setFriendsPlanned(true).setMyDone(true).setMyPlanned(true);
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
    public void MT020() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_POIS;
        String testId = mUtils.getId();
        MyTrip.RouteId.Builder builder = MyTrip.RouteId.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: ROUTE NOT FOUND
            builder.setRouteId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setRouteId(routeID);
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
    public void MT021() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_ADD_POI;
        String testId = mUtils.getId();
        General.POI.Builder builder = General.POI.newBuilder();
        createRoute();
        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: ROUTE NOT FOUND
            builder.setRouId(-1);
            builder.setLatitude(1.0);
            builder.setLongitude(1.0);
            builder.setTitle("TEST");
            builder.setType(General.PoiType.GAS_STATION);

            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.ROUTE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setRouId(routeID);
            Shared.OTCResponse resp = mUtils.request(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.SUCCESS);
            pass = resp != null;

            MyTrip.PoiId poiId = resp.getData().unpack(MyTrip.PoiId.class);
            poiID = poiId.getPoiId();
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
    public void MT023() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_POI_ADD_IMAGE;
        String testId = mUtils.getId();
        MyTrip.PoiImage.Builder builder = MyTrip.PoiImage.newBuilder();
        ByteString img = getImage();
        createRoute();
        createPoi();
        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: POI NOT FOUND
            builder.setPoiId(-1);
            builder.setName("Test");
            builder.setData(img);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.POI_NOT_FOUND);

            // Test 3: INVALID_FILE
            builder.setPoiId(poiID);
            builder.setData(ByteString.copyFrom(new byte[]{1, 2, 3}));
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_FILE);

            // Test 4: SUCCESS
            builder.setData(img);
            builder.setName("img.jpg");
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
    public void MT024() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_POI_DELETE_IMAGE;
        String testId = mUtils.getId();
        MyTrip.PoiImage.Builder builder = MyTrip.PoiImage.newBuilder();
        long filId = findPoiWithImage();
        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: FILE NOT FOUND
            builder.setFilId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.FILE_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setFilId(filId);
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
    public void MT022() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ROUTE_DELETE_POI;
        String testId = mUtils.getId();
        General.POI.Builder builder = General.POI.newBuilder();
        routeID = findRouteWithPoi();
        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: POI NOT FOUND
            builder.setPoiId(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.POI_NOT_FOUND);

            // Test 3: SUCCESS
            builder.setPoiId(routeID);
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
}
