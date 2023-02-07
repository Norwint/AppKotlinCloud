package com.otcengineering.white_app.network;

public class Endpoints {
    enum Server {
        CNT, CSQ, DAV, LOCAL
    }
    // BASE
    public static String URL_BASE;
    private static Server server = Server.CNT;

    public static void setServer() {
        switch (server) {
            case CNT:
                URL_BASE = "http://connectech.otc010.com/";
                break;
            case CSQ:
                URL_BASE = "http://192.168.1.220:8080/";
                break;
            case DAV:
                URL_BASE = "http://192.168.1.196:8080/";
                break;
            case LOCAL:
                URL_BASE = "http://192.168.1.39:8080/";
                break;
        }
    }

    public static final boolean SEND_EVENT_TO_FA = false;

    // WELCOME
    public static final String ACTIVATE = "/welcome/user/activate";
    public static final String LOGIN = "/welcome/user/login";
    public static final String LOGOUT = "/welcome/user/logout";
    public static final String PASSWORD_RECOVERY = "/welcome/password/recovery";
    public static final String CHECK_EMAIL = "/welcome/user/check/email";
    public static final String CHECK_USERNAME = "/welcome/user/check/username";
    public static final String REGISTER = "/welcome/user/register";
    public static final String DEALERSHIPS = "/welcome/dealerships";
    public static final String DEALERSHIPS_BY_NAME = "/welcome/dealerships/name";
    public static final String GET_COUNTRIES = "/welcome/countries";
    public static final String GET_TERMS_ACCEPTANCE = "/welcome/terms/acceptance";
    public static final String GET_TERMS_ACCEPTANCE_LANG = "/welcome/terms/acceptance/language/";
    public static final String GET_TERMS_ACCEPTANCE_USER_LANG = "/welcome/terms/acceptance/user-language";
    public static final String MODEL = "/welcome/model";
    public static final String PROFILE = "/welcome/user/profile";
    public static final String REGIONS = "/welcome/regions";
    public static final String CITIES = "/welcome/cities";
    public static final String ENABLE_USER = "/welcome/user/enabled";
    public static final String GENERATE_CERTIFICATE = "/welcome/generate/certificate";
    public static final String RESEND_SMS = "/welcome/sms/resend";
    public static final String REGISTER_PUSH_TOKEN = "/welcome/push/register";
    public static final String DEVICE_SPECS = "/welcome/user/device-specs";
    public static final String CHANGE_PHONE = "/welcome/user/change-phone";
    public static final String PASSWORD_IS_TEMPORAL = "/welcome/password/is-temporal";
    public static final String PASSWORD_UPDATE = "/welcome/password/update";

    // DASHBOARD
    public static final String DASHBOARD = "/dashboard/summary";
    public static final String VEHICLE_CONDITION = "/dashboard/vehicle/condition";
    public static final String VEHICLE_CONDITION_DESCRIPTION = "/dashboard/vehicle/condition/description";
    public static final String DASHBOARD_CAR_PHOTO = "/dashboard/car/photo";

    // FILE
    public static final String FILE_GET = "/file/get/";
    public static final String FILE_UPLOAD = "/file/upload";

    // PROFILE
    public static final String USER_UPDATE = "/profile/user/update";
    public static final String TYPE_UPDATE = "/profile/type/update";
    public static final String USER_INFO = "/profile/user";
    public static final String USER_TERMS = "/profile/user/terms";
    public static final String USER_TERMS_UPDATE = "/profile/user/terms/update";
    public static final String USER_IMAGE = "/profile/user/image";
    public static final String USER_IMAGE_DELETE = "/profile/user/image/delete";
    public static final String GET_NOTIFICATIONS = "/profile/notifications";
    public static final String NOTIFICATIONS_UPDATE = "/profile/notifications/update";
    public static final String RESEND_SMS_PROFILE = "/profile/sms/resend";
    public static final String GET_USER_NOTIFICATIONS = "/profile/notification/history";
    public static final String SOCIAL_NETWORK_UPDATE = "/profile/social-network/update";
    public static final String SOCIAL_NETWORK = "/profile/social-network";
    public static final String NOTIFICATION_COUNT = "/profile/notification/count";
    public static final String LAST_DONGLE_CONNECTION = "/profile/last/dongle/connection";
    public static final String READ_NOTIFICATION = "/profile/notification/history/read";
    public static final String NOTIFICATIONS_DELETE = "/profile/notification/history/delete";

