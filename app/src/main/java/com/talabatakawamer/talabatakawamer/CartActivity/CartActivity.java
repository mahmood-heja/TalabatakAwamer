package com.talabatakawamer.talabatakawamer.CartActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.talabatakawamer.talabatakawamer.R;
import com.talabatakawamer.talabatakawamer.VegetablesSection.VegetableItem;
import com.talabatakawamer.talabatakawamer.postOnPhp.NameValuePair;
import com.talabatakawamer.talabatakawamer.postOnPhp.PhpTask;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import com.yarolegovich.lovelydialog.LovelyTextInputDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {


    // to access from adapter
    public TextView totalPrice_tv;
    public double totalPrice;
    // to start activity when back if item deleted from cart
    public boolean cartIsEdited = false;

    private AdapterCartRecycle adapter;
    private SharedPreferences preferences;
    private KProgressHUD progress;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        // title added from layout
        setTitle("");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        totalPrice_tv = findViewById(R.id.totalPrice);

        RecyclerView cartRecycler = findViewById(R.id.cartRecycle);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));

        preferences = getSharedPreferences(getString(R.string.cart_sharePrefrance), MODE_PRIVATE);
        // number of product inside cart;
        int num = preferences.getInt(getString(R.string.numProductInsideCart), 0);

        List<CartItem> cartItems = new ArrayList<>();
        totalPrice = 0;

        for (int i = 0; i < num; i++) {

            VegetableItem vegetableItem = new Gson().fromJson(preferences.getString("product_" + i, ""), VegetableItem.class);
            double price = preferences.getFloat("price_" + i, 0);
            double pricePetUnit = preferences.getFloat("pricePerUnit_" + i, 0);
            String type = preferences.getString("type_" + i, "");
            double quantity = preferences.getFloat("quantity_" + i, 0);

            totalPrice += price;
            // save price in JD
            CartItem cartItem = new CartItem(vegetableItem, price / 100.0, pricePetUnit, quantity, type);

            cartItems.add(cartItem);
        }


        adapter = new AdapterCartRecycle(this, cartItems, true);

        // rotate total price to first 3 digit in EN Number
        DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(new Locale("en", "US")));
        totalPrice_tv.setText("المجموع : " + df.format(totalPrice / 100.0) + " دينار ");

        cartRecycler.setAdapter(adapter);


        // activity start from notification
        if (getIntent().getBooleanExtra("ShowFinalBillDialog", false)) {
            FinalBillDialog dialog = new FinalBillDialog(this);
            dialog.show();
        }

    }

    public void request_btn(View view) {

        DecimalFormat df = new DecimalFormat("#.###");

        if (totalPrice != 0) {
            new LovelyTextInputDialog(this)
                    .setTopColorRes(R.color.colorAccent)
                    // 1.0 JD price of deliver
                    .setTitle("المجموع الكلي :" + df.format(totalPrice / 100.0 + 1.0) + " دينار ")
                    .setMessage("أدخل رقم هاتفك من اجل التواصل")
                    .setIcon(R.drawable.ic_shooping_smartphone)
                    // return text.length() == 10
                    .setInputFilter("ادخل رقم هاتف صالح", text -> text.length() == 10 && text.startsWith("07"))
                    // # this::sentOrder =   #text -> sentOrder(text)
                    .setConfirmButton("إطلب", this::sentOrder)
                    .setNegativeButton("إلغاء", null)
                    .setInputType(EditorInfo.TYPE_CLASS_PHONE)
                    .show();
        } else
            Toast.makeText(this, "لا يوجد طلبات داخل السلة", Toast.LENGTH_LONG).show();

    }

    private void sentOrder(String phone_num) {
        progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAnimationSpeed(2)
                .setDimAmount(0.5f).show();

        // build bill of order
        JSONArray infoJsonArray = buildJsonBill();
        // get token for this device
        String token = getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE).getString(getString(R.string.token), "");

        List<NameValuePair> pairs = new ArrayList<>();

        pairs.add(new NameValuePair("phone_info", phone_num));
        pairs.add(new NameValuePair("info", infoJsonArray.toString()));
        // add one JD price of deliver
        // rotate price to first 3 digit in EN Number
        DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(new Locale("en", "US")));

        pairs.add(new NameValuePair("total_price", df.format(totalPrice / 100 + 1.0)));
        pairs.add(new NameValuePair("token", token));

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/insertNewOrder.php");

        phpTask.onPhpTaskFinished(result -> {

            if (result != null) {
                if (!result.getBoolean("error")) {
                    // send notification to admin
                    sendNotify(phone_num);
                    // stop method
                    return;
                } else
                    Toast.makeText(this, result.getString("error_msg"), Toast.LENGTH_LONG).show();


            } else
                Toast.makeText(this, "حدث خطأ اثناء ارسال الطلب, تحقق من اتصالك من الانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();


            progress.dismiss();
        });


    }


    private void sendNotify(String phone_num) {
        List<NameValuePair> pairs = new ArrayList<>();

        pairs.add(new NameValuePair("title", "طلب جديد"));
        pairs.add(new NameValuePair("message", "هناك طلب جديد من " + phone_num));
        // channel notification in #Admin App
        pairs.add(new NameValuePair("channel_id", "new_request"));
        // don't need any send data
        pairs.add(new NameValuePair("content", ""));

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/sendNotify/sentNotfiy.php");

        phpTask.onPhpTaskFinished(result -> {
            progress.dismiss();
            if (result != null) {
                Toast.makeText(this, "تم إرسال الطلب بنجاح", Toast.LENGTH_LONG).show();
                // clear the cart
                preferences.edit().clear().apply();
                preferences.edit().putInt(getString(R.string.numProductInsideCart), 0).apply();
                // remove final bill if it exist
                getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE).edit().remove(getString(R.string.lastFinalBill)).apply();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("mustUpdate", true);
                returnIntent.putExtra("numberProductInCart", "0");
                returnIntent.putExtra("newOrder", true);
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }


        });

    }

    private JSONArray buildJsonBill() {
        JSONArray jsonArray = new JSONArray();

        List<CartItem> items = adapter.getItems();
        int len = items.size();
        for (int i = 0; i < len; i++) {
            CartItem item = items.get(i);

            JSONObject itemObject = new JSONObject();

            try {
                itemObject.put("id", item.vegetableItem.id);
                // this initial bill ... final bill well send from admin
                itemObject.put("quantityType", item.vegetableItem.quantityType);

                itemObject.put("name", item.vegetableItem.name);
                itemObject.put("type", item.type);
                itemObject.put("quantity", item.quantity);
                // price in JD
                itemObject.put("price", item.price);
                itemObject.put("image", item.vegetableItem.imageUrl);
                // سعر المنتج لكل كيلو او لكل وحدة بالقرش
                itemObject.put("pricePerUnit", item.pricePerUnit);


                jsonArray.put(itemObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return jsonArray;
    }

    @SuppressLint("SetTextI18n")
    public void updateTotalPrice(List<CartItem> items) {

        int len = items.size();
        double newTotalPrice = 0;
        for (int i = 0; i < len; i++) {
            newTotalPrice += items.get(i).price;
        }

        DecimalFormat df = new DecimalFormat("#.###");
        totalPrice_tv.setText("المجموع : " + df.format(newTotalPrice) + " دينار ");

        totalPrice = newTotalPrice * 100;
    }

    @Override
    public void onBackPressed() {
        if (cartIsEdited) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("mustUpdate", true);
            returnIntent.putExtra("numberProductInCart", adapter.getItemCount() + "");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else
            super.onBackPressed();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cart_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (cartIsEdited)
                    onBackPressed();
                else
                    finish();
                return true;

            case R.id.finalBill_menu:
                FinalBillDialog dialog = new FinalBillDialog(this);
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);

    }


}
