package com.btcmarket.module;

import com.facebook.react.bridge.*;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class CryptoUtils extends ReactContextBaseJavaModule{
    private final static String HMAC_SHA512="HmacSHA512";

    CryptoUtils(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName(){
        return "CryptoUtils";
    }

    @ReactMethod
    public void hmac(String key, String input, Callback callback) throws Exception{
        Mac mac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), HMAC_SHA512);
        mac.init(keySpec);
        byte [] macdata = mac.doFinal(input.getBytes("UTF-8"));
        callback.invoke(Base64.encodeToString(macdata, Base64.NO_WRAP));
    }
}
