package ai.tnj.haui.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataStoreTest : StringSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    
    // Helper to create a clean DataStore for each test
    fun createTestDataStore(): DataStore<Preferences> {
        val tempFile = File.createTempFile("test_datastore", ".preferences_pb")
        tempFile.deleteOnExit()
        return PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFile }
        )
    }

    "should return default values initially" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.isDarkTheme.value shouldBe true
        localDataStore.showToolBubble.value shouldBe false
        localDataStore.chatProtocol.value shouldBe "RUN"
    }

    "saveServerConfig should update host, port and apiKey" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.saveServerConfig("localhost", "8080", "secret")

        val config = localDataStore.getServerConfig()
        config shouldBe Triple("localhost", "8080", "secret")
    }

    "setDarkTheme should update isDarkTheme flow" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.setDarkTheme(false)

        localDataStore.isDarkTheme.test {
            awaitItem() shouldBe false
        }
    }

    "setShowToolBubble should update showToolBubble flow" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.setShowToolBubble(true)

        localDataStore.showToolBubble.test {
            awaitItem() shouldBe true
        }
    }

    "setChatProtocol should update chatProtocol flow" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.setChatProtocol("CHAT_COMPLETIONS")

        localDataStore.chatProtocol.test {
            awaitItem() shouldBe "CHAT_COMPLETIONS"
        }
    }

    "initialIsDarkTheme should read from disk" {
        val dataStore = createTestDataStore()
        val localDataStore = LocalDataStore(dataStore, testScope)

        localDataStore.setDarkTheme(false)
        
        localDataStore.initialIsDarkTheme() shouldBe false
    }
})
