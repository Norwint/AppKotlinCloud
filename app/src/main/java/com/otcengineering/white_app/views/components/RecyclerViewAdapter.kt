package com.otcengineering.white_app.views.components

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Multimaps.index
import java.util.*
import com.otcengineering.white_app.BR
import com.otcengineering.white_app.utils.interfaces.OnClickListener

/**
 *  This is used to inflate the row layout with the recyclerview.
 *
 * @param T Type of model class which have data to set on the row layout.
 * @property rowLayout Android layout file to be inflated as the recyclerview row
 */
open class RecyclerViewAdapter<T>(
    private val rowLayout: Int,
    private val extender: (() -> ViewHolderExtender<ViewDataBinding, T>)? = null
) : RecyclerView.Adapter<RecyclerViewAdapter<T>.ViewHolder<ViewDataBinding>>(), Filterable {


    var onClickListener: OnClickListener<T>? = null

    // Text filter used for search
    private var textFilterApply: TextFilterApply<T>? = null

    // List to store the items
    private var list: MutableList<T> = mutableListOf()
    private val originalList: MutableList<T> = mutableListOf()

    /**
     * Get the list of  stored items
     *
     * @return Array of model
     */
    fun getList(): List<T> {
        return list
    }

    /**
     * Apply the search on the items in the list.
     *
     * @param textFilterApply Instance of [TextFilterApply]
     */
    fun setTextFilterApply(textFilterApply: TextFilterApply<T>) {
        this.textFilterApply = textFilterApply
    }

    /**
     * Set the list items
     *
     * @param list List of items
     */
    fun setList(list: List<T>?) {
        this.list.clear()
        if (list != null)
            this.list.addAll(list)

        this.originalList.clear()
        if (list != null)
            this.originalList.addAll(list)

        notifyDataSetChanged()
    }

    /**
     * Add item in the recycler view
     *
     * @param model Model to be added
     */
    fun addItem(model: T) {
        list.add(model)
        originalList.add(model)
        notifyItemInserted(list.indexOf(model))
    }

    /**
     * Remove item in the recycler view
     *
     * @param model Model to be removed
     */
    fun remove(model: T) {
        list.remove(model)
        originalList.remove(model)
        notifyItemRemoved(list.indexOf(model))
    }

    /**
     * Update item in the recycler view
     *
     * @param model Model to be updated
     */
    fun updateItem(model: T, index: Int) {
        list[index] = model
        originalList[index] = model
        notifyItemChanged(index)
    }

    /**
     * Clear all the items from the recycler view
     *
     */
    fun clearItems() {
        this.list.clear()
        this.originalList.clear()
        notifyDataSetChanged()
    }

    /**
     * Attach the observable list
     *
     * @param observableList List of items as [ObservableList] instance
     */
    fun addObservableList(observableList: ObservableList<T>) {

        observableList.addOnListChangedCallback(object :
            ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(sender: ObservableList<T>) {
                if (sender.size == 0) clearItems()
                setList(sender)
            }

            override fun onItemRangeChanged(
                sender: ObservableList<T>,
                positionStart: Int,
                itemCount: Int
            ) {
                if (sender.size == 0) {
                    clearItems()
                    return
                }
                for (t in sender.subList(positionStart, positionStart + itemCount)) {
                    updateItem(t, observableList.indexOf(t))
                }
            }

            override fun onItemRangeInserted(
                sender: ObservableList<T>,
                positionStart: Int,
                itemCount: Int
            ) {
                if (sender.size == 0) {
                    clearItems()
                    return
                }
                for (t in sender.subList(positionStart, positionStart + itemCount)) {
                    addItem(t)
                }
            }

            override fun onItemRangeMoved(
                sender: ObservableList<T>,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
            }

            override fun onItemRangeRemoved(
                sender: ObservableList<T>,
                positionStart: Int,
                itemCount: Int
            ) {
                if (sender.size == 0) {
                    clearItems()
                    return
                }
                for (t in sender.subList(positionStart, positionStart + (itemCount - 1))) {
                    remove(t)
                }
            }
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ViewDataBinding> {
        val bind = DataBindingUtil
            .bind<ViewDataBinding>(
                LayoutInflater.from(parent.context)

                    .inflate(rowLayout, parent, false)
            )
        return ViewHolder(Objects.requireNonNull<ViewDataBinding>(bind), extender)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder<ViewDataBinding>, position: Int) {
        val model = list[position]
        viewHolder.binding.setVariable(BR.model, model)
        viewHolder.binding.setVariable(BR.index, list.indexOf(model))
        //        if (holder.getBinding().hasPendingBindings())
        viewHolder.binding.setVariable(BR.itemClickListener, onClickListener)
        viewHolder.onBind(model)
        viewHolder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder<V : ViewDataBinding> constructor(
        val binding: V,
        extenderFun: (() -> ViewHolderExtender<ViewDataBinding, T>)? = null
    ) : RecyclerView.ViewHolder(binding.root) {

        private val extender: ViewHolderExtender<ViewDataBinding, T>? =
            extenderFun?.invoke()?.apply {
                onInit(binding)
            }

        fun onBind(model: T) = extender?.onBind(binding, model)

        fun onClear() = extender?.onClear()

    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.values != null) {
                    list = results.values as MutableList<T>
                    notifyDataSetChanged()
                }

            }

            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredResults: List<T>? = if (constraint.isEmpty()) {
                    originalList
                } else {
                    getFilteredResults(constraint.toString().lowercase(Locale.getDefault()))
                }
                val results = FilterResults()
                results.values = filteredResults

                return results
            }
        }
    }

    protected fun getFilteredResults(constraint: String): List<T> {
        val results = ArrayList<T>()
        for (item in originalList) {
            if (textFilterApply!!.apply(constraint, item)) results.add(item)
        }
        return results
    }

    /**
     * Interface to apply filter on the list
     *
     * @param T
     */
    interface TextFilterApply<T> {
        fun apply(filterText: String, model: T): Boolean
    }

    /**
     * Filter the items by text
     *
     * @param filterText String text used to filter the list
     */
    fun filterText(filterText: String) {
        filter.filter(filterText)
    }

    interface ViewHolderExtender<V : ViewDataBinding, T> {
        fun onInit(binding: V)
        fun onBind(binding: V, model: T)
        fun onClear()
    }

}

