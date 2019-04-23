package com.kotdroid.osm.views.activities

import android.os.Bundle
import android.support.annotation.AnimatorRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BaseAppCompactActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
//        if (GeneralFunctions.isAboveLollipopDevice) {
//            val window = window
//            if (isMakeStatusBarTransparent) {
//                window.statusBarColor = ContextCompat.getColor(this, R.color.colorTransparent)
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            } else {
//                window.statusBarColor = ContextCompat.getColor(this,
//                        R.color.colorPrimaryDark)
//            }
//        }

        init()
    }

    override fun onBackPressed() {
        if (null != supportFragmentManager && 1 < supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            finish()
            super.onBackPressed()
        }
    }


    abstract val layoutId: Int

    abstract val isMakeStatusBarTransparent: Boolean

    abstract fun init()

}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun AppCompatActivity.doFragmentTransaction(fragManager: FragmentManager = supportFragmentManager,
                                            @IdRes containerViewId: Int,
                                            fragment: Fragment,
                                            tag: String = "",
                                            @AnimatorRes enterAnimation: Int = 0,
                                            @AnimatorRes exitAnimation: Int = 0,
                                            @AnimatorRes popEnterAnimation: Int = 0,
                                            @AnimatorRes popExitAnimation: Int = 0,
                                            isAddFragment: Boolean = true,
                                            isAddToBackStack: Boolean = true,
                                            allowStateLoss: Boolean = false) {

    val fragmentTransaction = fragManager.beginTransaction()
            .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)

    if (isAddFragment) {
        fragmentTransaction.add(containerViewId, fragment, tag)
    } else {
        fragmentTransaction.replace(containerViewId, fragment, tag)
    }

    if (isAddToBackStack) {
        fragmentTransaction.addToBackStack(null)
    }

    if (allowStateLoss) {
        fragmentTransaction.commitAllowingStateLoss()
    } else {
        fragmentTransaction.commit()
    }
}
