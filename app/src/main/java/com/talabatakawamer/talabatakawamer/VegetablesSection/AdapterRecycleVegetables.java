package com.talabatakawamer.talabatakawamer.VegetablesSection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.talabatakawamer.talabatakawamer.R;
import com.google.gson.Gson;
import com.readystatesoftware.viewbadger.BadgeView;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.util.DisplayMetrics.DENSITY_LOW;

public class AdapterRecycleVegetables extends RecyclerView.Adapter<AdapterRecycleVegetables.ViewHolder> {

    private final Context context;
    private List<VegetableItem> items;
    private SharedPreferences prefs;
    // to save quantity value for every card
    private double[] quantity;
    // to save quantity of type gram
    private String[] quantity_st;
    // to save radio btn state for every card
    //value = 0 for not select yet ,
    // or =1 for #first_rb is checked,
    // or =2 for #super_rb  is checked
    private int[] typeSelected;
    private boolean[] inCart;
    private BadgeView badge;
    //Market Status
    private boolean marketIsOpen;
    private String closedMsg;

    public AdapterRecycleVegetables(Context context, List<VegetableItem> items, BadgeView badge) {

        this.context = context;
        this.items = items;
        this.badge = badge;

        // prepare sharedPreferences
        prefs = context.getSharedPreferences(context.getString(R.string.cart_sharePrefrance), MODE_PRIVATE);

        quantity = new double[items.size()];
        typeSelected = new int[items.size()];
        quantity_st = new String[items.size()];
        inCart = new boolean[items.size()];
        // fill array price with 0
        Arrays.fill(quantity, 1.0);
        Arrays.fill(quantity_st, "ربع كيلو");
        Arrays.fill(typeSelected, 0);
        Arrays.fill(inCart, false);
    }

    // to know if market open/closed
    protected void setMarketStatus(boolean isOpen, String closedMsg) {

        this.marketIsOpen = isOpen;
        this.closedMsg = closedMsg;
    }

