package com.otcengineering.white_app.utils;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;

/**
 * Created by cenci7
 */

public class Constants {
    public static final int ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY = 420;
    public static final int MAX_CHARS = 140;
    public static final double CAR_CONSUMPTION_BEST = 10.5D;
    public static final int CAR_MILEAGE_BEST = 50000;

    public class Prefs {
        public static final String HAS_NEW_CONTENT = "HAS_NEW_CONTENT";

        public static final String LAST_LOCATION = "LAST_LOCATION";

        public static final String ROUTE_IN_PROGRESS = "ROUTE_IN_PROGRESS";

        public static final String SETTINGS_REWARDS = "SETTINGS_REWARDS";
        public static final String SETTINGS_RANKINGS = "SETTINGS_RANKINGS";
        public static final String SETTINGS_RECENT_TRIP = "SETTINGS_RECENT_TRIP";
        public static final String SETTINGS_AUTOUPDATE = "SETTINGS_AUTOUPDATE";
        public static final String SETTINGS_PRIVACY_ALL = "SETTINGS_PRIVACY_ALL";
        public static final String SETTINGS_PRIVACY_FRIENDS = "SETTINGS_PRIVACY_FRIENDS";
        public static final String SETTINGS_PRIVACY_NONE = "SETTINGS_PRIVACY_NONE";
        public static final String SETTINGS_PUSH_INVITATIONS = "SETTINGS_PUSH_INVITATIONS";
        public static final String SETTINGS_PUSH_ROUTES = "SETTINGS_PUSH_ROUTES";
        public static final String SETTINGS_PUSH_MILEAGE = "SETTINGS_PUSH_MILEAGE";
        public static final String SETTINGS_PUSH_ECO = "SETTINGS_PUSH_ECO";
        public static final String SETTINGS_PUSH_SAFETY = "SETTINGS_PUSH_SAFETY";
        public static final String SETTINGS_PUSH_USER_POST = "SETTINGS_PUSH_USER_POST";
        public static final String SETTINGS_PUSH_DATSUN_POST = "SETTINGS_PUSH_DATSUN_POST";
        public static final String SETTINGS_PUSH_DATSUN_MESSAGE = "SETTINGS_PUSH_DATSUN_MESSAGE";
        public static final String SETTINGS_PUSH_DEALER_POST = "SETTINGS_PUSH_DEALER_POST";
        public static final String SETTINGS_PUSH_DEALER_MESSAGE = "SETTINGS_PUSH_DEALER_MESSAGE";
        public static final String SETTINGS_LANGUAGE = "SETTINGS_LANGUAGE";

        public static final String VEHICLE_CONDICION_DESCRIPTION = "VEHICLE_CONDITION_DESCRIPTION";

        public static final String MANUAL_VERSION = "MANUAL_VERSION";

        public static final String PUSH_TOKEN = "PUSH_TOKEN";

        public static final String DB_MY_DRIVE = "DB_MY_DRIVE";
        public static final String DB_MILEAGE_DAILY = "DB_MILEAGE_DAILY";
        public static final String DB_MILEAGE_WEEKLY = "DB_MILEAGE_WEEKLY";
        public static final String DB_MILEAGE_MONTHLY = "DB_MILEAGE_MONTHLY";
        public static final String DB_ECO_DAILY = "DB_ECO_DAILY";
        public static final String DB_ECO_WEEKLY = "DB_ECO_WEEKLY";
        public static final String DB_ECO_MONTHLY = "DB_ECO_MONTHLY";
        public static final String DB_SAFETY_DAILY = "DB_SAFETY_DAILY";
        public static final String DB_SAFETY_WEEKLY = "DB_SAFETY_WEEKLY";
        public static final String DB_SAFETY_MONTHLY = "DB_SAFETY_MONTHLY";
        public static final String DB_VEHICLE_STATUS = "DB_VEHICLE_STATUS";
        public static final String DB_ALL_USERS = "DB_ALL_USERS";
        public static final String DB_NEAR_USERS = "DB_NEAR_USERS";
        public static final String DB_FRIENDS = "DB_FRIENDS";
        public static final String DB_POSTS_GENERAL = "DB_POSTS_GENERAL";
        public static final String DB_POSTS_FRIENDS = "DB_POSTS_FRIENDS";
        public static final String DB_INVITATIONS = "DB_INVITATIONS";
        public static final String DB_DATSUN_POSTS = "DB_DATSUN_POSTS";
        public static final String DB_DATSUN_MESSAGES = "DB_DATSUN_MESSAGES";
        public static final String DB_DEALER_POSTS = "DB_DEALER_POSTS";
        public static final String DB_DEALER_MESSAGES = "DB_DEALER_MESSAGES";
        public static final String DB_DEALER_INFO = "DB_DEALER_INFO";
        public static final String DB_MY_PROFILE = "DB_MY_PROFILE";
        public static final String DB_PLANNED_ROUTES = "DB_PLANNED_ROUTES";
        public static final String DB_DONE_ROUTES = "DB_DONE_ROUTES";
        public static final String DB_AUTO_SAVE_ROUTES = "DB_AUTO_SAVE_ROUTES";

