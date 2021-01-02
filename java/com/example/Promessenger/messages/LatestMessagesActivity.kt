package com.example.Promessenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.Promessenger.MainActivity
import com.example.Promessenger.NewMessageActivity
import com.example.Promessenger.R
import com.example.Promessenger.encryption.AESEncryption.decrypt
import com.example.Promessenger.models.ChatMessage
import com.example.Promessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null

        var ChatPratnerUser: User?=null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
       // setupDummyRows()
        supportActionBar?.title = "ProMessenger"
        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        //setting item click lister
        adapter.setOnItemClickListener{ item, view->
            val intent = Intent(this, chatLogActivity::class.java)

            

         //val row = item as LatestMessagesRow
             intent.putExtra(NewMessageActivity.USER_KEY, ChatPratnerUser )
            startActivity(intent)
        }




        listenforlatestmessages()
        fetchCurrentuser()
        verifyuserisloggedin()

    }


val latestmessagesMap =  HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
     latestmessagesMap.values.forEach{
         adapter.add(LatestMessagesRow(it))
     }
    }

private fun listenforlatestmessages(){
    val fromId = FirebaseAuth.getInstance().uid
    val ref= FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
    ref.addChildEventListener(object: ChildEventListener {

        override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
            val chatMessage = p0.getValue(ChatMessage::class.java)?: return

            latestmessagesMap[p0.key!!] = chatMessage
            refreshRecyclerViewMessages()

        }

        override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
            val chatMessage = p0.getValue(ChatMessage::class.java)?: return

            latestmessagesMap[p0.key!!] = chatMessage
            refreshRecyclerViewMessages()

        }
        override fun onCancelled(error: DatabaseError) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }
    })
}
    class LatestMessagesRow (val chatMessage: ChatMessage): Item<ViewHolder>() {


        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_textview_latest_message.text= decrypt(chatMessage.text)
           val chatPartnerId: String
           if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
               chatPartnerId= chatMessage.toId
           }else
           {
               chatPartnerId = chatMessage.fromId
           }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    ChatPratnerUser = p0.getValue(User::class.java)

                    viewHolder.itemView.username_textview_latest_message.text= ChatPratnerUser?.username

                    val targetImageView = viewHolder.itemView.imageview_latest_message
                    Picasso.get().load(ChatPratnerUser?.profileImageUrl).into(targetImageView)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }


    }
    val adapter = GroupAdapter<ViewHolder>()
//    private fun setupDummyRows() {
//
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//    }

    private fun fetchCurrentuser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "CurentUser ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun verifyuserisloggedin() {
        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {

                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_author->{
                val intent = Intent(this, MadeBy::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_meenu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}