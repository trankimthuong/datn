package com.example.bluetoothkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_send_file.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SendFileActivity : AppCompatActivity() {
    var path: String? = null
    var textView_FileName: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_file)
        textView_FileName = findViewById<View>(R.id.textView_FileName) as TextView
        if (!canAccessLocation() || !canAccessCamera() || !canAccessWriteStorage() || !canAccessReadStorage() || !canAccessReadContacts() || !canAccessWriteContacts()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> if (canAccessWriteStorage()) {
                //reload my activity with permission granted or use the features what required the permission
                println("permission grantedddd")
            } else {
                Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun sendViaBluetooth(v: View?) {
        if (path == null) {
            Toast.makeText(this, "Please select file first", Toast.LENGTH_SHORT).show()
            return
        }
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
        } else {
            enableBluetooth()
        }
    }

    fun getFile(v: View?) {
        val mediaIntent = Intent(Intent.ACTION_GET_CONTENT)
        mediaIntent.type = "*/*" //set mime type as per requirement
        startActivityForResult(mediaIntent, 1001)
    }

    fun enableBluetooth() {
        val discoveryIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION)
        startActivityForResult(discoveryIntent, REQUEST_BLU)
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "*/*"
            val f = File(path)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f))
            val pm = packageManager
            val appsList = pm.queryIntentActivities(intent, 0)
            if (appsList.size > 0) {
                var packageName: String? = null
                var className: String? = null
                var found = false
                for (info in appsList) {
                    packageName = info.activityInfo.packageName
                    if (packageName == "com.android.bluetooth") {
                        className = info.activityInfo.name
                        found = true
                        break
                    }
                }
                if (!found) {
                    Toast.makeText(this, "Bluetooth havn't been found",
                        Toast.LENGTH_LONG).show()
                } else {
                    intent.setClassName(packageName!!, className!!)
                    startActivity(intent)
                }
            }
        } else if (requestCode == 1001
            && resultCode == Activity.RESULT_OK) {
            val uriPath = data!!.data
            Log.d("", "Video URI= $uriPath")
            path = getPath(this, uriPath) // "/mnt/sdcard/FileName.mp3"
            println("pathhhh $path")
            textView_FileName!!.text = path
        } else {
            Toast.makeText(this, "Bluetooth is cancelled", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun canAccessWriteStorage(): Boolean {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun canAccessReadStorage(): Boolean {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun canAccessReadContacts(): Boolean {
        return hasPermission(Manifest.permission.READ_CONTACTS)
    }

    private fun canAccessWriteContacts(): Boolean {
        return hasPermission(Manifest.permission.WRITE_CONTACTS)
    }

    private fun canAccessCamera(): Boolean {
        return hasPermission(Manifest.permission.CAMERA)
    }

    private fun canAccessLocation(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm)
    }

    companion object {
        private const val DISCOVER_DURATION = 300
        private const val REQUEST_BLU = 1
        private val INITIAL_PERMS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION)
        private const val INITIAL_REQUEST = 1337
        private const val REQUEST_WRITE_STORAGE = INITIAL_REQUEST + 4

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        fun getPath(context: Context, uri: Uri?): String? {
            val isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                    null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri?): Boolean {
            return "com.android.externalstorage.documents" == uri!!.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri?): Boolean {
            return "com.android.providers.downloads.documents" == uri!!.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri?): Boolean {
            return "com.android.providers.media.documents" == uri!!.authority
        }
    }
}
