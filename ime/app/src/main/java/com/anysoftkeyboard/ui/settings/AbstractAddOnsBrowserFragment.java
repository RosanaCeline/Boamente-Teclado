/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.addons.ui.AddOnStoreSearchController;
import com.anysoftkeyboard.addons.ui.AddOnStoreSearchView;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.evendanan.pixel.UiUtils;

public abstract class AbstractAddOnsBrowserFragment<E extends AddOn> extends Fragment {

  private final Set<String> mEnabledAddOnsIds = new HashSet<>();
  @NonNull private final String mLogTag;
  @StringRes private final int mFragmentTitleResId;
  private final boolean mIsSingleSelection;
  private final boolean mSimulateTyping;
  private final boolean mHasTweaksOption;
  private AddOnsFactory<E> mFactory;
  private final List<E> mAllAddOns = new ArrayList<>();

  private final ItemTouchHelper mRecyclerViewItemTouchHelper;
  private RecyclerView mRecyclerView;
  @Nullable private DemoAnyKeyboardView mSelectedKeyboardView;

  @Nullable private AddOnStoreSearchController mMarketSearchController;
  private int mColumnsCount = 2;

  protected AbstractAddOnsBrowserFragment(
      @NonNull String logTag,
      @StringRes int fragmentTitleResId,
      boolean isSingleSelection,
      boolean simulateTyping,
      boolean hasTweaksOption) {
    this(logTag, fragmentTitleResId, isSingleSelection, simulateTyping, hasTweaksOption, 0);
  }

  protected AbstractAddOnsBrowserFragment(
      @NonNull String logTag,
      @StringRes int fragmentTitleResId,
      boolean isSingleSelection,
      boolean simulateTyping,
      boolean hasTweaksOption,
      final int itemDragDirectionFlags) {
    if (isSingleSelection && (itemDragDirectionFlags != 0)) {
      throw new IllegalStateException(
          "Does not support drag operations (and order) with a single selection list");
    }

    mRecyclerViewItemTouchHelper =
        new ItemTouchHelper(createItemTouchCallback(itemDragDirectionFlags));
    mLogTag = logTag;
    mIsSingleSelection = isSingleSelection;
    mSimulateTyping = simulateTyping;
    mHasTweaksOption = hasTweaksOption;
    if (mSimulateTyping && !mIsSingleSelection) {
      throw new IllegalStateException(
          "only supporting simulated-typing in single-selection setup!");
    }
    mFragmentTitleResId = fragmentTitleResId;
    setHasOptionsMenu(mHasTweaksOption || getMarketSearchTitle() != 0);
  }

  @NonNull private ItemTouchHelper.SimpleCallback createItemTouchCallback(int itemDragDirectionFlags) {
    return new ItemTouchHelper.SimpleCallback(itemDragDirectionFlags, 0) {

      @Override
      public int getDragDirs(
          @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getBindingAdapterPosition() >= mAllAddOns.size()) {
          // this is the case where the item dragged is the Market row.
          return 0;
        }
        return super.getDragDirs(recyclerView, viewHolder);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean onMove(
          @NonNull RecyclerView recyclerView,
          @NonNull RecyclerView.ViewHolder viewHolder,
          @NonNull RecyclerView.ViewHolder target) {
        final int to = target.getBindingAdapterPosition();
        if (to >= mAllAddOns.size()) {
          // this is the case where the item is dragged AFTER the Market row.
          // we won't allow
          return false;
        }

        final int from = viewHolder.getBindingAdapterPosition();

        E temp = ((KeyboardAddOnViewHolder) viewHolder).mAddOn;
        // anything that is dragged, must be enabled
        mEnabledAddOnsIds.add(temp.getId());
        mFactory.setAddOnEnabled(temp.getId(), true);
        Collections.swap(mAllAddOns, from, to);
        recyclerView.getAdapter().notifyItemMoved(from, to);
        // making sure `to` is visible
        recyclerView.scrollToPosition(to);

        if (!mIsSingleSelection) {
          ((AddOnsFactory.MultipleAddOnsFactory<E>) mFactory).setAddOnsOrder(mAllAddOns);
        }

        return true;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
    };
  }

  @NonNull protected abstract AddOnsFactory<E> getAddOnFactory();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFactory = getAddOnFactory();
    if (mIsSingleSelection && !(mFactory instanceof AddOnsFactory.SingleAddOnsFactory)) {
      throw new IllegalStateException(
          "In single-selection state, factor must be SingleAddOnsFactory!");
    }
    if (!mIsSingleSelection && !(mFactory instanceof AddOnsFactory.MultipleAddOnsFactory)) {
      throw new IllegalStateException(
          "In multi-selection state, factor must be MultipleAddOnsFactory!");
    }

    mColumnsCount = getResources().getInteger(R.integer.add_on_items_columns);
  }

