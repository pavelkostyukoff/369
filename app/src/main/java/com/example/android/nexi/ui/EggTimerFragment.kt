/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.nexi.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.android.nexi.DataKeeper
import com.example.android.nexi.MyFirebaseMessagingService
import com.example.android.nexi.R
import com.example.android.nexi.databinding.FragmentEggTimerBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

class EggTimerFragment : Fragment() {
    private val supportChatManager: MyFirebaseMessagingService? = null
    internal var Database_Path = "All_Image_Uploads_Database"
    var mStorageRef: StorageReference? = null
    val REQUEST_RUNTIME_PERMISSION = 1
    var storageReference: StorageReference? = null
    var databaseReference: DatabaseReference? = null
    private val PICK_IMAGE_CODE = 2
    private var Test = 0
    private var imageUri: Uri? = null
    private val TOPIC = "breakfast"

    private lateinit var binding: FragmentEggTimerBinding
    val permission = Manifest.permission.CAMERA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path)
        storageReference = FirebaseStorage.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().getReference()

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_egg_timer, container, false
        )
        val viewModel = ViewModelProviders.of(this).get(EggTimerViewModel::class.java)

        binding.eggTimerViewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        // TODO: Step 1.7 call create channel
        createChannel(
            getString(R.string.egg_notification_channel_id),
            getString(R.string.egg_notification_channel_name)
        )
        // TODO: Step 3.1 create a new channel for FCM
        createChannel(
            getString(R.string.breakfast_notification_channel_id),
            getString(R.string.breakfast_notification_channel_name)
        )
        // TODO: Step 3.4 call subscribe topics on start
        subscribeTopic()

        binding.selectPhoto.setOnClickListener {
            Test = 0
          /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions()
            } else {
                pickFromGallery()
            }*/
            if(ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(permission),
                    REQUEST_RUNTIME_PERMISSION)
                            takingPhoto()
            }
            else {
                takingPhoto()
            }
        }

        binding.selectPhoto3.setOnClickListener {
            Test = 1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions()
            } else {
                pickFromGallery()
            }
        }

        binding.button2.setOnClickListener {
           activity?.finish()
        }

        supportChatManager?.operatorMessage =  BehaviorSubject.create()

        supportChatManager?.operatorMessage?.let {
            it.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it
                }, {
                    it
                })
        }

        binding.selectPhoto2.setOnClickListener {
          //  UploadImageFileToFirebaseStorage()
            checkPremission()

        }
        binding.selectPhoto4.setOnClickListener {
            //  UploadImageFileToFirebaseStorage()
            checkPremission()

        }




        return binding.root

    }

     fun takingPhoto(){

                val intent =  Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent,REQUEST_RUNTIME_PERMISSION)
    }


    fun upload() {
       // val file = Uri.fromFile(File("path/to/images/profile.jpg"))
        if (imageUri != null) {
            //todo profile - вместо этого ключ для нотификации и ФИО

            val tokken = DataKeeper.getClientName(context!!)
            val riversRef = mStorageRef?.child(
                "drivers/_____" + tokken
            )

            riversRef?.putFile(imageUri!!)
                ?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    // Get a URL to the uploaded content
                    // val downloadUrl = taskSnapshot.getDownloadUrl()
                    taskSnapshot

                    Toast.makeText(context, "Фото успешно загружено!", Toast.LENGTH_SHORT).show()

                })
                ?.addOnFailureListener(OnFailureListener {
                    // Handle unsuccessful uploads
                    Toast.makeText(
                        context,
                        "Ошибка загрузки попроуйте позже или нет интернета!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // ...
                })
        }
        else {
            Toast.makeText(context, "Сделайте снимок!", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPremission() {
        //select which permission you want
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                context!!,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    permission
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(permission),
                    REQUEST_RUNTIME_PERMISSION
                )
            }
        } else {
            // you have permission go ahead launch service
           // SomeTask()
          //  UploadImageFileToFirebaseStorage()
          //  neW()
            upload()
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_RUNTIME_PERMISSION -> {
                val numOfRequest = grantResults.size
                val isGranted =
                    numOfRequest == 1 && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1]
                if (isGranted) {
                    // you have permission go ahead
                    SomeTask()
                } else {
                    // you dont have permission show toast
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun SomeTask() {
        val firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.getReferenceFromUrl("gs://project-4057851275006104726.appspot.com")
        val storageReference2nd = storageReference!!.child("All_image_uploads/"/*Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(imageUri!!)*/)

        storageReference2nd?.putFile(imageUri!!)
            ?.addOnSuccessListener { taskSnapshot ->
                // Getting image name from EditText and store into string variable.
                val TempImageName = "TEST"
                val imageUploadInfo =
                    ImageUploadInfo(TempImageName, taskSnapshot.uploadSessionUri.toString())
                // Getting image upload ID.
                val ImageUploadId = databaseReference?.push()?.key

                // Adding image upload id s child element into databaseReference.
                databaseReference?.child(ImageUploadId!!)?.setValue(imageUploadInfo)
            }
            // If something goes wrong .
            ?.addOnFailureListener { exception ->
                exception
            }



        storageReference!!.getBytes(java.lang.Long.MAX_VALUE).addOnSuccessListener { bytes ->
            var bytes = bytes



        }.addOnFailureListener { exception ->
            // Handle any errors
            exception
        }
    }

    private fun pickFromGallery() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        val chooserIntent = Intent.createChooser(getIntent, getString(R.string.gcm_defaultSenderId))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
                imageUri = imageUri
                if (Test == 0)
                {
                    Picasso.get().load(imageUri).placeholder(R.drawable.placeholder)
                        .into(binding.ciAvatar)
                }
                else {
                    Picasso.get().load(imageUri).placeholder(R.drawable.placeholder)
                        .into(binding.ciAvatar2)
                }
            }
        }
    }


    fun GetFileExtension(uri: Uri): String? {

        val contentResolver = activity?.getContentResolver()

        val mimeTypeMap = MimeTypeMap.getSingleton()

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver?.getType(uri))

    }


    fun UploadImageFileToFirebaseStorage() {
        val storageReference2nd = storageReference!!.child("All_image_uploads/"/*Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(imageUri!!)*/)



        storageReference2nd
        // Adding addOnSuccessListener to second StorageReference.
        storageReference2nd?.putFile(imageUri!!)
            ?.addOnSuccessListener { taskSnapshot ->
                // Getting image name from EditText and store into string variable.
                val TempImageName = "TEST"

                val imageUploadInfo =
                    ImageUploadInfo(TempImageName, taskSnapshot.uploadSessionUri.toString())

                // Getting image upload ID.
                val ImageUploadId = databaseReference?.push()?.key

                // Adding image upload id s child element into databaseReference.
                databaseReference?.child(ImageUploadId!!)?.setValue(imageUploadInfo)
            }
            // If something goes wrong .
            ?.addOnFailureListener { exception ->
                exception
            }

            // On progress change upload time.
            ?.addOnProgressListener {
it
            }


      /*  val TempImageName = "test"
        val imageUploadInfo =
            ImageUploadInfo(TempImageName, imageUri.toString())

        val ImageUploadId = databaseReference?.push()?.getKey()

        if (ImageUploadId != null) {
            databaseReference?.child(ImageUploadId)?.setValue(imageUploadInfo)
        }*/
    }
       /* if (FilePathUri != null) {
            val storageReference2nd = storageReference?.child(
                Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri!!)
            )
            storageReference2nd?.putFile(FilePathUri!!)
                ?.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    val TempImageName = "test"
                    val imageUploadInfo =
                        ImageUploadInfo(TempImageName, imageUri.toString())

                    val ImageUploadId = databaseReference?.push()?.getKey()

                    if (ImageUploadId != null) {
                        databaseReference?.child(ImageUploadId)?.setValue(imageUploadInfo)
                    }
                })
                ?.addOnFailureListener(OnFailureListener { exception ->

                })
    }*/
    private fun checkPermissions() {

      /*  if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )

        } else {*/
            pickFromGallery()
      //  }
    }

    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_HIGH
            )
                // TODO: Step 2.6 disable badges for this channel
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.breakfast_notification_channel_description)

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(notificationChannel)

        }
        // TODO: Step 1.6 END create channel
    }

    // TODO: Step 3.3 subscribe to breakfast topic
    private fun subscribeTopic() {
        // [START subscribe_topic]
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var message = getString(R.string.message_subscribed)
                if (!task.isSuccessful) {
                    message = getString(R.string.message_subscribe_failed)
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        // [END subscribe_topics]
    }

    companion object {
        fun newInstance() = EggTimerFragment()
    }

}
