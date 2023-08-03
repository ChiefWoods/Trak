package com.bignerdranch.android.trak

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

private const val ARG_ENTRY_ID = "entry_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE = 2
private const val REQUEST_TIME = 3
private const val REQUEST_PHOTO = 4
private const val DATE_FORMAT = "EEE, MM, dd"
private const val TIME_FORMAT = "HH : mm : ss"

class EntryFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var entry: Entry
    private lateinit var titleField: EditText
    private lateinit var restedCheckBox: CheckBox
    private lateinit var progressPhoto: ImageView
    private lateinit var cameraButton: ImageButton
    private lateinit var weightField: EditText
    private lateinit var gymField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var chooseTrainerButton: Button
    private lateinit var callTrainerButton: Button
    private lateinit var shareButton: Button
    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    private val entryDetailViewModel: EntryDetailViewModel by lazy {
        ViewModelProviders.of(this).get(EntryDetailViewModel::class.java)
    }

    private val entryListViewModel: EntryListViewModel by lazy {
        ViewModelProviders.of(this).get(EntryListViewModel::class.java)
    }

    companion object {
        fun newInstance(entryId: UUID): EntryFragment {
            val args = Bundle().apply {
                putSerializable(ARG_ENTRY_ID, entryId)
            }
            return EntryFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entry = Entry()
        val entryId: UUID = arguments?.getSerializable(ARG_ENTRY_ID) as UUID
        entryDetailViewModel.loadEntry(entryId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_entry, container, false)
        titleField = view.findViewById(R.id.progress_title) as EditText
        restedCheckBox = view.findViewById(R.id.rested_yesterday) as CheckBox
        progressPhoto = view.findViewById(R.id.progress_photo) as ImageView
        cameraButton = view.findViewById(R.id.progress_camera) as ImageButton
        weightField = view.findViewById(R.id.progress_weight) as EditText
        gymField = view.findViewById(R.id.gym_name) as EditText
        dateButton = view.findViewById(R.id.entry_date) as Button
        timeButton = view.findViewById(R.id.entry_time) as Button
        chooseTrainerButton = view.findViewById(R.id.choose_trainer) as Button
        callTrainerButton = view.findViewById(R.id.call_trainer) as Button
        shareButton = view.findViewById(R.id.share_progress) as Button
        deleteButton = view.findViewById(R.id.delete_entry) as Button
        saveButton = view.findViewById(R.id.save_entry) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryDetailViewModel.entryLiveData.observe(
            viewLifecycleOwner
        ) { entry ->
            entry?.let {
                this.entry = entry
                updateUI()
            }
        }
    }

    private fun updateUI() {
        titleField.setText(entry.title)
        weightField.setText((entry.weight.toString()))
        gymField.setText(entry.gym)
        dateButton.text = SimpleDateFormat("dd-MMM-yyyy").format(entry.date)
        timeButton.text = SimpleDateFormat("HH:mm").format(entry.time)
        restedCheckBox.apply {
            isChecked = entry.rested
            jumpDrawablesToCurrentState()
        }

        if (entry.trainer.isNotEmpty()) {
            chooseTrainerButton.text = entry.trainer
        }

        if (entry.photo != null) {
            progressPhoto.setImageBitmap(entry.photo)
        }
    }

    private fun getEntryReport(): String {
        val restedString = if (entry.rested) {
            getString(R.string.progress_report_rested)
        } else {
            getString(R.string.progress_report_unrested)
        }
        val dateString = DateFormat.format(DATE_FORMAT, entry.date).toString()
        val timeString = DateFormat.format(TIME_FORMAT, entry.time).toString()
        val trainer = if (entry.trainer.isBlank()) {
            getString(R.string.progress_report_no_trainer)
        } else {
            getString(R.string.progress_report_trainer, entry.trainer)
        }
        return getString(
            R.string.progress_report,
            entry.title,
            dateString,
            timeString,
            entry.weight,
            entry.gym,
            restedString,
            trainer
        )
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                entry.title = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        val weightWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                try {
                    entry.weight = sequence.toString().toDouble()
                } catch (e: NumberFormatException) {
                    entry.weight = 0.0
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        val gymWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                entry.gym = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        restedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> entry.rested = isChecked }
        }

        cameraButton.apply {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    setOnClickListener {
                        startActivityForResult(takePictureIntent, REQUEST_PHOTO)
                    }
                }

                else -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_PHOTO
                    )
                }
            }
        }

        weightField.addTextChangedListener(weightWatcher)

        gymField.addTextChangedListener(gymWatcher)

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(entry.date).apply {
                setTargetFragment(this@EntryFragment, REQUEST_DATE)
                show(this@EntryFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(entry.time).apply {
                setTargetFragment(this@EntryFragment, REQUEST_TIME)
                show(this@EntryFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        shareButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getEntryReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.progress_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        chooseTrainerButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            Log.d("TrainerButton", resolvedActivity.toString())
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        callTrainerButton.apply {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) -> {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                        )
                    setOnClickListener {
                        startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
                    }
                }

                else -> {
                    // You can directly ask for the permission.
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_PHONE
                    )
                }
            }
        }
        saveButton.setOnClickListener {
            Toast.makeText(
                requireActivity(),
                "Entry saved!",
                Toast.LENGTH_SHORT
            ).show()
            activity?.onBackPressed()
        }

        deleteButton.setOnClickListener {
            entryListViewModel.deleteEntry(entry)
            Toast.makeText(
                requireActivity(),
                "Entry deleted!",
                Toast.LENGTH_SHORT
            ).show()
            activity?.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PHONE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK
                        ).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        }
                    startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Log.e("EntryFragment", "Unavailable permissions CONTACTS")
                }
                return
            }

            REQUEST_PHOTO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePictureIntent, REQUEST_PHOTO)
                } else {
                    Log.e("EntryFragment", "Unavailable permissions CAMERA")
                }
            }


            else -> {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        entryDetailViewModel.saveEntry(entry)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    // Verify that the cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val trainer = it.getString(0)
                    entry.trainer = trainer
                    entryDetailViewModel.saveEntry(entry)
                    chooseTrainerButton.text = trainer
                }
            }

            requestCode == REQUEST_PHONE && data != null -> {
                Log.i("Request Phone", "URI: ${data.data}")

                val contactUri = data.data
                val queries = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val cursor = requireActivity()
                    .contentResolver
                    .query(
                        contactUri!!,
                        queries,
                        null,
                        null,
                        null
                    )

                cursor?.use { it ->
                    if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()

                    val phoneIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    Log.d("Phone index", phoneIndex.toString())

                    val phone = it.getString(phoneIndex)

                    Log.d("EntryPhone", phone)
                    val number: Uri = Uri.parse("tel:$phone")

                    startActivity(Intent(Intent.ACTION_DIAL, number))
                }
            }

            requestCode == REQUEST_PHOTO && data != null -> {
                val imageBitmap = data?.extras?.get("data") as Bitmap?
                if (imageBitmap != null) {
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    progressPhoto.setImageBitmap(imageBitmap)
                    entry.photo = imageBitmap
                    entryDetailViewModel.saveEntry(entry)
                }
            }
        }
    }

    override fun onDateSelected(date: Date) {
        entry.date = date
        updateUI()
    }

    override fun onTimeSelected(time: Date) {
        entry.time = time
        updateUI()
    }
}