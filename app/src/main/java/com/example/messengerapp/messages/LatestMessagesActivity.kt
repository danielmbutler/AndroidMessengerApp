package com.example.messengerapp.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.R
import com.example.messengerapp.messages.NewMessageActivity.Companion.USER_KEY
import com.example.messengerapp.models.ChatMessage
import com.example.messengerapp.models.User
import com.example.messengerapp.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import views.LatestMessage

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
        val TAG = "LatestMessages"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        RecyclerView_LatestMessages.adapter = adapter

        // faint line underneath each row
        RecyclerView_LatestMessages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //set item click listener for recyclerview

        refreshRecyclerViewMessages()

        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123")

            val intent = Intent(this,ChatLogActivity::class.java)

            val row = item as LatestMessage

            //access chat partnet created in Latest Message View


            // add read recepit


            val ToId = FirebaseAuth.getInstance().uid
            val FromId = row.chatPartnerUser?.uid

            if(row.chatMessage.fromId != ToId) {
                Log.d(TAG,"From Id: ${row.chatMessage.fromId}, ToId: $ToId ")
                var upref = FirebaseDatabase.getInstance().reference.child("/latest-messages/$FromId/").child("${ToId}")
                var uprefsecondary = FirebaseDatabase.getInstance().reference.child("/latest-messages/$ToId/").child("${FromId}")
                Log.d(TAG,"Updating Ref: ${upref.ref}")
                val updates: MutableMap<String, Any> =
                    HashMap()
                updates["readReceipt"] = "Read"
                Log.d("notification", "setting read in chat log activity")

                upref.updateChildren(updates).addOnSuccessListener {
                    Log.d(
                        LatestMessagesActivity.TAG,"message Updated:")  }.addOnFailureListener { Log.d(
                    LatestMessagesActivity.TAG,"failed") }
                uprefsecondary.updateChildren(updates).addOnSuccessListener {
                    Log.d(
                        LatestMessagesActivity.TAG,"message Updated:")  }.addOnFailureListener { Log.d(
                    LatestMessagesActivity.TAG,"failed") }
                }


            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

       ListerForLatestMessages()

        fetchCurrentUser()

       verifyUserIsLoggedIn()
    }



    val LatestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        LatestMessagesMap.values.forEach {
            adapter.add(LatestMessage(it))
            if(it.readReceipt == "Unread"){
                Log.d(TAG,"NEW Unread Message: ${it.id}")


            }
        }
    }

    private fun ListerForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                LatestMessagesMap[snapshot.key!!] = chatMessage
                Log.d(TAG, chatMessage.readReceipt)

                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                LatestMessagesMap[snapshot.key!!] = chatMessage

                Log.d(TAG, chatMessage.readReceipt)

                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

//    private fun setupDummyRows(){
//        val adapter = GroupAdapter<GroupieViewHolder>()
//
//        adapter.add(LatestMessage())
//        adapter.add(LatestMessage())
//        adapter.add(LatestMessage())
//
//
//    }
    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessages","Current user ${currentUser?.username}")
            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid

        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.navmenu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}