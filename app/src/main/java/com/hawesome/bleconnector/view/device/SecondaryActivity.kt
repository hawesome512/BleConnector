package com.hawesome.bleconnector.view.device

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.hawesome.bleconnector.R

class SecondaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val title = intent.getStringExtra(DevicePageFragment.KEY_PAGE_TITLE)
        val children = intent.getStringArrayListExtra(DevicePageFragment.KEY_SECONDARY_CHILDREN)
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = DevicePageFragment.newInstance(title, children)
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}