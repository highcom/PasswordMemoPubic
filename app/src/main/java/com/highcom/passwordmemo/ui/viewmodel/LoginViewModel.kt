package com.highcom.passwordmemo.ui.viewmodel

import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.highcom.passwordmemo.R
import com.highcom.passwordmemo.data.GroupEntity
import com.highcom.passwordmemo.data.PasswordMemoRepository
import com.highcom.passwordmemo.util.login.LoginDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * ログイン画面のビューモデル
 *
 * @property repository データアクセスリポジトリ
 * @property loginDataManager ログインデータ管理
 */
class LoginViewModel(private val repository: PasswordMemoRepository, private val loginDataManager: LoginDataManager) : ViewModel() {
    /** ログイン時の案内メッセージ */
    private val _naviMessage = MutableStateFlow("")
    val naviMessage: StateFlow<String> = _naviMessage.asStateFlow()
    /** ログイン時の案内メッセージ */
    private val _keyIconRotate = MutableStateFlow(false)
    val keyIconRotate: StateFlow<Boolean> = _keyIconRotate.asStateFlow()
    /** 入力パスワード */
    val editMasterPassword = MutableStateFlow("")
    /** 初回ログインかどうか */
    var firstTime = false
    /** マスターパスワード作成時の1回目の入力値 */
    var firstPassword: String? = null
    /** パスワード誤り回数 */
    var incorrectPwCount = 0

    @Suppress("DEPRECATION")
    private val handler = Handler()
    private val executor = Executor { command -> handler.post(command) }

    /**
     * ログイン画面ビューモデル生成クラス
     *
     * @property repository
     */
    class Factory(private val repository: PasswordMemoRepository, private val loginDataManager: LoginDataManager) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repository, loginDataManager) as T
        }
    }

    /**
     * 全てのデータのデータ削除
     *
     * @param groupEntity リセット後の初期グループデータ
     */
    fun reset(groupEntity: GroupEntity) = viewModelScope.launch {
        repository.deleteAllPassword()
        repository.deleteAllGroup()
        repository.insertGroup(groupEntity)
    }

    /**
     * 鍵アイコン回転初期化処理
     *
     */
    fun resetKeyIconRotate() {
        _keyIconRotate.value = false
    }

    /**
     * 案内メッセージ初期化処理
     *
     * @param context コンテキスト
     */
    fun resetNaviMessage(context: Context) {
        if (loginDataManager.isMasterPasswordCreated) {
            _naviMessage.value = context.getString(R.string.input_password)
        } else {
            _naviMessage.value = context.getString(R.string.new_password)
        }
    }

    /**
     * パスワードログイン判定メッセージ取得処理
     * * パスワードの生成が必要か照合したかの判定処理をしてメッセージを返却する
     *
     * @param context コンテキスト
     * @param editPassword 入力パスワード
     * @return 判定メッセージ
     */
    fun passwordLogin(context: Context, editPassword: String) {
        val masterPassword = loginDataManager.masterPassword
        if (editPassword == "") {
            _naviMessage.value = context.getString(R.string.err_empty)
        } else if (!loginDataManager.isMasterPasswordCreated) {
            if (firstPassword == null) {
                firstPassword = editPassword
                _naviMessage.value = context.getString(R.string.err_input_same)
            } else if (editPassword == firstPassword) {
                // マスターパスワードが作成されていない場合は新規作成
                loginDataManager.setMasterPassword(editPassword)
                // ログイン中の表示に切り替える
                firstTime = true
                _naviMessage.value = context.getString(R.string.login_success)
                _keyIconRotate.value = true
            } else {
                // 一度目の入力と異なることを伝える
                val ts = Toast.makeText(
                    context,
                    context.getString(R.string.err_input_different),
                    Toast.LENGTH_SHORT
                )
                ts.show()
                firstPassword = null
                _naviMessage.value = context.getString(R.string.new_password)
            }
        } else if (editPassword == masterPassword) {
            // ログイン中の表示に切り替える
            firstTime = false
            _naviMessage.value = context.getString(R.string.login_success)
            _keyIconRotate.value = true
        } else if (!loginDataManager.deleteSwitchEnable) {
            // データ削除機能が無効の場合にはエラー表示を行うだけ
            _naviMessage.value = context.getString(R.string.err_incorrect)
        } else {
            // データ削除機能が有効の場合には5回連続で間違えるとデータを削除する
            incorrectPwCount += 1
            if (incorrectPwCount >= 5) {
                incorrectPwCount = 0
                // 設定されているマスターパスワードを削除する
                loginDataManager.clearAllData()
                // パスワードデータとグループデータを削除して初期グループを作成する
                reset(GroupEntity(1, 1, context.getString(R.string.list_title)))
                // すべてのデータを削除したことを表示
                val ts = Toast.makeText(
                    context,
                    context.getString(R.string.err_delete_all),
                    Toast.LENGTH_LONG
                )
                ts.show()
                _naviMessage.value = context.getString(R.string.new_password)
            } else {
                _naviMessage.value =
                    context.getString(R.string.err_incorrect_num_front) + incorrectPwCount + context.getString(
                        R.string.err_incorrect_num_back
                    )
            }
        }
        // 入力したパスワードはクリアする
        editMasterPassword.value = ""
    }

    /**
     * 生体認証ログイン処理
     *
     * @param context コンテキスト
     */
    fun biometricLogin(context: Context) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.login_biometrics))
            .setSubtitle(context.getString(R.string.login_biometrics_message))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()
        val biometricPrompt = BiometricPrompt(
            (context as FragmentActivity),
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        context.getApplicationContext(),
                        context.getString(R.string.err_authentication_message) + errString,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // ログイン中の表示に切り替える
                    firstTime = false
                    _naviMessage.value = context.getString(R.string.login_success)
                    _keyIconRotate.value = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        context.getApplicationContext(),
                        context.getString(R.string.err_authentication_failure),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        // Displays the "log in" prompt.
        biometricPrompt.authenticate(promptInfo)
    }
}