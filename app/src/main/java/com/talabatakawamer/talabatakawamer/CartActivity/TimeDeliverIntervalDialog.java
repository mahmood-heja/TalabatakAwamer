package com.talabatakawamer.talabatakawamer.CartActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.talabatakawamer.talabatakawamer.R;

import java.util.Calendar;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class TimeDeliverIntervalDialog extends Dialog implements View.OnClickListener {


    public TimeDeliverIntervalDialog(@NonNull Context context) {
        super(context);
    }

    TextView title;
    RadioGroup radioGroup;
    EditText phoneNum_et;
    String timeSelectedToDeliver = "not Yet";
    String totalPrice, dayOfDelivery;
    OnClickConfirmButton onClickConfirmButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.time_deliver_dialog);

        ((TextView) findViewById(R.id.totalPrice_tv)).setText(totalPrice);
        title = findViewById(R.id.title);
        radioGroup = findViewById(R.id.timeInterval_rg);
        phoneNum_et = findViewById(R.id.phoneNum);

        // get hour in 24 format

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int i_min = Calendar.getInstance().get(Calendar.MINUTE);

        // add prefix zero if min is less than 10 if first condition is not true
        String min;
        if (hour >= 16 || hour < 8)
            min = "00";
        else
            min = i_min < 10 ? "0" + i_min : i_min + "";

        //last hour to request  is 4PM
        // startInterval is begin from after 1 hour of time of request
        int startInterval = hour >= 16 || hour < 8 ? 9 : hour + 1;
        dayOfDelivery = hour >= 16 ? "TOMORROW" : "TODAY";
        if (dayOfDelivery.equals("TOMORROW"))
            title.setText("إختر فترة التوصيل من يوم الغد");

        //last hour to deliver is 7PM
        for (int i = 0; startInterval < 19; i++) {
            RadioButton radioButton = CreateRadioButton();
            if (i == 0) {
                //first interval range is two hour
                String from = String.valueOf(startInterval > 12 ? startInterval - 12 : startInterval) + ":" + min + " " + (startInterval >= 12 ? "PM" : "AM");
                startInterval += 2;
                String to = String.valueOf(startInterval > 12 ? startInterval - 12 : startInterval) + ":" + min + " " + (startInterval >= 12 ? "PM" : "AM");
                radioButton.setText(from + " - " + to);
            } else {
                //after first interval range is one hour
                String from = String.valueOf(startInterval > 12 ? startInterval - 12 : startInterval) + ":" + (i == 1 ? min : "00") + " " + (startInterval >= 12 ? "PM" : "AM");
                startInterval += 1;
                String to = String.valueOf(startInterval > 12 ? startInterval - 12 : startInterval) + ":00 " + (startInterval >= 12 ? "PM" : "AM");
                radioButton.setText(from + " - " + to);
            }

            radioGroup.addView(radioButton);
        }


        //btn cancel and confirm
        findViewById(R.id.cancel_action).setOnClickListener(v -> dismiss());
        findViewById(R.id.confirm_action).setOnClickListener(v -> {
            if (timeSelectedToDeliver.equals("not Yet")) {
                YoYo.with(Techniques.Shake).playOn(radioGroup);
                title.setText("إختر فترة التوصيل بالبداية");
            } else if (phoneNum_et.getText().toString().length() != 10 || !phoneNum_et.getText().toString().startsWith("07")) {
                YoYo.with(Techniques.Shake).playOn(phoneNum_et);
                phoneNum_et.setError("أدخل رقم هاتف صالح");
            } else {
                if (onClickConfirmButton != null)
                    onClickConfirmButton.onClick(timeSelectedToDeliver, phoneNum_et.getText().toString());
            }
        });

    }

    // radio btn Listener
    @Override
    public void onClick(View v) {

        timeSelectedToDeliver = ((RadioButton) v).getText().toString();
    }

    private RadioButton CreateRadioButton() {
        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setLayoutParams(new RadioGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        int padding = (int) convertDpToPixel(5);
        radioButton.setPadding(padding, padding, padding, padding);
        radioButton.setTextSize(spToPx(14));
        radioButton.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));

        radioButton.setOnClickListener(this);
        return radioButton;
    }


    public float convertDpToPixel(float dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public int spToPx(float sp) {
        return (int) (sp * getContext().getResources().getDisplayMetrics().scaledDensity) / 2;
    }

    public TimeDeliverIntervalDialog setConfirmButton(OnClickConfirmButton listener) {
        onClickConfirmButton = listener;
        return this;
    }

    public TimeDeliverIntervalDialog setMeesage(String totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    interface OnClickConfirmButton {
        void onClick(String interval, String phoneNum);
    }

}
