package com.example.carpoolapp.ui.sign_up

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carpoolapp.R
import com.example.carpoolapp.data.models.User
import com.example.carpoolapp.databinding.SignUpFragmentBinding
import com.example.carpoolapp.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SignUpFragment : Fragment() {

    private var _binding: SignUpFragmentBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()
    private var photoUri: Uri? = null
    private var random: Long? = null

    private lateinit var auth: FirebaseAuth

    private val pickLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                viewModel.setImageUri(it)
            }
        }

    private val cameraResultLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uri ->
                    val bitmap = BitmapFactory.decodeStream(
                        requireContext().contentResolver.openInputStream(uri)
                    )
                    bitmap?.let { bmp ->
                        saveImageToGallery(bmp)
                        viewModel.setImageUri(uri)
                    }
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePicture()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_must_approve_permissions), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignUpFragmentBinding.inflate(inflater, container, false)

        auth = (activity as MainActivity).auth

        viewModel.imageUri.observe(viewLifecycleOwner) {
            binding.ivPicture.setImageURI(viewModel.imageUri.value)
        }

        binding.btnPickPicture.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.btnTakePicture.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val password = binding.tvPassword.text.toString()
            val name = binding.tvName.text.toString()
            val phone = binding.tvPhone.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty() && viewModel.imageUri.value != null) {
                regFunc(name, email, password, phone)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_input),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            takePicture()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    takePicture()
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun takePicture() {
        val values = ContentValues().apply {
            random = System.currentTimeMillis()
            put(MediaStore.Images.Media.DISPLAY_NAME, "$random.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val contentResolver = requireContext().contentResolver
        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        photoUri?.let {
            cameraResultLauncher.launch(it)
        }

        if (photoUri == null) {
            val file = File(
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "$random.jpg"
            )
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${context?.packageName}.provider",
                file
            )
            photoUri?.let {
                cameraResultLauncher.launch(it)
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        photoUri?.let { uri ->
            val contentResolver = requireContext().contentResolver

            val inputStream = contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(uri.path),
                arrayOf("image/jpeg")
            ) { _, _ ->
                Log.d("ImageSave", "Image saved and scanned successfully")
            }
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun regFunc(name: String, email: String, password: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.registration_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let {
                        saveUserToDatabase(it, name, password, phone)
                        val saveNameBundle = Bundle()
                        saveNameBundle.putString("user_id", user.uid)
                        findNavController().navigate(
                            R.id.action_signUpFragment_to_findFragment,
                            saveNameBundle
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.registration_failed), Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    private fun saveUserToDatabase(
        user: FirebaseUser,
        name: String,
        password: String,
        phone: String,
    ) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val userData = mapOf(
            "name" to name,
            "email" to user.email,
            "phone" to phone,
            "picture" to "images/${user.uid}/${viewModel.imageUri.value?.lastPathSegment}"
        )
        user.email?.let {
            val newUser = User(user.uid, it, password)
            viewModel.addUser(newUser)
        }

        databaseReference.child(user.uid).setValue(userData)
        val userImagesRef =
            storageRef.child("images/${user.uid}/${viewModel.imageUri.value?.lastPathSegment}")

        if (viewModel.imageUri.value != null) {
            userImagesRef.putFile(viewModel.imageUri.value!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}