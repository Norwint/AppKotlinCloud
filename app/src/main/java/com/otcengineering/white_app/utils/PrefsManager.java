package com.otcengineering.white_app.utils;

import android.content.Context;
import android.location.Location;

import androidx.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otcengineering.white_app.interfaces.AsyncResponse;
import com.otcengineering.white_app.serialization.pojo.RouteItem;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static com.otcengineering.white_app.utils.Constants.Prefs.DB_AUTO_SAVE_ROUTES;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_DONE_ROUTES;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_FAVORITES_ROUTES;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_MY_DRIVE;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_PLANNED_ROUTES;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_POSTS_FRIENDS;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_POSTS_GENERAL;
import static com.otcengineering.white_app.utils.Constants.Prefs.HAS_NEW_CONTENT;
import static com.otcengineering.white_app.utils.Constants.Prefs.LAST_LOCATION;
import static com.otcengineering.white_app.utils.Constants.Prefs.MANUAL_VERSION;
import static com.otcengineering.white_app.utils.Constants.Prefs.ROUTE_IN_PROGRESS;

/**
 * Created by cenci7
 */

public class PrefsManager {

    private static PrefsManager instance;

    private PrefsManager() {

    }

    public static PrefsManager getInstance() {
        if (instance == null) {
            instance = new PrefsManager();
        }
        return instance;
    }

    public byte[] getToken(final Context ctx) {
        return MySharedPreferences.createLogin(ctx).getBytes("token");
    }

    public void getMyUserIDAsync(final Context ctx, AsyncResponse<Long> onResponse) {
        MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
        if (msp.contains("ID")) {
            Utils.runOnMainThread(() -> onResponse.onResponse(msp.getLong("ID")));
        } else {
            Utils.runOnBackThread(() -> {
                if (NetworkManager.login(ctx, null, null)) {
                    Utils.runOnMainThread(() -> onResponse.onResponse(msp.getLong("ID")));
                } else {
                    Utils.runOnMainThread(onResponse::onFailure);
                }
            });
        }
    }

    private static Long usrID;

    public long getMyUserId(final Context ctx) {
        if (usrID == null) {
            MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
            if (msp.contains("ID")) {
                usrID = msp.getLong("ID");
                return usrID;
            }
            return -1;
        }
        return usrID;
    }

    public void setHasNewContent(boolean hasNewContent, final Context ctx) {
        MySharedPreferences.createLogin(ctx).putBoolean(HAS_NEW_CONTENT, hasNewContent);
    }

    public boolean getHasNewContent(final Context ctx) {
        return MySharedPreferences.createLogin(ctx).getBoolean(HAS_NEW_CONTENT);
    }

    public void saveLastLocation(Location location, final Context ctx) {
        String locationJson = Utils.getGson().toJson(location, Location.class);
        MySharedPreferences.createLogin(ctx).putString(LAST_LOCATION, locationJson);
    }

