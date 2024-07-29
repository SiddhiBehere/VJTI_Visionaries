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

        // Determine whether to fetch all items or recommendations
        determineDataFetch()

//        fetchUserInteractions()  // Fetch interactions and recommend items

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
                    val item_id = document.id.toIntOrNull() ?: 0
                    val imageUrl = document.getString("item_img") ?: ""
                    val price = document.getString("selling_price") ?: ""
                    val discount = document.getString("mrp") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val description = document.getString("desc") ?: ""
                    val type = document.getString("type") ?: ""
                    val vibe = document.getString("vibe") ?: ""
                    val color = document.getString("color") ?: ""

                    items.add(
                        items_data(
                            item_id,
                            imageUrl,
                            price,
                            discount,
                            brand,
                            description,
                            type,
                            vibe,
                            color
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
                    val item_id = document.id.toIntOrNull() ?: 0
                    val imageUrl = document.getString("item_img") ?: ""
                    val price = document.getString("selling_price") ?: ""
                    val discount = document.getString("mrp") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val description = document.getString("desc") ?: ""
                    val type = document.getString("type") ?: ""
                    val vibe = document.getString("vibe") ?: ""
                    val color = document.getString("color") ?: ""

                    filteredItems.add(
                        items_data(
                            item_id,
                            imageUrl,
                            price,
                            discount,
                            brand,
                            description,
                            type,
                            vibe,
                            color
                        )
                    )
                }
                adapter.updateItems(filteredItems)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error applying filter.", exception)
            }
    }

    // to record user interaction with a specific item
    private fun recordUserInteraction(item: items_data, action: String) {
        val userAction = hashMapOf(
            "item_id" to item.item_id,
            "item_img" to item.item_img,
            "selling_price" to item.selling_price,
            "mrp" to item.mrp,
            "brand" to item.brand,
            "desc" to item.desc,
            "action" to action,
            "type" to item.type,
            "vibe" to item.vibe,
            "color" to item.color,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("user_actions").document("defaultUser")
            .collection("actions")
            .add(userAction)
            .addOnSuccessListener {
                Log.d("MainActivity", "User interaction recorded successfully")
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Error recording user interaction", e)
            }
    }

    // Calls this function whenever an interaction happens
    private fun onItemSwiped(item: items_data, direction: Direction) {
        when (direction) {
            Direction.Right -> {
                recordUserInteraction(item, "liked")
            }
            Direction.Top -> {
                recordUserInteraction(item, "added_to_cart")
            }
            Direction.Bottom -> {
                recordUserInteraction(item, "added_to_cart")
            }
            Direction.Left -> {
                recordUserInteraction(item, "disliked")
            }

            else -> {}
        }
    }

    private fun fetchUserInteractions() {
        val userId = "defaultUser" // or dynamically fetch the current user ID
        db.collection("user_actions")
            .document(userId)
            .collection("actions")
            .get()
            .addOnSuccessListener { result ->
                val likedItems = mutableListOf<items_data>()
                val dislikedItems = mutableListOf<items_data>()
                for (document in result) {
                    val action = document.getString("action")
                    val itemData = items_data(
                        document.getLong("item_id") ?.toInt() ?: 0,
                        document.getString("item_img") ?: "",
                        document.getString("selling_price") ?: "",
                        document.getString("mrp") ?: "",
                        document.getString("brand") ?: "",
                        document.getString("desc") ?: "",
                        document.getString("type") ?: "",
                        document.getString("vibe") ?: "",
                        document.getString("color") ?: ""
                    )
                    if (action == "liked" || action == "added_to_cart") {
                        likedItems.add(itemData)
                    } else if (action == "disliked") {
                        dislikedItems.add(itemData)
                    }
                }
                fetchRecommendedItems(likedItems, dislikedItems)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting user interactions.", exception)
            }
    }

    private fun fetchRecommendedItems(likedItems: List<items_data>, dislikedItems: List<items_data>) {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val recommendedItems = mutableListOf<items_data>()
                val likedItemIds = likedItems.map { it.item_id }

                for (document in result) {
                    val itemId = document.getLong("item_id")?.toInt() ?: 0
                    if (itemId in likedItemIds) continue // Skip liked items

                    val itemData = items_data(
                        document.id.toIntOrNull() ?: 0,
                        document.getString("item_img") ?: "",
                        document.getString("selling_price") ?: "",
                        document.getString("mrp") ?: "",
                        document.getString("brand") ?: "",
                        document.getString("desc") ?: "",
                        document.getString("type") ?: "",
                        document.getString("vibe") ?: "",
                        document.getString("color") ?: ""
                    )

                    if (isItemRecommended(itemData, likedItems, dislikedItems)) {
                        recommendedItems.add(itemData)
                    }
                }
                adapter.updateItems(recommendedItems)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting items.", exception)
            }
    }

    private fun determineDataFetch() {
        val userId = "defaultUser"

        db.collection("user_actions")
            .document(userId)
            .collection("actions")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // No interactions found, fetch all items
                    fetchAllItems()
                } else {
                    // Interactions found, fetch based on recommendations
                    fetchUserInteractions()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error checking user interactions.", exception)
                // Optionally, fetch all items if there's an error
                fetchAllItems()
            }
    }

    private fun fetchAllItems() {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<items_data>()
                for (document in result) {
                    val itemId = document.getLong("item_id")?.toInt() ?: 0 // Correctly handle the type as Int
                    val imageUrl = document.getString("item_img") ?: ""
                    val price = document.getString("selling_price") ?: ""
                    val discount = document.getString("mrp") ?: ""
                    val brand = document.getString("brand") ?: ""
                    val description = document.getString("desc") ?: ""
                    val color = document.getString("color") ?: ""
                    val type = document.getString("type") ?: ""
                    val vibe = document.getString("vibe") ?: ""

                    items.add(
                        items_data(
                            item_id = itemId,
                            item_img = imageUrl,
                            selling_price = price,
                            mrp = discount,
                            brand = brand,
                            desc = description,
                            color = color,
                            type = type,
                            vibe = vibe
                        )
                    )
                }
                adapter.updateItems(items)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error fetching all items.", exception)
            }
    }



    private fun isItemRecommended(item: items_data, likedItems: List<items_data>, dislikedItems: List<items_data>): Boolean {
        // Extracting liked and disliked features
        val likedColors = likedItems.map { it.color }.toSet()
        val dislikedColors = dislikedItems.map { it.color }.toSet()

        val likedTypes = likedItems.map { it.type }.toSet()
        val dislikedTypes = dislikedItems.map { it.type }.toSet()

        val likedVibes = likedItems.map { it.vibe }.toSet()
        val dislikedVibes = dislikedItems.map { it.vibe }.toSet()

        // Check if the item matches the liked features and doesn't match the disliked features
        val isLikedColor = item.color in likedColors && item.color !in dislikedColors
        val isLikedType = item.type in likedTypes && item.type !in dislikedTypes
        val isLikedVibe = item.vibe in likedVibes && item.vibe !in dislikedVibes

        // Recommend the item if it matches any liked feature and none of the disliked features
        return isLikedColor || isLikedType || isLikedVibe
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {}
    override fun onCardSwiped(direction: Direction) {
        val currentItem = adapter.items[manager.topPosition - 1]
        onItemSwiped(currentItem, direction)
        if (direction == Direction.Top || direction == Direction.Bottom) {
            Toast.makeText(this, "Item added to Shopping bag", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View, position: Int) {}
    override fun onCardDisappeared(view: View, position: Int) {}

}
