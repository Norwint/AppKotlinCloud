package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.SendPostActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.serialization.pojo.RouteItem;
import com.otcengineering.white_app.tasks.AddOrRemoveFavoriteTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class RouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface RouteListener {
        void onRouteItemSelected(int position);

        void onMenu(int position);
    }

    private Context context;
    private List<RouteItemInList> routeItemInLists = new ArrayList<>();
    private RouteListener listener;

    public RouteAdapter(Context context, RouteListener listener) {
        super();
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return routeItemInLists.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_route, viewGroup, false);
        return new RouteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final RouteHolder itemHolder = (RouteHolder) holder;

        itemHolder.layoutRootParent.setLockDrag(true);
        itemHolder.routeItemInList = routeItemInLists.get(position);
        final RouteItem routeItem = itemHolder.routeItemInList.routeItem;
        itemHolder.routeItem = routeItem;

        if (routeItem.isAutosave() || routeItem.getTitle() == null || routeItem.getTitle().isEmpty()) {
            itemHolder.txtTitle.setText(String.format("%s", DateUtils.utcToString(routeItem.getDateStart(), "dd/MM/yyyy HH:mm:ss")));
        } else {
            itemHolder.txtTitle.setText(routeItem.getTitle());
        }

        itemHolder.imgMap.setVisibility(View.GONE);
        itemHolder.layoutNoData.setVisibility(View.VISIBLE);
        if (Utils.canHaveRoute(routeItem)) {
            try {
                int last = routeItem.getLatLngList().size() - 1;
                if (last < 0) {
                    last = 0;
                }
                Utils.generateRouteImage(context, routeItem.getId(), routeItem.getPolyLine(), routeItem.getLatLngList().get(0), routeItem.getLatLngList().get(last), (bmp, err) -> Utils.runOnMainThread(() -> {
                    if (err == null && bmp != null) {
                        itemHolder.imgMap.setVisibility(View.VISIBLE);
                        itemHolder.layoutNoData.setVisibility(View.GONE);
                        itemHolder.imgMap.setImageBitmap(bmp);
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (routeItem.isAutosave()) {
            itemHolder.btnFavorite.setEnabled(false);
            ((ImageView)itemHolder.btnFavorite.findViewById(R.id.imgFav)).setColorFilter(ContextCompat.getColor(context, R.color.colorTexto));
            itemHolder.btnShare.setVisibility(View.GONE);
        }

        boolean hasGpx = routeItem.hasGpx();
        itemHolder.hasGpx = hasGpx;

        if (hasGpx) {
            itemHolder.txtDuration.setText(routeItem.getDurationInMinsFormatted());
            itemHolder.txtDistance.setVisibility(View.VISIBLE);
            itemHolder.txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
            itemHolder.txtConsumption.setText(String.format(Locale.US, "%.2f l", routeItem.getConsumption()));
            itemHolder.txtConsumptionAvg.setVisibility(View.VISIBLE);
            itemHolder.txtConsumptionAvg.setText(String.format(Locale.US, "%.2f km/l", routeItem.getConsumptionAvg()));
            itemHolder.txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique()/10));
        }

        itemHolder.imgFav.setImageDrawable(routeItem.isFavorite() ?
                context.getResources().getDrawable(R.drawable.my_routes_icons_2) : context.getResources().getDrawable(R.drawable.my_routes_icons_3));

        manageButtonDeleteUI(itemHolder);

        manageEditModeUI(itemHolder);

        itemHolder.viewPadding.setVisibility(position == getItemCount() - 1 ? View.VISIBLE : View.GONE);
    }

    private void manageFavoriteAction(final RouteHolder itemHolder, RouteItem routeItem) {
        @SuppressLint("StaticFieldLeak")
        AddOrRemoveFavoriteTask addOrRemoveFavoriteTask = new AddOrRemoveFavoriteTask(routeItem.getId(), routeItem.isFavorite()) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                itemHolder.btnFavorite.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                itemHolder.btnFavorite.setEnabled(true);
                Log.d("Fav", routeItem.isFavorite() ? "true" : "false");
                if (result) {
                    int messageRes = routeItem.isFavorite() ?
                            R.string.remove_route_to_favorites_correctly :
                            R.string.add_route_to_favorites_correctly;
                    showCustomDialog(messageRes);
                    manageFavoriteUI(routeItem, itemHolder);
                }
                s_sem.release();
            }
        };
        //setFavourite(routeItem.isFavorite(), itemHolder);
        //itemHolder.btnFavorite.setEnabled(false);
        if (ConnectionUtils.isOnline(context)) {
            addOrRemoveFavoriteTask.execute(context);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showCustomDialog(int messageRes) {
        CustomDialog customDialog = new CustomDialog(context, messageRes, false);
        customDialog.show();
    }

    private void manageFavoriteUI(RouteItem routeItem, RouteHolder itemHolder) {
        boolean isFavorite = routeItem.isFavorite();
        itemHolder.imgFav.setImageResource(isFavorite ? R.drawable.my_routes_icons_3 : R.drawable.my_routes_icons_2);
        routeItem.setFavorite(!isFavorite);
    }

    private void openShareRoute(RouteItem routeItem) {
        Intent intent = new Intent(context, SendPostActivity.class);
        intent.putExtra(Constants.Extras.ROUTE, routeItem.toString());
        context.startActivity(intent);
    }

    private void manageEditModeUI(RouteHolder itemHolder) {
        if (itemHolder.routeItemInList.opened) {
            itemHolder.layoutRootParent.open(true);
        } else {
            itemHolder.layoutRootParent.close(true);
        }
    }

    private void manageButtonDeleteUI(RouteHolder itemHolder) {
        boolean selected = itemHolder.routeItemInList.selected;
        int imageResource = selected ? R.drawable.my_routes_icons_1 : R.drawable.my_routes_icons_27;
        itemHolder.imgDelete.setImageResource(imageResource);
    }

    public @Nullable RouteItem getData(int position) {
        if (position >= 0 && position < routeItemInLists.size()) {
            return routeItemInLists.get(position).routeItem;
        } else {
            return null;
        }
    }

    public void clearItems() {
        routeItemInLists.clear();
    }

    public void addItems(List<RouteItem> routeItems) {
        for (RouteItem routeItem : routeItems) {
            if (routeItem.getGpxFileId() == 0) continue;
            boolean found = false;
            for (int i = 0; i < routeItemInLists.size(); ++i) {
                try {
                    if (routeItemInLists.get(i).routeItem.getId() == routeItem.getId()) {
                        routeItemInLists.set(i, new RouteItemInList(routeItem));
                        found = true;
                    }
                } catch (Exception ignored) {

                }
            }
            if (!found) {
                this.routeItemInLists.add(new RouteItemInList(routeItem));
            }
        }
    }

    public void selectAll() {
        for (RouteItemInList routeItemInList : routeItemInLists) {
            routeItemInList.selected = true;
        }
    }

    public void openAll() {
        for (RouteItemInList routeItemInList : routeItemInLists) {
            routeItemInList.opened = true;
        }
    }

    public void closeAll() {
        for (RouteItemInList routeItemInList : routeItemInLists) {
            routeItemInList.opened = false;
            routeItemInList.selected = false;
        }
    }

    public List<RouteItem> getItemsSelected() {
        List<RouteItem> routeSelected = new ArrayList<>();
        for (RouteItemInList routeItemInList : routeItemInLists) {
            if (routeItemInList.selected) {
                routeSelected.add(routeItemInList.routeItem);
            }
        }
        return routeSelected;
    }

    private class RouteItemInList {
        private RouteItem routeItem;
        private boolean opened;
        private boolean selected;

        RouteItemInList(RouteItem routeItem) {
            this.routeItem = routeItem;
            this.opened = false;
            this.selected = false;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class RouteHolder extends RecyclerView.ViewHolder {
        private RouteItem routeItem;
        private SwipeRevealLayout layoutRootParent;
        private LinearLayout layoutRoot;
        private FrameLayout btnShare;
        private FrameLayout btnDelete;
        private FrameLayout btnFavorite;
        private ConstraintLayout layoutNoData;
        private ImageView imgFav;
        private ImageView imgDelete;
        private ImageView btnMenu;
        private ImageView imgMap;
        private TextView txtTitle, txtDuration, txtDistance, txtConsumption, txtConsumptionAvg,
                txtDrivingTechnique;
        private View viewPadding;
        private RouteItemInList routeItemInList;
        private boolean hasGpx;

        RouteHolder(View itemView) {
            super(itemView);
            layoutRootParent = itemView.findViewById(R.id.row_route_layoutRootParent);
            layoutRoot = itemView.findViewById(R.id.row_route_layoutRoot);
            btnShare = itemView.findViewById(R.id.row_route_btnShare);
            btnDelete = itemView.findViewById(R.id.row_route_btnDelete);
            btnFavorite = itemView.findViewById(R.id.row_route_btnFavorite);
            layoutNoData = itemView.findViewById(R.id.row_route_layoutNoData);
            imgDelete = itemView.findViewById(R.id.row_route_imgDelete);
            btnMenu = itemView.findViewById(R.id.row_route_btnMenu);
            txtTitle = itemView.findViewById(R.id.row_route_txtTitle);
            txtDuration = itemView.findViewById(R.id.row_route_txtDuration);
            txtDistance = itemView.findViewById(R.id.row_route_txtDistance);
            txtConsumption = itemView.findViewById(R.id.row_route_txtConsumption);
            txtConsumptionAvg = itemView.findViewById(R.id.row_route_txtConsumptionAvg);
            txtDrivingTechnique = itemView.findViewById(R.id.row_route_txtDrivingTechnique);
            imgFav = itemView.findViewById(R.id.imgFav);
            imgMap = itemView.findViewById(R.id.row_route_imgMap);

            viewPadding = itemView.findViewById(R.id.row_route_viewPadding);

            btnMenu.setOnClickListener(view -> listener.onMenu(getAdapterPosition()));

            btnShare.setOnClickListener(view -> {
                if (hasGpx) {
                    openShareRoute(routeItem);
                }
            });

            btnDelete.setOnClickListener(view -> {
                routeItemInList.selected = !routeItemInList.selected;
                manageButtonDeleteUI(this);
            });

            btnFavorite.setOnClickListener(view -> {
                if (hasGpx) {
                    manageFavoriteAction(this, routeItem);
                }
            });

            layoutRoot.setOnClickListener(v -> {
                if (hasGpx) {
                    listener.onRouteItemSelected(getAdapterPosition());
                }
            });
        }
    }
}