  @Override
  public View onCreateView(
      LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
    final var keyword = getMarketSearchKeyword();
    if (keyword != null) {
      mMarketSearchController =
          new AddOnStoreSearchController(requireActivity(), getMarketSearchKeyword());
    }
    return paramLayoutInflater.inflate(
        mIsSingleSelection
            ? R.layout.add_on_browser_single_selection_layout
            : R.layout.add_on_browser_multiple_selection_layout,
        paramViewGroup,
        false);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mMarketSearchController != null) mMarketSearchController.dismiss();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Context appContext = requireContext().getApplicationContext();

    mRecyclerView = view.findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(false);
    mRecyclerView.setLayoutManager(createLayoutManager(appContext));
    mRecyclerView.setAdapter(new DemoKeyboardAdapter());
    mRecyclerViewItemTouchHelper.attachToRecyclerView(mRecyclerView);

    if (mIsSingleSelection) {
      mSelectedKeyboardView = view.findViewById(R.id.demo_keyboard_view);
      if (mSimulateTyping) {
        mSelectedKeyboardView.setSimulatedTypingText("bem-vindo ao anysoftkeyboard");
      }
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.add_on_selector_menu, menu);
    menu.findItem(R.id.add_on_market_search_menu_option).setVisible(getMarketSearchTitle() != 0);
    menu.findItem(R.id.tweaks_menu_option).setVisible(mHasTweaksOption);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.tweaks_menu_option:
        onTweaksOptionSelected();
        return true;
      case R.id.add_on_market_search_menu_option:
        if (mMarketSearchController != null) mMarketSearchController.searchForAddOns();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void onTweaksOptionSelected() {}

  @Override
  public void onStart() {
    super.onStart();
    // I need a mutable list.
    mAllAddOns.clear();
    mAllAddOns.addAll(mFactory.getAllAddOns());

    mEnabledAddOnsIds.clear();
    mEnabledAddOnsIds.addAll(mFactory.getEnabledIds());

    if (mSelectedKeyboardView != null) {
      applyAddOnToDemoKeyboardView(mFactory.getEnabledAddOn(), mSelectedKeyboardView);
    }

    Logger.d(
        mLogTag,
        "Got %d available addons and %d enabled addons",
        mAllAddOns.size(),
        mEnabledAddOnsIds.size());
    mRecyclerView.getAdapter().notifyDataSetChanged();
    UiUtils.setActivityTitle(this, mFragmentTitleResId);
  }

  @NonNull private RecyclerView.LayoutManager createLayoutManager(@NonNull Context appContext) {
    GridLayoutManager manager =
        new GridLayoutManager(appContext, mColumnsCount, LinearLayoutManager.VERTICAL, false);
    manager.setSpanSizeLookup(
        new GridLayoutManager.SpanSizeLookup() {
          @Override
          public int getSpanSize(int position) {
            if (position == mAllAddOns.size()) {
              return mColumnsCount;
            } else {
              return 1;
            }
          }
        });

    return manager;
  }

  protected abstract void applyAddOnToDemoKeyboardView(
      @NonNull final E addOn, @NonNull final DemoAnyKeyboardView demoKeyboardView);

