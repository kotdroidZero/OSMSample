package com.kotdroid.osm.utils

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import com.kotdroid.osm.R

class MarshMallowPermissions(private val mFragment: Fragment) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 99
        const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 178
        const val VIDEO_GROUP_PERMISSION_REQUEST_CODE = 190
        const val CAMERA_GROUP_PERMISSION_REQUEST_CODE = 190

        private val VIDEO_GROUP_PERMISSION = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        private val CAMERA_GROUP_PERMISSION = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }


    private var mActivity: FragmentActivity = mFragment.activity!!

    val isPermissionGrantedForWriteExtStorage: Boolean
        get() = ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestPermisssionForWriteStorage() {
        if (mFragment.shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showAlertDialog(mFragment.getString(R.string.camera_permission_needed),
                    DialogInterface.OnClickListener { dialog, which ->
                        mFragment.requestPermissions(
                                arrayOf(Manifest.permission.CAMERA),
                                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
                    }, null)
        } else {
            mFragment.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }


    fun requestPermissionForLocation() {
        if (mFragment.shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            showAlertDialog(mFragment.getString(R.string.location_permission_needed),
                    DialogInterface.OnClickListener { dialog, which ->
                        mFragment.requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE)
                    }, null)
        } else {
            mFragment.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
        }
    }


    val isPermissionGrantedForCamera: Boolean
        get() = ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED


    fun requestPermisssionForCamera() {
        if (mFragment.shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA)) {
            showAlertDialog(mFragment.getString(R.string.camera_permission_needed),
                    DialogInterface.OnClickListener { dialog, which ->
                        mFragment.requestPermissions(
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_PERMISSION_REQUEST_CODE)
                    }, null)
        } else {
            mFragment.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE)
        }
    }


    fun hasGroupPermissionGrantedForVideo(): Boolean {
        for (permission in VIDEO_GROUP_PERMISSION) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun hasGroupPermissionGrantedForCamera(): Boolean {
        for (permission in CAMERA_GROUP_PERMISSION) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun requestGroupPermissionForVideo() {
        if (shouldShowRequestPermissionRationale(VIDEO_GROUP_PERMISSION)) {
            showAlertDialog(mFragment.getString(R.string.permission_required),
                    DialogInterface.OnClickListener { dialog, which ->
                        mFragment.requestPermissions(
                                VIDEO_GROUP_PERMISSION,
                                VIDEO_GROUP_PERMISSION_REQUEST_CODE)
                    }, null)
        } else {
            mFragment.requestPermissions(VIDEO_GROUP_PERMISSION, VIDEO_GROUP_PERMISSION_REQUEST_CODE)
        }
    }

    fun requestGroupPermissionForCamera() {
        if (shouldShowRequestPermissionRationale(CAMERA_GROUP_PERMISSION)) {
            showAlertDialog(mFragment.getString(R.string.permission_required),
                    DialogInterface.OnClickListener { dialog, which ->
                        mFragment.requestPermissions(
                                CAMERA_GROUP_PERMISSION,
                                CAMERA_GROUP_PERMISSION_REQUEST_CODE)
                    }, null)
        } else {
            mFragment.requestPermissions(CAMERA_GROUP_PERMISSION, CAMERA_GROUP_PERMISSION_REQUEST_CODE)
        }
    }


    private fun showAlertDialog(message: String,
                                okListener: DialogInterface.OnClickListener,
                                cancelListener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(mActivity.getString(R.string.ok), okListener)
                .setNegativeButton(mActivity.getString(R.string.cancel), cancelListener)
                .create()
                .show()
    }

    private fun shouldShowRequestPermissionRationale(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (mFragment.shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }
        return false
    }

}