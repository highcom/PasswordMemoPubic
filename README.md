# PasswordMemoPubicリポジトリの概要
このリポジトリで管理されているコードはGoogle Playストアにて「[パスワードメモ　画面ロック付きのパスワード管理ツール](https://play.google.com/store/apps/details?id=com.highcom.passwordmemo&hl=ja)」アプリとして公開されています。
## このアプリの機能
手帳に書き留めるように簡単で自由にパスワードのメモができるアプリです。  
### 機能紹介
- マスターパスワードの設定によるアカウントデータへのアクセス制限  
入力を複数回間違えた場合には全てのデータを削除する機能も選べます。  
- 生体認証によるログイン機能  
Android標準の生体認証を利用して安全かつ簡単にログインできます。  
- 登録したアカウント情報の検索機能  
アカウント情報が増えすぎても文字列検索で一発で見つかります。  
- パスワードの自動生成機能  
文字の種類や文字数を指定して強力なパスワードを自動で生成できます。  
- 長押しによるパスワードのコピー機能  
クリップボードへコピーされるのでサイトのログイン時に入力の手間が省けます。  
- グループ分け機能  
好きな名前のグループを作成して、パスワードメモをグループ分けする事ができます。  
- 暗号化されたデータベースでアカウント情報を保存  
オープンソース「SQLCipher」を利用しているため、AESで暗号化されたデータベースで全てのアカウント情報が保存されます。  
- 編集モード時に行を長押しで並べ替え機能  
編集モード時に並べ替えたい行を長押しする事で好きな順番にデータを並べ替える事ができます。  
- 入力したサイトURLからブラウザに遷移する機能  
入力したサイトURLをタップする事でブラウザに遷移してサイトを表示する事ができます。  
- タイトル順、更新日順でのソート機能  
任意の並べ替えだけでなく、タイトル、更新日でソートできます。  
- パスワードデータのバックアップ機能  
パスワードデータをオフラインでもオンラインでも、SDカードやクラウドストレージ等の好きな場所に暗号化されたDBファイルをバックアップしておくことができます。  
- パスワードデータのCSV出力機能  
パスワードデータをCSV形式でファイル出力してバックアップしておくこともできます。  
- パスワードデータの復元機能  
バックアップした暗号化DBファイルを取り込んで復元する事ができます。  
- パスワードデータのCSV取込機能（各種文字コード対応）  
バックアップしたCSV形式のファイルを取り込んで復元する事ができます。  
また、各種文字コードの対応により、PC等で編集したCSV形式のファイルも取込み可能です。  
（UTF-8、ShiftJIS、JIS、EUC-JPの文字コードでの動作確認済）  
- 背景色が変更できる機能  
気分に合わせて背景色を変更することができます。  
- パスワード一覧画面にメモを表示する機能  
設定によって一覧画面にメモも表示するかどうかを切り替えられます。  
- 表示画面のテキストサイズを変更する機能  
表示するテキストサイズを自分に合ったサイズに変更する事ができます。
## 現在の対応状況
- [x] Java→Kotlin化
- [x] SQLiteOpenHelper→Room化
- [x] Coroutines Flow化
- [x] SingleActivity/MultiFragment化
- [x] NavigationFragment適用
- [ ] ViewBinding/DataBinding化
- [ ] Dagger Hilt化
- [ ] JUnit作成
## バージョンについて
vX.X.Xとしてタグを付けていますので、過去のリリースバージョンのコードはタグを過去に辿ることで参照出来ます。
## ライセンス
MIT