  @Nullable protected abstract String getMarketSearchKeyword();

  @StringRes
  protected abstract int getMarketSearchTitle();

  private class KeyboardAddOnViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener {
    private final DemoAnyKeyboardView mDemoKeyboardView;
    private final ImageView mAddOnEnabledView;
    private final TextView mAddOnTitle;
    private final TextView mAddOnDescription;
    private E mAddOn;

    public KeyboardAddOnViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      mDemoKeyboardView = itemView.findViewById(R.id.item_keyboard_view);
      mAddOnEnabledView = itemView.findViewById(R.id.enabled_image);
      mAddOnTitle = itemView.findViewById(R.id.title);
      mAddOnDescription = itemView.findViewById(R.id.subtitle);
      mAddOnTitle.setSelected(true);
    }

    private void bindToAddOn(@NonNull E addOn) {
      mAddOn = addOn;
      mAddOnTitle.setText(addOn.getName());
      mAddOnDescription.setText(addOn.getDescription());
      final boolean isEnabled = mEnabledAddOnsIds.contains(addOn.getId());
      mAddOnEnabledView.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
      mAddOnEnabledView.setImageResource(isEnabled ? R.drawable.ic_accept : R.drawable.ic_cancel);
      applyAddOnToDemoKeyboardView(addOn, mDemoKeyboardView);
    }

    @Override
    public void onClick(View v) {
      final boolean isEnabled = mEnabledAddOnsIds.contains(mAddOn.getId());
      if (mIsSingleSelection) {
        if (isEnabled) return; // already enabled
        final E previouslyEnabled = mFactory.getEnabledAddOn();
        final int previouslyEnabledIndex = mAllAddOns.indexOf(previouslyEnabled);

        mEnabledAddOnsIds.clear();
        mEnabledAddOnsIds.add(mAddOn.getId());
        // clicking in single selection mode, means ENABLED
        mFactory.setAddOnEnabled(mAddOn.getId(), true);
        if (mSelectedKeyboardView != null) {
          applyAddOnToDemoKeyboardView(mAddOn, mSelectedKeyboardView);
        }
        mRecyclerView.getAdapter().notifyItemChanged(previouslyEnabledIndex);
      } else {
        // clicking in multi-selection means flip
        if (isEnabled) {
          mEnabledAddOnsIds.remove(mAddOn.getId());
          mFactory.setAddOnEnabled(mAddOn.getId(), false);
        } else {
          mEnabledAddOnsIds.add(mAddOn.getId());
          mFactory.setAddOnEnabled(mAddOn.getId(), true);
        }
      }

      mRecyclerView.getAdapter().notifyItemChanged(getBindingAdapterPosition());
    }
  }

  private class DemoKeyboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;

    DemoKeyboardAdapter() {
      mLayoutInflater = LayoutInflater.from(getActivity());
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      if (viewType == 0) {
        View itemView = mLayoutInflater.inflate(R.layout.add_on_browser_view_item, parent, false);
        return new KeyboardAddOnViewHolder(itemView);
      } else {
        AddOnStoreSearchView searchView = new AddOnStoreSearchView(getActivity(), null);
        searchView.setTag(getMarketSearchKeyword());
        searchView.setTitle(getText(getMarketSearchTitle()));
        searchView.setSearchController(mMarketSearchController);
        return new RecyclerView.ViewHolder(searchView) {
          /*empty implementation*/
        };
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
      if (getItemViewType(position) == 0) {
        E addOn = mAllAddOns.get(position);
        ((KeyboardAddOnViewHolder) holder).bindToAddOn(addOn);
      }
    }

    @Override
    public int getItemViewType(int position) {
      if (position == mAllAddOns.size()) {
        return 1;
      } else {
        return 0;
      }
    }

    @Override
    public int getItemCount() {
      final int extra = getMarketSearchKeyword() != null ? 1 : 0;
      return mAllAddOns.size() + extra;
    }
  }
}
