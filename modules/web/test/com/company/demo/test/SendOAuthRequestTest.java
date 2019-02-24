package com.company.demo.test;

import okhttp3.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class SendOAuthRequestTest {

    @Test
    public void send() throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.get("http://localhost:8080/app/oauth/login")
                .newBuilder();

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", "admin")
                .add("password", "admin")
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic Y2xpZW50OnNlY3JldA==")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(urlBuilder.build())
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();

        ResponseBody body = response.body();
        assertNotNull(body);

        String result = body.string();
        assertNotNull(result);
    }
}