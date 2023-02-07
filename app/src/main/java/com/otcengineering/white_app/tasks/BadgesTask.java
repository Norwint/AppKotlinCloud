package com.otcengineering.white_app.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.otcengineering.white_app.activities.BadgesActivity;
import com.otcengineering.white_app.serialization.models.Badge;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.otc.alice.api.model.BadgeProto;
import com.otcengineering.white_app.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class BadgesTask {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public BadgesTask(Context context) {
        BadgesTask.context = context;
    }

    public static Context getContext() {
        return context;
    }

    public static class getVersionBadges extends AsyncTask<Object, Object, BadgeProto.Badges> {
        boolean updateBadges = false;

        @Override
        protected BadgeProto.Badges doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getContext());
                return ApiCaller.doCall(Endpoints.GET_BADGES_VERSION, msp.getBytes("token"), null, BadgeProto.Badges.class);
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BadgeProto.Badges badges) {
            super.onPostExecute(badges);

            MySharedPreferences bad = MySharedPreferences.createBadges(getContext());
            if (badges != null) {
                Utils.runOnBackground(() -> {
                    for (BadgeProto.Badges.Badge badge : badges.getBadgesList()) {
                        if (!bad.getString(badge.getName()).equals(badge.getVersion())) {
                            bad.remove(badge.getName());
                            bad.addBadgeVersion(badge);
                            updateBadges = true;
                        }
                    }
                });
            }

            (new getAllBadges()).execute();
        }
    }

    static class getAllBadges extends AsyncTask<Object, Object, BadgeProto.Badges> {
        @Override
        protected BadgeProto.Badges doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getContext());
                return ApiCaller.doCall(Endpoints.GET_BADGES, msp.getBytes("token"), null, BadgeProto.Badges.class);
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BadgeProto.Badges badges) {
            super.onPostExecute(badges);

            if (badges != null) {
                if (BadgesActivity.badgesListMileage == null) {
                    BadgesActivity.badgesListMileage = new ArrayList<>();
                }
                BadgesActivity.badgesListMileage.clear();

                if (BadgesActivity.badgesListEcoDriving == null) {
                    BadgesActivity.badgesListEcoDriving = new ArrayList<>();
                }
                BadgesActivity.badgesListEcoDriving.clear();

                if (BadgesActivity.badgesListLocalRank == null) {
                    BadgesActivity.badgesListLocalRank = new ArrayList<>();
                }
                BadgesActivity.badgesListLocalRank.clear();

                if (BadgesActivity.badgesListBehavior == null) {
                    BadgesActivity.badgesListBehavior = new ArrayList<>();
                }
                BadgesActivity.badgesListBehavior.clear();

                for (BadgeProto.Badges.Badge badge : badges.getBadgesList()) {
                    try {
                        switch (badge.getFamily().getNumber()) {
                            case BadgesActivity.TAB_MILEAGE:
                                BadgesActivity.badgesListMileage.add(new Badge(badge.getName(), badge.getDescription(), badge.getSubtitle(), badge.getName(), badge.getDate(), 0, badge.getType().getNumber(), 1));
                                break;
                            case BadgesActivity.TAB_ECO_DRIVING:
                                BadgesActivity.badgesListEcoDriving.add(new Badge(badge.getName(), badge.getDescription(), badge.getSubtitle(), badge.getName(), badge.getDate(), 0, badge.getType().getNumber(), 1));
                                break;
                            case BadgesActivity.TAB_LOCAL_RANK:
                                BadgesActivity.badgesListLocalRank.add(new Badge(badge.getName(), badge.getDescription(), badge.getSubtitle(), badge.getName(), badge.getDate(), 0, badge.getType().getNumber(), 1));
                                break;
                            case BadgesActivity.TAB_BEHAVIOR:
                                BadgesActivity.badgesListBehavior.add(new Badge(badge.getName(), badge.getDescription(), badge.getSubtitle(), badge.getName(), badge.getDate(), 0, badge.getType().getNumber(), 1));
                                break;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                //guardem les dades a shared preferences
                MySharedPreferences msp = MySharedPreferences.createBadges(getContext());
                Gson gson = new Gson();
                msp.addBadges("MILEAGE_LIST", gson.toJson(BadgesActivity.badgesListMileage));
                msp.addBadges("ECODRIVING_LIST", gson.toJson(BadgesActivity.badgesListEcoDriving));
                msp.addBadges("LOCALRANK_LIST", gson.toJson(BadgesActivity.badgesListLocalRank));
                msp.addBadges("BEHAVIOR_LIST", gson.toJson(BadgesActivity.badgesListBehavior));
            }
            (new getUserBadges()).execute();
        }
    }

    public static class getUserBadges extends AsyncTask<Object, Object, BadgeProto.Badges> {
        @Override
        protected BadgeProto.Badges doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getContext());
                return ApiCaller.doCall(Endpoints.GET_BADGES_USER, msp.getBytes("token"), null, BadgeProto.Badges.class);
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BadgeProto.Badges badges) {
            super.onPostExecute(badges);
            new Thread(() -> {
                try {
                    MySharedPreferences msp = MySharedPreferences.createBadges(getContext());
                    Gson gson = new Gson();
                    Type BadgeType = new TypeToken<ArrayList<Badge>>(){}.getType();

                    BadgesActivity.badgesListMileage = gson.fromJson(msp.getString("MILEAGE_LIST"), BadgeType);
                    BadgesActivity.badgesListEcoDriving = gson.fromJson(msp.getString("ECODRIVING_LIST"), BadgeType);
                    BadgesActivity.badgesListLocalRank = gson.fromJson(msp.getString("LOCALRANK_LIST"), BadgeType);
                    BadgesActivity.badgesListBehavior = gson.fromJson(msp.getString("BEHAVIOR_LIST"), BadgeType);

                    if (badges != null) {
                        msp.putInteger("TotalBadges", badges.getBadgesCount());
                        for (BadgeProto.Badges.Badge badge : badges.getBadgesList()) {
                            boolean trobat = false;
                            int i = 0;

                            switch (badge.getFamily().getNumber()) {
                                case BadgesActivity.TAB_MILEAGE:
                                    while (!trobat && BadgesActivity.badgesListMileage != null && i < BadgesActivity.badgesListMileage.size()) {
                                        if (badge.getName().equals(BadgesActivity.badgesListMileage.get(i).getName())) {
                                            BadgesActivity.badgesListMileage.get(i).setState(1);
                                            trobat = true;
                                        }
                                        i++;
                                    }
                                    break;
                                case BadgesActivity.TAB_ECO_DRIVING:
                                    while (!trobat && BadgesActivity.badgesListEcoDriving != null && i < BadgesActivity.badgesListEcoDriving.size()) {
                                        if (badge.getName().equals(BadgesActivity.badgesListEcoDriving.get(i).getName())) {
                                            BadgesActivity.badgesListEcoDriving.get(i).setState(1);
                                            trobat = true;
                                        }
                                        i++;
                                    }
                                    break;
                                case BadgesActivity.TAB_LOCAL_RANK:
                                    while (!trobat && BadgesActivity.badgesListLocalRank != null && i < BadgesActivity.badgesListLocalRank.size()) {
                                        if (badge.getName().equals(BadgesActivity.badgesListLocalRank.get(i).getName())) {
                                            BadgesActivity.badgesListLocalRank.get(i).setState(1);
                                            trobat = true;
                                        }
                                        i++;
                                    }
                                    break;
                                case BadgesActivity.TAB_BEHAVIOR:
                                    while (!trobat && BadgesActivity.badgesListBehavior != null && i < BadgesActivity.badgesListBehavior.size()) {
                                        if (badge.getName().equals(BadgesActivity.badgesListBehavior.get(i).getName())) {
                                            BadgesActivity.badgesListBehavior.get(i).setState(1);
                                            trobat = true;
                                        }
                                        i++;
                                    }
                                    break;
                            }
                        }
                    }
                } catch (JsonSyntaxException exc) {
                    exc.printStackTrace();
                }
            }, "BadgesThread").start();
        }
    }
}
