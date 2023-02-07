package com.otcengineering.white_app.network;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.interfaces.Callback;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import javax.annotation.Nonnull;

import static com.otcengineering.white_app.MyApp.getContext;

public class CommunityNetwork {
    public static void getDealer(Callback<Community.DealerResponse> cd) {
        TypedTask<Community.DealerResponse> getDealerInfo = new TypedTask<>(Endpoints.GET_DEALER, null, true, Community.DealerResponse.class,
                new TypedCallback<Community.DealerResponse>() {
                    @Override
                    public void onSuccess(@Nonnull Community.DealerResponse value) {
                        PrefsManager.getInstance().saveDealerInfo(value, getContext());
                        if (cd != null) {
                            try {
                                cd.run(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, String str) {

                    }
                });
        getDealerInfo.execute();
    }
}
