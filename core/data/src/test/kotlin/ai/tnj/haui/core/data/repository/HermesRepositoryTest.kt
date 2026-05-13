package ai.tnj.haui.core.data.repository

import ai.tnj.haui.core.model.HermesHealth
import ai.tnj.haui.core.model.HermesResponse
import ai.tnj.haui.core.model.HermesRun
import ai.tnj.haui.core.model.RunRequestBody
import ai.tnj.haui.core.network.HermesEndpoint
import ai.tnj.haui.core.network.HermesService
import ai.tnj.haui.core.network.NetworkManager
import ai.tnj.haui.core.network.SseEvent
import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class HermesRepositoryTest : StringSpec({

    val networkManager = mockk<NetworkManager>()
    val service = mockk<HermesService>()
    val endpoint = mockk<HermesEndpoint>()
    
    val isConfiguredFlow = MutableStateFlow(false)
    every { endpoint.isConfigured } returns isConfiguredFlow

    val repository = HermesRepositoryImpl(networkManager, service, endpoint)

    "updateBaseUrl should update endpoint" {
        every { endpoint.update(any(), any(), any()) } returns Unit
        
        repository.updateBaseUrl("192.168.1.1", "8080", "key123")
        
        verify { endpoint.update("192.168.1.1", "8080", "key123") }
    }

    "checkHealth should return success when service succeeds" {
        val health = mockk<HermesHealth>()
        coEvery { service.health() } returns health
        
        repository.checkHealth().test {
            awaitItem() shouldBe HermesResponse.Loading
            val success = awaitItem() as HermesResponse.Success
            success.data shouldBe health
            awaitComplete()
        }
    }

    "runs should call service with correct body" {
        val runResponse = mockk<HermesRun>()
        coEvery { service.runs(any()) } returns runResponse
        
        repository.runs("hello", "prev-id").test {
            awaitItem() shouldBe HermesResponse.Loading
            val success = awaitItem() as HermesResponse.Success
            success.data shouldBe runResponse
            awaitComplete()
        }
        
        io.mockk.coVerify { 
            service.runs(match { it.input == "hello" && it.previousResponseId == "prev-id" }) 
        }
    }

    "runEvents should connect to SSE via networkManager" {
        val events = flowOf(SseEvent.Closed)
        every { networkManager.connectSse(any()) } returns events
        
        repository.runEvents("run123").test {
            awaitItem() shouldBe SseEvent.Closed
            awaitComplete()
        }
        
        verify { networkManager.connectSse(match { it.url.toString().contains("v1/runs/run123/events") }) }
    }
})