        public static final String DB_FAVORITES_ROUTES = "DB_FAVORITES_ROUTES";
    }

    public class Extras {
        public static final String CHART_MODE = "CHART_MODE";
        public static final String MILEAGE_ITEM = "MILEAGE_ITEM";
        public static final String ECO_ITEM = "ECO_ITEM";
        public static final String SAFETY_ITEM = "SAFETY_ITEM";
        public static final String RANKING_MODE = "RANKING_MODE";
        public static final String RANKING_TYPE = "RANKING_TYPE";
        public static final String TIME_TYPE = "TIME_TYPE";
        public static final String ROUTE = "ROUTE";
        public static final String ROUTE_POLYLINE = "ROUTE_POLYLINE";
        public static final String POI_LOCATION = "POI_LOCATION";
        public static final String POI = "POI";
        public static final String POI_TYPE = "POI_TYPE";
        public static final String USER = "USER";
        public static final String LOCATION = "LOCATION";
        public static final String SHARE_TO_FRIENDS = "SHARE_TO_FRIENDS";
        public static final String QRG = "QRG";
        public static final String IMAGE = "IMAGE";
        public static final String PDF = "PDF";
        public static final String PDF_FORCE_DOWNLOAD = "PDF_FORCE_DOWNLOAD";
        public static final String VIDEO_URL = "VIDEO_URL";
    }

    public class Posts {
        public static final int GENERAL = 0;
        public static final int FRIENDS = 1;
        public static final int INVITATIONS = 2;
    }

    public class DatsunDealer {
        public static final int GENERAL = 0;
        public static final int MESSAGES_TO_ME = 1;
    }

    public class ChartMode {
        public static final int MILEAGE = 1;
        public static final int ECO = 2;
        public static final int SAFETY = 3;
    }

    public class RankingMode {
        public static final int BEST = 0;
        public static final int MILEAGE = 1;
        public static final int ECO = 2;
        public static final int SAFETY = 3;
    }

    public class BadgesMode {
        public static final int VIEW_ALL = 0;
        public static final int YOU_HAVE = 1;
    }

    public static class RankingType {
        public static final int GLOBAL = fromRankingTypeToInt(MyDrive.RankingType.GLOBAL);
        public static final int LOCAL = fromRankingTypeToInt(MyDrive.RankingType.LOCAL);

        private static int fromRankingTypeToInt(MyDrive.RankingType rankingType) {
            if (rankingType == MyDrive.RankingType.GLOBAL) {
                return MyDrive.RankingType.GLOBAL.getNumber();
            } else if (rankingType == MyDrive.RankingType.LOCAL) {
                return MyDrive.RankingType.LOCAL.getNumber();
            }
            return -1;
        }

        public static MyDrive.RankingType fromIntToRankingType(int value) {
            if (value == MyDrive.RankingType.GLOBAL.getNumber()) {
                return MyDrive.RankingType.GLOBAL;
            } else if (value == MyDrive.RankingType.LOCAL.getNumber()) {
                return MyDrive.RankingType.LOCAL;
            }
            return null;
        }
    }

