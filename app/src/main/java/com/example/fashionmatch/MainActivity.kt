package com.example.fashionmatch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputBinding
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import kotlin.time.Duration
import android.widget.LinearLayout


class MainActivity : AppCompatActivity(), CardStackListener{

    private lateinit var manager: CardStackLayoutManager
    private lateinit var adapter: items_adapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        val cardStackView = findViewById<CardStackView>(R.id.card_stack_view)
        manager = CardStackLayoutManager(this, this)
        manager.setDirections(Direction.HORIZONTAL + Direction.VERTICAL)

        cardStackView.layoutManager = manager
        adapter = items_adapter(emptyList())
        cardStackView.adapter = adapter
        fetchItemsFromFirestore()

        // Set onClick listeners for the icons
        val thumbsDownIcon = findViewById<ImageView>(R.id.thumbs_down_icon)
        val thumbsUpIcon = findViewById<ImageView>(R.id.thumbs_up_icon)
        val shoppingBagIcon = findViewById<ImageView>(R.id.bag_icon)
        val filterIcon = findViewById<ImageView>(R.id.filter_icon)
        val starIcon = findViewById<ImageView>(R.id.star_icon)

        thumbsDownIcon.setOnClickListener {
            swipeLeft()
        }

        thumbsUpIcon.setOnClickListener {
            swipeRight()
        }

        shoppingBagIcon.setOnClickListener {
            swipeUp()
        }

        filterIcon.setOnClickListener {
            showFilterDialog()
        }

        starIcon.setOnClickListener {
            var intent = Intent(this@MainActivity, VibeActivity::class.java)
            startActivity(intent)
        }

    }

    private fun swipeLeft() {
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(200)
            .build()
        manager.setSwipeAnimationSetting(setting)
        findViewById<CardStackView>(R.id.card_stack_view).swipe()
    }

    private fun swipeRight() {
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(200)
            .build()
        manager.setSwipeAnimationSetting(setting)
        findViewById<CardStackView>(R.id.card_stack_view).swipe()
    }

    private fun swipeUp() {
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(200)
            .build()
        manager.setSwipeAnimationSetting(setting)
        findViewById<CardStackView>(R.id.card_stack_view).swipe()
    }

    private fun fetchItemsFromFirestore() {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<items_data>()
                for (document in result) {
                    val imageUrl = document.getString("item_img") ?: ""
                    val price = document.getString("selling_price") ?: ""
                    val discount = document.getString("mrp") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val description = document.getString("desc") ?: ""

                    items.add(
                        items_data(
                            imageUrl,
                            price,
                            discount,
                            brand,
                            description
                        )
                    )
                }
                adapter.updateItems(items)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
            }
    }

    private fun showFilterDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_filter_selection, null)

        var dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_filter_color).setOnClickListener {
            dialog.dismiss()
            showFilterOptionsDialog("color")
        }

        dialogView.findViewById<Button>(R.id.btn_filter_type).setOnClickListener {
            dialog.dismiss()
            showFilterOptionsDialog("type")
        }

        dialogView.findViewById<Button>(R.id.btn_filter_vibe).setOnClickListener {
            dialog.dismiss()
            showFilterOptionsDialog("vibe")
        }

        dialog.show()
    }

    private fun showFilterOptionsDialog(filterCategory: String) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_filter_options, null)
        val optionsLayout = dialogView.findViewById<LinearLayout>(R.id.ll_filter_options)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        db.collection("items").get()
            .addOnSuccessListener { result ->
                val options = mutableSetOf<String>()
                for (document in result) {
                    val option = document.getString(filterCategory) ?: ""
                    options.add(option)
                }



                options.forEach { option ->
                    val button = Button(this)
                    button.text = option
                    button.setOnClickListener {
                        dialog.dismiss()
                        applyFilter(filterCategory, option)
                    }
                    optionsLayout.addView(button)
                }
                dialog.show()
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting filter options.", exception)
            }

    }

    private fun applyFilter(filterCategory: String, filterValue: String) {
        db.collection("items")
            .whereEqualTo(filterCategory, filterValue)
            .get()
            .addOnSuccessListener { result ->
                val filteredItems = mutableListOf<items_data>()
                for (document in result) {
                    val imageUrl = document.getString("item_img") ?: ""
                    val price = document.getString("selling_price") ?: ""
                    val discount = document.getString("mrp") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val description = document.getString("desc") ?: ""

                    filteredItems.add(
                        items_data(
                            imageUrl,
                            price,
                            discount,
                            brand,
                            description
                        )
                    )
                }
                adapter.updateItems(filteredItems)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error applying filter.", exception)
            }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {}
    override fun onCardSwiped(direction: Direction) {
        if (direction == Direction.Top || direction == Direction.Bottom) {
            val currentItem = adapter.items[manager.topPosition - 1]
            Toast.makeText(this, "Item added to Shopping bag", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View, position: Int) {}
    override fun onCardDisappeared(view: View, position: Int) {}

}






