package com.zacneubert.echo.api.callback;

import retrofit2.Callback;

/**
 * Created by zac on 5/26/17.
 */

public interface CombinedCallback<T> extends Callback<T>, ExtendedCallback<T> {

}
