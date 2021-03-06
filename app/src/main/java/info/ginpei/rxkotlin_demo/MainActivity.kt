package info.ginpei.rxkotlin_demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Run just hello
        runJustHello.setOnClickListener {
            val observer = Observable.just("Hello")
            observer.subscribe { s ->
                Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
            }
        }

        // Sleep in a thread
        sleepThread.setOnClickListener {
            Toast.makeText(applicationContext, "Sleeping...", Toast.LENGTH_SHORT).show()

            thread {
                Thread.sleep(1000)

                runOnUiThread {
                    Toast.makeText(applicationContext, "Woke up!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Sleep in an Rx-ish way
        sleepRx.setOnClickListener {
            Toast.makeText(applicationContext, "Sleeping...", Toast.LENGTH_SHORT).show()

            Single.create<String> { subscriber ->
                Thread.sleep(1000)
                subscriber.onSuccess("Woke up!")
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message ->
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }, {
                        println("onError")
                    })
        }

        // Count down
        countDown.setOnClickListener {
            val countToast = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
            countToast.show()

            Observable.create<String> { subscriber ->
                for (count in 3 downTo 1) {
                    subscriber.onNext(count.toString())
                    Thread.sleep(1000)
                }
                subscriber.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ sCount ->
                        countToast.setText(sCount)
                    }, {
                        println("onError")
                    }, {
                        countToast.cancel()
                        Toast.makeText(applicationContext, "Go!", Toast.LENGTH_SHORT).show()
                    })
        }

        // Handle user list
        handleUserList.setOnClickListener {
            class User(val name: String, val birthYear: Int, val active: Boolean)

            val users = arrayListOf(
                    User("Alice", 2000, true),
                    User("Bob", 2011, true),
                    User("Charlie", 1966, false),
                    User("Derik", 1980, true),
                    User("Emma", 1981, true),
                    User("Fred", 1982, true),
                    User("Ginpei", 1983, false),
                    User("Hiroki", 1987, true)
            )

            Observable.fromIterable(users)
                    .skipLast(1)  // remove Hiroki
                    .filter { user -> user.active }  // remove Charlie and Ginpei
                    .sorted { u1, u2 -> u2.birthYear - u1.birthYear }
                    .skipLast(1)  // remove Derik
                    .map { user -> "${user.name} (${2017 - user.birthYear}-year-old)" }
                    .subscribe({ text ->
                        println("- ${text}")
                    })
            Toast.makeText(applicationContext, "Find result in logs", Toast.LENGTH_SHORT).show()
        }
    }
}
