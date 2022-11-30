package com.amk.me.view.BindingAdapter;

import android.util.Log;
import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingConversion;
import androidx.lifecycle.LiveData;

import com.amk.me.R;
import com.amk.me.model.models.User;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public abstract class MyBiding {

    @BindingAdapter("pictureProgress")
    public static void setProgressText(MaterialTextView view, Integer pictureUploadProgress) {
        String value =
                view.getContext().getString(R.string.progress) + ": " + pictureUploadProgress + "%";
        view.setText(value);
    }

    @BindingAdapter("android:text")
    public static void setUserFullName(MaterialTextView view, User user) {
        if (user != null)
            view.setText(user.getFull_name());
    }

    @BindingAdapter("setErrorText")
    public static void setErrorMessage(TextInputLayout view, Integer errorMessageId) {
        if (errorMessageId == 0)
            return;
        view.setError(view.getContext().getString(errorMessageId));
    }


    @BindingAdapter("myVisibility")
    public static void setMyVisibility(CircularProgressIndicator view, LiveData<Boolean> livedata) {
        if (Boolean.TRUE.equals(livedata.getValue())) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }

}
