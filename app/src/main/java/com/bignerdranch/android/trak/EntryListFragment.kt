package com.bignerdranch.android.trak

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.trak.database.EntryDao
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "EntryListFragment"

class EntryListFragment : Fragment() {
    /**
     * Required interface for hosting activities
     *
     */
    interface Callbacks {
        fun onEntrySelected(entryId: UUID);
    }

    private var callbacks: Callbacks? = null

    private val entryListViewModel: EntryListViewModel by lazy {
        ViewModelProviders.of(this).get(EntryListViewModel::class.java)
    }

    private lateinit var entryRecyclerView: RecyclerView;

    private var adapter: EntryAdapter? = EntryAdapter(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_entry_list, container, false)
        entryRecyclerView = view.findViewById(R.id.entry_recycler_view) as RecyclerView
        entryRecyclerView.layoutManager = LinearLayoutManager(context)
        entryRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryListViewModel.entryListLiveData.observe(
            viewLifecycleOwner,
            Observer { entries ->
                entries?.let { Log.i(TAG, "Got entries ${entries.size}") }
                updateUI(entries)
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_entry_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_entry -> {
                val entry = Entry()
                entryListViewModel.addEntry(entry)
                callbacks?.onEntrySelected(entry.id)
                true
            }

            R.id.clear_entries -> {
                var isIterationCompleted = false

                val observer = object : Observer<List<Entry>> {
                    override fun onChanged(entryList: List<Entry>?) {
                        if (!isIterationCompleted) {
                            entryList?.forEach { entry ->
                                entryListViewModel.deleteEntry(entry)
                            }
                            isIterationCompleted = true
                            entryListViewModel.entryListLiveData.removeObserver(this)
                        }
                    }
                }

                entryListViewModel.entryListLiveData.observe(this, observer)
                true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(entries: List<Entry>) {
        adapter = EntryAdapter(entries)
        entryRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): EntryListFragment {
            return EntryListFragment()
        }
    }

    private inner class EntryHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.progress_title) as TextView
        private val weightTextView: TextView =
            itemView.findViewById(R.id.progress_weight) as TextView
        private val dateTextView: TextView = itemView.findViewById(R.id.entry_date) as TextView
        private val timeTextView: TextView = itemView.findViewById(R.id.entry_time) as TextView
        private val restedImageView: ImageView =
            itemView.findViewById(R.id.entry_rested) as ImageView
        private lateinit var entry: Entry

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(entry: Entry) {
            this.entry = entry
            if (entry.title != "") {
                titleTextView.text = entry.title
            } else {
                titleTextView.text = "No Title"
            }
            weightTextView.text = entry.weight.toString() + "kg"
            dateTextView.text = SimpleDateFormat("dd-MMM-yyyy").format(entry.date)
            timeTextView.text = SimpleDateFormat("HH:mm").format(entry.time)
            restedImageView.visibility = if (entry.rested) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View?) {
            callbacks?.onEntrySelected(entry.id)
        }
    }

    private inner class EntryAdapter(var entries: List<Entry>) :
        RecyclerView.Adapter<EntryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryHolder {
            val view = layoutInflater.inflate(R.layout.list_item_entry, parent, false)
            return EntryHolder(view)
        }

        override fun onBindViewHolder(holder: EntryHolder, position: Int) {
            val entry = entries[position]
            holder.bind(entry)
        }

        override fun getItemCount(): Int = entries.size
    }
}