    public static class TimeType {
        public static final int DAILY = fromTimeTypeToInt(General.TimeType.DAILY);
        public static final int WEEKLY = fromTimeTypeToInt(General.TimeType.WEEKLY);
        public static final int MONTHLY = fromTimeTypeToInt(General.TimeType.MONTHLY);

        private static int fromTimeTypeToInt(General.TimeType timeType) {
            if (timeType == General.TimeType.DAILY) {
                return General.TimeType.DAILY.getNumber();
            } else if (timeType == General.TimeType.WEEKLY) {
                return General.TimeType.WEEKLY.getNumber();
            } else if (timeType == General.TimeType.MONTHLY) {
                return General.TimeType.MONTHLY.getNumber();
            }
            return -1;
        }

        public static General.TimeType fromIntToTimeType(int value) {
            if (value == General.TimeType.DAILY.getNumber()) {
                return General.TimeType.DAILY;
            } else if (value == General.TimeType.WEEKLY.getNumber()) {
                return General.TimeType.WEEKLY;
            } else if (value == General.TimeType.MONTHLY.getNumber()) {
                return General.TimeType.MONTHLY;
            }
            return null;
        }
    }

}

/*
RELEASE NOTES ConnecTech:

23/01/2020 - 1.0.0:
    - Primera versió ConnecTech
    - Afegida plataforma de pagament
    - Afegit Espanya com a país
    - Afegit promocions com a posts
    - Ara es pot seleccionar el país adient

18/11/2019 - 0.0.38:
    - Quan l'app agafa la hora local, agafa la hora de Google Time per evitar discrepancies
    - Afegides més traduccions del Indonesi
    - La informació del BLE es desa ara cada segon en comptes de cada 6.25s
    - Quan es fa un logout, s'elimina la cache de posts

15/11/2019 - 0.0.37:
    - Afegides més traduccions del Indonesi
    - Millorat algorisme de canvi de dongle quan es fa en canvi de dongle
    - Posat limit per temps als logins automàtics consecutius

14/11/2019 - 0.0.36:
    - Canviat ordre del Vehicle Condition per que quadri amb els Wireframes
    - Certes pantalles, ara s'actualitzen si reben una notificació
    - Si l'usuari encara no ha ficat el gènere, ja no posa per defecte "Female"
    - Cache en el Vehicle Condition
    - Si l'usuari té deshabilitada la ubicació, al planejar una ruta pot posar punt inicial, però si intenta planejar sense punt inicial, li demanarà d'activar el GPS
    - El Vehicle Condition, quan passa una variable de error a working, la data es posa en el color correcte

12/11/2019 - 0.0.35:
    - Bug fixes

11/11/2019 - 0.0.34:
    - Aquesta versió es va fer perque la 0.0.33 estava en DEV

11/11/2019 - 0.0.33:
    - Afegides traduccions al Indonesi
    - Arreglat bug que feia que al canviar d'idioma, el servidor no s'actualitzés aquesta dada
    - Quan amagaves una ruta pending, la tornaves a obrir, no apareixia el camí en el Maps
    - Quan amagues una ruta pending, l'app no se'n anava al Dashboard

08/11/2019 - 0.0.32:
    - Popup d'usuari o contrasenya incorrecte quan falta algun dels dos camps
    - Quan una ruta pending es transfereix a una done, l'app pot consultar el servidor per agafar la ruta done i actualitzar-la
    - Quan canvies de dongle en el canvi del mobil, registra el nou dongle si fa falta
    - Arreglat bug que feia que es deixessin d'estirar rutes o statuslogs

07/11/2019 - 0.0.31:
    - Seleccionar punts de una ruta planned es poden fer escribint la direcció/ciutat
    - Ara es desen els POIs d'una ruta ja feta o planejada en una ruta pending quan la crees
    - S'ha arreglat el bit del Vehicle Condition que estava invertit
    - Arreglada la forma del àrea del Geofencing. Ara és més quadrada
    - Si l'usuari puja una imatge massa gran, es redueix abans de mostrar-la

05/11/2019 - 0.0.30:
    - Afegida notificació del Service Timing
    - Modificat titol inicial de les rutes pending a "dd/MM/yyyy - HH:mm:ss"
    - Amagat botó "SAVE" quan fas "START" en una ruta Autosave
    - Arreglat bug que permitia duplicar rutes
    - Quan edites un POI a la pantalla de "SAVE ROUTE", es desa el text canviat quan deses la ruta

04/10/2019 - 0.0.21:
    - Added 0-image detector to image library.
    - Changed more images to use image library load.
    - Update password in Shared Preferences when changed.
    - Fixed crash with Android less than 8.0, that the New Route screen crashes on calculate route.
    - Modified encryption variables to improve performance
    - Added Dashboard Cache as variable instead of storing in Shared Preferences
    - Fixed bug that crashed Posts Screen when the posts are scrolled and then change to Invitations
    - Added Mitsubishi/Dealer posts Cache

27/09/2019 - 0.0.20:
    - Improved interaction between app and notifications.
    - Vehicle condition will show "---" when the date is undefined.
    - Improved performance reducing calls to app cache.
    - The user avatars will be managed and cached by the image library.
    - Fixed invite button.
    - Add interactions with friends notifications.

26/09/2019 - 0.0.19:
    - When profile is updated, the app now sends the VIN.
    - Once a user sends an invitation to another, it cannot send an invitation to the same user.
    - If the serial number is not valid, a popup telling "Invalid Serial Number" will show.
    - Fixed more bugs.

25/09/2019 - 0.0.18:
    - Bugfixes to test production.
    - Fixed notification count overflow.
    - Routes popup fixed.
    - Notification count size increased.
    - Added users cache.
    - Fixed dashboard will show car fault when the car is ok.
    - Fixed bug that allows to connect via BLE with disabled Bluetooth.

19/09/2019 - 0.0.17:
    - Car status date Bugfixes.

19/09/2019 - 0.0.16:
    - Car status date Bugfixes.

19/09/2019 - 0.0.15:
    - Standarized dates to the format dd/MM/yyyy - HH:mm:ss.
    - In Android 10, IMEI cannot be retrieved. If the app is running in Android 10 or newer version, will catch this and generate a random IMEI.
    - Geofencing central point is initializated to current car position.
    - Geofencing stores the central point even if is disabled. This allows the user to maintain the point without losing it on Geofencing deactivation.
    - Improved performance in some screens.
    - Improved transition on Push Notification clicked on the smartphone.

13/09/2019 - 0.0.14:
    - Bugfixes.

12/09/2019 - 0.0.13:
    - Fixed tips.
    - Interface issues fixed.
    - New Route screen only allows to select any points by clicking to the map.
    - The Search Screen shows if another user is blocked by the user.

05/09/2019 - 0.0.12:
    - Bug fixes.

??/09/2019 - 0.0.11:
    ???

??/09/2019 - 0.0.10:
    ???

30/08/2019 - 0.0.9:
    - Improved New Phone procedure.
    - Preparation for Indonesian translations.
    - Add Community unit tests.

27/08/2019 - 0.0.8:
    - Add Trip unit tests.
    - Add Community MD unit tests.
    - Add Configuration unit tests.
    - Add Badge unit tests.
    - Add Storage unit tests.
    - Add My Drive unit tests.
    - Add Welcome unit tests.
    - Add File unit tests.
    - Add Dashboard and Status unit tests.
    - Add Location unit tests.

07/08/2019 - 0.0.7:
    ???

07/08/2019 - 0.0.6:
    ???

01/08/2019 - 0.0.5:
    - Fixed FOTA Popup.
    - Add app version check.
    - Deleted "Friend Share Location" notification.
    - Add posts images from MMC and Dealers.
    - Updated Notifications in the history.
    - Added InServiceTiming.

29/07/2019 - 0.0.4:
    - Bug fixes.

29/07/2019 - 0.0.3:
    - New version of the BLE library. Updated to MMC dongle.
    - Adapted app to the new BLE library.
    - Interface improvements.
    - Second Register improvements.
    - Added Notification Count.
    - Added Prelogin.
    - Notification count updates in real time.
    - Added DashboardAndStatus.
    - Changed Google Maps API.

06/06/2019 - 0.0.2:
    - Modified UI.
    - Enabled tips.
    - Dashboard with redirection.
    - Storage shows the main image of the car.

06/06/2019 - 0.0.1:
    - Initial version
    - Cached values of the menu.
*/