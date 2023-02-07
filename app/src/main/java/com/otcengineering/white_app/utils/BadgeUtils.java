package com.otcengineering.white_app.utils;


import com.otcengineering.white_app.R;

public class BadgeUtils {

    public static int getImageId(String name) {
        switch (name) {
            case "NEWCOMER":
                return R.drawable.badges_31;
            case "ROOKIE":
                return R.drawable.badges_30;
            case "BEGINNER":
                return R.drawable.badges_29;
            case "STANDARD":
                return R.drawable.badges_28;
            case "INTERMEDIATE":
                return R.drawable.badges_27;
            case "PASSIONATE":
                return R.drawable.badges_26;
            case "SKILLED":
                return R.drawable.badges_3;
            case "ADVANCED":
                return R.drawable.badges_2;
            case "PROFESSIONAL":
                return R.drawable.badges_1;
            case "ECO NOVICE":
                return R.drawable.badges_25;
            case "ECO ROOKIE":
                return R.drawable.badges_24;
            case "ECO TALENTED":
                return R.drawable.badges_23;
            case "ECO SKILLFUL":
                return R.drawable.badges_22;
            case "ECO EXPERT":
                return R.drawable.badges_4;
            case "PROFESSIONAL DRIVER OF THE MONTH":
                return R.drawable.badges_21;
            case "SKILLFUL DRIVER OF THE MONTH":
                return R.drawable.badges_20;
            case "ADVANCED DRIVER OF THE MONTH":
                return R.drawable.badges_19;
            case "TALENTED DRIVER OF THE MONTH":
                return R.drawable.badges_18;
            case "BRILLIANT DRIVER":
                return R.drawable.badges_8;
            case "EXPERT DRIVER":
                return R.drawable.badges_9;
            case "MASTER DRIVER":
                return R.drawable.badges_7;
            case "ULTIMATE DRIVER":
                return R.drawable.badges_6;
            case "THE SMART":
                return R.drawable.badges_17;
            case "THE CONTRIBUTOR":
                return R.drawable.badges_16;
            case "THE HELPFUL":
                return R.drawable.badges_13;
            case "THE SOCIABLE":
                return R.drawable.the_friendly;
            case "THE ACTIVE":
                return R.drawable.badges_11;
            case "THE CONTENT CREATOR":
                return R.drawable.badges_10;
            default:
                return R.drawable.badges_2;
        }
    }

    public static int getNotificationImageId(String name) {
        switch (name) {
            case "NEWCOMER":
                return R.drawable.notification_history37;
            case "ROOKIE":
                return R.drawable.notification_history36;
            case "BEGINNER":
                return R.drawable.notification_history39;
            case "STANDARD":
                return R.drawable.notification_history38;
            case "INTERMEDIATE":
                return R.drawable.notification_history40;
            case "PASSIONATE":
                return R.drawable.notification_history41;
            case "SKILLED":
                return R.drawable.notification_history10;
            case "ADVANCED":
                return R.drawable.notification_history11;
            case "PROFESSIONAL":
                return R.drawable.notification_history12;
            case "ECO NOVICE":
                return R.drawable.notification_history14;
            case "ECO ROOKIE":
                return R.drawable.notification_history15;
            case "ECO TALENTED":
                return R.drawable.notification_history16;
            case "ECO SKILLFUL":
                return R.drawable.notification_history17;
            case "ECO EXPERT":
                return R.drawable.notification_history19;
            case "PROFESSIONAL DRIVER OF THE MONTH":
                return R.drawable.notification_history20;
            case "SKILLFUL DRIVER OF THE MONTH":
                return R.drawable.notification_history21;
            case "ADVANCED DRIVER OF THE MONTH":
                return R.drawable.notification_history22;
            case "TALENTED DRIVER OF THE MONTH":
                return R.drawable.notification_history24;
            case "BRILLIANT DRIVER":
                return R.drawable.notification_history27;
            case "EXPERT DRIVER":
                return R.drawable.notification_history26;
            case "MASTER DRIVER":
                return R.drawable.notification_history28;
            case "ULTIMATE DRIVER":
                return R.drawable.notification_history29;
            case "THE SMART":
                return R.drawable.notification_history30;
            case "THE CONTRIBUTOR":
                return R.drawable.notification_history31;
            case "THE HELPFUL":
                return R.drawable.notification_history32;
            case "THE SOCIABLE":
                return R.drawable.the_friendly;
            case "THE ACTIVE":
                return R.drawable.notification_history34;
            case "THE CONTENT CREATOR":
                return R.drawable.notification_history35;
            default:
                return R.drawable.badges_2;
        }
    }
}
