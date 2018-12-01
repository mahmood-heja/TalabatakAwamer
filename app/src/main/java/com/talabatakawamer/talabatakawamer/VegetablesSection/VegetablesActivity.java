package com.talabatakawamer.talabatakawamer.VegetablesSection;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.talabatakawamer.talabatakawamer.CartActivity.CartActivity;
import com.talabatakawamer.talabatakawamer.R;
import com.talabatakawamer.talabatakawamer.postOnPhp.NameValuePair;
import com.talabatakawamer.talabatakawamer.postOnPhp.PhpTask;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.readystatesoftware.viewbadger.BadgeView;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.util.DisplayMetrics.DENSITY_LOW;

public class VegetablesActivity extends AppCompatActivity implements RatingDialogListener {

    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;
    private RecyclerView vegetablesRecycler;
    private BadgeView badge;
    private int CartActivityRequestCode = 1;
    private ImageView noConn_iv;
    private TextView noConn_tv;
    private BroadcastReceiver mMessageReceiver;
    private KProgressHUD progress;
    private AdapterRecycleVegetables adapter;


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), new IntentFilter("ShowRating"));
        checkIfMarketAvailable();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vegetables);

        checkFirstLaunched();
        // show rating dialog if data is received after order delivered
        checkIfMustShowRatingDialog();


        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkIfMustShowRatingDialog();
            }
        };

        noConn_iv = findViewById(R.id.noConn_iv);
        noConn_tv = findViewById(R.id.noConn_tv);

        // prepare action bar and shopping cart
        View shoppingCart = findViewById(R.id.shopping_cart);
        View call_HelpCenter = findViewById(R.id.call);
        badge = new BadgeView(this, shoppingCart);

        shoppingCart.setOnClickListener(v -> startActivityForResult(new Intent(this, CartActivity.class), CartActivityRequestCode));
        call_HelpCenter.setOnClickListener(v -> showCallDialog());


        SharedPreferences pref = getSharedPreferences(getString(R.string.cart_sharePrefrance), MODE_PRIVATE);
        if (pref.getInt(getString(R.string.numProductInsideCart), -1) == -1) {
            pref.edit().putInt(getString(R.string.numProductInsideCart), 0).apply();
            badge.setText("0");
        } else
            badge.setText(String.valueOf(pref.getInt(getString(R.string.numProductInsideCart), 0)));
        //

        badge.show();


        // width of card + space between another card
        final int dp;
        // the card will be small when DENSITY_LOW
        // the width of card will be change to (130dp) in AdapterRecycleVegetables#onCreateViewHolder
        if (getResources().getDisplayMetrics().densityDpi == DENSITY_LOW)
            dp = dpToPx(146); //spacing*2+Width of Card  = 8*2+130
        else
            dp = dpToPx(156); //spacing*2+Width of Card  = 8*2+140

        vegetablesRecycler = findViewById(R.id.vegetablesRecycle);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1) {

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

                int Rec_width = vegetablesRecycler.getWidth();
                int numOfSpan = Rec_width / dp;

                if (numOfSpan != 0)
                    this.setSpanCount(numOfSpan);


                vegetablesRecycler.setPadding((Rec_width % dp) / 2, 0, (Rec_width % dp) / 2, 0);

                super.onLayoutChildren(recycler, state);
            }


        };


        EqualSpacingItemDecoration decoration = new EqualSpacingItemDecoration(dpToPx(8), 2, 0);
        vegetablesRecycler.addItemDecoration(decoration);

        vegetablesRecycler.setLayoutManager(layoutManager);

        loadProduct();

    }

    private void showCallDialog() {

        // don't show dialog if Permission don't generated yet
        if (!checkCallPermission())
            return;


        new AlertDialog.Builder(this).setIcon(R.drawable.ic_final_bill)
                .setMessage("هل تريد الاتصال بالفعل بمركز خدمة العملاء ؟")
                .setPositiveButton("إتصل", (dialog, which) -> {

                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "0798718364"));
                    startActivity(intent);

                })
                .setNegativeButton("إلغاء", null)
                .create().show();

    }


    private void loadProduct() {
        noConn_iv.setVisibility(View.GONE);
        noConn_tv.setVisibility(View.GONE);

        progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setAnimationSpeed(2)
                .setDimAmount(0.5f).show();


        List<NameValuePair> pairs = new ArrayList<>();
        // 1 mean get product Visible just
        pairs.add(new NameValuePair("is_Visible", "1"));

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/getAllProduct.php");

        phpTask.onPhpTaskFinished(result -> {

            if (result != null) {
                if (!result.getBoolean("error")) {

                    JSONArray jsonArray = result.getJSONArray("products");
                    int len = jsonArray.length();
                    List<VegetableItem> vegetableItems = new ArrayList<>();

                    for (int i = 0; i < len; i++) {
                        JSONObject object = jsonArray.getJSONObject(i);

                        VegetableItem item = new VegetableItem(
                                object.getInt("id"),
                                object.getString("name"),
                                object.getString("des"),
                                object.getString("image_url"),
                                object.getDouble("first_price"),
                                object.getDouble("super_price"),
                                object.getInt("quantity_type"),
                                object.getInt("type_available")
                        );

                        vegetableItems.add(item);
                    }

                    adapter = new AdapterRecycleVegetables(VegetablesActivity.this, vegetableItems, badge);
                    vegetablesRecycler.swapAdapter(adapter, false);

                    checkIfMarketAvailable();
                }
                return;

            } else {
                Toast.makeText(VegetablesActivity.this, "حدث خطأ اثناء التحميل, تحقق من اتصالك من الانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();
                vegetablesRecycler.setAdapter(null);
                noConn_tv.setVisibility(View.VISIBLE);
                noConn_iv.setVisibility(View.VISIBLE);

                noConn_iv.setOnClickListener(v -> loadProduct());
            }

            progress.dismiss();

        });

    }

    private void checkIfMarketAvailable() {

        List<NameValuePair> pairs = new ArrayList<>();

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/marketAvailable.php");
        phpTask.onPhpTaskFinished(result -> {

            // first hide Progress
            if (result != null) {


                JSONObject object = new JSONObject(result.getString("market_status"));
                if (adapter != null)
                    adapter.setMarketStatus(object.getBoolean("marketOpen"), object.getString("msg"));

                if (!object.getBoolean("marketOpen")) {
                    // clear the cart when market is closed
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.cart_sharePrefrance), MODE_PRIVATE);
                    preferences.edit().clear().apply();
                    preferences.edit().putInt(getString(R.string.numProductInsideCart), 0).apply();
                    badge.setText("0");
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                }


            } else {
                Toast.makeText(VegetablesActivity.this, "حدث خطأ اثناء التحميل, تحقق من اتصالك من الانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();
                vegetablesRecycler.setAdapter(null);
                noConn_tv.setVisibility(View.VISIBLE);
                noConn_iv.setVisibility(View.VISIBLE);

                noConn_iv.setOnClickListener(v -> loadProduct());
            }

            if (progress.isShowing())
                progress.dismiss();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CartActivityRequestCode) {
            if (resultCode == RESULT_OK)
                if (data.getBooleanExtra("mustUpdate", false)) {
                    badge.setText(data.getStringExtra("numberProductInCart"));
                    // reload product
                    //loadProduct();
                    adapter.notifyDataSetChanged();

                    // order sent to admin
                    // show waring dialog
                    if (data.getBooleanExtra("newOrder", false))
                        new AlertDialog.Builder(this).setIcon(R.drawable.ic_final_bill)
                                .setMessage("قد يطرأ تغير بسيط على الفاتورة بسبب إختلاف الاوزان , ستصلك الفاتورة النهائية قريبا في حال حصول ذلك ")
                                .setPositiveButton("حسنا", null)
                                .create().show();
                }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCallDialog();
                } else
                    Toast.makeText(this, "لا يمكن إجراء الاتصال دون قبول الاذن للتطبيق ", Toast.LENGTH_SHORT).show();

            }

        }
    }

    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void checkFirstLaunched() {

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (!prefs.contains("isFirst")) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // first channel for receive final bill
                String channel_ID = "final Bill";
                CharSequence name = "Final Bill";
                String description = "ارسال الفاتورة النهائية";
                NotificationChannel channel_1 = new NotificationChannel(channel_ID, name, NotificationManager.IMPORTANCE_HIGH);
                channel_1.setDescription(description);
                // first channel for receive Rating request
                String channel_ID_2 = "Rating";
                CharSequence name_2 = "Rating The Service";
                String description_2 = " وصول اشعار للتقيم بعد استلام الطلب  ";
                NotificationChannel channel_2 = new NotificationChannel(channel_ID_2, name_2, NotificationManager.IMPORTANCE_HIGH);
                channel_2.setDescription(description_2);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel_1);
                notificationManager.createNotificationChannel(channel_2);


            }


            // show waring dialog for first time
            new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                    .setTitle("ملاحظة")
                    .setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                    .setMessage("الطلبات تصل في اليوم التالي من وقت ارسال الطلب ")
                    .setPositiveButton("حسنا", null)
                    .create().show();

            // Number Of Notification not open yet
            editor.putInt("NumberOfNotification", 0);
            editor.putInt("NotificationID", 0);

            editor.putBoolean("isFirst", false);


        }

        editor.apply();

    }


    private void checkIfMustShowRatingDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE);

        if (!sharedPreferences.getBoolean(getString(R.string.showRating), false))
            return;

        new AppRatingDialog.Builder()
                .setPositiveButtonText("تقيم")
                .setNegativeButtonText("الغاء")
                .setNoteDescriptions(Arrays.asList("سيء", "ليس جيد", "جيد", "جيد جدا", "رائع !!!"))
                .setDefaultRating(4)
                .setTitle("ما هو تقييمك للخدمة التي تلقيتها مؤخرًا")
                .setCommentInputEnabled(true)
                .setStarColor(R.color.colorAccent)
                .setNoteDescriptionTextColor(R.color.black)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.black)
                .setHint("أدخل تعليقك هنا رجاء")
                .setHintTextColor(R.color.gray)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(this)
                .show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private boolean checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this, CALL_PHONE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return false;
        } else
            return true;
    }

    // Rating Dialog Btn
    @Override
    public void onPositiveButtonClicked(int rating, @NonNull String comment) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE);

        // to stop show rating dialog when app launched
        preferences.edit().putBoolean(getString(R.string.showRating), false).apply();

        progress.show();

        int orderId = preferences.getInt(getString(R.string.orderId), 0);
        String phone = preferences.getString(getString(R.string.phoneNumber), "0");

        List<NameValuePair> pairs = new ArrayList<>();

        pairs.add(new NameValuePair("order_id", orderId + ""));
        pairs.add(new NameValuePair("rating", rating + ""));
        pairs.add(new NameValuePair("phone_num", phone));
        pairs.add(new NameValuePair("comment", comment));

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/VegetableSection/insertNewRating.php");

        phpTask.onPhpTaskFinished(result -> {

            if (result != null) {
                if (!result.getBoolean("error")) {
                    // to stop show rating dialog when app launched
                    preferences.edit().putBoolean(getString(R.string.showRating), false).apply();
                    notifyAdmin(phone);

                    return;
                } else
                    Toast.makeText(this, result.getString("error_msg"), Toast.LENGTH_LONG).show();


            } else
                Toast.makeText(VegetablesActivity.this, "حدث خطأ اثناء محاولة الارسال, تحقق من اتصالك من الانترنت وحاول مرة إخرى", Toast.LENGTH_LONG).show();


            progress.dismiss();

        });


    }

    private void notifyAdmin(String phone_num) {
        List<NameValuePair> pairs = new ArrayList<>();

        pairs.add(new NameValuePair("title", "تقيم جديد"));
        pairs.add(new NameValuePair("message", "هناك تقيم جديد من " + phone_num));
        // channel notification in #Admin App
        pairs.add(new NameValuePair("channel_id", "new_request"));
        // don't need any send data
        pairs.add(new NameValuePair("content", "NewRating"));

        PhpTask phpTask = new PhpTask(pairs);

        phpTask.execute("http://talabatakawamer.com/TalabatakAwamerApp/sendNotify/sentNotfiy.php");

        phpTask.onPhpTaskFinished(result -> progress.dismiss());
    }

    @Override
    public void onNegativeButtonClicked() {
        getSharedPreferences(getString(R.string.notificationPreference), MODE_PRIVATE).
                edit().putBoolean(getString(R.string.showRating), false).apply();
    }

    @Override
    public void onNeutralButtonClicked() {

    }


}
