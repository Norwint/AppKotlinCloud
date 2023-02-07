package com.otcengineering.white_app.apis;

import androidx.test.runner.AndroidJUnit4;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.TestUtils;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MyDriveTests {
    private static String s_className = "MyDrive";
    private TestUtils mUtils = new TestUtils();

    @BeforeClass
    public static void before() throws Exception {

    }

    @AfterClass
    public static void after() throws Exception {

    }

    @Test
    public void MD001() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.SUMMARY;
        String testId = mUtils.getId();
        MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD002() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.USER_MILEAGE;
        String testId = mUtils.getId();
        MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: MALFORMED DATE
            builder.setDateStart("Time");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DATE);

            // Test 3: SUCCESS
            builder.setTypeTime(General.TimeType.WEEKLY);
            builder.setDateStart("2019-08-01");
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
    public void MD003() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.USER_ECO;
        String testId = mUtils.getId();
        MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: MALFORMED DATE
            builder.setDateStart("Time");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DATE);

            // Test 3: SUCCESS
            builder.setTypeTime(General.TimeType.WEEKLY);
            builder.setDateStart("2019-08-01");
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
    public void MD004() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.USER_SAFETY;
        String testId = mUtils.getId();
        MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: MALFORMED DATE
            builder.setDateStart("Time");
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.MALFORMED_DATE);

            // Test 3: SUCCESS
            builder.setTypeTime(General.TimeType.WEEKLY);
            builder.setDateStart("2019-08-01");
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
    public void MD005() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.TIPS;
        String testId = mUtils.getId();
        MyDrive.Tips.Builder builder = MyDrive.Tips.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setType(MyDrive.DriveType.MILEAGE);
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
    public void MD007() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.GET_USER_STATE;
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
    public void MD010() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.RANKING;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD011() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.MILEAGE;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD012() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ECO;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD013() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.SAFETY;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: INVALID PAGE NUMBER
            builder.setPage(-1);
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, mUtils.getUserToken(), builder.build(), Shared.OTCStatus.INVALID_PAGE_NUMBER);

            // Test 3: SUCCESS
            builder.setPage(1);
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD016() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.RANKING_POSITION;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD017() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.MILEAGE_POSITION;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD018() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.ECO_POSITION;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
    public void MD019() throws Exception {
        boolean pass = true;
        String endpoint = Endpoints.SAFETY_POSITION;
        String testId = mUtils.getId();
        MyDrive.Ranking.Builder builder = MyDrive.Ranking.newBuilder();

        try {
            mUtils.testBegin(s_className, testId, endpoint);

            // Test 1: INVALID AUTHORIZATION
            pass = mUtils.simplifiedRequest(s_className, testId, endpoint, builder.build(), Shared.OTCStatus.INVALID_AUTHORIZATION);

            // Test 2: SUCCESS
            builder.setTypeRanking(MyDrive.RankingType.GLOBAL);
            builder.setTypeTime(General.TimeType.WEEKLY);
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
