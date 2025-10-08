package com.payment.payment;

import java.io.IOException;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class PayMongoService {

    @Value("${paymongo.api.key.secret}")
    private String paymongoSecretKey;

    private final OkHttpClient client = new OkHttpClient();

    private String createCheckoutSession(Payment payment, String... paymentMethodTypes) throws IOException {
        int amountInCents = (int) (payment.getAmount().doubleValue() * 100);

        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject attributes = new JSONObject();
        JSONArray lineItems = new JSONArray();
        JSONObject lineItem = new JSONObject();
        JSONArray paymentMethods = new JSONArray(paymentMethodTypes);

        lineItem.put("currency", "PHP");
        lineItem.put("amount", amountInCents);
        lineItem.put("name", "Payment for " + payment.getName());
        lineItem.put("quantity", 1);
        lineItems.put(lineItem);

        attributes.put("line_items", lineItems);
        attributes.put("payment_method_types", paymentMethods);

        attributes.put("success_url", "https://interlunar-maria-undemocratically.ngrok-free.dev/payment/payment-successful?paymentId=" + payment.getId());
        attributes.put("cancel_url", "https://interlunar-maria-undemocratically.ngrok-free.dev/payment/failed");


        JSONObject metadata = new JSONObject();
        metadata.put("internal_payment_id", payment.getId().toString());
        attributes.put("metadata", metadata);

        data.put("attributes", attributes);
        payload.put("data", data);

        RequestBody body = RequestBody.create(
            payload.toString(),
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url("https://api.paymongo.com/v1/checkout_sessions")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((paymongoSecretKey + ":").getBytes()))
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " with body " + responseBody);
            }

            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getJSONObject("data")
                               .getJSONObject("attributes")
                               .getString("checkout_url");
        }
    }


    public String createGcashSource(Payment payment) throws IOException {
        return createCheckoutSession(payment, "gcash");
    }

    public String createPayMayaSource(Payment payment) throws IOException {
        return createCheckoutSession(payment, "paymaya");
    }

    public String createCardPayment(Payment payment) throws IOException {
        return createCheckoutSession(payment, "card");
    }
    
}