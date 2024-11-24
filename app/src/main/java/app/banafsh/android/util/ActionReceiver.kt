package app.banafsh.android.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class ActionReceiver(private val base: String) : BroadcastReceiver() {
    class Action internal constructor(val value: String, internal val onReceive: (Context, Intent) -> Unit) {
        fun pendingIntent(context: Context) = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ 100,
            /* intent = */ Intent(value).setPackage(context.packageName),
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or
                (if (isAtLeastAndroid6) PendingIntent.FLAG_IMMUTABLE else 0),
        )
    }

    private val mutableActions = hashMapOf<String, Action>()
    val all get() = mutableActions.toMap()

    val intentFilter
        get() = IntentFilter().apply {
            mutableActions.keys.forEach { addAction(it) }
        }

    internal fun action(onReceive: (Context, Intent) -> Unit) =
        readOnlyProvider<ActionReceiver, Action> { thisRef, property ->
            val name = "$base.${property.name}"
            val action = Action(name, onReceive)

            thisRef.mutableActions += name to action
            { _, _ -> action }
        }

    override fun onReceive(context: Context, intent: Intent) {
        mutableActions[intent.action]?.onReceive?.let { it(context, intent) }
    }

    fun register(
        context: Context,
        @ContextCompat.RegisterReceiverFlags
        flags: Int = ContextCompat.RECEIVER_NOT_EXPORTED,
    ) = ContextCompat.registerReceiver(
        /* context  = */ context,
        /* receiver = */ this@ActionReceiver,
        /* filter   = */ intentFilter,
        /* flags    = */ flags,
    )
}

private inline fun <ThisRef, Return> readOnlyProvider(
    crossinline provide: (
        thisRef: ThisRef,
        property: KProperty<*>,
    ) -> (thisRef: ThisRef, property: KProperty<*>) -> Return,
) = PropertyDelegateProvider<ThisRef, ReadOnlyProperty<ThisRef, Return>> { thisRef, property ->
    val provider = provide(thisRef, property)
    ReadOnlyProperty { innerThisRef, innerProperty -> provider(innerThisRef, innerProperty) }
}
