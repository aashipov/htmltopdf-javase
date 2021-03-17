package org.dummy

import kotlinx.coroutines.runBlocking
import org.hildan.chrome.devtools.domains.page.PrintToPDFRequest
import org.hildan.chrome.devtools.protocol.ChromeDPClient
import org.hildan.chrome.devtools.targets.*

/**
 * Wrapper around [ChromeBrowserSession].
 */
class ChromiumWrapper {

    companion object {
        private val browserSession: ChromeBrowserSession =
            runBlocking {
                return@runBlocking ChromeDPClient("http://0.0.0.0:9222").webSocket()
            }
        @JvmStatic
        fun pdf(url: String, printToPDFRequest: PrintToPDFRequest): String = runBlocking {
            val pageSession: ChromePageSession = browserSession.attachToNewPageAndAwaitPageLoad(url)
            Thread.sleep(50)
            val pdf: String = pageSession.page.printToPDF(printToPDFRequest).data
            pageSession.close()
            return@runBlocking pdf
        }
    }
}
