package com.example.Promessenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.Promessenger.NewMessageActivity
import com.example.Promessenger.R
import com.example.Promessenger.encryption.AESEncryption.decrypt
import com.example.Promessenger.encryption.AESEncryption.encrypt
import com.example.Promessenger.models.ChatMessage
import com.example.Promessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class chatLogActivity : AppCompatActivity() {

    var toUser: User?=null


    companion object {
        val TAG = ""
    }


    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        supportActionBar?.title = "Chat Log"
        recyclerview_chat_log.adapter = adapter


       toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        if (toUser != null) {
            supportActionBar?.title = toUser?.username
        }
        // setupDummyData()
        listenforMessages()


        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send mesage")
            performSendMessage()
        }
    }

    private fun listenforMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")


        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)


                 if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser= LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                      //  adapter.add(ChatToItem(chatMessage.text, toUser))
                        toUser?.let { ChatToItem(chatMessage.text, it) }?.let { adapter.add(it) }
                    }
                    Log.d(TAG, chatMessage.text)

                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })


    }


    private fun performSendMessage() {


        val text = edittext_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return
       // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


        val chatMessage =
            ChatMessage(reference.key!!, encrypt(text)!!, fromId, toId!!, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }

        toReference.setValue(chatMessage)
//
       val latestMessageRef= FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
       latestMessageRef.setValue(chatMessage)

        val latestMessageToRef= FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageToRef.setValue(chatMessage)

    }
}

class ChatFromItem(val text: String,val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = decrypt(text)

        val uri= user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.textview_to_row.text = decrypt(text)


        val uri= user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