    @NonNull
    @Override
    public AdapterRecycleVegetables.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_vegetables_item, parent, false);

        if (context.getResources().getDisplayMetrics().densityDpi == DENSITY_LOW) {
            view.setLayoutParams(new CardView.LayoutParams(dpToPx(130), CardView.LayoutParams.WRAP_CONTENT));
            ((CardView) view).setRadius(0);
        }
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterRecycleVegetables.ViewHolder holder, final int position) {

        VegetableItem item = items.get(position);
        Glide.with(context).load(item.imageUrl).into(holder.product_iv);

        if (item.quantityType != 0)
            holder.quantity.setText(quantity[position] + (item.quantityType == 1 || item.quantityType == 3 ? " كيلو " : ""));
        else
            holder.quantity.setText(quantity_st[position]);

        holder.name_tv.setText(item.name);
        holder.radioGroup.clearCheck();


        // check if the product already added to cart
        if (prefs.getBoolean(item.id + "_inCard", false)) {
            holder.addToCart.setText("تمت الإضافة");
            holder.addToCart.setBackgroundColor(context.getResources().getColor(R.color.gray));
            inCart[position] = true;
        } else {
            holder.addToCart.setText("أضف إلى السلة");
            holder.addToCart.setBackground(context.getResources().getDrawable(R.drawable.button_shape_shoping));
            inCart[position] = false;
        }


        // 0 for super,1 for (صنف اول), and 2 for both
        switch (item.type_available) {
            case 0:
                holder.priceSuper_tv.setText((int) item.priceSuper + " قرش ");
                holder.priceFirst_tv.setText("غير متوفر");

                holder.first_rb.setBackgroundColor(context.getResources().getColor(R.color.grayTransparent));
                holder.priceFirst_tv.setBackgroundColor(context.getResources().getColor(R.color.grayTransparent));
                holder.super_rb.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.priceSuper_tv.setBackgroundColor(context.getResources().getColor(R.color.white));

                holder.super_rb.setClickable(true);
                holder.first_rb.setClickable(false);

                holder.radioGroup.check(R.id.radio_super);

                break;
            case 1:
                holder.priceFirst_tv.setText((int) item.priceFirst + " قرش ");
                holder.priceSuper_tv.setText("غير متوفر");

                holder.super_rb.setBackgroundColor(context.getResources().getColor(R.color.grayTransparent));
                holder.priceSuper_tv.setBackgroundColor(context.getResources().getColor(R.color.grayTransparent));
                holder.first_rb.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.priceFirst_tv.setBackgroundColor(context.getResources().getColor(R.color.white));

                holder.super_rb.setClickable(false);
                holder.first_rb.setClickable(true);

                holder.radioGroup.check(R.id.radio_first);

                break;
            case 2:
                holder.priceSuper_tv.setText((int) item.priceSuper + " قرش ");
                holder.priceFirst_tv.setText((int) item.priceFirst + " قرش ");

                holder.super_rb.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.priceSuper_tv.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.first_rb.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.priceFirst_tv.setBackgroundColor(context.getResources().getColor(R.color.white));

                holder.super_rb.setClickable(true);
                holder.first_rb.setClickable(true);
                holder.radioGroup.check(R.id.radio_super);

                break;
        }

        // return state of radio btn after scroll
        // check btn by radio group class is better ,, don't ask why
        if (typeSelected[position] == 1)
            holder.radioGroup.check(R.id.radio_first);
        else if (typeSelected[position] == 2)
            holder.radioGroup.check(R.id.radio_super);

        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_first)
                typeSelected[position] = 1;
            else if (checkedId == R.id.radio_super)
                typeSelected[position] = 2;
        });

        //	0 for gram,1 for kg,2 for number
        holder.plus_1.setOnClickListener(v -> {

            if (item.quantityType == 1 || item.quantityType == 3) {
                // quantityType =3 mean make jump value is 0.5 kg
                // quantityType =1 mean make jump value is 1 kg
                quantity[position] = item.quantityType == 1 ? ++quantity[position] : quantity[position] + 0.5;
                holder.quantity.setText(quantity[position] + " كيلو ");
            } else if (item.quantityType == 2) {
                quantity[position] = ++quantity[position];
                holder.quantity.setText(quantity[position] + "");
            } else {
                // specific quantity when quantity type in gram
                String quantity_crr = quantity_st[position];

                switch (quantity_crr) {
                    case "ربع كيلو":
                        quantity_st[position] = "نصف كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "نصف كيلو":
                        quantity_st[position] = "كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "كيلو":
                        quantity_st[position] = "كيلو ونصف";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "كيلو ونصف":
                        quantity_st[position] = "2 كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                }

            }

            // click addToCart btn to edit item inside cart
            if (inCart[position]) {
                // set inCart[position] =false to avoid remove item from cart
                inCart[position] = false;
                // remove old information from cart
                editProduct(item, ItemProcess.editInsideCart);
                // insert new information in cart
                holder.addToCart.performClick();
            }

        });

        holder.neg_1.setOnClickListener(v -> {


            if ((item.quantityType == 1 && quantity[position] != 1) || (item.quantityType == 3 && quantity[position] != 0.5)) {
                // quantityType =3 mean make jump value is 0.5 kg
                // quantityType =1 mean make jump value is 1 kg
                quantity[position] = item.quantityType == 1 ? --quantity[position] : quantity[position] - 0.5;
                holder.quantity.setText(quantity[position] + " كيلو ");
            } else if (item.quantityType == 2 && quantity[position] != 1) {
                // cant select 0  when item inside card
                quantity[position] = --quantity[position];
                holder.quantity.setText(quantity[position] + "");

            } else if (item.quantityType == 0) {
                // specific quantity when quantity type in gram
                String quantity_crr = quantity_st[position];

                switch (quantity_crr) {
                    case "نصف كيلو":
                        quantity_st[position] = "ربع كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "كيلو":
                        quantity_st[position] = "نصف كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "كيلو ونصف":
                        quantity_st[position] = "كيلو";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                    case "2 كيلو":
                        quantity_st[position] = "كيلو ونصف";
                        holder.quantity.setText(quantity_st[position]);
                        break;
                }

            }

            // click addToCart btn to edit item inside cart
            if (inCart[position]) {
                // set inCart[position] =false to avoid remove item from cart
                inCart[position] = false;
                editProduct(item, ItemProcess.editInsideCart);
                holder.addToCart.performClick();
            }

        });


        holder.addToCart.setOnClickListener(v -> {
            //first check  market is open or not
            if (!marketIsOpen) {
                new AlertDialog.Builder(context)
                        .setTitle("المتجر مغلق")
                        .setIcon(context.getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                        .setMessage(closedMsg)
                        .setPositiveButton("حسنا", null)
                        .create().show();
                return;
            }

            // if item is already selected remove it from cart
            if (inCart[position]) {
                editProduct(item, ItemProcess.deleteFromCart);
                notifyDataSetChanged();
                inCart[position] = false;
                return;
            }

            double price = -1;
            double quantity_select = 0.0;
            // calculate the price
            if (holder.super_rb.isChecked() || holder.first_rb.isChecked()) {

                if (item.quantityType == 1 || item.quantityType == 2 || item.quantityType == 3) {
                    // quantity in kg or number
                    price = quantity[position] * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                    quantity_select = quantity[position];

                } else
                    // quantity in gram so show item text from quantity textView
                    switch (holder.quantity.getText().toString()) {
                        // 250 gram
                        case "ربع كيلو":
                            price = 0.25 * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                            quantity_select = 0.25;
                            break;
                        // 500 gram
                        case "نصف كيلو":
                            price = 0.5 * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                            quantity_select = 0.5;
                            break;
                        // 1000 gram
                        case "كيلو":
                            price = 1.0 * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                            quantity_select = 1.0;
                            break;
                        // 1500 gram
                        case "كيلو ونصف":
                            price = 1.5 * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                            quantity_select = 1.5;
                            break;
                        // 2000 gram
                        case "2 كيلو":
                            price = 2.0 * (holder.super_rb.isChecked() ? item.priceSuper : item.priceFirst);
                            quantity_select = 2.0;
                            break;

                    }
            } else
                Toast.makeText(context, "إختر الصنف قبل الاضافة الى السلة", Toast.LENGTH_LONG).show();

            // if price == -1 so not type (صنف) is selected yet
            if (price != -1) {
                sendProductToCard(item, price, holder.first_rb.isChecked() ? "صنف اول" : "صنف سوبر", quantity_select);
                holder.addToCart.setText("تمت الإضافة");
                holder.addToCart.setBackgroundColor(context.getResources().getColor(R.color.gray));
                inCart[position] = true;
            }

        });

        holder.product_iv.setOnClickListener(v -> {
            VegetablesInfoDialog dialog = new VegetablesInfoDialog(context, item);
            dialog.show();
        });

    }

    private void sendProductToCard(VegetableItem item, double price, String type, double quantity_select) {

        String itemJson = new Gson().toJson(item);

        // number Of Product Inside Cart and Position
        int num = prefs.getInt(context.getString(R.string.numProductInsideCart), 0);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("product_" + num, itemJson);
        editor.putFloat("price_" + num, (float) price);
        // pricePerUnit is cent (بالقرش)
        editor.putFloat("pricePerUnit_" + num, (float) (type.equals("صنف اول") ? item.priceFirst : item.priceSuper));
        editor.putString("type_" + num, type);
        editor.putFloat("quantity_" + num, (float) quantity_select);
        // to the item is add to card in recycle
        editor.putBoolean(item.id + "_inCard", true);

        editor.putInt(context.getString(R.string.numProductInsideCart), ++num);
        editor.apply();

        // update badge of cart icon in actionBar
        badge.setText(String.valueOf(num));

    }

    private void editProduct(VegetableItem item, ItemProcess process) {

        SharedPreferences.Editor editor = prefs.edit();

        //number of product inside cart before remove product
        int number = prefs.getInt(context.getString(R.string.numProductInsideCart), 0);


        // remove product from cart
        // jump all item one position to avoid null pointer
        // number-1 to avoid #OutOfIndixExciption
        // reachedProductOrder to check the item order after current item in for loop or not yet
        boolean reachedProductOrder = false;

        for (int i = 0; i < number - 1; i++) {
            int currentId = new Gson().fromJson(prefs.getString("product_" + i, ""), VegetableItem.class).id;

            Log.e("test", currentId + "," + item.id);
            if (reachedProductOrder || item.id == currentId) {
                // get cart product in position +1
                String product_ = prefs.getString("product_" + (i + 1), "");
                double price_ = prefs.getFloat("price_" + (i + 1), 0);
                String type_ = prefs.getString("type_" + (i + 1), "");
                float quantity_ = prefs.getFloat("quantity_" + (i + 1), 0);
                boolean isPackaging=prefs.getBoolean("packaging_" + (i + 1), false);


                //save product  (position +1) in right position
                editor.putString("product_" + i, product_);
                editor.putFloat("price_" + i, (float) price_);
                editor.putString("type_" + i, type_);
                editor.putFloat("quantity_" + i, quantity_);
                editor.putBoolean("packaging_" + i, isPackaging);

                reachedProductOrder = true;
            }


        }

        // Now last product is duplicate so remove it
        editor.remove("product_" + (number - 1));
        editor.remove("price_" + (number - 1));
        editor.remove("type_" + (number - 1));
        editor.remove("quantity_" + (number - 1));
        editor.remove("packaging_" + (number - 1));
        //delete  id  item is add to card in recycle
        editor.remove(item.id + "_inCard");

        editor.putInt(context.getString(R.string.numProductInsideCart), --number);

        editor.apply();

        if (process == ItemProcess.deleteFromCart)
            badge.setText(String.valueOf(number));

    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.radioGroup.clearCheck();
        holder.radioGroup.setOnCheckedChangeListener(null);

        holder.neg_1.setOnClickListener(null);
        holder.plus_1.setOnClickListener(null);
        holder.addToCart.setOnClickListener(null);


        Glide.with(context).clear(holder.product_iv);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        // first refer to (صنف اول ) && super refer to (صنف ثاني )
        public ImageView product_iv;
        public TextView priceFirst_tv, priceSuper_tv, name_tv, quantity;
        public RadioButton first_rb, super_rb;
        public RadioGroup radioGroup;
        public ImageButton plus_1, neg_1;

        public Button addToCart;

        public ViewHolder(View itemView) {
            super(itemView);

            addToCart = itemView.findViewById(R.id.addToCart);
            quantity = itemView.findViewById(R.id.quantity);
            product_iv = itemView.findViewById(R.id.product_image);
            priceFirst_tv = itemView.findViewById(R.id.priceFirst);
            priceSuper_tv = itemView.findViewById(R.id.priceSuper);
            name_tv = itemView.findViewById(R.id.nameProduct);
            first_rb = itemView.findViewById(R.id.radio_first);
            super_rb = itemView.findViewById(R.id.radio_super);
            plus_1 = itemView.findViewById(R.id.plus1);
            neg_1 = itemView.findViewById(R.id.neg1);
            radioGroup = itemView.findViewById(R.id.radioGroup);

            if (context.getResources().getDisplayMetrics().densityDpi == DENSITY_LOW) {
                priceFirst_tv.setTextSize(14 * context.getResources().getDisplayMetrics().density);
                priceSuper_tv.setTextSize(14 * context.getResources().getDisplayMetrics().density);
                first_rb.setTextSize(14 * context.getResources().getDisplayMetrics().density);
                super_rb.setTextSize(14 * context.getResources().getDisplayMetrics().density);
                addToCart.setTextSize(14 * context.getResources().getDisplayMetrics().density);
                quantity.setTextSize(14 * context.getResources().getDisplayMetrics().density);
            }

        }
    }

    public int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    // to divided between process inside #editProduct method
    private enum ItemProcess {
        deleteFromCart,
        editInsideCart
    }
}
