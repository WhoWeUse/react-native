/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.views.picker;

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewDefaults;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.picker.events.PickerItemSelectEvent;
import com.facebook.react.views.text.ReactFontManager;

import static com.facebook.react.views.text.ReactTextShadowNode.UNSET;

/**
 * {@link ViewManager} for the {@link ReactPicker} view. This is abstract because the
 * {@link Spinner} doesn't support setting the mode (dropdown/dialog) outside the constructor, so
 * that is delegated to the separate {@link ReactDropdownPickerManager} and
 * {@link ReactDialogPickerManager} components. These are merged back on the JS side into one
 * React component.
 */
public abstract class ReactPickerManager extends SimpleViewManager<ReactPicker> {

  @ReactProp(name = "items")
  public void setItems(ReactPicker view, @Nullable ReadableArray items) {
    if (items != null) {
      ReadableMap[] data = new ReadableMap[items.size()];
      for (int i = 0; i < items.size(); i++) {
        data[i] = items.getMap(i);
      }
      ReactPickerAdapter adapter = new ReactPickerAdapter(view.getContext(), data);
      assignTextStyle(view, adapter);
      view.setAdapter(adapter);
    } else {
      view.setAdapter(null);
    }
  }

  private void assignTextStyle(ReactPicker view, ReactPickerAdapter adapter) {
      adapter.setPrimaryTextColor(view.getPrimaryColor());
      adapter.setTypeface(view.getTypeface(), view.getTypefaceStyle());
      adapter.setTextSize(view.getTextSize());
  }

