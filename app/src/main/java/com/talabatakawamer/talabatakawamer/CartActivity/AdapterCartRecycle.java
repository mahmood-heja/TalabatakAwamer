package com.talabatakawamer.talabatakawamer.CartActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.talabatakawamer.talabatakawamer.R;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class AdapterCartRecycle extends RecyclerView.Adapter<AdapterCartRecycle.ViewHolder> {


    private final CartActivity context;
    private final List<CartItem> cartItems;
    // false when adapter use to show final Bill
    private boolean canItemEdit;

    public AdapterCartRecycle(Context context, List<CartItem> cartItems, boolean canItemEdit) {

        this.context = (CartActivity) context;
        this.cartItems = cartItems;
        this.canItemEdit = canItemEdit;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterCartRecycle.ViewHolder holder, int position) {

        CartItem item = cartItems.get(position);

        Glide.with(context).load(item.vegetableItem.imageUrl).into(holder.product_iv);

        holder.name_tv.setText(item.vegetableItem.name);
        holder.quantity_tv.setText(String.valueOf(item.quantity) + (item.vegetableItem.quantityType == 2 ? "" : " كيلو "));
        holder.type_tv.setText(item.type);

        // rotate price to first 3 digit in EN Number
        DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(new Locale("en", "US")));

        holder.price_tv.setText(df.format(item.price) + " دينار ");


        if (canItemEdit)
            holder.option_btn.setOnClickListener(v -> {

                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.item_cart_option, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(item1 -> {

                    if (item1.getItemId() == R.id.edit_menu) {
                        EditProductDialog dialog = new EditProductDialog(context, item, position, this);
                        dialog.show();

                    } else if (item1.getItemId() == R.id.delete_menu)
                        showDeleteDialog(item.vegetableItem.id, position);


                    return false;
                });
            });
        else {
            holder.option_btn.setVisibility(View.GONE);
            holder.packaging_tv.setVisibility(View.GONE);
        }

        if (item.isPackaging) {
            holder.packaging_tv.setText("إلغاء التغليف");
            holder.packaging_tv.setTextColor(context.getResources().getColor(R.color.colorAccent));

        } else {
            holder.packaging_tv.setText("تغليف");
            holder.packaging_tv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        holder.packaging_tv.setOnClickListener(v -> {
            if (!item.isPackaging) {
                holder.packaging_tv.setText("إلغاء التغليف");
                holder.packaging_tv.setTextColor(context.getResources().getColor(R.color.colorAccent));
                context.updatePackagingPrice(item.quantity * 0.15);
            } else {
                holder.packaging_tv.setText("تغليف");
                holder.packaging_tv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                context.updatePackagingPrice(-item.quantity * 0.15);

            }

            //update value of item
            context.getSharedPreferences(context.getString(R.string.cart_sharePrefrance),MODE_PRIVATE).edit().putBoolean("packaging_" + position,!item.isPackaging).apply();
            item.isPackaging = !item.isPackaging;

        });

    }

    @SuppressLint("SetTextI18n")
    private void showDeleteDialog(int id, int position) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("حذف من السلة ")
                .setMessage("هل انت متاكد من حذف المنتج من السلة ؟")
                .setPositiveButton("نعم", (dialog, which) -> {

                    SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.cart_sharePrefrance), MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    //number of product inside cart before remove product
                    int number = preferences.getInt(context.getString(R.string.numProductInsideCart), 0);


                    // remove product from cart
                    // jump all item one position to avoid null pointer
                    // number-1 to avoid #OutOfIndixExciption
                    for (int i = position; i < number - 1; i++) {
                        // get cart product in position +1
                        String product_ = preferences.getString("product_" + (i + 1), "");
                        double price_ = preferences.getFloat("price_" + (i + 1), 0);
                        String type_ = preferences.getString("type_" + (i + 1), "");
                        float quantity_ = preferences.getFloat("quantity_" + (i + 1), 0);
                        boolean isPackaging_=preferences.getBoolean("packaging_" + (i + 1),false);


                        //save product  (position +1) in right position
                        editor.putString("product_" + i, product_);
                        editor.putFloat("price_" + i, (float) price_);
                        editor.putString("type_" + i, type_);
                        editor.putFloat("quantity_" + i, quantity_);
                        editor.putBoolean("packaging_" + i,isPackaging_);

                    }

                    // Now last product is duplicate so remove it
                    editor.remove("product_" + (number - 1));
                    editor.remove("price_" + (number - 1));
                    editor.remove("type_" + (number - 1));
                    editor.remove("quantity_" + (number - 1));
                    editor.remove("packaging_" + (number - 1));
                    //delete  id  item is add to card in recycle
                    editor.remove(id + "_inCard");

                    editor.putInt(context.getString(R.string.numProductInsideCart), --number);

                    editor.apply();


                    cartItems.remove(position);
                    notifyDataSetChanged();
                    context.cartIsEdited = true;

                    context.updateTotalPrice(cartItems);


                })
                .setNegativeButton("إلغاء", null);


        alertDialog.create().show();


    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView product_iv;
        // type == الصنف
        public TextView name_tv, price_tv, type_tv, quantity_tv, packaging_tv;
        //delete item from cart
        public ImageButton option_btn;


        public ViewHolder(View itemView) {
            super(itemView);

            product_iv = itemView.findViewById(R.id.product_image);
            price_tv = itemView.findViewById(R.id.price);
            type_tv = itemView.findViewById(R.id.type);
            quantity_tv = itemView.findViewById(R.id.quantity);
            name_tv = itemView.findViewById(R.id.nameProduct);
            option_btn = itemView.findViewById(R.id.option_btn);
            packaging_tv = itemView.findViewById(R.id.packaging);
        }
    }

    @NonNull
    @Override
    public AdapterCartRecycle.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_cart_item, parent, false);

        return new ViewHolder(view);
    }

    public void updateCartItem(CartItem item, int pos) {
        cartItems.set(pos, item);
        notifyDataSetChanged();

        // update item into sharedPreference
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.cart_sharePrefrance), MODE_PRIVATE).edit();

        // price saves in cent (قرش ) inside sharedPreferences
        editor.putFloat("price_" + pos, (float) item.price * 100);
        // pricePerUnit is cent (بالقرش)
        editor.putFloat("pricePerUnit_" + pos, (float) (item.type.equals("صنف اول") ? item.vegetableItem.priceFirst : item.vegetableItem.priceSuper));
        editor.putString("type_" + pos, item.type);
        editor.putFloat("quantity_" + pos, (float) item.quantity);

        editor.apply();

        //update total in activity
        context.updateTotalPrice(cartItems);

    }


    public List<CartItem> getItems() {
        return this.cartItems;
    }


}
