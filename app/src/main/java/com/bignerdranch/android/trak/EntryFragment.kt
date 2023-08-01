package com.bignerdranch.android.trak

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_ENTRY_ID = "entry_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE = 2
private const val DATE_FORMAT = "EEE, MM, dd"

class EntryFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var entry: Entry
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button

    private val entryDetailViewModel: EntryDetailViewModel by lazy {
        ViewModelProviders.of(this).get(EntryDetailViewModel::class.java)
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
        titleField = view.findViewById(R.id.entry_title) as EditText
        dateButton = view.findViewById(R.id.entry_date) as Button
        solvedCheckBox = view.findViewById(R.id.entry_solved) as CheckBox
        reportButton = view.findViewById(R.id.entry_report) as Button
        suspectButton = view.findViewById(R.id.entry_suspect) as Button
        callButton = view.findViewById(R.id.entry_call) as Button
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
        dateButton.text = entry.date.toString()
        solvedCheckBox.apply {
            isChecked = entry.isSolved
            jumpDrawablesToCurrentState()
        }

        if (entry.suspect.isNotEmpty()) {
            suspectButton.text = entry.suspect
        }
    }

    private fun getEntryReport(): String {
        val solvedString = if (entry.isSolved) {
            getString(R.string.entry_report_solved)
        } else {
            getString(R.string.entry_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, entry.date).toString()
        val suspect = if (entry.suspect.isBlank()) {
            getString(R.string.entry_report_no_suspect)
        } else {
            getString(R.string.entry_report_suspect, entry.suspect)
        }
        return getString(R.string.entry_report, entry.title, dateString, solvedString, suspect)
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

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> entry.isSolved = isChecked }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(entry.date).apply {
                setTargetFragment(this@EntryFragment, REQUEST_DATE)
                show(this@EntryFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getEntryReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.entry_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            Log.d("SuspectButton", resolvedActivity.toString())
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        callButton.apply {

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
                    val suspect = it.getString(0)
                    entry.suspect = suspect
                    entryDetailViewModel.saveEntry(entry)
                    suspectButton.text = suspect
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
        }
    }

    override fun onDateSelected(date: Date) {
        entry.date = date
        updateUI()
    }
}