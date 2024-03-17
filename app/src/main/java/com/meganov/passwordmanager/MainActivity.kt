package com.meganov.passwordmanager

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meganov.passwordmanager.ui.composables.App
import com.meganov.passwordmanager.ui.theme.PasswordManagerTheme

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordManagerTheme {
                val viewModel = viewModel<PasswordManagerVM>(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PasswordManagerVM(application) as T
                        }
                    }
                )
                App(fragmentActivity = this, viewModel = viewModel)
            }
        }
    }
}
