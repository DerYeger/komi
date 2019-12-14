package eu.yeger.komi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

fun AppCompatActivity.startActivity(newActivityKClass: KClass<out AppCompatActivity>) =
    this.startActivity(Intent(this, newActivityKClass.java))
