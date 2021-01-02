package com.example.Promessenger.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.Promessenger.R
import kotlinx.android.synthetic.main.activity_made_by.*


class MadeBy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_made_by)


        linkedin.setOnClickListener{
            val uri: Uri = Uri.parse("https://www.linkedin.com/in/prabhav-garg-75a060183/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        codeforces.setOnClickListener{
            val uri: Uri = Uri.parse("https://codeforces.com/profile/probhav")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        github.setOnClickListener{
            val uri: Uri = Uri.parse("https://github.com/probhav")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

    }
}