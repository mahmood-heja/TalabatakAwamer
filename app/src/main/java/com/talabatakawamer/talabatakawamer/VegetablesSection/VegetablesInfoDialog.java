package com.talabatakawamer.talabatakawamer.VegetablesSection;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.talabatakawamer.talabatakawamer.R;

public class VegetablesInfoDialog extends Dialog {

    private final VegetableItem item;

    public VegetablesInfoDialog(@NonNull Context context, VegetableItem item) {
        super(context);
        this.item = item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.vegetables_info_dialog);

        ImageView product_iv = findViewById(R.id.product_image);
        TextView des_tv = findViewById(R.id.des);

        Glide.with(getContext()).load(item.imageUrl).into(product_iv);
        des_tv.setText(item.des);

        findViewById(R.id.dismiss).setOnClickListener(v -> dismiss());

    }
}