    // MY DRIVE
    public static final String SUMMARY = "/drive/summary";
    public static final String USER_MILEAGE = "/drive/user/mileage";
    public static final String USER_ECO = "/drive/user/eco";
    public static final String USER_SAFETY = "/drive/user/safety";
    public static final String TIPS = "/drive/tips";
    public static final String GET_USER_STATE = "/drive/user/state";
    public static final String RANKING = "/drive/ranking";
    public static final String MILEAGE = "/drive/mileage";
    public static final String ECO = "/drive/eco";
    public static final String SAFETY = "/drive/safety";
    public static final String RANKING_POSITION = "/drive/ranking/position";
    public static final String MILEAGE_POSITION = "/drive/mileage/position";
    public static final String ECO_POSITION = "/drive/eco/position";
    public static final String SAFETY_POSITION = "/drive/safety/position";

    // TRIPS
    public static final String ROUTES = "/trip/routes";
    public static final String ROUTE_FAV = "/trip/route/favourite";
    public static final String ROUTE_UNFAV = "/trip/route/unfavourite";
    public static final String ROUTE_STATUS = "/trip/route/status";
    public static final String ROUTE_INFO = "/trip/route/info";
    public static final String ROUTE_NEW = "/trip/route/new";
    public static final String ROUTE_UPDATE = "/trip/route/update";
    public static final String GET_POI_TYPES = "/trip/poi/types";
    public static final String ROUTES_COUNT_AUTOSAVED = "/trip/routes/count/autosaved";
    public static final String ROUTE_AUTOSAVED_CHECK = "/trip/route/autosaved/check";
    public static final String GET_ROUTES_FILTER = "/trip/routes/filter";
    public static final String ROUTES_FAVOURITE = "/trip/routes/favourite";
    public static final String ROUTE_POIS = "/trip/route/pois";
    public static final String ROUTE_ADD_POI = "/trip/route/pois/add";
    public static final String ROUTE_DELETE_POI = "/trip/route/pois/delete";
    public static final String ROUTE_POI_ADD_IMAGE = "/trip/route/pois/image/add";
    public static final String ROUTE_POI_DELETE_IMAGE = "/trip/route/pois/image/delete";
    public static final String ROUTE_ADD_GPX = "/trip/route/add/gpx";
    public static final String ROUTE_DONE = "/trip/route/done";
    public static final String TRIP_SUMMARY = "/trip/route/last/summary";
    public static final String GET_ROUTE = "/trip/route/get";
    public static final String GET_SOURCE_ROUTE = "/trip/route/get/source-route";

    // COMMUNITY DATSUN AND DEALERS
    public static final String CONNECTECH_POSTS = "/community/connectech/posts";
    public static final String DEALER_POSTS = "/community/dealer/posts";
    public static final String CONNECTECH_MESSAGES = "/community/messages/connectech";
    public static final String DEALER_MESSAGES = "/community/messages/dealer";
    public static final String GET_DEALER = "/community/dealer";
    public static final String DEALER_REPORT = "/community/dealer/report";
    public static final String CONNECTECH_LIKE = "/community/like/connectech";
    public static final String DEALER_LIKE = "/community/like/dealer";

    // COMMUNITY
    public static final String SEARCH_USERS = "/community/search/users";
    public static final String SEARCH_USERS_NEAR = "/community/search/users/near";
    public static final String FRIENDS = "/community/friends";
    public static final String FRIEND_REQUESTS = "/community/friends/requests";
    public static final String USER_PROFILE = "/community/user/profile";
    public static final String SEND_REQUEST = "/community/send/request";
    public static final String GET_USER_MILEAGE = "/community/user/mileage";
    public static final String GET_USER_ECO = "/community/user/eco";
    public static final String GET_USER_SAFETY = "/community/user/safety";
    public static final String USER_POSTS = "/community/user/posts";
    public static final String POST_LIKE = "/community/like/post";
    public static final String POST_DISLIKE = "/community/dislike/post";
    public static final String POSTS = "/community/posts";
    public static final String FRIENDS_POSTS = "/community/friends/posts";
    public static final String SEND_POST = "/community/send/post";
    public static final String ANSWER_REQUEST = "/community/answer/request";
    public static final String REPORT_POST = "/community/report/post";
    public static final String UNFRIEND = "/community/unfriend";
    public static final String BLOCK_USER = "/community/block/user";
    public static final String USER_FRIENDS = "/community/user/friends";
    public static final String GET_NUMBER_REQUEST = "/community/number/request";
    public static final String ROUTE_LIKE = "/community/like/route";
    public static final String ROUTE_DISLIKE = "/community/dislike/route";
    public static final String ROUTE_IMAGE_GENERATE = "/community/route/image/generator";
    public static final String POST_STATS = "/community/post/stats";

