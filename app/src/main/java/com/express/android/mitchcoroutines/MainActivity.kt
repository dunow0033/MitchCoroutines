package com.express.android.mitchcoroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.express.android.mitchcoroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.jobButton.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }
            binding.jobProgressBar.startJobOrCancel(job)
        }
    }

    fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    fun initJob() {
        binding.jobButton.setText("Start Job #1")
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "Unknown cancellation error."
                }
                //println("${job} was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        binding.jobProgressBar.max = PROGRESS_MAX
        binding.jobProgressBar.progress = PROGRESS_START
    }

    fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            //println("${job} is already active.  Cancelling...")
            resetJob()
        } else {
            binding.jobButton.setText("Cancel job #1")
            CoroutineScope(IO + job).launch {
                //println("coroutine ${this} is activated with job ${job}")

                for (i in PROGRESS_START..PROGRESS_MAX) {
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String) {
        GlobalScope.launch(Main) {
            binding.jobCompleteText.setText(text)
        }
    }


    private fun showToast(text: String) {
        GlobalScope.launch(Main) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
//    private fun setNewText(input: String){
//        val newText = binding.text.text.toString() + "\n$input"
//        binding.text.text = newText
//    }
//
//    private suspend fun setTextOnMainThread(input: String) {
//        withContext(Main){
//            setNewText(input)
//        }
//    }
//
//    private suspend fun fakeApiRequest(){
//        val result1 = getResult1FromApi()
//        println("debug: $result1")
//        setTextOnMainThread(result1)
//
//        val result2 = getResult2FromApi(result1)
//        setTextOnMainThread(result2)
//    }
//
//    private suspend fun getResult1FromApi(): String {
//        logThread("getResult1FromApi")
//        delay(1000)
//        return RESULT_1
//    }
//
//    private suspend fun getResult2FromApi(result1: String): String {
//        logThread("getResult2FromApi")
//        delay(1000)
//        return RESULT_2
//    }
//
//    private fun logThread(methodName: String) {
//        println("debug: ${methodName}: ${Thread.currentThread().name}")
//    }