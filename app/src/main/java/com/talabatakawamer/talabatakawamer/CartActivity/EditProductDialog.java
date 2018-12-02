package com.talabatakawamer.talabatakawamer.CartActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.talabatakawamer.talabatakawamer.R;
import com.talabatakawamer.talabatakawamer.CartActivity.AdapterCartRecycle;
import com.talabatakawamer.talabatakawamer.CartActivity.CartItem;

public class EditProductDialog extends Dialog implements View.OnClickListener {


    private final CartItem cartItem;
    private final int posOfItem;
    private final AdapterCartRecycle adapterCartRecycle;

    public EditProductDialog(@NonNull Context context, CartItem cartItem, int posOfItem, AdapterCartRecycle adapterCartRecycle) {
        super(context);
        this.cartItem = cartItem;
        this.posOfItem = posOfItem;
        this.adapterCartRecycle = adapterCartRecycle;
    }

    // first refer to (صنف اول ) && super refer to (صنف ثاني )
    public ImageView product_iv;
    public TextView priceFirst_tv, priceSuper_tv, name_tv, quantity;
    public RadioButton first_rb, super_rb;
    public RadioGroup radioGroup;
    public ImageButton plus_1, neg_1;

    public Button edit_btn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // this layout using too in #AdapterRecycleVegetables as card cartItem.vegetableItem
        setContentView(R.layout.card_vegetables_item);

        assert getWindow() != null;
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        edit_btn = findViewById(R.id.addToCart);
        quantity = findViewById(R.id.quantity);
        product_iv = findViewById(R.id.product_image);
        priceFirst_tv = findViewById(R.id.priceFirst);
        priceSuper_tv = findViewById(R.id.priceSuper);
        name_tv = findViewById(R.id.nameProduct);
        first_rb = findViewById(R.id.radio_first);
        super_rb = findViewById(R.id.radio_super);
        plus_1 = findViewById(R.id.plus1);
        neg_1 = findViewById(R.id.neg1);
        radioGroup = findViewById(R.id.radioGroup);

        Glide.with(getContext()).load(cartItem.vegetableItem.imageUrl).into(product_iv);

        name_tv.setText(cartItem.vegetableItem.name);

        // bind data type product available
        typeProductAvailable();

        int quantityType = cartItem.vegetableItem.quantityType;
        if (quantityType != 0)
            quantity.setText(cartItem.quantity + (quantityType == 1 || quantityType == 3 ? "كيلو " : ""));
        else
            quantity.setText(getStringInGram(cartItem.quantity));

        edit_btn.setText("تعديل");
        // remove drawable from btn
        edit_btn.setCompoundDrawables(null, null, null, null);
        edit_btn.setOnClickListener(this);

