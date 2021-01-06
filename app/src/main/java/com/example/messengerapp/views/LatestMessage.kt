package views

import android.graphics.Color
import android.graphics.Color.red
import android.util.Log
import com.example.messengerapp.R
import com.example.messengerapp.models.ChatMessage
import com.example.messengerapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessage(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.LatestMessageTextView.text = chatMessage.text //Latest Message TextBody

        val ChatPartnerID: String

        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            ChatPartnerID = chatMessage.toId
        } else {
            ChatPartnerID = chatMessage.fromId
        }



        val reference = FirebaseDatabase.getInstance().getReference("/users/$ChatPartnerID")
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)

                viewHolder.itemView.UsernameTextView_LatestMessage.text = chatPartnerUser?.username

                val targetID = viewHolder.itemView.imageView

                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetID)

                if(chatMessage.readReceipt == "Unread" && chatMessage.fromId != FirebaseAuth.getInstance().uid ){
                    viewHolder.itemView.setBackgroundColor(Color.YELLOW)
                    Log.d("lATESTMESSAGE VIEW", chatMessage.id)
                    Log.d("lATESTMESSAGE VIEW", chatMessage.readReceipt)
                } else {
                    viewHolder.itemView.setBackgroundColor(Color.WHITE)
                }

            }
        })




    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}