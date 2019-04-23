package com.kotdroid.osm.views.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotdroid.osm.views.activities.inflate


abstract class BaseFragment : Fragment() {

//    private val mMyCustomLoader: MyCustomLoader by lazy { MyCustomLoader(context) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return container?.inflate(layoutRes = layoutId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        // Set toolbar
//        if (null != toolbar) {
//            if (isNavigationBarEnabled) {
//                toolbar.setNavigationIcon(R.drawable.ic_menu_white)
//                toolbar.setNavigationOnClickListener {
////                          (activityContext as CameraActivity).openDrawer()
//                }
//            } else {
//                toolbar.setNavigationIcon(R.drawable.ic_back)
//                if (activityContext is BaseAppCompactActivity)
//                    toolbar.setNavigationOnClickListener {
//                        (activityContext as BaseAppCompactActivity)
//                                .supportFragmentManager.popBackStackImmediate()
//                    }
//            }
//        }

        init()
//        observeCommonData()
    }

//    private fun observeCommonData() {
//        // Observe any general exception
//        viewModel?.getErrorHandler()?.observe(this, Observer {
//            if (null != it) {
//                showMessage(resId = it.getErrorResource(), isShowSnackbarMessage = false)
//            }
//        })
//
//        // Observe user session expiration
//        viewModel?.isSessionExpired()?.observe(this, Observer {
//            if (it!!) {
//                expireUserSession()
//            }
//        })
//
//        // Observe visibility of loader
//        viewModel?.isShowLoader()?.observe(this, Observer {
//            if (it!!) {
//                showProgressLoader()
//            } else {
//                hideProgressLoader()
//            }
//        })
//
//        // Observe retrofit error messages
//        viewModel?.getRetrofitErrorMessage()?.observe(this, Observer {
//            showMessage(resId = it?.errorResId, message = it?.errorMessage, isShowSnackbarMessage = false)
//        })
//
//        // Observe screen specific data
//        observeData()
//    }

    val activityContext: Context
        get() = activity!!

    fun showMessage(resId: Int? = null, message: String? = null, isShowSnackbarMessage: Boolean = false) {
        if (isShowSnackbarMessage) {
//            mMyCustomLoader.showSnackBar(view, message ?: getString(resId!!))
        } else {
//            mMyCustomLoader.showToast(message ?: getString(resId!!))
        }
    }

    fun dismissDialogFragment() {
//        (fragmentManager!!.findFragmentByTag(getString(R.string.dialog)) as DialogFragment).dismiss()
    }

    protected fun navigateToMainActivity() {
//        startActivity(Intent(activityContext, HomeActivity::class.java)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
//        activity?.finish()
    }


    private fun showProgressLoader() {
//        mMyCustomLoader.showProgressDialog(getString(R.string.dialog))
    }

    private fun hideProgressLoader() {
//        mMyCustomLoader.dismissProgressDialog()
    }

    private fun expireUserSession() {
//        showMessage(R.string.session_expired, null, false)
//
//        //cancel scheduled task of workManager
//        WorkManager.getInstance().cancelAllWork()
//
//        startActivity(Intent(activity, HomeActivity::class.java)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
//        activity!!.finish()
    }

    abstract val layoutId: Int

    abstract val isNavigationBarEnabled: Boolean

//    abstract val viewModel: BaseViewModel?

    abstract fun init()

    abstract fun observeData()

}

