package com.example.fashionmatch

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class VibeActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
//    private lateinit var adapter: VibeAdapter
//    private var vibeDataList: MutableList<vibe_data> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vibe)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        fetchTopVibe()
    }

    private fun fetchTopVibe() {
        db.collection("user_actions").document("defaultUser").collection("actions")
            .get()
            .addOnSuccessListener { documents ->
                val vibeCounts = mutableMapOf<String, Int>()
                for (document in documents) {
                    val vibe = document.getString("vibe")
                    if (vibe != null) {
                        vibeCounts[vibe] = vibeCounts.getOrDefault(vibe, 0) + 1
                    } else {
                        Log.d("VibeActivity", "Vibe field is null or missing in document: ${document.id}")
                    }
                }

                if (vibeCounts.isNotEmpty()) {
                    // Find the most liked vibe
                    val topVibe = vibeCounts.maxByOrNull { it.value }?.key
                    Log.d("VibeActivity", "Top vibe: $topVibe with count: ${vibeCounts[topVibe]}")
                    topVibe?.let { fetchVibeDetails(it) }
                } else {
                    Log.d("VibeActivity", "No vibes found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("VibeActivity", "Error fetching interactions", e)
            }
    }

    private fun fetchVibeDetails(vibe: String) {
        db.collection("vibe").document(vibe)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val vibeData = document.toObject(vibe_data::class.java)
                    vibeData?.let { updateUI(it) }
                } else {
                    Log.d("VibeActivity", "No such vibe document: $vibe")
                }
            }
            .addOnFailureListener { e ->
                Log.e("VibeActivity", "Error fetching vibe details", e)
            }
    }

//    private fun fetchDataFromFirestore() {
//        db.collection("vibes")
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val vibeData = document.toObject(vibe_data::class.java)
//                    updateUI(vibeData)
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("VibeActivity", "Error getting documents: ", exception)
//            }
//    }

    private fun updateUI(vibeData: vibe_data) {
        val vibeImage: ImageView = findViewById(R.id.vibeimg)
        val vibeImage1: ImageView = findViewById(R.id.vibecelebrityimg1)
        val vibeImage2: ImageView = findViewById(R.id.vibecelebrityimg2)
        val vibeImage3: ImageView = findViewById(R.id.vibecelebrityimg3)
        val vibeImage4: ImageView = findViewById(R.id.vibecelebrityimg4)
        val vibeText1: TextView = findViewById(R.id.vibecelebritytext1)
        val vibeText2: TextView = findViewById(R.id.vibecelebritytext2)
        val vibeText3: TextView = findViewById(R.id.vibecelebritytext3)
        val vibeText4: TextView = findViewById(R.id.vibecelebritytext4)
        val vibeTagline: TextView = findViewById(R.id.vibeline)
        val vibeType: TextView = findViewById(R.id.vibetype)
        val vibeQ: TextView = findViewById(R.id.vibeq)

        Glide.with(this).load(vibeData.vibe_img).into(vibeImage)
        Glide.with(this).load(vibeData.vibe_img1).into(vibeImage1)
        Glide.with(this).load(vibeData.vibe_img2).into(vibeImage2)
        Glide.with(this).load(vibeData.vibe_img3).into(vibeImage3)
        Glide.with(this).load(vibeData.vibe_img4).into(vibeImage4)
        vibeText1.text = vibeData.vibe_text1
        vibeText2.text = vibeData.vibe_text2
        vibeText3.text = vibeData.vibe_text3
        vibeText4.text = vibeData.vibe_text4
        vibeTagline.text = vibeData.vibe_tagline
        vibeType.text = vibeData.name
        vibeQ.text = vibeData.vibeq

    }
}
