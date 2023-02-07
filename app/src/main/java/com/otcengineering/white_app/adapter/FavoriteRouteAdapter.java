package com.otcengineering.white_app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.otcengineering.white_app.R;
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

public class FavoriteRouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface RouteListener {
        void onRouteItemSelected(int position);

        void onMenu(int position);

        void onUnfavorite();
    }

    private Context context;
    private List<RouteItemInList> routeItemInLists = new ArrayList<>();
    private RouteListener listener;

    public FavoriteRouteAdapter(Context context, RouteListener listener) {
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
        final RouteItemInList routeItemInList = routeItemInLists.get(position);
        final RouteItem routeItem = routeItemInList.routeItem;

        itemHolder.layoutRootParent.setLockDrag(true);

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

        if (routeItem.getTitle().isEmpty()) {
            itemHolder.txtTitle.setText(String.format("%sh", DateUtils.utcToString(routeItem.getDateStart(), "dd/MM/yyyy - HH:mm:ss")));
        } else {
            itemHolder.txtTitle.setText(routeItem.getTitle());
        }

        itemHolder.txtDuration.setText(routeItem.getDurationInMinsFormatted());
        itemHolder.txtDistance.setText(String.format(Locale.US, "%.1f km", routeItem.getDistanceInKms()));
        itemHolder.txtConsumption.setText(String.format(Locale.US, "%.2f l", routeItem.getConsumption()));
        itemHolder.txtConsumptionAvg.setText(String.format(Locale.US, "%.1f km/l", routeItem.getConsumptionAvg()));
        itemHolder.txtDrivingTechnique.setText(String.format(Locale.US, "%.1f", routeItem.getDrivingTechnique() / 10));

        if (routeItem.isFavorite()) {
            itemHolder.imgFav.setImageDrawable(context.getResources().getDrawable(R.drawable.my_routes_icons_2));
        } else {
            itemHolder.imgFav.setImageDrawable(context.getResources().getDrawable(R.drawable.my_routes_icons_3));
        }

        manageButtonDeleteUI(itemHolder, routeItemInList);

        manageEditModeUI(itemHolder, routeItemInList);

        itemHolder.btnMenu.setOnClickListener(view -> listener.onMenu(itemHolder.getAdapterPosition()));

        itemHolder.btnDelete.setOnClickListener(view -> {
            routeItemInList.selected = !routeItemInList.selected;
            manageButtonDeleteUI(itemHolder, routeItemInList);
        });

        itemHolder.btnFavorite.setOnClickListener(view -> manageFavoriteAction(itemHolder, routeItem));

        itemHolder.layoutRoot.setOnClickListener(v -> listener.onRouteItemSelected(itemHolder.getAdapterPosition()));

        itemHolder.viewPadding.setVisibility(position == getItemCount() - 1 ? View.VISIBLE : View.GONE);
    }

    private void manageFavoriteAction(final RouteHolder itemHolder, RouteItem routeItem) {
        @SuppressLint("StaticFieldLeak")
        AddOrRemoveFavoriteTask addOrRemoveFavoriteTask = new AddOrRemoveFavoriteTask(routeItem.getId(), routeItem.isFavorite()) {
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
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
        listener.onUnfavorite();
    }

    private void manageEditModeUI(RouteHolder itemHolder, RouteItemInList routeItemInList) {
        if (routeItemInList.opened) {
            itemHolder.layoutRootParent.open(true);
        } else {
            itemHolder.layoutRootParent.close(true);
        }
    }

    private void manageButtonDeleteUI(RouteHolder itemHolder, RouteItemInList routeItemInList) {
        boolean selected = routeItemInList.selected;
        Drawable background = context.getDrawable(selected ? R.drawable.circle_red_background : R.drawable.circle_background);
        itemHolder.btnDelete.setBackground(background);
        int imageResource = selected ? R.drawable.check_white : R.drawable.my_routes_icons_27;
        itemHolder.imgDelete.setImageResource(imageResource);
    }

    public RouteItem getData(int position) {
        return routeItemInLists.get(position).routeItem;
    }

    public void clearItems() {
        routeItemInLists.clear();
    }

    public void addItems(List<RouteItem> routeItems) {
        try {
            for (RouteItem routeItem : routeItems) {
                this.routeItemInLists.add(new RouteItemInList(routeItem));
            }
        } catch (ClassCastException cce) {
            cce.printStackTrace();
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

        RouteItemInList(RouteItem routeFavourite) {
            this.routeItem = routeFavourite;
            this.opened = false;
            this.selected = false;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected class RouteHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout layoutRootParent;
        private LinearLayout layoutRoot;
        private FrameLayout btnDelete;
        private ImageView imgDelete;
        private FrameLayout btnFavorite;
        private ImageView imgFav;
        private ImageView btnMenu;
        private ConstraintLayout layoutNoData;

        private ImageView imgMap;
        private TextView txtTitle, txtDuration, txtDistance, txtConsumption, txtConsumptionAvg,
                txtDrivingTechnique;
        private View viewPadding;


        RouteHolder(View itemView) {
            super(itemView);
            layoutRootParent = itemView.findViewById(R.id.row_route_layoutRootParent);
            layoutRoot = itemView.findViewById(R.id.row_route_layoutRoot);
            btnFavorite = itemView.findViewById(R.id.row_route_btnFavorite);
            btnDelete = itemView.findViewById(R.id.row_route_btnDelete);
            imgDelete = itemView.findViewById(R.id.row_route_imgDelete);
            btnMenu = itemView.findViewById(R.id.row_route_btnMenu);
            imgMap = itemView.findViewById(R.id.row_route_imgMap);
            txtTitle = itemView.findViewById(R.id.row_route_txtTitle);
            txtDuration = itemView.findViewById(R.id.row_route_txtDuration);
            txtDistance = itemView.findViewById(R.id.row_route_txtDistance);
            txtConsumption = itemView.findViewById(R.id.row_route_txtConsumption);
            txtConsumptionAvg = itemView.findViewById(R.id.row_route_txtConsumptionAvg);
            txtDrivingTechnique = itemView.findViewById(R.id.row_route_txtDrivingTechnique);
            imgFav = itemView.findViewById(R.id.imgFav);
            viewPadding = itemView.findViewById(R.id.row_route_viewPadding);
            layoutNoData = itemView.findViewById(R.id.row_route_layoutNoData);
            itemView.findViewById(R.id.row_route_btnShare).setVisibility(View.GONE);
        }
    }

}