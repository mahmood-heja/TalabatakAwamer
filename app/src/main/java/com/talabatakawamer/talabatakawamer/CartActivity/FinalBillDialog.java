package com.talabatakawamer.talabatakawamer.CartActivity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.talabatakawamer.talabatakawamer.R;
import com.talabatakawamer.talabatakawamer.VegetablesSection.VegetableItem;
import com.talabatakawamer.talabatakawamer.postOnPhp.NameValuePair;
import com.talabatakawamer.talabatakawamer.postOnPhp.PhpTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinalBillDialog extends Dialog {


    private Context context;

    public FinalBillDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    private RecyclerView recyclerView;
    private TextView totalPrice_tv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.final_bill_dialog);


        recyclerView = findViewById(R.id.bill_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.notificationPreference), Context.MODE_PRIVATE);
        // get  last final by Order ID
        String orderID = preferences.getString(context.getString(R.string.lastFinalBill), "-1");
        String lastInitialBill = preferences.getString(context.getString(R.string.lastInitialBill_pref), "");
        double lastInitialBillTotalPrice = preferences.getFloat(context.getString(R.string.lastInitialBillTotalPrice_pref), -1);


        totalPrice_tv = findViewById(R.id.totalPrice);
        TextView title = findViewById(R.id.title);

        findViewById(R.id.done_btn).setOnClickListener(v -> dismiss());

        // first check to know if has final bill or initialBill or  not yet
        if (orderID.equals("-1") && lastInitialBill.trim().isEmpty()) {
            findViewById(R.id.noBill_tv).setVisibility(View.VISIBLE);
            totalPrice_tv.setVisibility(View.GONE);
            return;
        } else if (!lastInitialBill.isEmpty() && orderID.equals("-1")) {
            //have just initial Bill and no final bill yet so show initial Bill
            title.setText("الفاتورة الابتدائية لأخر طلب لديك");
            loadDataIntoAdapter(lastInitialBill, lastInitialBillTotalPrice);
            return;
        }

        //if final bill is exist remove last initial Bill
        preferences.edit().remove(context.getString(R.string.lastInitialBill_pref)).apply();

        ProgressBar progressBar = findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        List<NameValuePair> pairs = new ArrayList<>();

        pairs.add(new NameValuePair("id", orderID));

        PhpTask php = new PhpTask(pairs);

        php.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/getFinalBill.php");

        php.onPhpTaskFinished(result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                if (!result.getBoolean("error"))
                    loadDataIntoAdapter(result.getString("final Bill"), result.getDouble("total price"));
                else
                    Toast.makeText(getContext(), result.getString("msg_error"), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "تحقق من إتصالك بالانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();
                dismiss();
            }

        });

    }


    @SuppressLint("SetTextI18n")
    private void loadDataIntoAdapter(String finalBill, double totalPrice) {
        try {

            JSONArray jsonArray = new JSONArray(finalBill);
            int len = jsonArray.length();

            List<CartItem> items = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                // fill need information to show
                // 0  mean no need for this failed
                VegetableItem item = new VegetableItem(
                        object.getInt("id"), object.getString("name"),
                        "", object.getString("image"), 0, 0,
                        object.getInt("quantityType"), 0);

                double price = object.getDouble("price");

                boolean isPackaging = false;
                if (object.has("isPackaging"))
                    isPackaging = object.getBoolean("isPackaging");

                CartItem cartItem = new CartItem(item, price, 0, object.getDouble("quantity"),
                        object.getString("type"), isPackaging);

                items.add(cartItem);
            }

            DecimalFormat df = new DecimalFormat("#.###");
            //final total price  come from admin
            totalPrice_tv.setText("المجموع الكلي : " + df.format(totalPrice) + " دينار ");

            AdapterCartRecycle adapter = new AdapterCartRecycle(context, items, false);
            recyclerView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
