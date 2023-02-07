package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.payment.activity.CartActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.interfaces.ShowMenuPostsListener;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.serialization.pojo.OemDealerItem;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class ConnecTechDealerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<OemDealerItem> connectechDealerPosts = new ArrayList<>();
    private boolean mode;
    private boolean isMessage;
    private ShowMenuPostsListener listener;

    public ConnecTechDealerAdapter(Context context, boolean isDealer, boolean isMessage, ShowMenuPostsListener listener) {
        super();
        this.context = context;
        this.mode = isDealer;
        this.isMessage = isMessage;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return connectechDealerPosts.size();
    }

    public OemDealerItem getItem(int position) {
        return connectechDealerPosts.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        try {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_connectech_dealer, viewGroup, false);
        } catch (RuntimeException e) {
            e.printStackTrace();
            view = new View(context);
        }
        return new DatsunDealerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final DatsunDealerHolder itemHolder = (DatsunDealerHolder) holder;
        OemDealerItem oemDealerItem = connectechDealerPosts.get(position);

        itemHolder.viewDivider.setVisibility(mode ? View.VISIBLE : View.GONE);
        itemHolder.layoutThreeDots.setVisibility(mode ? View.VISIBLE : View.GONE);
        itemHolder.separator.setVisibility(mode ? View.VISIBLE : View.GONE);

        itemHolder.txtDate.setText(oemDealerItem.getDate());

        String text = oemDealerItem.getText();

        for (Community.PostImage img : oemDealerItem.getImages()) {
            if (text.contains("./" + img.getName())) {
                text = text.replace("./" + img.getName(), "data:image/png;base64," + img.getBase64Image());
            }
            else {
                text = text.replace(img.getName(), "data:image/png;base64," + img.getBase64Image());
            }
        }

        itemHolder.txtHtml = text;
        itemHolder.content.getSettings().setJavaScriptEnabled(false);
        itemHolder.content.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);

        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String request) {
                if (request.startsWith("promo:")) {
                    String promoCode = request.replace("promo:", "");
                    DialogYesNo dyn = new DialogYesNo(context);
                    dyn.setMessage("Do you want to add the promotion to the cart?");
                    dyn.setYesButtonClickListener(() -> {
                        PaymentNetwork.addPromo(promoCode, new Callback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean success) {
                                context.startActivity(new Intent(context, CartActivity.class));
                            }

                            @Override
                            public void onError(Shared.OTCStatus status) {
                                showCustomDialogError(CloudErrorHandler.handleError(status));
                            }
                        });
                    });
                    dyn.show();
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }
        };
        itemHolder.content.setWebViewClient(client);

        itemHolder.txtLikes.setText(String.valueOf(oemDealerItem.getLikes()));

        itemHolder.btnLike.setOnClickListener(view -> likePost(itemHolder, oemDealerItem.getId()));

        itemHolder.layoutThreeDots.setOnClickListener(view -> {
            if (listener != null) {
                listener.showMenu(oemDealerItem);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void likePost(DatsunDealerHolder itemHolder, long postId) {
        if (ConnectionUtils.isOnline(context)) {
            LikePostTask likePostTask = new LikePostTask() {
                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);
                    if (result == Shared.OTCStatus.SUCCESS_VALUE) {
                        String likesString = itemHolder.txtLikes.getText().toString();
                        int likes = Integer.parseInt(likesString);
                        itemHolder.txtLikes.setText(String.valueOf(likes + 1));
                    } else if (result == Shared.OTCStatus.ALREADY_LIKED_VALUE) {
                        //showCustomDialogError(R.string.already_liked);
                        Toast.makeText(context, R.string.already_liked, Toast.LENGTH_LONG).show();
                    } else {
                        showCustomDialogError(R.string.error_default);
                    }
                }
            };
            likePostTask.execute(postId);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showCustomDialogError(int messageRes) {
        CustomDialog customDialog = new CustomDialog(context, messageRes, true);
        customDialog.show();
    }

    private void showCustomDialogError(String messageRes) {
        CustomDialog customDialog = new CustomDialog(context, messageRes, true);
        customDialog.show();
    }

    public void addItem(OemDealerItem pst) {
        connectechDealerPosts.add(pst);
    }

    @SuppressLint("StaticFieldLeak")
    private class LikePostTask extends AsyncTask<Long, Void, Integer> {
        @Override
        protected Integer doInBackground(Long... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(context);

                Community.PostId.Builder builder = Community.PostId.newBuilder();
                Long postId = params[0];
                builder.setPostId(postId);

                String endpoint;
                if (!mode) {
                    endpoint = Endpoints.CONNECTECH_LIKE;
                } else {
                    endpoint = Endpoints.DEALER_LIKE;
                }
                Shared.OTCResponse response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                return response.getStatusValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void clearItems() {
        connectechDealerPosts.clear();
    }

    public void addItems(List<OemDealerItem> items) {
        connectechDealerPosts.addAll(items);
    }

    private class DatsunDealerHolder extends RecyclerView.ViewHolder {
        private TextView txtDate;
        private LinearLayout btnLike;
        private TextView txtLikes;
        private View viewDivider;
        private FrameLayout layoutThreeDots;
        private WebView content;
        private View separator;
        private String txtHtml;

        private DatsunDealerHolder(View itemView) {
            super(itemView);
            try {
                txtDate = itemView.findViewById(R.id.row_connectech_dealer_txtDate);
                btnLike = itemView.findViewById(R.id.row_connectech_dealer_btnLike);
                txtLikes = itemView.findViewById(R.id.row_connectech_dealer_txtLikes);
                viewDivider = itemView.findViewById(R.id.row_connectech_dealer_viewDivider);
                layoutThreeDots = itemView.findViewById(R.id.row_connectech_dealer_layoutThreeDots);
                content = itemView.findViewById(R.id.wvContent);
                separator = itemView.findViewById(R.id.separator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}