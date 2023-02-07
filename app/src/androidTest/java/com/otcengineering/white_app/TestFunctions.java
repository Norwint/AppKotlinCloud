package com.otcengineering.white_app;

import com.otc.alice.api.model.General;

public class TestFunctions {
    public static General.UserProfile.Builder getUserProfile(String username, String sn) {
        General.UserProfile.Builder builder = General.UserProfile.newBuilder();

        builder.setVin(TestConstants.testVin)
                .setMac("00:00:00:00:00:00")
                .setImei(TestConstants.testImei)
                .setInstallationNumber("0123456789abcdef")
                .setName(username)
                .setCountryId(2)
                .setRegion(40)
                .setCity("Barcelona")
                .setAddress("Av. Diagonal 399")
                .setBirthdayDate("1000-06-09")
                .setSexType(General.SexType.MALE)
                .setDrivingLicenseDate("1969-04-20")
                .setDealershipId(6)
                .setDongleSerialNumber(sn)
                .setPlate("test-16")
                .setLanguage(General.Language.ENGLISH)
                .setCarOwner(true)
                .setCarRegistrationDate("2019-01-01")
                .setFinanceTermDateStart("2019-01-01")
                .setFinanceTermDateEnd("2022-01-01")
                .setInsuranceTermDateStart("2019-01-01")
                .setInsuranceTermDateEnd("2021-01-01")
                .setTradeInPrice(0)
                .setBloodType(General.BloodType.UNDEFINED_);

        for (int i = 0; i < 3; ++i) {
            General.TermType term = General.TermType.forNumber(i);
            General.TermAcceptance ta = General.TermAcceptance.newBuilder()
                    .setMobileIdentifier(TestConstants.testImei)
                    .setTimestamp("2019-08-01 10:00:00")
                    .setType(term).build();
            builder.addTerms(ta);
        }

        return builder;
    }

    public static General.UserProfile.Builder getUserProfile() {
        General.UserProfile.Builder builder = General.UserProfile.newBuilder();

        builder.setVin(TestConstants.testVin)
               .setMac("00:00:00:00:00:00")
               .setImei(TestConstants.testImei)
               .setInstallationNumber("0123456789abcdef")
               .setName(TestConstants.testUsername)
               .setCountryId(2)
               .setRegion(40)
               .setCity("Barcelona")
               .setAddress("Av. Diagonal 399")
               .setBirthdayDate("1000-06-09")
               .setSexType(General.SexType.MALE)
               .setDrivingLicenseDate("1969-04-20")
               .setDealershipId(6)
               .setDongleSerialNumber(TestConstants.testSN)
               .setPlate("test-16")
               .setLanguage(General.Language.ENGLISH)
               .setCarOwner(true)
               .setCarRegistrationDate("2019-01-01")
               .setFinanceTermDateStart("2019-01-01")
               .setFinanceTermDateEnd("2022-01-01")
               .setInsuranceTermDateStart("2019-01-01")
               .setInsuranceTermDateEnd("2021-01-01")
               .setTradeInPrice(0)
               .setBloodType(General.BloodType.UNDEFINED_);

        for (int i = 0; i < 3; ++i) {
            General.TermType term = General.TermType.forNumber(i);
            General.TermAcceptance ta = General.TermAcceptance.newBuilder()
                    .setMobileIdentifier(TestConstants.testImei)
                    .setTimestamp("2019-08-01 10:00:00")
                    .setType(term).build();
            builder.addTerms(ta);
        }

        return builder;
    }

    public static General.UserProfile.Builder getUserProfileBadDate() {
        General.UserProfile.Builder builder = getUserProfile();
        builder.setBirthdayDate("►▬2ª646416♠¨8♠98");
        return builder;
    }

    public static General.UserProfile.Builder getUserProfileBadVin() {
        General.UserProfile.Builder builder = getUserProfile();
        builder.setVin(TestConstants.testBadVin);
        return builder;
    }

    public static General.UserProfile.Builder getUserProfileBadPlate() {
        General.UserProfile.Builder builder = getUserProfile();
        builder.setPlate("abcdefghijklmnopqrstuvwxyzçñ");
        return builder;
    }

    public static General.UserProfile.Builder getUserProfileBadCountry() {
        General.UserProfile.Builder builder = getUserProfile();
        builder.setCountryId(-1);
        return builder;
    }
}