    public Location getLastLocation(final Context ctx) {
        String locationJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.LAST_LOCATION);
        return Utils.getGson().fromJson(locationJson, Location.class);
    }

    public void saveRouteInProgress(RouteItem routeItem, final Context ctx) {
        try {
            String routeJson = Utils.getGson().toJson(routeItem, RouteItem.class);
            MySharedPreferences.createLogin(ctx).putString(ROUTE_IN_PROGRESS, routeJson);
        } catch (Exception e) {
            //Log.e("PrefsManager", "Exception", e);
        }
    }

    public void deleteRouteInProgress(final Context ctx) {
        MySharedPreferences.createLogin(ctx).remove(ROUTE_IN_PROGRESS);
    }

    public RouteItem getRouteInProgress(final Context ctx) {
        String routeJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.ROUTE_IN_PROGRESS);
        try {
            return Utils.getGson().fromJson(routeJson, RouteItem.class);
        } catch (Exception e) {
            //Log.e("GetRouteInProgress", "Exception", e);
            return null;
        }
    }

    public boolean getSettingValue(String prefName, final Context ctx) {
        return MySharedPreferences.createLogin(ctx).getBoolean(prefName);
    }

    public void saveSettingValue(String prefName, boolean value, final Context ctx) {
        MySharedPreferences.createLogin(ctx).putBoolean(prefName, value);
    }

    public int getManualVersion(final Context ctx) {
        return MySharedPreferences.createLogin(ctx).getInteger(Constants.Prefs.MANUAL_VERSION);
    }

    public void saveManualVersion(int manualVersion, final Context ctx) {
        MySharedPreferences.createLogin(ctx).putInteger(MANUAL_VERSION, manualVersion);
    }

    public void saveMyDriveInfo(MyDrive.SummaryResponse summaryResponse, final Context ctx) {
        try {
            String myDriveInfoJson = Utils.getGson().toJson(summaryResponse, MyDrive.SummaryResponse.class);
            MySharedPreferences.createLogin(ctx).putString(DB_MY_DRIVE, myDriveInfoJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MyDrive.SummaryResponse getMyDriveInfo(final Context ctx) {
        String myDriveInfoJson = MySharedPreferences.createLogin(ctx).getString(DB_MY_DRIVE);
        return Utils.getGson().fromJson(myDriveInfoJson, MyDrive.SummaryResponse.class);
    }

    public void saveMileage(MyDrive.UserMileageResponse mileageResponse, General.TimeType timeType, final Context ctx) {
        String mileageInfoJson = Utils.getGson().toJson(mileageResponse, MyDrive.UserMileageResponse.class);
        MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
        if (General.TimeType.DAILY == timeType) {
            msp.putString(Constants.Prefs.DB_MILEAGE_DAILY, mileageInfoJson);
        } else if (General.TimeType.WEEKLY == timeType) {
            msp.putString(Constants.Prefs.DB_MILEAGE_WEEKLY, mileageInfoJson);
        } else if (General.TimeType.MONTHLY == timeType) {
            msp.putString(Constants.Prefs.DB_MILEAGE_MONTHLY, mileageInfoJson);
        }
    }

    public MyDrive.UserMileageResponse getMileage(General.TimeType timeType, final Context ctx) {
        String mileageInfoJson = null;
        if (General.TimeType.DAILY == timeType) {
            mileageInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_MILEAGE_DAILY);
        } else if (General.TimeType.WEEKLY == timeType) {
            mileageInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_MILEAGE_WEEKLY);
        } else if (General.TimeType.MONTHLY == timeType) {
            mileageInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_MILEAGE_MONTHLY);
        }
        return Utils.getGson().fromJson(mileageInfoJson, MyDrive.UserMileageResponse.class);
    }

    public void saveEco(MyDrive.UserEcoResponse ecoResponse, General.TimeType timeType, final Context ctx) {
        try {
            String ecoInfoJson = Utils.getGson().toJson(ecoResponse, MyDrive.UserEcoResponse.class);
            MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
            if (General.TimeType.DAILY == timeType) {
                msp.putString(Constants.Prefs.DB_ECO_DAILY, ecoInfoJson);
            } else if (General.TimeType.WEEKLY == timeType) {
                msp.putString(Constants.Prefs.DB_ECO_WEEKLY, ecoInfoJson);
            } else if (General.TimeType.MONTHLY == timeType) {
                msp.putString(Constants.Prefs.DB_ECO_MONTHLY, ecoInfoJson);
            }
        } catch (Exception jse) {
            jse.printStackTrace();
        }
    }

    public MyDrive.UserEcoResponse getEco(General.TimeType timeType, final Context ctx) {
        String ecoInfoJson = null;
        if (General.TimeType.DAILY == timeType) {
            ecoInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_ECO_DAILY);
        } else if (General.TimeType.WEEKLY == timeType) {
            ecoInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_ECO_WEEKLY);
        } else if (General.TimeType.MONTHLY == timeType) {
            ecoInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_ECO_MONTHLY);
        }
        return Utils.getGson().fromJson(ecoInfoJson, MyDrive.UserEcoResponse.class);
    }

    public void saveSafety(MyDrive.UserSafetyResponse safetyResponse, General.TimeType timeType, final Context ctx) {
        String safetyInfoJson = Utils.getGson().toJson(safetyResponse, MyDrive.UserSafetyResponse.class);
        MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
        if (General.TimeType.DAILY == timeType) {
            msp.putString(Constants.Prefs.DB_SAFETY_DAILY, safetyInfoJson);
        } else if (General.TimeType.WEEKLY == timeType) {
            msp.putString(Constants.Prefs.DB_SAFETY_WEEKLY, safetyInfoJson);
        } else if (General.TimeType.MONTHLY == timeType) {
            msp.putString(Constants.Prefs.DB_SAFETY_MONTHLY, safetyInfoJson);
        }
    }

    public MyDrive.UserSafetyResponse getSafety(General.TimeType timeType, final Context ctx) {
        String safetyInfoJson = null;
        if (General.TimeType.DAILY == timeType) {
            safetyInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_SAFETY_DAILY);
        } else if (General.TimeType.WEEKLY == timeType) {
            safetyInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_SAFETY_WEEKLY);
        } else if (General.TimeType.MONTHLY == timeType) {
            safetyInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_SAFETY_MONTHLY);
        }
        return Utils.getGson().fromJson(safetyInfoJson, MyDrive.UserSafetyResponse.class);
    }

    public General.VehicleStatus getVehicleStatus(final Context ctx) {
        String vehicleStatusJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_VEHICLE_STATUS);
        return Utils.getGson().fromJson(vehicleStatusJson, General.VehicleStatus.class);
    }

    public Community.SearchUsersResponse getAllUsers(final Context ctx) {
        String allUsersJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_ALL_USERS);
        return Utils.getGson().fromJson(allUsersJson, Community.SearchUsersResponse.class);
    }

    public void saveNearUsers(Community.SearchUsersResponse friendsResponse, final Context ctx) {
        String nearUsersJson = Utils.getGson().toJson(friendsResponse, Community.SearchUsersResponse.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_NEAR_USERS, nearUsersJson);
    }

    public Community.SearchUsersResponse getNearUsers(final Context context) {
        String nearUsersJson = MySharedPreferences.createLogin(context).getString(Constants.Prefs.DB_NEAR_USERS);
        return Utils.getGson().fromJson(nearUsersJson, Community.SearchUsersResponse.class);
    }

    public void saveFriends(Community.FriendsResponse friendsResponse, final Context ctx) {
        String friendsJson = Utils.getGson().toJson(friendsResponse, Community.FriendsResponse.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_FRIENDS, friendsJson);
    }

    public Community.FriendsResponse getFriends(final Context ctx) {
        String friendsJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_FRIENDS);
        return Utils.getGson().fromJson(friendsJson, Community.FriendsResponse.class);
    }

    public Community.UserPostsResponse getPostsGeneral(final Context ctx) {
        String generalPostsJson = MySharedPreferences.createLogin(ctx).getString(DB_POSTS_GENERAL);
        return Utils.getGson().fromJson(generalPostsJson, Community.UserPostsResponse.class);
    }

    public void savePostsFriends(Community.UserPostsResponse generalPostsResponse, final Context ctx) {
        String friendsPostsJson = Utils.getGson().toJson(generalPostsResponse, Community.UserPostsResponse.class);
        MySharedPreferences.createLogin(ctx).putString(DB_POSTS_FRIENDS, friendsPostsJson);
    }

    public Community.UserPostsResponse getPostsFriends(final Context ctx) {
        String friendsPostsJson = MySharedPreferences.createLogin(ctx).getString(DB_POSTS_FRIENDS);
        return Utils.getGson().fromJson(friendsPostsJson, Community.UserPostsResponse.class);
    }

    public void saveInvitations(Community.FriendRequests generalPostsResponse, final Context ctx) {
        String invitationsJson = Utils.getGson().toJson(generalPostsResponse, Community.FriendRequests.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_INVITATIONS, invitationsJson);
    }

    public Community.FriendRequests getInvitations(final Context ctx) {
        String invitationsJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_INVITATIONS);
        return Utils.getGson().fromJson(invitationsJson, Community.FriendRequests.class);
    }

    public void saveDatsunPosts(Community.ConnecTechPosts generalPostsResponse, final Context ctx) {
        String connectechPostsJson = Utils.getGson().toJson(generalPostsResponse, Community.ConnecTechPosts.class);
        MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
        msp.putString(Constants.Prefs.DB_DATSUN_POSTS, connectechPostsJson);
    }

    public Community.ConnecTechPosts getDatsunPosts(final Context ctx) {
        String connectechPostsJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_DATSUN_POSTS);
        return Utils.getGson().fromJson(connectechPostsJson, Community.ConnecTechPosts.class);
    }

    public void saveDatsunMessages(Community.ConnecTechPosts generalPostsResponse, final Context ctx) {
        String connectechMessagesJson = Utils.getGson().toJson(generalPostsResponse, Community.ConnecTechPosts.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_DATSUN_MESSAGES, connectechMessagesJson);
    }

    public Community.ConnecTechPosts getDatsunMessages(final Context ctx) {
        String connectechMessagesJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_DATSUN_MESSAGES);
        return Utils.getGson().fromJson(connectechMessagesJson, Community.ConnecTechPosts.class);
    }

    public void saveDealerPosts(Community.DealerPosts generalPostsResponse, final Context ctx) {
        String dealerPostJson = Utils.getGson().toJson(generalPostsResponse, Community.DealerPosts.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_DEALER_POSTS, dealerPostJson);
    }

    public Community.DealerPosts getDealerPosts(final Context ctx) {
        String dealerPostJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_DEALER_POSTS);
        return Utils.getGson().fromJson(dealerPostJson, Community.DealerPosts.class);
    }

    public void saveDealerMessages(Community.DealerPosts generalPostsResponse, final Context ctx) {
        String dealerMessagesJson = Utils.getGson().toJson(generalPostsResponse, Community.DealerPosts.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_DEALER_MESSAGES, dealerMessagesJson);
    }

    public Community.DealerPosts getDealerMessages(final Context ctx) {
        String dealerMessagesJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_DEALER_MESSAGES);
        return Utils.getGson().fromJson(dealerMessagesJson, Community.DealerPosts.class);
    }

    public void saveDealerInfo(Community.DealerResponse dealerInfo, final Context ctx) {
        String dealerInfoJson = Utils.getGson().toJson(dealerInfo, Community.DealerResponse.class);
        MySharedPreferences.createLogin(ctx).putString(Constants.Prefs.DB_DEALER_INFO, dealerInfoJson);
    }

    public Community.DealerResponse getDealerInfo(final Context ctx) {
        String dealerInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_DEALER_INFO);
        return Utils.getGson().fromJson(dealerInfoJson, Community.DealerResponse.class);
    }

    public ProfileAndSettings.UserDataResponse getMyProfileInfo(final Context ctx) {
        String myProfileInfoJson = MySharedPreferences.createLogin(ctx).getString(Constants.Prefs.DB_MY_PROFILE);
        return Utils.getGson().fromJson(myProfileInfoJson, ProfileAndSettings.UserDataResponse.class);
    }

    public void savePlannedRoutes(List<RouteItem> routeItem, final Context ctx) {
        try {
            Type listType = new TypeToken<List<RouteItem>>(){}.getType();
            String routesJson = Utils.getGson().toJson(routeItem, listType);
            MySharedPreferences.createRoutes(ctx).putRaw(DB_PLANNED_ROUTES, routesJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public @Nullable List<RouteItem> getPlannedRoutes(final Context ctx) {
        byte[] routesJson = MySharedPreferences.createRoutes(ctx).getRaw(DB_PLANNED_ROUTES);
        if (routesJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<RouteItem>>() {}.getType();
        try {
            return Utils.getGson().fromJson(new String(routesJson), listType);
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    public void saveAutosaveRoutes(List<RouteItem> routeItems, final Context ctx) {
        try {
            Type listType = new TypeToken<List<RouteItem>>(){}.getType();
            String routesJson = Utils.getGson().toJson(routeItems, listType);
            MySharedPreferences.createRoutes(ctx).putRaw(DB_AUTO_SAVE_ROUTES, routesJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RouteItem> getAutosavedRoutes(Context context) {
        byte[] routesJson = MySharedPreferences.createRoutes(context).getRaw(DB_AUTO_SAVE_ROUTES);
        if (routesJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<RouteItem>>(){}.getType();
        try {
            return Utils.getGson().fromJson(new String(routesJson), listType);
        } catch (JsonSyntaxException ex) {
            return Collections.emptyList();
        }
    }

    public void saveDoneRoutes(List<RouteItem> routeItem, final Context ctx) {
        try {
            Type listType = new TypeToken<List<RouteItem>>(){}.getType();
            String routesJson = Utils.getGson().toJson(routeItem, listType);
            MySharedPreferences.createRoutes(ctx).putRaw(DB_DONE_ROUTES, routesJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RouteItem> getDoneRoutes(final Context ctx) {
        byte[] routesJson = MySharedPreferences.createRoutes(ctx).getRaw(DB_DONE_ROUTES);
        if (routesJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<RouteItem>>() {}.getType();
        try {
            return Utils.getGson().fromJson(new String(routesJson), listType);
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    // static because it's stored in default prefs
    public static void saveMyPushToken(Context context, String pushToken) {
        MySharedPreferences.createDefault(context).putString(Constants.Prefs.PUSH_TOKEN, pushToken);
    }

    public static String getMyPushToken(Context context) {
        return MySharedPreferences.createDefault(context).getString(Constants.Prefs.PUSH_TOKEN);
    }

    public void saveFavoritesRoutes(List<RouteItem> routeItem, final Context ctx) {
        try {
            Type listType = new TypeToken<List<RouteItem>>() {}.getType();
            String routesJson = Utils.getGson().toJson(routeItem, listType);
            MySharedPreferences.createRoutes(ctx).putRaw(DB_FAVORITES_ROUTES, routesJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RouteItem> getFavoritesRoutes(final Context ctx) {
        byte[] routesJson = MySharedPreferences.createRoutes(ctx).getRaw(Constants.Prefs.DB_FAVORITES_ROUTES);
        if (routesJson == null) {
            return null;
        }
        Type listType = new TypeToken<List<RouteItem>>() {}.getType();
        try {
            return Utils.getGson().fromJson(new String(routesJson), listType);
        } catch (JsonSyntaxException e) {
            return Collections.emptyList();
        }
    }

    public static String getLanguage(final Context ctx){
        return MySharedPreferences.createLanguage(ctx).getString("Idioma");
    }
}
