package com.talabatakawamer.talabatakawamer.CartActivity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.talabatakawamer.talabatakawamer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.final_bill_dialog);

        // get  last final by Order ID
        String orderID =
                context.getSharedPreferences(context.getString(R.string.notificationPreference), Context.MODE_PRIVATE).
                        getString(context.getString(R.string.lastFinalBill), "-1");

        totalPrice_tv = findViewById(R.id.totalPrice);

        findViewById(R.id.done_btn).setOnClickListener(v -> dismiss());

        if (orderID.equals("-1")) {
            findViewById(R.id.noBill_tv).setVisibility(View.VISIBLE);
            totalPrice_tv.setVisibility(View.GONE);
            return;
        }

        ProgressBar progressBar=findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.bill_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        List<com.talabatakawamer.talabatakawamer.postOnPhp.NameValuePair> pairs = new ArrayList<>();

        pairs.add(new com.talabatakawamer.talabatakawamer.postOnPhp.NameValuePair("id", orderID));

        com.talabatakawamer.talabatakawamer.postOnPhp.PhpTask php = new com.talabatakawamer.talabatakawamer.postOnPhp.PhpTask(pairs);

        php.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/getFinalBill.php");

        php.onPhpTaskFinished(result -> {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                if (!result.getBoolean("error"))
                    loadDataIntoAdapter(result.getString("final Bill"),result.getDouble("total price"));
                else
                    Toast.makeText(getContext(), result.getString("msg_error"), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "تحقق من إتصالك بالانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();
                dismiss();
            }

        });

    }


    @SuppressLint("SetTextI18n")
    private void loadDataIntoAdapter(String finalBill,double totalPrice) {
        try {

            JSONArray jsonArray = new JSONArray(finalBill);
            int len = jsonArray.length();

            List<CartItem> items = new ArrayList<>();

            for (int i = 0; i < len; i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                // fill need information to show
                // 0  mean no need for this failed
                com.talabatakawamer.talabatakawamer.VegetablesSection.VegetableItem item = new com.talabatakawamer.talabatakawamer.VegetablesSection.VegetableItem(
                        object.getInt("id"), object.getString("name"),
                        "", object.getString("image"), 0, 0,
                        object.getInt("quantityType"), 0);

                double price = object.getDouble("price");

                // isPackaging value is not required here
                CartItem cartItem = new CartItem(item, price, 0,
                        object.getDouble("quantity"), object.getString("type"),false);

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
