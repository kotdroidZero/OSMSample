package com.kotdroid.osm.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.StringRes

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val isShowLoader = MutableLiveData<Boolean>()
    protected val isShowNoDataText = MutableLiveData<Boolean>()
    protected val isShowSwipeRefreshLayout = MutableLiveData<Boolean>()
    protected val isSessionExpired = MutableLiveData<Boolean>()
//    protected val retrofitErrorDataMessage = MutableLiveData<RetrofitErrorMessage>()
//    protected val retrofitErrorMessage = MutableLiveData<RetrofitErrorMessage>()
    protected val errorHandler = MutableLiveData<ErrorHandler>()
//    protected val mCompositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    fun isSessionExpired(): LiveData<Boolean> = isSessionExpired

    fun isShowLoader(): LiveData<Boolean> = isShowLoader

    fun isShowNoDataText(): LiveData<Boolean> = isShowNoDataText

    fun isShowSwipeRefreshLayout(): LiveData<Boolean> = isShowSwipeRefreshLayout

//    fun getRetrofitErrorDataMessage(): LiveData<RetrofitErrorMessage> = retrofitErrorDataMessage

//    fun getRetrofitErrorMessage(): LiveData<RetrofitErrorMessage> = retrofitErrorMessage

    fun getErrorHandler(): LiveData<ErrorHandler> = errorHandler

    enum class ErrorHandler(@StringRes private val resourceId: Int) : ErrorEvent {;
//        EMPTY_FULLNAME(R.string.empty_full_name),
//        EMPTY_NAME(R.string.empty_name),
//        EMPTY_REASON(R.string.empty_reason),
//        EMPTY_PLACE_NAME(R.string.empty_place_name),
//        EMPTY_CIRCLE_NAME(R.string.empty_circle_name),
//        EMPTY_QUERY(R.string.empty_query),
//        EMPTY_EMAIL(R.string.empty_email),
//        EMPTY_PHONE(R.string.empty_phone_no),
//        EMPTY_COUNTRY_CODE(R.string.empty_country_code),
//        INVALID_EMAIL(R.string.invalid_email),
//        EMPTY_PASSWORD(R.string.empty_password),
//        INVALID_PASSWORD(R.string.invalid_password),
//        EMPTY_OLD_PASSWORD(R.string.empty_old_password),
//        EMPTY_NEW_PASSWORD(R.string.empty_new_password),
//        EMPTY_CONFIRM_PASSWORD(R.string.empty_confirm_password),
//        NEW_CONFIRM_PASSWORD_MISMATCH(R.string.new_confirm_password_mismatch),
//        INVALID_NEW_PASSWORD(R.string.invalid_new_password),
//        PASSWORD_NOT_MATCHED(R.string.password_not_matched),
//        EMPTY_INVITE_CODE(R.string.empty_invite_code),
//        INVALID_INVITE_CODE(R.string.invalid_invite_code);

        override fun getErrorResource() = resourceId
    }

    interface ErrorEvent {
        @StringRes
        fun getErrorResource(): Int
    }
}