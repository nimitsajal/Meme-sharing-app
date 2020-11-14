package com.nimitsajal.memeshareapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

    var selectedTopic: String = "General"
    var subReddit:String = "memes"
    var currentMemeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadMeme()

        btnSave.setOnClickListener{
            if(isReadStorageAllowed()){
                saveMeme(getBitmapFromView(ivMeme))
            }
            else{
                requestStoragePermission()
            }
        }

        btnShare.setOnClickListener{
            if(isReadStorageAllowed()){
                shareMeme(getBitmapFromView(ivMeme))
            }
            else{
                requestStoragePermission()
            }
        }

        val topics = resources.getStringArray(R.array.Topics)

        // access the spinner
        val spinner = spTopics
        if (spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, topics)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    selectedTopic = topics[position]
                    when(selectedTopic){
                        "General" -> subReddit = "memes"
                        "CS:GO" -> subReddit = "CSGOmemes"
                        "Among Us" -> subReddit = "AmongUsMemes"
                        "Indian" -> subReddit = "HindiMemes"
                        "Pubg" -> subReddit = "PUBGmemes"
                        "The Office" -> subReddit = "DunderMifflin"
                        "Game of Thrones" -> subReddit = "GameOfThronesMemes"
                        "Marvel" -> subReddit = "MCUmemes"
                        "Cricket" -> subReddit = "CricketShitpost"
                        "Football" -> subReddit = "footballmemes"
                        "Engineering" -> subReddit = "engineeringmemes"
                    }
                    loadMeme()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedTopic = "General"
                    subReddit = "memes"
                    loadMeme()
                }
            }
        }
    }

    private fun loadMeme(){
        pbMeme.visibility = View.VISIBLE
        val queue = Volley.newRequestQueue(this)
        val url = "https://meme-api.herokuapp.com/gimme/$subReddit"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                currentMemeUrl = response.getString("url")
                Glide.with(this).load(currentMemeUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        pbMeme.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        pbMeme.visibility = View.GONE
                        return false
                    }
                }).into(ivMeme)
            },
            {
                Toast.makeText(this, "Can't fetch the Meme due to some reason!", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest)
    }

    private fun saveMeme(mBitmap: Bitmap){
        val externalStorage = Environment.getExternalStorageState()
        if(externalStorage == Environment.MEDIA_MOUNTED){
            val storageDirectory = Environment.getExternalStorageDirectory().absolutePath
            val dir = File("${storageDirectory}/MemeShareApp")
            val created = dir.mkdirs()
            //Toast.makeText(this, "$created", Toast.LENGTH_SHORT).show()
            val filename = String.format("%d.png", System.currentTimeMillis())
            val outFile = File(dir.toString() + File.separator + "MemeShareApp" + System.currentTimeMillis() / 1000 + ".jpg")
            //Toast.makeText(this, "Image location -> ${Uri.parse(outFile.absolutePath)}", Toast.LENGTH_SHORT).show()
            //val file = File(storageDirectory + File.separator + "DrawingApp_" + System.currentTimeMillis()/1000 + ".jpg")
            try{
                val stream:OutputStream = FileOutputStream(outFile)
                //Toast.makeText(this, "reached till here", Toast.LENGTH_SHORT).show()
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
                Toast.makeText(
                    this,
                    "Image saved successfully at ${Uri.parse(outFile.absolutePath)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            catch (e: java.lang.Exception){
                e.printStackTrace()
                Toast.makeText(this, "There is some problem", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "Unable, to access storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareMeme(mBitmap: Bitmap) {
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.putExtra(Intent.EXTRA_TEXT, "Hey, checkout this awesome meme: $currentMemeUrl")
//        val chooser = Intent.createChooser(intent, "Share this meme using...")
//        startActivity(chooser)

        var result = ""
        val externalStorage = Environment.getExternalStorageState()
        if(externalStorage == Environment.MEDIA_MOUNTED){
            val storageDirectory = Environment.getExternalStorageDirectory().absolutePath
            val dir = File("${storageDirectory}/MemeShareApp")
            val created = dir.mkdirs()
            //Toast.makeText(this, "$created", Toast.LENGTH_SHORT).show()
            val filename = String.format("%d.png", System.currentTimeMillis())
            val outFile = File(dir.toString() + File.separator + "MemeSharingApp" + System.currentTimeMillis() / 1000 + ".jpg")
            //Toast.makeText(this, "Image location -> ${Uri.parse(outFile.absolutePath)}", Toast.LENGTH_SHORT).show()
            //val file = File(storageDirectory + File.separator + "DrawingApp_" + System.currentTimeMillis()/1000 + ".jpg")
            try{
                val stream: OutputStream = FileOutputStream(outFile)
                //Toast.makeText(this, "reached till here", Toast.LENGTH_SHORT).show()
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
                Toast.makeText(
                    this,
                    "Image saved successfully at ${Uri.parse(outFile.absolutePath)}",
                    Toast.LENGTH_SHORT
                ).show()
                result = outFile.absolutePath
            }
            catch (e: java.lang.Exception){
                e.printStackTrace()
                Toast.makeText(this, "There is some problem", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "Unable, to access storage", Toast.LENGTH_SHORT).show()
        }
        if(result != ""){
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null){ path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/jpeg"
                startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        }
        else{
            Toast.makeText(this, "Cannot share", Toast.LENGTH_SHORT).show()
        }

    }

    fun nextMeme(view: View) {
        loadMeme()
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )){
            Toast.makeText(this, "Need permission to add a Background Image.", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Storage permission granted.", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

//    private fun getBitmapFromURL(src: String?): Bitmap? {
//        var returnedBitmap: Bitmap? = null
//        Glide.with(this)
//            .asBitmap()
//            .load(src)
//            .into(object : CustomTarget<Bitmap>(){
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    ivMeme.setImageBitmap(resource)
//                    returnedBitmap = resource
//                }
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // this is called when imageView is cleared on lifecycle call or for
//                    // some other reason.
//                    // if you are referencing the bitmap somewhere else too other than this imageView
//                    // clear it here as you can no longer have the bitmap
//                }
//            })
//        return returnedBitmap
//    }

    private fun getBitmapFromView(view: View): Bitmap{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}