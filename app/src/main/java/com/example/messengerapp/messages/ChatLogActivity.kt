package com.example.messengerapp.messages

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.R
import com.example.messengerapp.models.ChatMessage
import com.example.messengerapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_from_row.view.textView
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.latest_message_row.*


class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatlog.adapter = adapter

       // val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser!!.username



     //   setupDummyData()
        listenForMessages()

        send_button_chatlog.setOnClickListener {
            Log.d(TAG,"Attempt to send Message")
            performSendMessage()
        }

        // starts chat from bottom
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerview_chatlog.layoutManager = layoutManager

        // pushes up recycler view when softkeyboard popups up
        recyclerview_chatlog.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerview_chatlog.postDelayed(Runnable {
                    recyclerview_chatlog.scrollToPosition(
                        recyclerview_chatlog.adapter!!.itemCount -1)
                }, 100)
            }
        }
    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)


                if(chatMessage != null){
                    Log.d(TAG, chatMessage.text!!)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                        recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)

                    } else {

                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                        recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)


                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            })
    }


    private fun performSendMessage(){
        // send message to firebase

        val text = editText_chatlog.text.toString()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId = FirebaseAuth.getInstance().uid //currently logged in user
        val toId = user!!.uid

        val ReadReceipt = "Unread"

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()


    // DB Ref for to user
        val toreference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        if(fromId == null) return



        val ChatMessage = ChatMessage(reference.key!!,text, fromId, toId, System.currentTimeMillis() / 1000, ReadReceipt)
        val ChatMessageSelf = ChatMessage(reference.key!!,text, fromId, toId, System.currentTimeMillis() / 1000, "Read")
        reference.setValue(ChatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Saved our Chat Message: ${reference.key}")
                editText_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1) // scroll to bottom of recycler view
                // when message is sent
            }
        toreference.setValue(ChatMessage)

        val LatestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        LatestMessageRef.setValue(ChatMessageSelf) // set new message to be latest message for the conversation

        // to user ref

        val LatestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        LatestMessageToRef.setValue(ChatMessage).addOnCompleteListener {
            Log.d(TAG,it.result.toString())
        }
    }

//    private fun setupDummyData(){
//        val adapter = GroupAdapter<GroupieViewHolder>()
//
//        adapter.add(ChatFromItem("lskefnwenoweifnfklnc"))
//        adapter.add(ChatToItem("enoewfboewbfoweibofweb"))
//        adapter.add(ChatFromItem("this is a test message for a new chat app"))
//
//        recyclerview_chatlog.adapter = adapter
//    }
}

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView.text = text

           //load user image into chat
            val uri = user.profileImageUrl
            val targetImageView = viewHolder.itemView.imageViewFromUser
           Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView.text = text

                //load user image into chat
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageViewChatToRow
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}