        // plus and mines btn
        plus_1.setOnClickListener(v -> {

            if (quantityType == 1 || quantityType == 3) {
                // quantityType =3 mean make jump value is 0.5 kg
                // quantityType =1 mean make jump value is 1 kg
                cartItem.quantity = quantityType == 1 ? ++cartItem.quantity : cartItem.quantity + 0.5;
                quantity.setText(cartItem.quantity + "كيلو ");
            } else if (quantityType == 2) {
                cartItem.quantity = ++cartItem.quantity;
                quantity.setText(cartItem.quantity + "");
            } else {
                // specific quantity when quantity type in gram
                String quantity_crr = quantity.getText().toString();

                switch (quantity_crr) {
                    case "0.0 كيلو":
                        cartItem.quantity = 0.25;
                        quantity.setText("ربع كيلو");
                        break;
                    case "ربع كيلو":
                        cartItem.quantity = 0.5;
                        quantity.setText("نصف كيلو");
                        break;
                    case "نصف كيلو":
                        cartItem.quantity = 1.0;
                        quantity.setText("كيلو");
                        break;
                    case "كيلو":
                        cartItem.quantity = 1.5;
                        quantity.setText("كيلو ونصف");
                        break;
                    case "كيلو ونصف":
                        cartItem.quantity = 2;
                        quantity.setText("2 كيلو");
                        break;
                }

            }

        });
        neg_1.setOnClickListener(v -> {


            if ((quantityType == 1 || quantityType == 3) && cartItem.quantity != 0) {
                // quantityType =3 mean make jump value is 0.5 kg
                // quantityType =1 mean make jump value is 1 kg
                cartItem.quantity = quantityType == 1 ? --cartItem.quantity : cartItem.quantity - 0.5;
                quantity.setText(cartItem.quantity + " كيلو ");
            } else if (quantityType == 2 && cartItem.quantity != 0) {
                cartItem.quantity = --cartItem.quantity;
                quantity.setText(cartItem.quantity + "");

            } else if (quantityType == 0) {
                // specific quantity when quantity type in gram
                String quantity_crr = quantity.getText().toString();

                switch (quantity_crr) {
                    case "ربع كيلو":
                        cartItem.quantity = 0;
                        quantity.setText("0.0 كيلو");
                        break;
                    case "نصف كيلو":
                        quantity.setText("ربع كيلو");
                        break;
                    case "كيلو":
                        cartItem.quantity = 0.5;
                        quantity.setText("نصف كيلو");
                        break;
                    case "كيلو ونصف":
                        cartItem.quantity = 1.0;
                        quantity.setText("كيلو");
                        break;
                    case "2 كيلو":
                        cartItem.quantity = 1.5;
                        quantity.setText("كيلو ونصف");
                        break;
                }

            }


        });

    }

    private String getStringInGram(double quantity) {
        if (quantity == 0.25) {
            return "ربع كيلو";
        } else if (quantity == 0.5) {
            return "نصف كيلو";
        } else if (quantity == 1.0) {
            return "كيلو";
        } else if (quantity == 1.5) {
            return "كيلو ونصف";
        } else if (quantity == 2.0) {
            return "2 كيلو";
        }

        return "0 كيلو";
    }

    @SuppressLint("SetTextI18n")
    private void typeProductAvailable() {
        // 0 for super,1 for (صنف اول), and 2 for both
        switch (cartItem.vegetableItem.type_available) {
            case 0:
                priceSuper_tv.setText(cartItem.vegetableItem.priceSuper + " قرش ");
                priceFirst_tv.setText("غير متوفر");

                first_rb.setBackgroundColor(getContext().getResources().getColor(R.color.grayTransparent));
                priceFirst_tv.setBackgroundColor(getContext().getResources().getColor(R.color.grayTransparent));
                super_rb.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                priceSuper_tv.setBackgroundColor(getContext().getResources().getColor(R.color.white));

                super_rb.setClickable(true);
                first_rb.setClickable(false);

                radioGroup.check(R.id.radio_super);

                break;
            case 1:
                priceFirst_tv.setText(cartItem.vegetableItem.priceFirst + " قرش ");
                priceSuper_tv.setText("غير متوفر");

                super_rb.setBackgroundColor(getContext().getResources().getColor(R.color.grayTransparent));
                priceSuper_tv.setBackgroundColor(getContext().getResources().getColor(R.color.grayTransparent));
                first_rb.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                priceFirst_tv.setBackgroundColor(getContext().getResources().getColor(R.color.white));

                super_rb.setClickable(false);
                first_rb.setClickable(true);

                radioGroup.check(R.id.radio_first);

                break;
            case 2:
                priceSuper_tv.setText(cartItem.vegetableItem.priceSuper + " قرش ");
                priceFirst_tv.setText(cartItem.vegetableItem.priceFirst + " قرش ");

                super_rb.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                priceSuper_tv.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                first_rb.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                priceFirst_tv.setBackgroundColor(getContext().getResources().getColor(R.color.white));

                if (cartItem.type.equals("صنف اول"))
                    radioGroup.check(R.id.radio_first);
                else
                    radioGroup.check(R.id.radio_super);

                break;
        }
    }

    // edit btn
    @Override
    public void onClick(View v) {

        if (cartItem.quantity == 0) {
            Toast.makeText(getContext(), "حدد الكمية قبل التعديل", Toast.LENGTH_LONG).show();
            return;
        }

        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_first) {
            cartItem.type = "صنف اول";
            cartItem.price=(cartItem.vegetableItem.priceFirst*cartItem.quantity)/100;
        } else {
            cartItem.type = "صنف سوبر";
            cartItem.price=(cartItem.vegetableItem.priceSuper*cartItem.quantity)/100;
        }

        // updated with total packaging price also
        adapterCartRecycle.updateCartItem(cartItem,posOfItem);

        dismiss();

    }
}
