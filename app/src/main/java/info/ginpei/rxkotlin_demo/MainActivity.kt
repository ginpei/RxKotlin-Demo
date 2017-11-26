package info.ginpei.rxkotlin_demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class DoSomething {
    fun doSomething(s: String) {
        println(s)
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runJustHello.setOnClickListener {
            val observer = Observable.just("Hello")
            observer.subscribe { s ->
                Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
            }

            println("Hello?")
        }

        sleepThread.setOnClickListener {
            Toast.makeText(applicationContext, "Sleeping...", Toast.LENGTH_SHORT).show()

            val thread = object : Thread() {
                override fun run() {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    runOnUiThread {
                        Toast.makeText(applicationContext, "Woke up!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            thread.start()
        }

        sleepRx.setOnClickListener {
            Toast.makeText(applicationContext, "Sleeping...", Toast.LENGTH_SHORT).show()

            Single.create<String> { subscriber ->
                println("Single.create()")
                Thread.sleep(3000)
                subscriber.onSuccess("Woke up!")
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message ->
                        println("onSuccess")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }, {
                        println("onError")
                    })
        }

        countUntilWakingUp.setOnClickListener {
            val countToast = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
            countToast.show()

            Observable.create<String> { subscriber ->
                println("Observable.create()")
                for (count in 3 downTo 1) {
                    subscriber.onNext(count.toString())
                    Thread.sleep(1000)
                }
                subscriber.onComplete()
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ sCount ->
                        println("onNext ${sCount}")
                        countToast.setText(sCount)
                    }, {
                        println("onError")
                    }, {
                        println("onComplete")
                        countToast.cancel()
                        Toast.makeText(applicationContext, "Woke up!", Toast.LENGTH_SHORT).show()
                    })
        }
    }
}
