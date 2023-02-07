package com.otcengineering.white_app.payment.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.fragments.BaseFragment;
import com.otcengineering.white_app.payment.activity.CartActivity;
import com.otcengineering.white_app.activities.ItemDetailsActivity;
import com.otcengineering.white_app.payment.adapter.ShoppingAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingFragment extends BaseFragment {
    private ArrayList<PaymentItem> mItems;
    private SearchView mSearch;
    private ShoppingAdapter mAdapter;
    private RecyclerView mItemList;
    private Button mViewShoppingCart, mPrevious, mNext;
    private TextView mNoItemsFound, mItemsNumber;
    private static int sPage = 1;

    public ShoppingFragment() {
        mItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        findViews(view);
        setEvents();
        getItems(sPage);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getItemsQuantity();
    }

    private void getItemsQuantity() {
        TypedTask<Payment.ItemTotalQuantity> task = new TypedTask<>(Endpoints.Payment.ITEMS_QUANTITIY, null, true, Payment.ItemTotalQuantity.class, new TypedCallback<Payment.ItemTotalQuantity>() {
            @Override
            public void onSuccess(@Nonnull Payment.ItemTotalQuantity value) {
                mItemsNumber.setText(String.format(Locale.US, "%d", value.getQuantity()));
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                mItemsNumber.setText(String.format(Locale.US, "%d", 0));
            }
        });
        task.execute();
    }

    private void getItems(int page) {
        Payment.Items items = Payment.Items.newBuilder().setPage(page).build();
        TypedTask<Payment.ItemsResponse> getItems = new TypedTask<>(Endpoints.Payment.ITEMS, items, true, Payment.ItemsResponse.class, new TypedCallback<Payment.ItemsResponse>() {
            @Override
            public void onSuccess(@Nonnull Payment.ItemsResponse value) {
                mAdapter.clearItems();
                mAdapter.update();
                mItemList.getLayoutManager().scrollToPosition(0);
                for (Payment.Item it : value.getItemsList()) {
                    mAdapter.addItem(new PaymentItem(it));
                }
                mAdapter.setFilter("");
                mAdapter.update();

                if (sPage > 1) {
                    mPrevious.setEnabled(true);
                } else {
                    mPrevious.setEnabled(false);
                }
                if (value.getPages() > sPage) {
                    mNext.setEnabled(true);
                } else {
                    mNext.setEnabled(false);
                }
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                showCustomDialogError(CloudErrorHandler.handleError(status));
            }
        });
        getItems.execute();
    }

    private void setEvents() {
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByText(newText);
                return true;
            }
        });

        mItemList.setAdapter(mAdapter);
        mItemList.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration did = new DividerItemDecoration(mItemList.getContext(), DividerItemDecoration.VERTICAL);
        did.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.separator_shopping));
        mItemList.addItemDecoration(did);

        mViewShoppingCart.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CartActivity.class));
        });

        mPrevious.setOnClickListener(v -> getItems(--sPage));
        mNext.setOnClickListener(v -> getItems(++sPage));
    }

    private void filterByText(String filter) {
        mNoItemsFound.setVisibility(mAdapter.setFilter(filter) ? View.VISIBLE : View.GONE);
        mAdapter.update();
    }

    private void findViews(View root) {
        mSearch = root.findViewById(R.id.search);

        mAdapter = new ShoppingAdapter(this);
        mItemList = root.findViewById(R.id.itemList);
        mViewShoppingCart = root.findViewById(R.id.viewShoppingCart);
        mNoItemsFound = root.findViewById(R.id.noItemsFound);
        mPrevious = root.findViewById(R.id.previous);
        mNext = root.findViewById(R.id.next);
        mItemsNumber = root.findViewById(R.id.itemsNumber);
    }

    public void showItem(PaymentItem item) {
        Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
        intent.putExtra("Item", Utils.getGson().toJson(item));
        startActivity(intent);
    }
}
