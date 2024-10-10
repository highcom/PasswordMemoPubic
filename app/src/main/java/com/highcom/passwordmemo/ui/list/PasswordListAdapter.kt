package com.highcom.passwordmemo.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.highcom.passwordmemo.R
import com.highcom.passwordmemo.data.PasswordEntity
import com.highcom.passwordmemo.databinding.RowPasswordBinding
import com.highcom.passwordmemo.databinding.RowFooterBinding
import com.highcom.passwordmemo.util.TextSizeUtil
import com.highcom.passwordmemo.util.login.LoginDataManager
import java.util.Locale

/**
 * パスワード一覧表示用アダプタ
 *
 * @property loginDataManager ログインデータ管理インスタンス
 * @property adapterListener パスワード一覧表示用アダプタリスナーインスタンス
 * @constructor
 * パスワード一覧表示用アダプタコンストラクタ
 *
 * @param context コンテキスト
 */
class PasswordListAdapter(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val loginDataManager: LoginDataManager?,
    private val adapterListener: AdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    /** パスワードビューホルダーのbinding */
    private var bindingPassword: RowPasswordBinding? = null
    /** フッタービューホルダーのbinding */
    private var bindingFooter: RowFooterBinding? = null
    /** DBに登録されているパスワード一覧データ */
    private var origPasswordList: List<PasswordEntity>? = null
    /** ソートやフィルタされた表示用のパスワード一覧データ */
    private var passwordList: List<PasswordEntity>? = null
    /** ソート種別 */
    private var sortType: String? = null
    /** レイアウト高さ設定マップデータ */
    private val layoutHeightMap: Map<Int, Float>
    /** 表示テキストサイズ */
    var textSize = 15f
    /** 編集モードかどうか */
    var editEnable = false

    /**
     * パスワード一覧表示ようアダプタのリスナー
     *
     */
    interface AdapterListener {
        /**
         * パスワードデータ選択イベント
         *
         * @param passwordEntity 選択対象パスワードデータ
         */
        fun onAdapterClicked(passwordEntity: PasswordEntity)
    }

    /**
     * パスワード一覧表示用ビューホルダー
     *
     * @constructor
     * パスワード一覧表示用ビューホルダーコンストラクタ
     *
     * @param binding 表示アイテムビュー
     */
    inner class RowPasswordViewHolder(val binding: RowPasswordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(passwordEntity: PasswordEntity) {
            binding.passwordEntity = passwordEntity
            binding.executePendingBindings()
        }
    }

    /**
     * パスワード一覧フッター用ビューホルダー
     *
     * @property binding
     */
    inner class RowFooterViewHolder(private val binding: RowFooterBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        layoutHeightMap = object : HashMap<Int, Float>() {
            init {
                put(
                    TextSizeUtil.TEXT_SIZE_SMALL,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_SMALL)
                )
                put(
                    TextSizeUtil.TEXT_SIZE_MEDIUM,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_MEDIUM)
                )
                put(
                    TextSizeUtil.TEXT_SIZE_LARGE,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_LARGE)
                )
                put(
                    TextSizeUtil.TEXT_SIZE_EXTRA_LARGE,
                    convertFromDpToPx(context, ROW_LAYOUT_HEIGHT_EXTRA_LARGE)
                )
            }
        }
    }

    /**
     * パスワード一覧データ設定処理
     *
     * @param list パスワード一覧データ
     */
    fun setList(list: List<PasswordEntity>) {
        origPasswordList = list
        passwordList = origPasswordList
    }

    /**
     * dpからpxへの変換処理
     *
     * @param context コンテキスト
     * @param dp 変換元dp値
     * @return 変換後px値
     */
    private fun convertFromDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /**
     * パスワード一覧データ数取得処理
     *
     * @return データ数+フッター行
     */
    override fun getItemCount(): Int {
        // フッターがあるので最低でも1を返す
        return if (passwordList != null) {
            passwordList!!.size + 1
        } else {
            0
        }
    }

    /**
     * ビュー種別取得処理
     *
     * @param position ビューの位置
     * @return ビュー種別
     */
    override fun getItemViewType(position: Int): Int {
        return if (position >= passwordList!!.size) {
            TYPE_FOOTER
        } else TYPE_ITEM
    }

    /**
     * ビューホルダー生成処理
     *
     * @param parent 親のビューグループ
     * @param viewType ビュー種別
     * @return 生成したビューホルダー
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                bindingPassword = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_password, parent, false)
                bindingPassword?.lifecycleOwner = lifecycleOwner
                RowPasswordViewHolder(bindingPassword!!)
            }
            TYPE_FOOTER -> {
                bindingFooter = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_footer, parent, false)
                bindingFooter?.lifecycleOwner = lifecycleOwner
                RowFooterViewHolder(bindingFooter!!)
            }
            else -> {
                bindingPassword = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_password, parent, false)
                bindingPassword?.lifecycleOwner = lifecycleOwner
                RowPasswordViewHolder(bindingPassword!!)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RowPasswordViewHolder -> {
                passwordList?.let { holder.bind(it[position]) }
                // レイアウト高さの設定
                val layoutHeight = layoutHeightMap[textSize.toInt()]
                if (layoutHeight != null) {
                    val params = holder.binding.rowLinearLayout.layoutParams
                    params?.height = layoutHeight.toInt()
                    holder.binding.rowLinearLayout.layoutParams = params
                }
                // 文字サイズの設定
                holder.binding.titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
                holder.binding.dateView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize - 3)
                // メモ表示が有効でメモが入力されている場合は表示する
                if (loginDataManager!!.memoVisibleSwitchEnable && holder.binding.memoView.text != "") {
                    holder.binding.memoView.visibility = View.VISIBLE
                } else {
                    holder.binding.memoView.visibility = View.GONE
                }
                holder.binding.memoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize - 3)
                // アイテムクリック時ののイベントを追加
                holder.binding.roundKeyIcon.setOnClickListener { view ->
                    val parentView = view.parent as View
                    parentView.callOnClick()
                }
                holder.binding.rowLinearLayout.setOnClickListener {
                    holder.binding.passwordEntity?.let { adapterListener.onAdapterClicked(it) }
                }
                // 編集モードの場合には並べ替えアイコンを表示する
                if (editEnable) {
                    holder.binding.rearrangeButton.visibility = View.VISIBLE
                } else {
                    holder.binding.rearrangeButton.visibility = View.GONE
                }
            }
            is RowFooterViewHolder -> {}
        }
    }

    /**
     * フィルター取得処理
     *
     * @return フィルターデータ
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<PasswordEntity>()
                if (origPasswordList == null) origPasswordList = passwordList
                if (constraint != null) {
                    if (origPasswordList != null && origPasswordList!!.isNotEmpty()) {
                        for (entity in origPasswordList!!) {
                            if (entity.title.lowercase(Locale.getDefault())
                                    .contains(constraint.toString())
                            ) results.add(entity)
                        }
                    }
                    oReturn.values = results
                } else {
                    oReturn.values = origPasswordList
                }
                return oReturn
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {
                passwordList = results?.values as List<PasswordEntity>?
                notifyDataSetChanged()
            }
        }
    }

    /**
     * パスワードデータ一覧の並べ替え処理
     *
     * @param fromPos 移動元の位置
     * @param toPos 移動先の位置
     * @return 並べ替え後のパスワード一覧データ
     */
    fun rearrangePasswordList(fromPos: Int, toPos: Int): List<PasswordEntity> {
        val origPasswordIds = ArrayList<Long>()
        val rearrangePasswordList = ArrayList<PasswordEntity>()
        // 元のIDの並びを保持と並べ替えができるリストに入れ替える
        origPasswordList?.let {
            for (entity in it) {
                origPasswordIds.add(entity.id)
                rearrangePasswordList.add(entity)
            }
        }
        // 引数で渡された位置で並べ替え
        val fromPassword = rearrangePasswordList[fromPos]
        rearrangePasswordList.removeAt(fromPos)
        rearrangePasswordList.add(toPos, fromPassword)
        // 再度IDを振り直す
        val itr = origPasswordIds.listIterator()
        for (comic in rearrangePasswordList) {
            comic.id = itr.next()
        }

        return rearrangePasswordList
    }

    /**
     * パスワードデータ一覧のソート処理
     *
     * @param key ソート種別
     */
    fun sortPasswordList(key: String?) {
        sortType = key
        // 比較処理の実装
        val comparator = Comparator<PasswordEntity> { t1, t2 ->
            var result = when(sortType) {
                SORT_ID -> t1.id.compareTo(t2.id)
                SORT_TITLE -> t1.title.compareTo(t2.title)
                SORT_INPUTDATE -> t1.inputDate.compareTo(t2.inputDate)
                else -> t1.id.compareTo(t2.id)
            }

            // ソート順が決まらない場合には、idで比較する
            if (result == 0) {
                result = t1.id.compareTo(t2.id)
            }
            return@Comparator result
        }
        origPasswordList = origPasswordList?.sortedWith(comparator)
        passwordList = passwordList?.sortedWith(comparator)
    }

    companion object {
        const val SORT_ID = "id"
        const val SORT_TITLE = "title"
        const val SORT_INPUTDATE = "inputdate"
        // private Context context;
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
        private const val ROW_LAYOUT_HEIGHT_SMALL = 40f
        private const val ROW_LAYOUT_HEIGHT_MEDIUM = 45f
        private const val ROW_LAYOUT_HEIGHT_LARGE = 50f
        private const val ROW_LAYOUT_HEIGHT_EXTRA_LARGE = 55f
    }
}