    // WALLET
    public static final String STORAGE_USER = "/storage/user";
    public static final String UPLOAD = "/storage/upload";
    public static final String DELETE = "/storage/delete";
    public static final String MANUAL = "/storage/manual";

    // CONFIGURATION
    public static final String FIRMWARE = "/configuration/firmware";
    public static final String FIRMWARE_ANSWER = "/configuration/firmware/answer";
    public static final String DONGLE_CREATE = "/configuration/dongle/create";
    public static final String VERSION = "/configuration/version";
    public static final String UNREGISTER_DONGLE = "/configuration/dongle/unregister";
    public static final String BLUETOOTH_SETTINGS = "/configuration/bluetooth/settings";

    // BADGES
    public static final String GET_BADGES = "/badge/get";
    public static final String GET_BADGES_VERSION = "/badge/get/version";
    public static final String GET_BADGES_USER = "/badge/get/user";

    // CONNECTOR
    public static final String CONNECTOR_ROUTE = "/connector/route";
    public static final String CONNECTOR_ROUTES = "/connector/routes";
    public static final String CONNECTOR_ROUTE_STATUS = "/connector/route/status";
    public static final String CONNECTOR_USER_ID = "/connector/user/id";
    public static final String CONNECTOR_VEHICLE_STATUS = "/connector/vehicle/status";
    public static final String CONNECTOR_NOTIFICATIONS_DATSUN_SEND = "/connector/notifications/connectech/send";
    public static final String CONNECTOR_NOTIFICATIONS_DEALER_SEND = "/connector/notifications/dealer/send";
    public static final String CONNECTOR_DONGLE_NEW = "/connector/dongle/new";

    // LOCATION AND SECURITY
    public static final String VEHICLE_STATUS = "/location/vehicle/status";
    public static final String CAR = "/location/car";
    public static final String LOCATION_CALLCENTER = "/location/callcenter";
    public static final String PHONE = "/location/phone";
    public static final String GEOFENCING = "/location/geofencing";

    public static final class Internal {
        public static final String IS_REPORTING_ENABLED = "/configuration/bluetooth/is-reporting-enabled";
        public static final String REPORT_ISSUE = "/configuration/bluetooth/report-issue";
    }

    public static final class Payment {
        public static final String PAYPAL_CLIENT_TOKEN = "/payment/paypal/client-token";
        public static final String PAYPAL_CHECKOUT = "/payment/paypal/checkout";
        public static final String ITEMS = "/payment/items";
        public static final String ADD_ITEM_CART = "/payment/shopping-cart/item-add";
        public static final String MODIFY_ITEM_CART = "/payment/shopping-cart/item-update";
        public static final String GET_ITEM_CART = "/payment/shopping-cart/items";
        public static final String ORDERS = "/payment/orders";
        public static final String ORDER_BY_ID = "/payment/order/%d";
        public static final String ITEMS_QUANTITIY = "/payment/shopping-cart/item-quantity";
        public static final String GOOGLE_PAY_CHECKOUT = "/payment/googlepay/checkout";
        public static final String PROMO = "/payment/shopping-cart/promotion-add";
        public static final String CHECK_CHECKOUT = "/payment/check-checkout";
        public static final String ORDERS_BY_DATE = "/payment/orders-date";
        public static final String SEND_ORDERS_BY_DATE = "/payment/mail-orders-date";
        public static final String CREDIT_CARD_CHECKOUT = "/payment/creditcard/checkout";
    }

    // Analytics
    public static final String ANALYTICS = "/configuration/app-event/log";

    // Surveys
    public static final class Surveys {
        public static final String SUMMARY = "/survey/summary";
        public static final String LIST = "/survey/list/%d";
        public static final String ID = "/survey/%d";
        public static final String USER_ANSWER = "/survey/user-answer";
        public static final String USER_UNCHANGED_ANSWERS = "/survey/user-unchanged-answers";
    }
}