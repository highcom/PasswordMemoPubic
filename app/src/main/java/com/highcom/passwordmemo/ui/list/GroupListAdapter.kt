package com.highcom.passwordmemo.ui.list

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.highcom.passwordmemo.R
import com.highcom.passwordmemo.ui.list.GroupListAdapter.GroupViewHolder
import com.highcom.passwordmemo.util.TextSizeUtil

class GroupListAdapter(
    context: Context,
    private val listData: List<Map<String?, *>?>?,
    private val adapterListener: GroupAdapterListener
) : RecyclerView.Adapter<GroupViewHolder?>() {
    private val inflater: LayoutInflater
    private val layoutHeightMap: Map<Int, Float>
    var textSize = 15f
    var editEnable = false

    interface GroupAdapterListener {
        fun onGroupNameClicked(view: View, groupId: Long?)
        fun onGroupNameOutOfFocused(view: View, data: Map<String?, String?>)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rowLinearLayout: LinearLayout? = null
        var groupId: Long? = null
        var groupOrder = 0
        var groupImage: ImageButton? = null
        var groupName: EditText? = null
        private var orgGroupName = ""
        var groupRearrangeButton: ImageButton? = null

        init {
            // フッターの場合には何も設定しない
            if (itemView.id != R.id.row_footer) {
                rowLinearLayout = itemView.findViewById<View>(R.id.rowGroupLayout) as LinearLayout
                groupImage = itemView.findViewById<View>(R.id.folder_icon) as ImageButton
                groupName = itemView.findViewById<View>(R.id.groupName) as EditText
                groupImage?.setOnClickListener { view: View ->
                    if (!editEnable) {
                        orgGroupName = groupName?.text.toString()
                        adapterListener.onGroupNameClicked(view, groupId)
                    }
                }
                groupName?.setOnClickListener { view: View ->
                    orgGroupName = groupName?.text.toString()
                    adapterListener.onGroupNameClicked(view, groupId)
                }
                groupName?.onFocusChangeListener = OnFocusChangeListener { view: View, b: Boolean ->
                    if (b) {
                        orgGroupName = groupName?.text.toString()
                        adapterListener.onGroupNameClicked(view, groupId)
                    } else {
                        // グループ名を空欄で登録することは出来ないので元の名前にする
                        if (groupName?.text.toString() == "") {
                            groupName?.setText(orgGroupName)
                        }
                        val data: MutableMap<String?, String?> = HashMap()
                        data["group_id"] = java.lang.Long.valueOf(groupId!!).toString()
                        data["group_order"] = Integer.valueOf(groupOrder).toString()
                        data["name"] = groupName?.text.toString()
                        // フォーカスが外れた時に内容が変更されていたら更新する
                        adapterListener.onGroupNameOutOfFocused(view, data)
                    }
                }
                // キーボードのエンターが押下された場合
                groupName?.setOnEditorActionListener { textView, i, keyEvent ->
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        textView.isFocusable = false
                        textView.isFocusableInTouchMode = false
                        textView.requestFocus()
                    }
                    false
                }
                groupRearrangeButton = itemView.findViewById(R.id.groupRearrangeButton)
            }
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        layoutHeightMap = object : HashMap<Int, Float>() {
            init {
                put(
                    TextSizeUtil.Companion.TEXT_SIZE_SMALL,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_SMALL)
                )
                put(
                    TextSizeUtil.Companion.TEXT_SIZE_MEDIUM,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_MEDIUM)
                )
                put(
                    TextSizeUtil.Companion.TEXT_SIZE_LARGE,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_LARGE)
                )
                put(
                    TextSizeUtil.Companion.TEXT_SIZE_EXTRA_LARGE,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_EXTRA_LARGE)
                )
            }
        }
    }

    private fun convertFromDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    override fun getItemCount(): Int {
        return if (listData != null) {
            listData.size + 1
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= listData!!.size) {
            TYPE_FOOTER
        } else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return if (viewType == TYPE_ITEM) {
            GroupViewHolder(inflater.inflate(R.layout.row_group, parent, false))
        } else if (viewType == TYPE_FOOTER) {
            GroupViewHolder(inflater.inflate(R.layout.row_footer, parent, false))
        } else {
            GroupViewHolder(inflater.inflate(R.layout.row_group, parent, false))
        }
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        // フッターの場合にはデータをバインドしない
        if (position >= listData!!.size) return
        val layoutHeight = layoutHeightMap[textSize.toInt()]
        if (layoutHeight != null) {
            val params = holder!!.rowLinearLayout?.layoutParams
            params?.height = layoutHeight.toInt()
            holder.rowLinearLayout?.layoutParams = params
        }
        val groupId = (listData[position] as HashMap<*, *>?)!!["group_id"].toString()
        val groupOrder = (listData[position] as HashMap<*, *>?)!!["group_order"].toString()
        val groupName = (listData[position] as HashMap<*, *>?)!!["name"].toString()
        holder!!.groupId = java.lang.Long.valueOf(groupId)
        holder.groupOrder = Integer.valueOf(groupOrder)
        holder.groupName?.setText(groupName)
        holder.groupName?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        holder.itemView.tag = holder
        // グループ名が空欄があった場合は新規追加時なのでフォーカスする
        if (groupName == "") {
            adapterListener.onGroupNameClicked(
                holder.itemView.findViewById(R.id.groupName),
                holder.groupId
            )
        }
        // 編集モードでも1番目の「すべて」は並べ替えさせない
        if (editEnable && holder.groupId != null && holder.groupId != 1L) {
            holder.groupRearrangeButton?.visibility = View.VISIBLE
        } else {
            holder.groupRearrangeButton?.visibility = View.GONE
        }
    }

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
        private const val ROW_LAYOUT_HEIGHT_SMALL = 40f
        private const val ROW_LAYOUT_HEIGHT_MEDIUM = 45f
        private const val ROW_LAYOUT_HEIGHT_LARGE = 50f
        private const val ROW_LAYOUT_HEIGHT_EXTRA_LARGE = 55f
    }
}