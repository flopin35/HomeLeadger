package com.homeledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeledger.app.data.LedgerViewModel
import com.homeledger.app.ui.HomeLedgerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeLedgerApp(viewModel<LedgerViewModel>()) }
    }
}
