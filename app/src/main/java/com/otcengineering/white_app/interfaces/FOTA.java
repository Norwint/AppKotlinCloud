package com.otcengineering.white_app.interfaces;

public interface FOTA {
    void initFotaCallback();

    void fotaUpdateAsk(String version);
    void fotaUpdateAskYes();
    void fotaUpdated();
    void fotaError(String error);
    void fotaDeletingImage();
    void fotaSendingNewFw();
    void fotaRestartDongle();
}
