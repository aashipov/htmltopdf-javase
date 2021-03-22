package org.dummy

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.hildan.chrome.devtools.domains.page.PrintToPDFRequest
import org.hildan.chrome.devtools.protocol.ChromeDPClient
import org.hildan.chrome.devtools.targets.*

/**
 * [ChromeBrowserSession] wrapper.
 */
class ChromiumWrapper {

    companion object {
        private suspend fun browserSession(): ChromeBrowserSession {
            return ChromeDPClient("http://0.0.0.0:9222").webSocket()
        }

        private val browserSession: ChromeBrowserSession =
            runBlocking {
                browserSession()
            }

        private suspend fun pdfInner(url: String, printToPDFRequest: PrintToPDFRequest): String {
            val pageSession: ChromePageSession = browserSession.attachToNewPage(url)
            pageSession.page.enable()
            //https://stackoverflow.com/a/59109105
            try {
                coroutineScope {
                    launch {
                        // three events as page loads
                        pageSession.page.events().take(3).collect()
                        //and a bit more for rendering to finish
                        Thread.sleep(100)
                        this@coroutineScope.cancel()
                    }
                }

            } catch (e: CancellationException) {
                //ignore
            }
            val pdf: String = pageSession.page.printToPDF(printToPDFRequest).data
            pageSession.close()
            return pdf
        }

        @JvmStatic
        fun pdf(url: String, printToPDFRequest: PrintToPDFRequest): String =
            runBlocking {
                pdfInner(url, printToPDFRequest)
            }
    }
}
