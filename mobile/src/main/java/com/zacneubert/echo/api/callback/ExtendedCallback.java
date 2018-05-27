package com.zacneubert.echo.api.callback;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by zac on 5/27/17.
 */

public interface ExtendedCallback<T> {
    void onFinally();
    void onSuccessfulResponse(Call<T> call, Response<T> response);
    void onTotalFailure(Call<T> call, int stringId);
}
