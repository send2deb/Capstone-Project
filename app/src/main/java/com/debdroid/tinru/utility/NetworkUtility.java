package com.debdroid.tinru.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.debdroid.tinru.R;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import timber.log.Timber;

public class NetworkUtility {
    private static final String AMADEUS_SANDBOX_BASE_URL = "https://api.sandbox.amadeus.com/v1.2/";
    private static final String GOOGLE_PLACES_API_BASE_URL = "https://maps.googleapis.com/";

    /**
     * This method returns the base url of the recipe
     * @return The recipe base url
     */
    public static String getAmadeusSandboxBaseUrl() {
        return AMADEUS_SANDBOX_BASE_URL;
    }

    /**
     * This method returns the base url of the recipe
     * @return The recipe base url
     */
    public static String getGooglePlaceApiBaseUrl() {
        return GOOGLE_PLACES_API_BASE_URL;
    }

    /**
     * This method checks if the device is connected to a network to perform network operation.
     * @param ctx Application Context
     * @return True if WiFi or Mobile network is available otherwise returns False
     */
    public static boolean isOnline(final Context ctx) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        return activeInfo != null && activeInfo.isConnected();
    }

    /**
     * This method shows a Toast message if the device is not online.
     * @param context Application Context
     */
    public static void checkInternetConnection(Context context) {
        boolean networkStatus = NetworkUtility.isOnline(context);
        if(!networkStatus) {
            Timber.d(context.getString(R.string.no_network_error_msg));
            Toast.makeText(context, context.getString(R.string.no_network_error_msg),Toast.LENGTH_SHORT).show();
        } else {
            Timber.d("Internet connection available");
        }
    }

    /**
     * Custom method to handle SSLHandshakeException
     * Courtesy - https://mobikul.com/android-retrofit-handling-sslhandshakeexception/
     * @return OkHttp client builder
     */
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with the all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
