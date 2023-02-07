package com.otcengineering.white_app;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.protobuf.Message;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.network.Endpoints;

import org.junit.Assert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

public class TestUtils {
    final String LOGS_FOLDER = Environment.getExternalStorageDirectory() + "/test/unit-testing-logs/";
    private Date mStartTime = null;
    private Boolean mRequestIsRunning = false;

    public void testBegin(String testClass, String testId, String testEndpoint) {
        try {
            // Consola: Writes message
            Log.d("TestRunner", "\nUNIT-TEST " + testId + " - testing endpoint: " + testEndpoint);

            // LOG-File: Creates log folder
            Files.createDirectories(Paths.get(this.LOGS_FOLDER + '/' + testClass));

            // LOG-File: Removes old files
            File file = new File(getLogFileName(testClass, testId));
            file.delete();
            file = new File(getLogFileName_Failed(testClass, testId));
            file.delete();

            // LOG-File: Writes message
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), false));
            writer.write("CONNECTECH MOBILE\n");
            writer.write("API UNIT TESTING\n");
            writer.write("(c) OTC Enginnering 2019. All rights reserved\n\n");
            writer.write("API " + testId + "\n");
            writer.write(testEndpoint + "\n\n\n");
            writer.close();
            this.mStartTime = new Date();
        } catch (Exception ex) {
        }
    }

    public void testEnd(String testClass, String testId, Boolean testResult) {
        try {
            // Consola
            Log.d("TestRunner", ">> Result: " + getResult(testResult));
            // File
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), true));
            writer.write("\nStart time: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(this.mStartTime) + "\n");
            writer.write("Total time: " + ((new Date()).getTime() - this.mStartTime.getTime()) + "ms.\n\n");
            writer.append(">> Result: " + getResult(testResult));
            writer.close();
            // Renames files (if test fails)
            if (!testResult) {
                File fileSrc = new File(getLogFileName(testClass, testId));
                File fileDst = new File(getLogFileName_Failed(testClass, testId));
                fileSrc.renameTo(fileDst);
            }
        } catch (Exception ex) {
        }
    }

    public void testRequestStart(String testClass, String testId, String testDesc) {
        try {
            this.mRequestIsRunning = true;
            // Consola
            Log.d("TestRunner", ">> Testing " + testDesc + "... ");
            // File
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), true));
            writer.append("Checking requests with '" + testDesc + "'... ");
            writer.close();
        } catch (Exception ex) {
        }
    }

    public void testRequestFails(String testClass, String testId, String testResult) {
        try {
            this.mRequestIsRunning = false;

            // Consola
            Log.d("TestRunner", testResult);

            // File
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), true));
            writer.append("fails (returned " + testResult + ")\n");
            writer.close();
        } catch (Exception ex) {
        }
    }

    public void testRequestEnd(String testClass, String testId, Boolean testResult) {
        try {
            this.mRequestIsRunning = false;

            // Consola
            Log.d("TestRunner", getResult(testResult));

            // File
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), true));
            writer.append(getResult(testResult) + "\n");
            writer.close();
        } catch (Exception ex) {
        }
    }

    public void testInfo(String testClass, String testId, String testInfo) {
        try {
            this.mRequestIsRunning = false;

            // Consola
            Log.d("TestRunner", testInfo);

            // File
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFileName(testClass, testId), true));
            writer.append("\n" + testInfo + "\n");
            writer.close();
        } catch (Exception ex) {
        }
    }

    public Boolean testRequestIsRunning() {
        return this.mRequestIsRunning;
    }

    private String getResult(Boolean testResult) {
        return (testResult ? "OK" : "FAILS");
    }

    private String getLogFileName(String testClass, String testId) {
        return this.LOGS_FOLDER + testClass + "/" + testId + ".log";
    }

    private String getLogFileName_Failed(String testClass, String testId) {
        return this.LOGS_FOLDER + testClass + "/" + testId + "-FAILED.log";
    }

    public String getImei() {
        return "123456789012345";
    }

    public byte[] login(String username, String password, String imei) throws ApiCaller.OTCException {
        Welcome.Login login = Welcome.Login.newBuilder().setUsername(username).setPassword(password).setMobileIMEI(imei).build();
        Welcome.LoginResponse resp = ApiCaller.doCall(Endpoints.LOGIN, false, login, Welcome.LoginResponse.class);

        Assert.assertNotNull(resp);

        byte[] tok = resp.getApiToken().getBytes();

        deviceSpecs(imei, tok);

        return tok;
    }

    private byte[] userToken;
    public byte[] getUserToken() throws ApiCaller.OTCException {
        if (userToken == null) {
            userToken = login(TestConstants.testUsername, TestConstants.testPassword, TestConstants.testImei);
        }

        return Arrays.copyOf(userToken, userToken.length);
    }

    public void deviceSpecs(String imei, byte[] token) throws ApiCaller.OTCException {
        Welcome.DeviceSpecs specs = Welcome.DeviceSpecs.newBuilder().setAppVersion(BuildConfig.VERSION_NAME).setMobileIMEI(imei).setMobileSO("Android X").build();
        ApiCaller.doCall(Endpoints.DEVICE_SPECS, Arrays.copyOf(token, token.length), specs, Shared.OTCResponse.class);
    }

    public String getId() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte[] data = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     *
     * Zips a subfolder
     *
     */

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte[] data = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        return segments[segments.length - 1];
    }

    public boolean simplifiedRequest(String className, String testId, String endpoint, Message request, Shared.OTCStatus expected) throws Exception {
        return simplifiedRequest(className, testId, endpoint, null, request, expected);
    }

    private boolean testIsValid = true;
    public boolean simplifiedRequest(String className, String testId, String endpoint, byte[] token, Message request, Shared.OTCStatus expected) throws Exception {
        Shared.OTCResponse response = null;
        boolean result;
        try {
            testRequestStart(className, testId, expected.name().replace("_", " "));

            if (token == null) {
                response = ApiCaller.doCall(endpoint, request, Shared.OTCResponse.class);
            } else {
                response = ApiCaller.doCall(endpoint, token, request, Shared.OTCResponse.class);
            }
            assertEquals(expected, response.getStatus());

            testRequestEnd(className, testId, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (testRequestIsRunning()) {
                testRequestFails(className, testId, response.getStatus().name());
                result = false;
                testIsValid = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    public Shared.OTCResponse request(String className, String testId, String endpoint, byte[] token, Message request, Shared.OTCStatus expected) {
        Shared.OTCResponse response = null;
        try {
            testRequestStart(className, testId, expected.name().replace("_", " "));

            if (token == null) {
                response = ApiCaller.doCall(endpoint, request, Shared.OTCResponse.class);
            } else {
                response = ApiCaller.doCall(endpoint, token, request, Shared.OTCResponse.class);
            }
            assertEquals(expected, response.getStatus());

            testRequestEnd(className, testId, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (testRequestIsRunning()) {
            testRequestFails(className, testId, response.getStatus().name());
            return null;
        } else {
            return response;
        }
    }

    private String testClass, testId, testEndpoint;
    public void simplifiedTestStart(String testClass, String testId, String testEndpoint) {
        this.testClass = testClass;
        this.testId = testId;
        this.testEndpoint = testEndpoint;
        testBegin(testClass, testId, testEndpoint);
        testIsValid = true;
    }

    public void simplifiedTestEnd() throws Exception {
        simplifiedTestEnd(true);
    }

    public void simplifiedTestEnd(boolean pass) throws Exception {
        testEnd(testClass, testId, pass && testIsValid);
        testClass = null;
        testId = null;
        testEndpoint = null;
        if (!pass || !testIsValid) {
            throw new Exception();
        }
    }

    public boolean simplifiedRequest(Shared.OTCStatus expected) throws Exception {
        return simplifiedRequest(testClass, testId, testEndpoint, null, null, expected);
    }

    public boolean simplifiedRequest(boolean auth, Shared.OTCStatus expected) throws Exception {
        return simplifiedRequest(testClass, testId, testEndpoint, auth ? getUserToken() : null, null, expected);
    }

    public boolean simplifiedRequest(@NonNull Message.Builder builder, boolean auth, Shared.OTCStatus expected) throws Exception {
        return simplifiedRequest(builder.build(), auth, expected);
    }

    public boolean simplifiedRequest(Message request, boolean auth, Shared.OTCStatus expected) throws Exception {
        return simplifiedRequest(testClass, testId, testEndpoint, auth ? getUserToken() : null, request, expected);
    }

    public int randomValue(int max, int min) {
        SecureRandom secureRandom = new SecureRandom();
        double val = secureRandom.nextDouble();
        return (int)((val * max + min) - min);
    }

    public double randomValue(double max, double min) {
        SecureRandom secureRandom = new SecureRandom();
        double val = secureRandom.nextDouble();
        return (val * max + min) - min;
    }

    public long getPostID(boolean dealer) throws Exception {
        General.Page page = General.Page.newBuilder().setPage(1).build();

        if (dealer) {
            Community.DealerPosts posts = ApiCaller.doCall(Endpoints.DEALER_POSTS, getUserToken(), page, Community.DealerPosts.class);
            int index = randomValue(posts.getPostsCount() - 1, 0);
            return posts.getPosts(index).getId();
        } else {
            Community.ConnecTechPosts posts = ApiCaller.doCall(Endpoints.CONNECTECH_POSTS, getUserToken(), page, Community.ConnecTechPosts.class);
            int index = randomValue(posts.getPostsCount() - 1, 0);
            return posts.getPosts(index).getId();
        }
    }

    public boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    deleteFile(file);
                }
            }
        }
        return directoryToBeDeleted.delete();
    }

    public boolean deleteFile(File fileToDelete) {
        return fileToDelete.delete();
    }
}
