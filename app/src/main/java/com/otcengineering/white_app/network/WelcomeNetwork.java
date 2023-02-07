package com.otcengineering.white_app.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.tasks.GenericTask;

public class WelcomeNetwork {
    public static void getCountry(long id, Callback<String> resp) {
        new GenericTask(Endpoints.GET_COUNTRIES, null, false, otcResponse -> {
            try {
                Welcome.CountriesResponse cr = otcResponse.getData().unpack(Welcome.CountriesResponse.class);
                for (Welcome.CountriesResponse.Country ct : cr.getCountriesList()) {
                    if (ct.getId() == id) {
                        resp.onSuccess(ct.getName());
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }).execute();
    }

    public static void getRegion(int countryId, long id, Callback<String> resp) {
        Welcome.Regions regs = Welcome.Regions.newBuilder().setCountryId(countryId).build();
        new GenericTask(Endpoints.REGIONS, regs, false, otcResponse -> {
            try {
                Welcome.RegionsResponse cr = otcResponse.getData().unpack(Welcome.RegionsResponse.class);
                for (Welcome.RegionsResponse.Region rg : cr.getRegionsList()) {
                    if (rg.getId() == id) {
                        resp.onSuccess(rg.getName());
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }).execute();
    }

    public static void getDealer(Callback<String> resp) {
        new GenericTask(Endpoints.GET_DEALER, null, true, otcResponse -> {
           if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
               try {
                   Community.DealerResponse dr = otcResponse.getData().unpack(Community.DealerResponse.class);
                   resp.onSuccess(dr.getName());
               } catch (InvalidProtocolBufferException e) {
                   e.printStackTrace();
                   resp.onError(Shared.OTCStatus.UNRECOGNIZED);
               }
           } else {
               resp.onError(otcResponse.getStatus());
           }
        }).execute();
    }
}