  @ReactProp(name = ViewProps.COLOR, customType = "Color")
  public void setColor(ReactPicker view, @Nullable Integer color) {
    view.setPrimaryColor(color);
    view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

    ReactPickerAdapter adapter = (ReactPickerAdapter) view.getAdapter();
    if (adapter != null) {
      adapter.setPrimaryTextColor(color);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////

  @ReactProp(name = ViewProps.FONT_SIZE, defaultFloat = ViewDefaults.FONT_SIZE_SP)
  public void setFontSize(ReactPicker view, float fontSize) {
    view.setTextSize((int) Math.ceil(PixelUtil.toPixelFromSP(fontSize)));

    ReactPickerAdapter adapter = (ReactPickerAdapter) view.getAdapter();
    if (adapter != null) {
      adapter.setTextSize(view.getTextSize());
    }
  }

  @ReactProp(name = ViewProps.FONT_FAMILY)
  public void setFontFamily(ReactPicker view, String fontFamily) {
    int style = Typeface.NORMAL;
    if (view.getTypeface() != null) {
      style = view.getTypeface().getStyle();
    }
    Typeface newTypeface = ReactFontManager.getInstance().getTypeface(
            fontFamily,
            style,
            view.getContext().getAssets());
    view.setTypeface(newTypeface);

    ReactPickerAdapter adapter = (ReactPickerAdapter) view.getAdapter();
    if (adapter != null) {
      adapter.setTypeface(view.getTypeface(), view.getTypefaceStyle());
    }
  }
  /**
   /* This code was taken from the method setFontWeight of the class ReactTextShadowNode
   /* TODO: Factor into a common place they can both use
   */
  @ReactProp(name = ViewProps.FONT_WEIGHT)
  public void setFontWeight(ReactPicker view, @Nullable String fontWeightString) {
    int fontWeightNumeric = fontWeightString != null ?
            parseNumericFontWeight(fontWeightString) : -1;
    int fontWeight = UNSET;
    if (fontWeightNumeric >= 500 || "bold".equals(fontWeightString)) {
      fontWeight = Typeface.BOLD;
    } else if ("normal".equals(fontWeightString) ||
            (fontWeightNumeric != -1 && fontWeightNumeric < 500)) {
      fontWeight = Typeface.NORMAL;
    }
    Typeface currentTypeface = view.getTypeface();
    if (currentTypeface == null) {
      currentTypeface = Typeface.DEFAULT;
    }
    if (fontWeight != currentTypeface.getStyle()) {
      view.setTypeface(currentTypeface, fontWeight);
    }

    ReactPickerAdapter adapter = (ReactPickerAdapter) view.getAdapter();
    if (adapter != null) {
      adapter.setTypeface(view.getTypeface(), view.getTypefaceStyle());
    }
  }

  /**
   /* This code was taken from the method setFontStyle of the class ReactTextShadowNode
   /* TODO: Factor into a common place they can both use
   */
  @ReactProp(name = ViewProps.FONT_STYLE)
  public void setFontStyle(ReactPicker view, @Nullable String fontStyleString) {
    int fontStyle = UNSET;
    if ("italic".equals(fontStyleString)) {
      fontStyle = Typeface.ITALIC;
    } else if ("normal".equals(fontStyleString)) {
      fontStyle = Typeface.NORMAL;
    }

    Typeface currentTypeface = view.getTypeface();
    if (currentTypeface == null) {
      currentTypeface = Typeface.DEFAULT;
    }
    if (fontStyle != currentTypeface.getStyle()) {
      view.setTypeface(currentTypeface, fontStyle);
    }

    ReactPickerAdapter adapter = (ReactPickerAdapter) view.getAdapter();
    if (adapter != null) {
      adapter.setTypeface(view.getTypeface(), view.getTypefaceStyle());
    }
  }

  /**
   * This code was taken from the method parseNumericFontWeight of the class ReactTextShadowNode
   * TODO: Factor into a common place they can both use
   *
   * Return -1 if the input string is not a valid numeric fontWeight (100, 200, ..., 900), otherwise
   * return the weight.
   */
  private static int parseNumericFontWeight(String fontWeightString) {
    // This should be much faster than using regex to verify input and Integer.parseInt
    return fontWeightString.length() == 3 && fontWeightString.endsWith("00")
            && fontWeightString.charAt(0) <= '9' && fontWeightString.charAt(0) >= '1' ?
            100 * (fontWeightString.charAt(0) - '0') : -1;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////

  @ReactProp(name = "prompt")
  public void setPrompt(ReactPicker view, @Nullable String prompt) {
    view.setPrompt(prompt);
  }

  @ReactProp(name = ViewProps.ENABLED, defaultBoolean = true)
  public void setEnabled(ReactPicker view, boolean enabled) {
    view.setEnabled(enabled);
  }

  @ReactProp(name = "selected")
  public void setSelected(ReactPicker view, int selected) {
    view.setStagedSelection(selected);
  }

  @Override
  protected void onAfterUpdateTransaction(ReactPicker view) {
    super.onAfterUpdateTransaction(view);
    view.updateStagedSelection();
  }

  @Override
  protected void addEventEmitters(
      final ThemedReactContext reactContext,
      final ReactPicker picker) {
    picker.setOnSelectListener(
            new PickerEventEmitter(
                    picker,
                    reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher()));
  }

  private static class ReactPickerAdapter extends ArrayAdapter<ReadableMap> {

    private final LayoutInflater mInflater;
    private @Nullable Integer mPrimaryTextColor;
    private @Nullable Typeface mTypeface;
    private @Nullable Integer mTypefaceStyle;
    private @Nullable Integer mTextSize;

    private int mTopPadding;
    private int mLeftPadding;
    private int mBottomPadding;
    private int mRightPadding;

    public ReactPickerAdapter(Context context, ReadableMap[] data) {
      super(context, 0, data);

      mInflater = (LayoutInflater) Assertions.assertNotNull(
          context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return getView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return getView(position, convertView, parent, true);
    }

    private View getView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
      ReadableMap item = getItem(position);

      if (convertView == null) {
        int layoutResId = isDropdown
            ? android.R.layout.simple_spinner_dropdown_item
            : android.R.layout.simple_spinner_item;
        convertView = mInflater.inflate(layoutResId, parent, false);

        mTopPadding = convertView.getPaddingTop();
        mLeftPadding = convertView.getPaddingLeft();
        mBottomPadding = convertView.getPaddingBottom();
        mRightPadding = convertView.getPaddingRight();
      }

      TextView textView = (TextView) convertView;
      textView.setText(item.getString("label"));
      if (!isDropdown && mPrimaryTextColor != null) {
        textView.setTextColor(mPrimaryTextColor);
        convertView.setPadding((int) PixelUtil.toPixelFromSP(4), mTopPadding, mRightPadding, mBottomPadding);
      } else if (item.hasKey("color") && !item.isNull("color")) {
        textView.setTextColor(item.getInt("color"));
        convertView.setPadding(mLeftPadding, mTopPadding, mRightPadding, mBottomPadding);
      }

      if(mTypeface != null) {
        if(mTypefaceStyle != null) {
          textView.setTypeface(mTypeface, mTypefaceStyle);
        } else {
          textView.setTypeface(mTypeface);
        }
      }

      if(mTextSize != null) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
      }

      return convertView;
    }

    public void setPrimaryTextColor(@Nullable Integer primaryTextColor) {
      mPrimaryTextColor = primaryTextColor;
      notifyDataSetChanged();
    }

    public void setTextSize(@Nullable Integer textSize) {
      mTextSize = textSize;
      notifyDataSetChanged();
    }

    public void setTypeface(Typeface typeface, @Nullable Integer typefaceStyle) {
      mTypeface = typeface;
      mTypefaceStyle = typefaceStyle;
      notifyDataSetChanged();
    }
  }

  private static class PickerEventEmitter implements ReactPicker.OnSelectListener {

    private final ReactPicker mReactPicker;
    private final EventDispatcher mEventDispatcher;

    public PickerEventEmitter(ReactPicker reactPicker, EventDispatcher eventDispatcher) {
      mReactPicker = reactPicker;
      mEventDispatcher = eventDispatcher;
    }

    @Override
    public void onItemSelected(int position) {
      mEventDispatcher.dispatchEvent( new PickerItemSelectEvent(
              mReactPicker.getId(), position));
    }
  }
}
