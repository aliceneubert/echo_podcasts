package com.zacneubert.echo.api.callback;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.zacneubert.echo.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Created by zac on 5/27/17.
 */

public abstract class HandledCallback<T> implements CombinedCallback<T>, Callback<T> {
    private Context mContext;
    private int retryCount = 0;

    protected Response<T> response;

    private static List<Integer> retryWaitTimes = Collections.unmodifiableList(Arrays.asList(1000, 2000));

    public HandledCallback(Context context) {
        mContext = context;
        retryCount = 0;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        try {
            this.response = response;
            if (response.isSuccessful()) {
                onSuccessfulResponse(call, response);
            } else {
                switch (response.code()) {
                    case HTTP_UNAUTHORIZED:
                        // TODO get new auth token
                        onTotalFailure(call, R.string.response_error_401);
                        break;
                    case HTTP_FORBIDDEN:
                        // TODO get new auth token
                        break;
                    case HTTP_NOT_FOUND:
                        onTotalFailure(call, R.string.response_error_404);
                        break;
                    case HTTP_BAD_METHOD: //Method not allowed
                        onTotalFailure(call, R.string.response_error_405);
                        break;
                    case HTTP_INTERNAL_ERROR: //Internal Server Error
                        onTotalFailure(call, R.string.response_error_500);
                        break;
                    default:
                        //TODO Handle general unknown error
                        onTotalFailure(call, R.string.response_error_general);
                        break;
                }

                // Try to get good details
                if (response.errorBody() != null) {
                    String errorString = mContext.getString(R.string.response_error,
                            response.code(),
                            response.errorBody().string());
                } else {
                    // No error body?
                    String errorString = mContext.getString(R.string.response_error, response.code(), "Failed to get error body");
                }
            }
        } catch (IOException e) {
            // Report failure
            String errorString = mContext.getString(R.string.response_error, response.code(), response.errorBody().toString());
        } finally {
            onFinally();
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String url = call.request().url().toString();
        final Call callClone = call.clone();
        if (retryCount < retryWaitTimes.size()) {
            Runnable retry = new Runnable() {
                @Override
                public void run() {
                    callClone.enqueue(HandledCallback.this);
                }
            };
            new Handler().postDelayed(retry, retryWaitTimes.get(retryCount));
            retryCount++;
        } else {
            try {
                onTotalFailure(call, R.string.check_connection);
            } catch (Exception e) {
            } finally {
                onFinally();
            }
        }
    }

    @Override
    public void onTotalFailure(Call<T> call, int stringId) {
        if(stringId < 0) return;

        Toast.makeText(mContext, stringId, Toast.LENGTH_SHORT).show();
    }
}
