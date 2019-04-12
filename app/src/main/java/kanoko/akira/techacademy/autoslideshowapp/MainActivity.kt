package kanoko.akira.techacademy.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    // 画像Uri配列
    private val imaglist = mutableListOf<String>()
    // 配列の数
    private var imgtotal:Int = 0
    // 初期表示画像id
    private var imgnum:Int = 0
    // タイマー用変数
    private var mTimerSec = 0.0
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        bak.setOnClickListener(this)
        ctrl.setOnClickListener(this)
        fwd.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bak -> {
                if (imgnum != 0) {
                    imgnum = imgnum - 1
                    this.imageView.setImageURI(Uri.parse(imaglist[imgnum]))
                    // ログに出力
                    Log.d("imglist", "前に戻る $imgnum")
                } else {
                    this.imageView.setImageURI(Uri.parse(imaglist[imgtotal]))
                    imgnum = imgtotal
                    Log.d("imglist", "前に戻る $imgnum")
                }
            }
        }
        when (v.id) {
            R.id.ctrl -> {
                // ログに出力
                Log.d("imglist", "再生・停止")
                val ctrltext : TextView = findViewById(R.id.ctrl)
                if ( ctrltext.text == "再生" ) {
                    ctrltext.text = "停止"
                    bak.setClickable(false)
                    fwd.setClickable(false)

                    // タイマーの作成
                    mTimer = Timer()

                    // タイマーの始動
                    mTimer!!.schedule(0, 2000) {
                        Log.d("imglist", "再生中")
                        try {
                            if (imgnum != imgtotal) {
                                imgnum = imgnum + 1
                                imageView.setImageURI(Uri.parse(imaglist[imgnum]))
                                // ログに出力
                                Log.d("imglist", "先に進む $imgnum / 合計 $imgtotal")
                            } else {
                                imageView.setImageURI(Uri.parse(imaglist[0]))
                                imgnum = 0
                                Log.d("imglist", "ループ　先に進む $imgnum / 合計 $imgtotal")
                            }
                        } catch (e: Exception ) {

                        }

                    }
                } else {
                    ctrltext.text = "再生"
                    bak.setOnClickListener(this)
                    fwd.setOnClickListener(this)
                    mTimer!!.cancel()
                }
            }
        }
        when (v.id) {
            R.id.fwd -> {
                if (imgnum != imgtotal) {
                    imgnum = imgnum + 1
                    this.imageView.setImageURI(Uri.parse(imaglist[imgnum]))
                    // ログに出力
                    Log.d("imglist", "先に進む $imgnum")
                } else {
                    this.imageView.setImageURI(Uri.parse(imaglist[0]))
                    imgnum = 0
                    Log.d("imglist", "先に進む $imgnum")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                // リストに追加
                imaglist.add(imageUri.toString())
                // ログに出力
                Log.d("imglist", "$imaglist")
            } while (cursor.moveToNext())
            // 配列の数をカウント
            imgtotal = imaglist.count()-1
            // 初期画像を表示
            this.imageView.setImageURI(Uri.parse(imaglist[imgnum]))
            // ログに出力
            Log.d("imglist", "要素数 $imgtotal")
            Log.d("imglist", "初期表示 $imgnum")
            cursor.close()
        }
    }

}