/**
 * Extension function used to add the data source on the any recyclerview
 *
 * @param T
 * @param rowLayout
 * @param observableList
 * @param onItemClickListener
 */
fun <T> RecyclerView.addSource(
    rowLayout: Int,
    observableList: ObservableList<T>,
    onClickListener: OnClickListener<T>? = null
) {
    addSource(rowLayout, observableList, onClickListener, null)
}

fun <T> RecyclerView.addSource(
    rowLayout: Int,
    observableList: ObservableList<T>,
    onClickListener: OnClickListener<T>? = null,
    extender: (() -> RecyclerViewAdapter.ViewHolderExtender<ViewDataBinding, T>)? = null
) {
    // Set default linear layout manage if null
    if (layoutManager == null)
        layoutManager = LinearLayoutManager(context)

    val adapter = RecyclerViewAdapter<T>(rowLayout, extender)
    adapter.setList(observableList)
    adapter.addObservableList(observableList)
    adapter.onClickListener = onClickListener
    this.adapter = adapter
}


fun <T> RecyclerView.addSource(
    rowLayout: Int,
    observableList: List<T>,
    OnClickListener: OnClickListener<T>? = null,
    extender: (() -> RecyclerViewAdapter.ViewHolderExtender<ViewDataBinding, T>)? = null
) {
    // Set default linear layout manage if null
    if (layoutManager == null)
        layoutManager = LinearLayoutManager(context)

    val adapter = RecyclerViewAdapter(rowLayout, extender)
    adapter.setList(observableList)
    adapter.onClickListener = OnClickListener
    this.adapter = adapter
}


fun <T> RecyclerView.addSource(
    rowLayout: Int,
    observableList: List<T>,
    onClickListener: OnClickListener<T>? = null
) {
    addSource(rowLayout, observableList, onClickListener, null)
}
