package com.fast0n.ipersonalarea;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cooltechworks.creditcarddesign.CreditCardView;
import com.fast0n.ipersonalarea.java.myDbAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class ChargeActivity extends AppCompatActivity {

    myDbAdapter helper;
    TextInputLayout edt_password_visibility;
    String typecard = null, account;
    private EditText ncvv, nCard, nExpiration, edt_password;
    private CreditCardView creditCardView;
    private Boolean touch = true;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.chargeNumber);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // set row icon in the toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        helper = new myDbAdapter(this);

        // java adresses
        Button button = findViewById(R.id.button);
        creditCardView = findViewById(R.id.card);
        spinner = findViewById(R.id.spinner);
        edt_password_visibility = findViewById(R.id.edt_password_visibility);
        edt_password = findViewById(R.id.edt_password);

        SharedPreferences settings = getSharedPreferences("sharedPreferences", 0);

        // prendere le SharedPreferences
        account = settings.getString("account", null);

        final Bundle extras = getIntent().getExtras();
        assert extras != null;
        final String token = extras.getString("token", null);
        final String name = extras.getString("name", null);
        String price_spinner = extras.getString("price", null);

        if (price_spinner.equals("false")) {
            spinner.setVisibility(View.INVISIBLE);
            edt_password_visibility.setVisibility(View.VISIBLE);
        }

        creditCardView.setCardHolderName(name);

        // prende gli importi
        final String site_url = getString(R.string.site_url);
        RequestQueue queue = Volley.newRequestQueue(ChargeActivity.this);
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, site_url + getString(R.string.recharge) + "?payinfoprice=true&&token=" + token, null,
                response -> {
                    try {

                        JSONObject json_raw = new JSONObject(response.toString());
                        String iliad = json_raw.getString("iliad");

                        JSONObject json = new JSONObject(iliad);
                        String price = json.getString("0");
                        JSONArray json_title = new JSONArray(price);

                        final List<String> list = new ArrayList<>();

                        for (int z = 0; z < json_title.length(); z++) {

                            String stringPrice = json_title.getString(z);
                            list.add(stringPrice + "€");
                        }
                        list.add("Importo");
                        final int listsize = list.size() - 1;
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ChargeActivity.this, R.layout.spinner_item, list) {
                            @Override
                            public int getCount() {
                                return (listsize);
                            }
                        };
                        spinner.setAdapter(dataAdapter);
                        spinner.setSelection(list.size() - 1);

                    } catch (JSONException ignored) {
                    }

                }, error -> {

        });
        queue.add(getRequest);


        creditCardView.setOnClickListener(v -> {
            if (touch) {
                creditCardView.showBack();
                touch = false;
            } else {
                creditCardView.showFront();
                touch = true;
            }
        });


        final ArrayList<String> listOfPattern = new ArrayList<>();

        String ptVisa = "^4[0-9]{6,}$";
        listOfPattern.add(ptVisa);
        String ptMasterCard = "^5[1-5][0-9]{5,}$";
        listOfPattern.add(ptMasterCard);

        nCard = findViewById(R.id.nCard);
        nExpiration = findViewById(R.id.nExpiration);
        ncvv = findViewById(R.id.ncvv);

        nCard.addTextChangedListener(new TextWatcher() {
            private final String space = " "; // you can change this to whatever you want
            private final Pattern pattern = Pattern.compile("^(\\d{4}" + space + "){0,3}\\d{1,4}$"); // check whether we need to modify or not

            @Override
            public void onTextChanged(CharSequence s, int st, int be, int count) {
                creditCardView.setCardNumber(nCard.getText().toString().replaceAll("\\s+", ""));
                String currentText = nCard.getText().toString();
                if (currentText.isEmpty() || pattern.matcher(currentText).matches())
                    return;
                String numbersOnly = currentText.trim().replaceAll("[^\\d.]", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < numbersOnly.length(); i += 4)
                    if (i + 4 < numbersOnly.length())
                        formatted.append(numbersOnly.substring(i, i + 4)).append(space);
                    else
                        formatted.append(numbersOnly.substring(i));
                nCard.setText(formatted.toString());
                nCard.setSelection(nCard.getText().toString().length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        ncvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ncvv.getText().toString().length() == 3 || ncvv.getText().toString().length() == 4) {
                    creditCardView.showFront();
                    touch = true;
                } else {
                    if (touch) {
                        creditCardView.showBack();
                        touch = false;
                    }
                }
                creditCardView.setCVV(ncvv.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        nExpiration.addTextChangedListener(new TextWatcher() {
            private final String space = "/"; // you can change this to whatever you want
            private final Pattern pattern = Pattern.compile("^(\\d{2}" + space + ")?\\d{1,2}$"); // check whether we need to modify or not

            @Override
            public void onTextChanged(CharSequence s, int st, int be, int count) {
                creditCardView.setCardExpiry(nExpiration.getText().toString().replaceAll("\\s+", ""));
                String currentText = nExpiration.getText().toString();
                if (currentText.isEmpty() || pattern.matcher(currentText).matches())
                    return;
                String numbersOnly = currentText.trim().replaceAll("[^\\d.]", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < numbersOnly.length(); i += 2)
                    if (i + 2 < numbersOnly.length())
                        formatted.append(numbersOnly.substring(i, i + 2)).append(space);
                    else
                        formatted.append(numbersOnly.substring(i));
                nExpiration.setText(formatted.toString());
                nExpiration.setSelection(nExpiration.getText().toString().length());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
            }
        });

        button.setOnClickListener(v -> {
            String montant = spinner.getSelectedItem().toString();

            if (price_spinner.equals("true")) {
                if (montant.equals("Importo") || price_spinner.equals("false"))
                    Toasty.warning(ChargeActivity.this, montant + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (nCard.getText().toString().length() < 19)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtCard) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (nExpiration.getText().toString().length() == 0)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtExpiration) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (ncvv.getText().toString().length() == 0)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtCvv) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else {

                    String ccNum = nCard.getText().toString().replaceAll("\\s+", "");
                    for (String p : listOfPattern) {
                        if (ccNum.matches(p)) {
                            switch (p) {
                                case "^4[0-9]{6,}$":
                                    typecard = "visa";
                                    break;
                                case "^5[1-5][0-9]{5,}$":
                                    typecard = "mastercard";
                                    break;
                                default:
                                    typecard = "";
                                    break;
                            }
                            break;
                        }
                    }

                    button.setEnabled(false);
                    RequestQueue queue1 = Volley.newRequestQueue(ChargeActivity.this);
                    JsonObjectRequest getRequest1 = new JsonObjectRequest(Request.Method.GET, site_url + getString(R.string.recharge) + "?phonecharge=true&montant="
                            + montant.replace("€", "")
                            + "&cbtype=" + typecard
                            + "&cbnumero=" + nCard.getText().toString().replaceAll("\\s+", "")
                            + "&cbexpmois=" + nExpiration.getText().toString().split("/")[0]
                            + "&cbexpannee=20" + nExpiration.getText().toString().split("/")[1]
                            + "&cbcrypto=" + ncvv.getText().toString()
                            + "&token=" + token, null,
                            response -> {
                                try {

                                    JSONObject json_raw = new JSONObject(response.toString());
                                    String iliad = json_raw.getString("iliad");
                                    JSONObject json = new JSONObject(iliad);

                                    String price = json.getString("0");


                                    if (price.equals("true")) {
                                        Toasty.success(ChargeActivity.this, price, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ChargeActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        button.setEnabled(true);
                                    } else {
                                        Toasty.error(ChargeActivity.this, price, Toast.LENGTH_SHORT).show();
                                        button.setEnabled(true);

                                    }


                                } catch (JSONException ignored) {
                                }

                            }, error -> {

                    });

                    queue1.add(getRequest1);
                }
            } else {
                //cambio metodo
                if (nCard.getText().toString().length() < 19)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtCard) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (nExpiration.getText().toString().length() == 0)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtExpiration) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (ncvv.getText().toString().length() == 0)
                    Toasty.warning(ChargeActivity.this, getString(R.string.edtCvv) + " " + getString(R.string.missing), Toast.LENGTH_SHORT).show();
                else if (edt_password.getText().toString().length() == 0) {
                    String getAllData = helper.getAllData();
                    String[] arrayData = getAllData.split("\n");
                    String pwd = null;
                    for (String anArrayData : arrayData) {
                        String onlyname = anArrayData.split("&")[0];
                        String onlypassword = anArrayData.split("&")[1];
                        if (onlyname.equals(account)) {
                            pwd = onlypassword;
                            break;
                        }
                    }

                    byte[] decodeValue1 = Base64.decode(pwd, Base64.DEFAULT);
                    String ppassword = new String(decodeValue1);

                    if (!edt_password.getText().toString().equals(ppassword.replaceAll("\\s+", ""))) {
                        Toasty.warning(ChargeActivity.this, getString(R.string.wrong_password), Toast.LENGTH_LONG,
                                true).show();
                    }
                } else {

                    String ccNum = nCard.getText().toString().replaceAll("\\s+", "");
                    for (String p : listOfPattern) {
                        if (ccNum.matches(p)) {
                            switch (p) {
                                case "^4[0-9]{6,}$":
                                    typecard = "visa";
                                    break;
                                case "^5[1-5][0-9]{5,}$":
                                    typecard = "mastercard";
                                    break;
                                default:
                                    typecard = "";
                                    break;
                            }
                            break;
                        }
                    }

                    byte[] encodeValue1 = Base64.encode(edt_password.getText().toString().getBytes(), Base64.DEFAULT);
                    String newpassword = new String(encodeValue1);
                    button.setEnabled(false);
                    RequestQueue queue1 = Volley.newRequestQueue(ChargeActivity.this);
                    JsonObjectRequest getRequest1 = new JsonObjectRequest(Request.Method.GET,
                            site_url + getString(R.string.infomation)
                                    + "?method=cb&cbtype=" + typecard
                                    + "&cbnumero=" + nCard.getText().toString()
                                    + "&cbexpmois=" + nExpiration.getText().toString().split("/")[0]
                                    + "&cbexpannee=20" + nExpiration.getText().toString().split("/")[1]
                                    + "&cbcrypto=" + ncvv.getText().toString()
                                    + "&password=" + newpassword
                                    + "&token=" + token, null,
                            response -> {
                                try {

                                    JSONObject json_raw = new JSONObject(response.toString());
                                    String iliad = json_raw.getString("iliad");
                                    JSONObject json = new JSONObject(iliad);

                                    String price = json.getString("0");


                                    if (price.equals("true")) {
                                        Toasty.success(ChargeActivity.this, price, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ChargeActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        button.setEnabled(true);
                                    } else {
                                        Toasty.error(ChargeActivity.this, price, Toast.LENGTH_SHORT).show();
                                        button.setEnabled(true);

                                    }


                                } catch (JSONException ignored) {
                                }

                            }, error -> {

                    });

                    queue1.add(getRequest1);

                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                super.onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
